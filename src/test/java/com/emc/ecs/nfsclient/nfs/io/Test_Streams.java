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
import java.util.Calendar;

import org.junit.Test;

import com.emc.ecs.nfsclient.NfsTestBase;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import com.emc.ecs.nfsclient.rpc.RpcRequest;

/**
 * @author seibed
 *
 */
public class Test_Streams extends NfsTestBase {

    /**
     * @throws IOException
     */
    public Test_Streams() throws IOException {
        super();
    }

    @Test
    public void testReadingAndWriting() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);

        Nfs3File test = new Nfs3File(nfs3, "/testIo1");

        NfsFileOutputStream outputStream = new NfsFileOutputStream(test);
        assertTrue(test.exists());
        assertTrue(test.canModify());
        assertTrue(test.canRead());
        byte[] expectedData = new byte[] { 1, 2, 3, 127, -1, -128, 0, 1, 32 };
        outputStream.write(expectedData);
        outputStream.close();

        NfsFileInputStream inputStream = new NfsFileInputStream(test);
        assertEquals(expectedData.length, inputStream.available());
        int bytesRead = (int) inputStream.skip(expectedData.length);
        assertEquals(0, inputStream.available());
        inputStream.close();

        inputStream = new NfsFileInputStream(test);
        for (int i = 0; i < expectedData.length; ++i) {
            int nextByte = inputStream.read();
            assertNotEquals(NfsFileInputStream.EOF, nextByte);
            assertEquals(expectedData[i], (byte) nextByte);
        }
        assertEquals(NfsFileInputStream.EOF, inputStream.read());
        inputStream.close();

        inputStream = new NfsFileInputStream(test);
        byte[] buffer = new byte[1000];
        assertEquals(expectedData.length, inputStream.available());
        bytesRead = inputStream.read(buffer);
        assertEquals(0, inputStream.available());
        inputStream.close();

        assertEquals(expectedData.length, bytesRead);
        for (int i = 0; i < bytesRead; ++i) {
            assertEquals(expectedData[i], buffer[i]);
        }

        test.delete();
        assertFalse(test.exists());

        test = new Nfs3File(nfs3, "/testIo2");
        assertFalse(test.exists());

        byte[] dataChunk = "another chunk of data!".getBytes(RpcRequest.CHARSET);
        int longerSize = 100000000 + dataChunk.length;
        outputStream = new NfsFileOutputStream(test);
        assertTrue(test.exists());
        assertTrue(test.canModify());
        assertTrue(test.canRead());
        expectedData = new byte[longerSize];
        int chunkStart = 0;
        while (chunkStart + dataChunk.length < longerSize) {
            System.arraycopy(dataChunk, 0, expectedData, chunkStart, dataChunk.length);
            chunkStart += dataChunk.length;
        }

        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        outputStream.write(expectedData);
        outputStream.close();
        timeInMillis = Calendar.getInstance().getTimeInMillis() - timeInMillis;
        System.out.println("Milliseconds to write 100 MB: " + timeInMillis);

        inputStream = new NfsFileInputStream(test);
        buffer = new byte[longerSize];
        timeInMillis = Calendar.getInstance().getTimeInMillis();
        bytesRead = inputStream.read(buffer);
        inputStream.close();
        timeInMillis = Calendar.getInstance().getTimeInMillis() - timeInMillis;
        System.out.println("Milliseconds to read 100 MB: " + timeInMillis);
        assertEquals(expectedData.length, bytesRead);
        for (int i = 0; i < bytesRead; ++i) {
            assertEquals(expectedData[i], buffer[i]);
        }

        test.delete();
    }

    @Test
    public void testClosing() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/testIo1");
        NfsFileOutputStream outputStream = new NfsFileOutputStream(test);
        outputStream.close();

        try {
            outputStream.close();
        } catch (IOException e) {
            fail("This should not throw an IOException");
        }

        try {
            outputStream.flush();
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            outputStream.write(new byte[1]);
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            outputStream.write(0);
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            outputStream.write(null, -1, -1);
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        NfsFileInputStream inputStream = new NfsFileInputStream(test);
        inputStream.close();

        try {
            inputStream.available();
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            fail("This should not throw an IOException");
        }

        try {
            inputStream.read();
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            inputStream.read(new byte[1]);
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            inputStream.read(null, -1, -1);
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        try {
            inputStream.skip(-1);
            fail("This should throw an IOException");
        } catch (IOException e) {
            // Do nothing, this was expected.
        }

        test.delete();
//        assertFalse(test.exists());
    }

}
