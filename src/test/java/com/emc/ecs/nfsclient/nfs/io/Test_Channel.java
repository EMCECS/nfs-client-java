/**
 * Copyright 2016-2018 Dell Inc. or its subsidiaries. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.ecs.nfsclient.nfs.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

import org.junit.Test;

import com.emc.ecs.nfsclient.NfsTestBase;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import com.emc.ecs.nfsclient.rpc.RpcRequest;

/**
 * @author amarcionek[at]seven10storage.com
 */
public class Test_Channel extends NfsTestBase {

    /**
     * @throws IOException
     */
    public Test_Channel() throws IOException {
        super();
    }

    @Test
    public void testReadingAndWriting() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);

        NfsFileChannel channel;
        byte[] expectedData = new byte[] { 1, 2, 3, 127, -1, -128, 0, 1, 32 };
        ByteBuffer dst = ByteBuffer.allocate(expectedData.length);
        int bytesRead = 0;

        Nfs3File test1 = new Nfs3File(nfs3, "/testIoSmall");
        Nfs3File test2 = new Nfs3File(nfs3, "/testIoLarge1");
        try
        {
            // Test write
            channel = new NfsFileChannel(test1, NfsFileChannel.standardReadWrite);
            assertTrue(test1.exists());
            assertTrue(test1.canModify());
            assertTrue(test1.canRead());
            channel.write(ByteBuffer.wrap(expectedData));
            channel.close();

            // Test read and reposition
            channel = new NfsFileChannel(test1);
            assertEquals(expectedData.length, channel.size());
            // Test read at EOF
            channel.position(channel.size());
            bytesRead = (int) channel.read(dst);
            assertEquals(NfsFileChannel.EOF, bytesRead);
    
            // Test single byte read
            channel.position(0);
            dst.limit(1);
            for (int i = 0; i < expectedData.length; i++) {
                bytesRead = channel.read(dst);
                dst.flip();
                assertNotEquals(NfsFileChannel.EOF, bytesRead);
                assertEquals(expectedData[i], dst.get());
                dst.rewind();
            }
            assertEquals(NfsFileChannel.EOF, channel.read(dst));
            channel.close();
            dst.clear();
    
            // Test entire read and overflow
            channel = new NfsFileChannel(test1);
            dst = ByteBuffer.wrap(new byte[1000]);
            assertEquals(expectedData.length, channel.size());
            bytesRead = channel.read(dst);
            dst.flip();
            assertEquals(NfsFileChannel.EOF, channel.read(dst));
            channel.close();
            assertEquals(expectedData.length, bytesRead);
            for (int i = 0; i < bytesRead; ++i) {
                assertEquals(expectedData[i], dst.get(i));
            }
            
            test1.delete();
            assertFalse(test1.exists());
        }
        finally {
            if (test1.exists())
                test1.delete();
        }

        try {
            assertFalse(test2.exists());
    
            // Test big chunks of data, will cross rsize/wsize boundaries
            byte[] dataChunk = "another chunk of data!".getBytes(RpcRequest.CHARSET);
            int longerSize = 100000000 + dataChunk.length;
            channel = new NfsFileChannel(test2, NfsFileChannel.standardReadWrite);
            assertTrue(test2.exists());
            assertTrue(test2.canModify());
            assertTrue(test2.canRead());
            expectedData = new byte[longerSize];
            int chunkStart = 0;
            while (chunkStart + dataChunk.length < longerSize) {
                System.arraycopy(dataChunk, 0, expectedData, chunkStart, dataChunk.length);
                chunkStart += dataChunk.length;
            }

            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            int bytesWritten = channel.write(ByteBuffer.wrap(expectedData));
            channel.close();   
            assertEquals(expectedData.length, bytesWritten);
            timeInMillis = Calendar.getInstance().getTimeInMillis() - timeInMillis;
            System.out.println("Milliseconds to write 100 MB: " + timeInMillis);

            channel = new NfsFileChannel(test2);
            dst = ByteBuffer.wrap(new byte[longerSize]);
            timeInMillis = Calendar.getInstance().getTimeInMillis();
            bytesRead = channel.read(dst);
            channel.close();
            dst.flip();
            timeInMillis = Calendar.getInstance().getTimeInMillis() - timeInMillis;
            System.out.println("Milliseconds to read 100 MB: " + timeInMillis);
            assertEquals(expectedData.length, bytesRead);
            for (int i = 0; i < bytesRead; ++i) {
                assertEquals(expectedData[i], dst.get());
            }

            dst.clear();
            int bytesToTest = 1024;
            int offsetManipulate = longerSize / 2; // Choose this to manipulate half way through the file

            // Test read with position parameters
            channel = new NfsFileChannel(test2);
            int offsetRead = offsetManipulate;
            dst.limit(bytesToTest);          
            bytesRead = channel.read(dst, longerSize); // Read past EOF
            assertEquals(NfsFileChannel.EOF, bytesRead);
            assertEquals(longerSize, channel.size());
            bytesRead = channel.read(dst, offsetRead); // Read at some offset
            assertEquals(bytesToTest, bytesRead);
            assertEquals(0, channel.position());
            dst.flip();
            assertEquals(bytesToTest, bytesRead);
            for (int i = bytesToTest; i < bytesRead; ++i) {
                assertEquals(expectedData[i], dst.get());
            }
            channel.close();

            dst.clear();
            dst.limit(bytesToTest);

            // Test write with position parameters
            channel = new NfsFileChannel(test2, NfsFileChannel.standardReadWrite);
            int offsetWrite = offsetManipulate;
            bytesWritten = channel.write(ByteBuffer.wrap(expectedData, offsetManipulate, bytesToTest), offsetWrite); // Write at some offset
            assertEquals(bytesToTest, bytesWritten);
            assertEquals(0, channel.position());

            long size = channel.size();
            bytesWritten = channel.write(ByteBuffer.wrap(new byte[bytesToTest]), size); // Write past EOF
            assertEquals(bytesToTest, bytesWritten);
            assertEquals(size + bytesToTest, channel.size());
            assertEquals(0, channel.position());
            channel.truncate(size); // To original size
            channel.close();
            channel = new NfsFileChannel(test2, NfsFileChannel.standardReadWrite);
            assertEquals(size, channel.size());
            channel.close();
        }
        finally {
            if (test2.exists())
                test2.delete();
        }
    }
    
    @Test
    public void testReadWriteMultiple() throws Exception {

        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);

        NfsFileChannel channel;
        byte[] expectedData = new byte[] { 1, 2, 3, 127, -1, -128, 0, 1, 32, -64 };

        Nfs3File test = new Nfs3File(nfs3, "/testReadWriteMultiple");

        assertFalse(test.exists());

        try {
            // Test read and write with multiple buffers
            channel = new NfsFileChannel(test, NfsFileChannel.standardReadWrite);
            ByteBuffer[] dsts = new ByteBuffer[2];
            dsts[0] = ByteBuffer.wrap(expectedData);
            dsts[1] = ByteBuffer.wrap(expectedData);
    
            long totalBytesWritten = channel.write(dsts, 0, 2);
            assertEquals(dsts.length * expectedData.length, totalBytesWritten);
            assertEquals(dsts.length * expectedData.length, channel.position());
            
            try {
                channel.write(dsts, 1, 2);
                fail("This should throw IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ex) {
                // Expected
            }

            try {
                channel.write(dsts, -1, 0);
                fail("This should throw IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ex) {
                // Expected
            }

            try {
                channel.write(dsts, 0, -1);
                fail("This should throw IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ex) {
                // Expected
            }
            assertEquals(0, channel.write(dsts, 2, 0));
            assertEquals(0, channel.write(dsts, 0, 0));

            channel.close();

            channel = new NfsFileChannel(test);
            dsts = new ByteBuffer[2];
            dsts[0] = ByteBuffer.wrap(new byte[expectedData.length]);
            dsts[1] = ByteBuffer.wrap(new byte[expectedData.length]);
            
            long totalBytesRead = channel.read(dsts, 0, 2);
            assertEquals(dsts.length * expectedData.length, totalBytesRead);
            assertEquals(dsts.length * expectedData.length, channel.position());
            dsts[0].flip();
            for (int i = 0; i < expectedData.length; ++i) {
                assertEquals(expectedData[i], dsts[0].get());
            }
            dsts[1].flip();
            for (int i = 0; i < expectedData.length; ++i) {
                assertEquals(expectedData[i], dsts[1].get());
            }

            try {
                channel.read(dsts, 1, 2);
                fail("This should throw IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ex) {
                // Expected
            }

            try {
                channel.read(dsts, -1, 0);
                fail("This should throw IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ex) {
                // Expected
            }

            try {
                channel.read(dsts, 0, -1);
                fail("This should throw IndexOutOfBoundsException");
            }
            catch (IndexOutOfBoundsException ex) {
                // Expected
            }
            assertEquals(0, channel.read(dsts, 2, 0));
            assertEquals(0, channel.read(dsts, 0, 0));

            channel.close();
        }
        finally {
            if (test.exists())
                test.delete();
        }
    }

    @Test
    public void testTransferTo() throws Exception {

        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);

        NfsFileChannel channel;
        byte[] expectedData = new byte[] { 1, 2, 3, 127, -1, -128, 0, 1, 32, -64 };

        Nfs3File test1 = new Nfs3File(nfs3, "/testTransferTo1");
        Nfs3File test2 = new Nfs3File(nfs3, "/testTransferTo2");

        assertFalse(test1.exists());
        assertFalse(test2.exists());

        try {
            int numberOfWrites = 10;
            channel = new NfsFileChannel(test1, NfsFileChannel.standardReadWrite);
            for (int i = 0; i < numberOfWrites; i++) {
                channel.write(ByteBuffer.wrap(expectedData));
            }
            channel.position(0);
            ByteBuffer src = ByteBuffer.wrap(new byte[expectedData.length]);
            for (int i = 0; i < numberOfWrites; i++) {
                src.clear();
                int bytesRead = channel.read(src);
                src.flip();
                assertEquals(bytesRead, src.limit());
                for (int j = 0; j < bytesRead; ++j) {
                    assertEquals(expectedData[j], src.get());
                }
            }
            channel.close();
    
            // Test transfer to
            channel = new NfsFileChannel(test1, NfsFileChannel.standardReadWrite);
            NfsFileChannel channelTransfer = new NfsFileChannel(test2, NfsFileChannel.standardReadWrite);
            long bytesTransferred = channel.transferTo(0, channel.size(), channelTransfer);
            assertEquals(bytesTransferred, channel.size());
            assertEquals(channel.size(), channelTransfer.size());
            channelTransfer.position(0);
            
            ByteBuffer dst = ByteBuffer.allocate(expectedData.length * numberOfWrites);
            int bytesRead = channelTransfer.read(dst);
            dst.flip();
            for (int i = 0; i < bytesRead; ++i) {
                int index = i % expectedData.length;
                assertEquals(expectedData[index], dst.get());
            }        
            channel.close();
            channelTransfer.close();
            
            channelTransfer = new NfsFileChannel(test2, StandardOpenOption.WRITE);
            try {
                channelTransfer.transferTo(0, channel.size(), channel);
                fail ("This should throw NonReadableChannelException");
            }
            catch (NonReadableChannelException ex) {
                // Expected
            }
            channelTransfer.close();
        }
        finally {
            if (test1.exists())
                test1.delete();
            if (test2.exists())
                test2.delete();
        }
    }
    
    @Test
    public void testTransferFrom() throws Exception {

        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);

        NfsFileChannel channel;
        byte[] expectedData = new byte[] { 1, 2, 3, 127, -1, -128, 0, 1, 32, -64 };

        Nfs3File test1 = new Nfs3File(nfs3, "/testTransferFrom1");
        Nfs3File test2 = new Nfs3File(nfs3, "/testTransferFrom2");

        assertFalse(test1.exists());
        assertFalse(test2.exists());

        try {
            int numberOfWrites = 10;
            channel = new NfsFileChannel(test1, NfsFileChannel.standardReadWrite);
            for (int i = 0; i < numberOfWrites; i++) {
                channel.write(ByteBuffer.wrap(expectedData));
            }
            channel.position(0);
            ByteBuffer src = ByteBuffer.wrap(new byte[expectedData.length]);
            for (int i = 0; i < numberOfWrites; i++) {
                src.clear();
                int bytesRead = channel.read(src);
                src.flip();
                assertEquals(bytesRead, src.limit());
                for (int j = 0; j < bytesRead; ++j) {
                    assertEquals(expectedData[j], src.get());
                }
            }
            channel.close();            
    
            // Test transfer from
            channel = new NfsFileChannel(test1, NfsFileChannel.standardReadWrite);
            NfsFileChannel channelTransfer = new NfsFileChannel(test2, NfsFileChannel.standardReadWrite);
            long bytesTransferred = channelTransfer.transferFrom(channel, 0, channel.size());
            assertEquals(bytesTransferred, channelTransfer.size());
            assertEquals(channel.size(), channelTransfer.size());
            channelTransfer.position(0);
            
            ByteBuffer dst = ByteBuffer.allocate(expectedData.length * numberOfWrites);
            int bytesRead = channelTransfer.read(dst);
            dst.flip();
            for (int i = 0; i < bytesRead; ++i) {
                int index = i % expectedData.length;
                assertEquals(expectedData[index], dst.get());
            }        
            channel.close();
            channelTransfer.close();
            
            channelTransfer = new NfsFileChannel(test2, StandardOpenOption.READ);
            try {
                channelTransfer.transferFrom(channel, 0, channel.size());
                fail ("This should throw NonWritableChannelException");
            }
            catch (NonWritableChannelException ex) {
                // Expected
            }
            channelTransfer.close();
        }
        finally {
            if (test1.exists())
                test1.delete();
            if (test2.exists())
                test2.delete();
        }
    }

    @Test
    public void testClosing() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/testClosing");
        try {
            NfsFileChannel channel = new NfsFileChannel(test, NfsFileChannel.standardReadWrite);
            assertEquals(true, channel.isOpen());            
            channel.close();
            assertEquals(false, channel.isOpen());

            try {
                channel.close();
            } catch (ClosedChannelException e) {
                fail("This should not throw an ClosedChannelException");
            }
    
            try {
                channel.commit(0, 0);
                fail("This should throw a ClosedChannelException");
            } catch (ClosedChannelException e) {
                // Do nothing, this was expected.
            }
    
            try {
                channel.write(ByteBuffer.wrap(new byte[1]));
                fail("This should throw a ClosedChannelException");
            } catch (ClosedChannelException e) {
                // Do nothing, this was expected.
            }
            
            try {
                channel.read(ByteBuffer.wrap(new byte[1]));
                fail("This should throw a ClosedChannelException");
            } catch (ClosedChannelException e) {
                // Do nothing, this was expected.
            }
            
            try {
                channel.position(-1);
                fail("This should throw a ClosedChannelException");
            } catch (ClosedChannelException e) {
                // Do nothing, this was expected.
            }
            try {
                channel.truncate(0);
                fail("This should throw a ClosedChannelException");
            } catch (ClosedChannelException e) {
                // Do nothing, this was expected.
            }
            
            try {
                long size = channel.size();
                assertEquals(0, size);
            } catch (IOException e) {
                fail("This should not throw a ClosedChannelException");
            }
        }
        finally {
            try {
                test.delete();
            } catch (Exception ex) {
             // Do nothing, this could happen
            }
        }
    }

    @Test
    public void testUnsupported() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/testUnsupported");
        test.createNewFile();
        try {
            
            NfsFileChannel channel = new NfsFileChannel(test, NfsFileChannel.standardReadWrite);
            try {
                channel.map(MapMode.PRIVATE, 0, 0);
                fail ("This should throw IllegalArgumentException");
            }
            catch (IllegalArgumentException ex ) {
                // Expected
            }
            
            try {
                channel.lock();
                fail ("This should throw IllegalArgumentException");
            }
            catch (IllegalArgumentException ex ) {
                // Expected
            }
            
            try {
                channel.lock(0, 0, false);
                fail ("This should throw IllegalArgumentException");
            }
            catch (IllegalArgumentException ex ) {
                // Expected
            }
            
            try {
                channel.tryLock(0, 0, false);
                fail ("This should throw IllegalArgumentException");
            }
            catch (IllegalArgumentException ex ) {
                // Expected
            }
            channel.close();
        }
        finally {
            if (test.exists())
                test.delete();
        }
    }
    
    @Test
    public void testFlags() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/testFlags1");
        try {
            NfsFileChannel channel = null;

            try {
                channel = new NfsFileChannel(test);
                fail("This should throw an IOException");
            } catch (IOException e) {
                // Do nothing, this was expected, because file doesn't exist.
            }

            try {
                channel = new NfsFileChannel(test, NfsFileChannel.standardReadWrite);
            } catch (IOException e) {
                fail("This should not throw an IOException");               
            }

            try {
                channel.write(ByteBuffer.wrap(new byte[10]));
            } catch (IOException e) {
                fail("This should not throw an IOException");               
            }
            
            channel.close();
            try {
                channel = new NfsFileChannel(test, StandardOpenOption.READ);
            } catch (IOException e) {
                fail("This should not throw an IOException");               
            }            
            try {
                channel.write(ByteBuffer.wrap(new byte[10]));
                fail("This should throw a NonWritableChannelException");
            } catch (NonWritableChannelException e) {
                // Do nothing, this was expected, channel not opened for write               
            }
            try {
                channel.truncate(0);
                fail("This should throw a NonWritableChannelException");
            } catch (NonWritableChannelException e) {
                // Do nothing, this was expected, channel not opened for write               
            }
            // Test zero byte read
            int bytesRead = channel.read(ByteBuffer.wrap(new byte[0]));
            assertEquals(0, bytesRead);

            channel.close();
            try {
                channel = new NfsFileChannel(test, StandardOpenOption.WRITE);
            } catch (IOException e) {
                fail("This should not throw an IOException");               
            }
            try {
                channel.read(ByteBuffer.wrap(new byte[10]));
                fail("This should throw a NonReadableChannelException");
            } catch (NonReadableChannelException e) {
                // Do nothing, this was expected, channel not opened for read               
            }
            // Test zero byte write
            int bytesWritten = channel.write(ByteBuffer.wrap(new byte[0]));
            assertEquals(0, bytesWritten);
            channel.truncate(0);
            assertEquals(0,  channel.size());
            channel.close();
            
            // Test basic open option conflicts
            try {
                channel = new NfsFileChannel(test, StandardOpenOption.READ, StandardOpenOption.APPEND);
                fail("This should throw an IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Do nothing, this was expected
            }
            try {
                channel = new NfsFileChannel(test, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.APPEND);
                fail("This should throw an IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Do nothing, this was expected
            }

            // Do some operations on existing files
            try {
                channel = new NfsFileChannel(test, StandardOpenOption.CREATE_NEW);
                fail("This should throw an IOException");
            } catch (IOException e) {
                // Do nothing, this was expected
            }

            // Test truncating on open, first we must write 1 byte to the file
            channel = new NfsFileChannel(test, StandardOpenOption.READ, StandardOpenOption.WRITE);
            channel.write(ByteBuffer.wrap(new byte[1]));
            assertEquals(1, channel.size());
            channel.close();
            channel = new NfsFileChannel(test, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            assertEquals(0, channel.size());
            channel.close();
            
            test.delete();
            
            // Test some operations on non-existing files
            try {
                channel = new NfsFileChannel(test, StandardOpenOption.READ, StandardOpenOption.WRITE);
                fail("This should throw an IOException");
            } catch (IOException e) {
                // Do nothing, this was expected
            }

            channel = new NfsFileChannel(test, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
            channel.close();
            assertTrue(test.exists());
            test.delete();

            channel = new NfsFileChannel(test, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            channel.close();
            assertTrue(test.exists());
            test.delete();

            // Test append

            // First write some data
            channel = new NfsFileChannel(test, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            assertTrue(test.exists());
            assertEquals(0, channel.size());
            bytesWritten = channel.write(ByteBuffer.wrap(new byte[10]));
            assertEquals(10, bytesWritten);
            assertEquals(10, channel.size());
            channel.close();
            assertTrue(test.exists());
            channel = new NfsFileChannel(test, StandardOpenOption.APPEND);
            assertTrue(test.exists());
            assertEquals(10, channel.size());
            assertEquals(10, channel.position());
            bytesWritten = channel.write(ByteBuffer.wrap(new byte[10]));
            assertEquals(10, bytesWritten);
            assertEquals(20, channel.size());
            channel.truncate(21);
            assertEquals(20, channel.size());
            channel.position(10);
            assertEquals(20, channel.size());
            channel.close();
            
            // Test delete on close
            channel = new NfsFileChannel(test, StandardOpenOption.DELETE_ON_CLOSE);
            assertTrue(test.exists());
            channel.close();
            assertTrue(!test.exists());            
        }
        finally {
            try {
                test.delete();
            } catch (Exception ex) {
             // Do nothing, this could happen
            }
        }
    }
}