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
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emc.ecs.nfsclient.nfs.NfsCreateMode;
import com.emc.ecs.nfsclient.nfs.NfsCreateResponse;
import com.emc.ecs.nfsclient.nfs.NfsGetAttrResponse;
import com.emc.ecs.nfsclient.nfs.NfsReadResponse;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsWriteRequest;
import com.emc.ecs.nfsclient.nfs.NfsWriteResponse;

/**
 * The NFS implementation of <code>java.nio.Channels.FileChannel</code>.
 * 
 * @author amarcionek[at]seven10storage.com
 */
public class NfsFileChannel extends FileChannel
{
    /**
     * Constant for output.
     */
    public static final int EOF = -1;

    /**
     * Standard set of options to create (if doesn't exist), read and write using data sync
     */
    public final static Set<OpenOption> standardReadWrite = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE,
            StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                    StandardOpenOption.DSYNC));

    /**
     * The reference to the file
     */
    private NfsFile<?, ?> _nfsFile;

    /**
     * The current value of the file position pointer.
     */
    private long _currentPosition = 0;

    /**
     * Lock for operations involving position and size
     */
    private final Object positionLock = new Object();

    /**
     * The current value of the file size.
     */
    private long _currentSize;

    /**
     * The NFS server's preferred write size.
     */
    private int _wtpref;
    
    /**
     * The NFS server's preferred read size.
     */
    private int _rtpref;

    /**
     * How the NFS server should write to the file.
     * <ul>
     * <li>UNSTABLE = 0 - Best effort, no promises.</li>
     * <li>DATA_SYNC = 1 - Commit all data to stable storage, plus enough
     * metadata for retrieval, before returning.</li>
     * <li>FILE_SYNC = 2 - Commit all data and metadata to stable storage before
     * returning.</li>
     * </ul>
     */
    private final int _syncType;

    /**
     * Instance of Flags class from the OpenOptions
     */
    private final Flags _flags;

    /**
     * Creates a file channel to read from an existing file represented by the
     * specified <code>NfsFile</code> object, starting at position 0.
     *
     * @param nfsFile
     *            The file to be opened for reading.
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file or does not exist, or cannot be opened for any other reason.
     */
    public NfsFileChannel(NfsFile<?, ?> nfsFile) throws IOException {
        this(nfsFile, StandardOpenOption.READ, StandardOpenOption.SYNC);
    }

    /**
     * Creates a file channel to read, write and/or delete a file represented by the
     * specified <code>NfsFile</code> object, starting at position 0.
     * <p>
     * The options control behavior of this channel. The model of option handling was taken from 
     * {@link java.nio.channels.FileChannel#open(Path, Set, FileAttribute...) FileChannel.open()}. 
     * <p>
     * DSYNC corresponds to NfsWriteRequest.FILE_SYNC, SYNC corresponds to NfsWriteRequest.FILE_SYNC,
     * and the absence of either corresponds to NfsWriteRequest.UNSTABLE.
     * <p>
     * StandardOpenOption.SPARSE and NO_FOLLOW_LINKS are both ignored.
     *
     * @param nfsFile
     *            The file to be opened for reading, writing, or deleting
     * @param options
     *            The OpenOptions
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, or cannot be
     *             opened for any other reason
     * @see java.nio.channels.FileChannel#open(Path, Set, FileAttribute...)
     */
    public NfsFileChannel(NfsFile<?, ?> nfsFile, OpenOption... options) throws IOException {

        _nfsFile = nfsFile;
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        _flags = Flags.toFlags(set);
        
        if (_flags.dsync) {
            _syncType = NfsWriteRequest.DATA_SYNC;
        } else if (_flags.sync) {
            _syncType = NfsWriteRequest.FILE_SYNC;
        } else {
            _syncType = NfsWriteRequest.UNSTABLE;
        }
        open();
    }
        
    /**
     * Creates a file output stream to write to the file represented by the
     * specified <code>NfsFile</code> object, starting at <code>offset</code>
     * and using <code>syncType</code> behavior.
     * <p>
     * If the file does not exist, it will first be created.
     *
     * @param nfsFile
     *            The file to be opened for writing.
     * @param offset
     *            Where to start writing to the file.
     * @param syncType
     *            One of the values below.
     *            <ul>
     *            <li>UNSTABLE = 0 - Best effort, no promises.</li>
     *            <li>DATA_SYNC = 1 - Commit all data to stable storage, plus
     *            enough metadata for retrieval, before returning.</li>
     *            <li>FILE_SYNC = 2 - Commit all data and metadata to stable
     *            storage before returning.</li>
     *            </ul>
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, or cannot be
     *             opened for any other reason
     */
    public NfsFileChannel(NfsFile<?, ?> nfsFile, Set<OpenOption> set) throws IOException {

        _nfsFile = nfsFile;
        _flags = Flags.toFlags(set);
        if (_flags.dsync) {
            _syncType = NfsWriteRequest.DATA_SYNC;
        } else if (_flags.sync) {
            _syncType = NfsWriteRequest.FILE_SYNC;
        } else {
            _syncType = NfsWriteRequest.UNSTABLE;
        }
        open();
    }

    /**
     * Opens the file channel using the passed in parameters
     * @throws IOException
     */
    private void open() throws IOException {

        if (_flags.append && _flags.read)
                throw new IllegalArgumentException(StandardOpenOption.APPEND.name()
                        + " + "
                            + StandardOpenOption.READ.name()
                                + " not allowed");

        if (_flags.append && _flags.truncateExisting)
                throw new IllegalArgumentException(StandardOpenOption.APPEND.name()
                        + " + "
                            + StandardOpenOption.TRUNCATE_EXISTING.name()
                                + " not allowed");

        if (_nfsFile.exists()) {
            _currentSize = _nfsFile.length();

            if (_nfsFile.isDirectory())
                throw new IOException("Entry is a directory");

            if (_flags.createNew)
                throw new IOException("File already exists");

            if ((_flags.write) || (_flags.append)) {
                // Validate the file.
                if (!(_nfsFile.canExtend() && _nfsFile.canModify())) {
                    throw new IllegalArgumentException(
                            "The file must be writable by the client: " + _nfsFile.getAbsolutePath());
                }

                if (_flags.truncateExisting) {
                    NfsSetAttributes attributes = new NfsSetAttributes();
                    attributes.setSize(0L);
                    _nfsFile.setAttributes(attributes);
                }
            }
        } else {
            // If not opening for write or append, its read-only and file doesn't exist
            if ((!_flags.write) && (!_flags.append))  
                throw new IOException("File does not exist");

            NfsCreateMode createMode;
            if (_flags.createNew)
                createMode = NfsCreateMode.GUARDED;
            else if (_flags.create)
                createMode = NfsCreateMode.UNCHECKED;
            else
                throw new IOException("File does not exist"); // Not asked to create and the file doesn't exist

            // Create the file.
            NfsSetAttributes attributes = new NfsSetAttributes();
            attributes.setMode(NfsFile.ownerReadModeBit 
                    | NfsFile.ownerWriteModeBit
                    | NfsFile.ownerExecuteModeBit
                    | NfsFile.groupReadModeBit
                    | NfsFile.groupWriteModeBit
                    | NfsFile.othersReadModeBit
                    | NfsFile.othersWriteModeBit);
            NfsCreateResponse response = _nfsFile.create(createMode, attributes, null);
            if (!response.stateIsOk())
                throw new IOException("Error " + response.getState() + " from create");
        }

        // Advance the position to the end
        if (_flags.append) {
            _currentPosition = _currentSize;
        }

        _wtpref = (int) Math.min(_nfsFile.fsinfo().getFsInfo().wtpref, Integer.MAX_VALUE);
        _rtpref = (int) Math.min(_nfsFile.fsinfo().getFsInfo().rtpref, Integer.MAX_VALUE);
    }

    /**
     * Flush any uncommitted data to the file.
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param offsetToCommit
     *            The position within the file at which the flush is to begin.
     *            An offset of 0 means to flush data starting at the beginning
     *            of the file.
     * @param dataSizeToCommit
     *            The number of bytes of data to flush. If count is 0, a flush
     *            from offset to the end of file is done.
     * @throws IOException
     *            If some other I/O error occurs
     * @throws ClosedChannelException
     *            If this channel is closed
     */
    public void commit(long offsetToCommit, int dataSizeToCommit) throws IOException, ClosedChannelException {
        checkForClosed();
        _nfsFile.commit(offsetToCommit, dataSizeToCommit);
    }

    /**
     * <p><b>NOTE: NfsFileChannel implementation is to call commit on the entire file range</b>
     * @see java.nio.channels.FileChannel#force(boolean)
     */
    public void force(boolean metaData) throws ClosedChannelException, IOException {
        this.commit(0, 0);
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) throws IOException, ClosedChannelException {
        checkForClosed();

        if (!_flags.read) { 
            throw new NonReadableChannelException();
        } else if (!dst.hasRemaining()) {
            return 0;
        } else if (_currentPosition > _currentSize) {
            return EOF;
        }

        synchronized (positionLock) {
            int bytesRead = 0;
            while (dst.remaining() > 0) {
                NfsReadResponse response;
                if (dst.hasArray()) {
                    response = _nfsFile.read(_currentPosition, Math.min(dst.remaining(), _rtpref), dst.array(), dst.position());
                    // Repositioning ByteBuffer because the call to read() does not.
                    if (response.getBytesRead() > 0)
                        dst.position(dst.position() + response.getBytesRead());
                        
                } else {
                    byte[] src = new byte[Math.min(dst.remaining(), _rtpref)];
                    response = _nfsFile.read(_currentPosition, src.length, src, dst.position());
                    if (response.getBytesRead() > 0)
                        dst.put(src, 0, response.getBytesRead());
                }

                if (!response.stateIsOk()) {
                    throw new IOException("Error " + response.getState() + " from read");
                }

                bytesRead += response.getBytesRead();
                _currentPosition += response.getBytesRead();

                if (response.isEof()) {
                    if (bytesRead > 0)
                        return bytesRead;
                    return EOF;
                }

                if (response.getBytesRead() == 0) {
                    throw new IOException("Returned zero bytes read");
                }
            }
            return bytesRead;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.FileChannel#read(java.nio.ByteBuffer, long)
     */
    @Override
    public int read(ByteBuffer dst, long position) throws IOException
    {
        checkForClosed();

        if (!_flags.read) { 
            throw new NonReadableChannelException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (!dst.hasRemaining()) {
            return 0;
        } else if (position > _currentSize) {
            return EOF;
        }

        synchronized (positionLock) {
            int bytesRead = 0;
            while (dst.remaining() > 0) {
                NfsReadResponse response;
                if (dst.hasArray()) {
                    response = _nfsFile.read(position, Math.min(dst.remaining(), _rtpref), dst.array(), dst.position());
                    // Repositioning ByteBuffer because the call to read() does not.
                    if (response.getBytesRead() > 0)
                        dst.position(dst.position() + response.getBytesRead());
                        
                } else {
                    byte[] src = new byte[Math.min(dst.remaining(), _rtpref)];
                    response = _nfsFile.read(position, src.length, src, dst.position());
                    if (response.getBytesRead() > 0)
                        dst.put(src, 0, response.getBytesRead());
                }

                if (!response.stateIsOk()) {
                    throw new IOException("Error " + response.getState() + " from read");
                }

                bytesRead += response.getBytesRead();
                position += response.getBytesRead();

                if (response.isEof()) {
                    if (bytesRead > 0)
                        return bytesRead;
                    return EOF;
                }

                if (response.getBytesRead() == 0) {
                    throw new IOException("Returned zero bytes read");
                }
            }
            return bytesRead;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.FileChannel#read(java.nio.ByteBuffer[], int, int)
     */
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        checkForClosed();
        if (!_flags.read) { 
            throw new NonReadableChannelException();
        } else if ((offset < 0) || (length < 0) || (offset > dsts.length - length)) {
            throw new IndexOutOfBoundsException();
        }

        long bytesRead = 0;
        for (int i = offset; i < length; i++) {
            bytesRead += this.read(dsts[i]);
        }
        return bytesRead;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(ByteBuffer src) throws IOException, ClosedChannelException {
        checkForClosed();
        if ((!_flags.write) && (!_flags.append)) { 
            throw new NonWritableChannelException();
        } else if (!src.hasRemaining()) {
            return 0;
        }

        synchronized (positionLock) {
            long offsetWrite;                
            if (_flags.append)
                offsetWrite = size();
            else
                offsetWrite = _currentPosition;

            int bytesWritten = 0;
            while (src.remaining() > 0) {
                List<ByteBuffer> payload = new ArrayList<ByteBuffer>(1);
                if (src.remaining() > _wtpref) {
                    if (src.hasArray()) {
                        payload.add(ByteBuffer.wrap(src.array(), bytesWritten, _wtpref));
                    } else {
                        ByteBuffer srcWrite = ByteBuffer.allocate(src.capacity());
                        src.mark();
                        srcWrite.put(src);
                        src.reset();
                        srcWrite.position(_wtpref);
                        srcWrite.flip();
                        payload.add(srcWrite);
                    }
                } else {
                    payload.add(src);
                }
                /*
                 *  Since there is no analog in NFSv3 to write to the end of a file atomically,
                 *  this is a best guess effort by 
                 */
                NfsWriteResponse response = _nfsFile.write(offsetWrite, payload, _syncType);
                if (!response.stateIsOk()) {
                    throw new IOException("Error " + response.getState() + " from write");
                }
                if (response.getCount() == 0) {
                    throw new IOException("Write return zero bytes written");
                }
                // Repositioning ByteBuffer because the call to write() does not.
                src.position(src.position() + response.getCount());
                bytesWritten += response.getCount();
                offsetWrite += response.getCount();
                _currentPosition = offsetWrite;
                if (_currentPosition > _currentSize)
                    _currentSize = _currentPosition; 
            }
            if (_flags.append) {
                _currentPosition = size();
            }
            return bytesWritten;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.FileChannel#write(java.nio.ByteBuffer, long)
     */
    @Override
    public int write(ByteBuffer src, long position) throws IOException
    {
        checkForClosed();
        if ((!_flags.write) && (!_flags.append)) { 
            throw new NonWritableChannelException();
        } else if (!src.hasRemaining()) {
            return 0;
        }

        synchronized (positionLock) {
            long offsetWrite;                
            if (_flags.append)
                offsetWrite = size();
            else
                offsetWrite = position;

            int bytesWritten = 0;
            while (src.remaining() > 0) {
                List<ByteBuffer> payload = new ArrayList<ByteBuffer>(1);
                if (src.remaining() > _wtpref) {
                    if (src.hasArray()) {
                        payload.add(ByteBuffer.wrap(src.array(), bytesWritten, _wtpref));
                    } else {
                        ByteBuffer srcWrite = ByteBuffer.allocate(src.capacity());
                        src.mark();
                        srcWrite.put(src);
                        src.reset();
                        srcWrite.position(_wtpref);
                        srcWrite.flip();
                        payload.add(srcWrite);
                    }
                } else {
                    payload.add(src);
                }
                /*
                 *  Since there is no analog in NFSv3 to write to the end of a file atomically,
                 *  this is a best guess effort by 
                 */
                NfsWriteResponse response = _nfsFile.write(offsetWrite, payload, _syncType);
                if (!response.stateIsOk()) {
                    throw new IOException("Error " + response.getState() + " from write");
                }
                if (response.getCount() == 0) {
                    throw new IOException("Write return zero bytes written");
                }
                // Repositioning ByteBuffer because the call to write() does not.
                src.position(src.position() + response.getCount());
                bytesWritten += response.getCount();
                offsetWrite += response.getCount();
                position = offsetWrite;
                if (position > _currentSize)
                    _currentSize = position; 
            }
            if (_flags.append) {
                position = size();
            }
            return bytesWritten;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.FileChannel#write(java.nio.ByteBuffer[], int, int)
     */
    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException, ClosedChannelException
    {
        checkForClosed();
        if (!_flags.write && !_flags.append) { 
            throw new NonWritableChannelException();
        } else if ((offset < 0) || (length < 0) || (offset > srcs.length - length)) {
            throw new IndexOutOfBoundsException();  
        }

        long bytesRead = 0;
        for (int i = offset; i < length; i++) {
            bytesRead += this.write(srcs[i]);
        }
        return bytesRead;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.SeekableByteChannel#position()
     */
    @Override
    public long position() throws IOException {
        synchronized (positionLock) {
            // in append-mode, the position is advanced to end before writing
            if (_flags.append) {
                return size();
            }
            return _currentPosition;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.SeekableByteChannel#position(long)
     */
    @Override
    public NfsFileChannel position(long newPosition) throws IOException, ClosedChannelException {
        checkForClosed();
        if (newPosition < 0)
            throw new IllegalArgumentException("Negative size");

        synchronized (positionLock) {
            /*
             *  JDK8 allows the position to move even in append mode, which might be why it reports
             *  current file size from position(). There doesn't seem any value in doing so.
             */
            if (!_flags.append) {
                _currentPosition = newPosition;
            }
            return this;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.SeekableByteChannel#size()
     */
    @Override
    public long size() throws IOException {
        synchronized (positionLock) {
            NfsGetAttrResponse response = _nfsFile.getattr();
            if (!response.stateIsOk()) {
                throw new IOException("Unable to get size");
            }
            _currentSize = response.getAttributes().getSize();
            return _currentSize;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)
     */
    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        checkForClosed();
        if (!_flags.read) { 
            throw new NonReadableChannelException();
        } else if ((position < 0) || (count < 0)) {
            throw new IndexOutOfBoundsException();
        }

        ByteBuffer src = ByteBuffer.allocate(_rtpref);
        long position0 = position;
        long bytesToTransfer = count;
        long bytesTransferred = 0;
        while (bytesToTransfer > 0) {
            int bytesRead = this.read(src, position0);
            if ((bytesRead == 0) || (bytesRead == EOF)) {
                return bytesTransferred;
            }
            src.flip();
            int bytesToWrite = bytesRead;
            long positionWriting = position0;
            while (bytesToWrite > 0) {
                int bytesWritten = target.write(src);
                if (bytesWritten == 0) {
                    throw new IOException("Failed to write bytes starting at offset " + positionWriting);
                }
                bytesToWrite -= bytesToWrite;
                positionWriting += bytesToWrite;
            }
            src.clear();
            bytesTransferred += bytesRead;
            bytesToTransfer -= bytesRead;
            position0 += bytesRead;
        }
        return bytesTransferred;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
     */
    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        checkForClosed();
        if (!_flags.write && !_flags.append) { 
            throw new NonWritableChannelException();
        } else if ((position < 0) || (count < 0)) {
            throw new IndexOutOfBoundsException();
        }

        ByteBuffer dst = ByteBuffer.allocate(_rtpref);
        long position0 = position;
        long bytesToTransfer = count;
        long bytesTransferred = 0;
        while (bytesToTransfer > 0) {
            int bytesRead = src.read(dst);
            if ((bytesRead == 0) || (bytesRead == EOF)) {
                return bytesTransferred;
            }
            dst.flip();
            int bytesToWrite = bytesRead;
            long positionWriting = position0;
            while (bytesToWrite > 0) {
                int bytesWritten = this.write(dst, position0);
                if (bytesWritten == 0) {
                    throw new IOException("Failed to write bytes starting at offset " + positionWriting);
                }
                bytesToWrite -= bytesToWrite;
                positionWriting += bytesToWrite;
            }
            dst.clear();
            bytesTransferred += bytesRead;
            bytesToTransfer -= bytesRead;
            position0 += bytesRead;
        }
        return bytesTransferred;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.SeekableByteChannel#truncate(long)
     */
    @Override
    public FileChannel truncate(long size) throws IOException, ClosedChannelException {
        checkForClosed();
        
        if ((!_flags.write) && (!_flags.append))
            throw new NonWritableChannelException();
        else if (size < 0)
            throw new IllegalArgumentException("Negative size");
        else if (size > size())
            return this;
        synchronized (positionLock) {
            if (size < _currentSize) {
                NfsSetAttributes setAttr = new NfsSetAttributes();
                setAttr.setSize(size);
                _nfsFile.setAttributes(setAttr);
                _currentSize = size;
            }
            if (_currentPosition > _currentSize) {
                _currentPosition = _currentSize;
            }
            return this;
        }
    }

    /**
     * <b>NOTE: Mapping not implemented for NfsFileChannel</b>
     */
    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException
    {
        throw new IllegalArgumentException("Mapping not implemented for class");
    }

    /**
     * <b>NOTE: Locking not implemented for NfsFileChannel</b>
     */
    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        throw new IllegalArgumentException("Locking not implemented for class");
    }

    /**
     * <b>NOTE: Locking not implemented for NfsFileChannel</b>
     */
    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        throw new IllegalArgumentException("Locking not implemented for class");
    }

    /**
     * Convenience function.
     * 
     * @throws ClosedChannelException
     *             If the channel has been closed.
     */
    private void checkForClosed() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.nio.channels.spi.AbstractInterruptibleChannel#implCloseChannel()
     */
    @Override
    protected void implCloseChannel() throws IOException {
        synchronized (_flags) {
            if (!isOpen())
                return;
            if (_flags.deleteOnClose) {
                _nfsFile.delete();
            }
        }
    }

    /**
     * Convenience class mapping OpenOptions to boolean flags
     */
    private static class Flags {
        boolean read = false;
        boolean write = false;
        boolean append = false;
        boolean truncateExisting = false;
        boolean create = false;
        boolean createNew = false;
        boolean deleteOnClose = false;
        boolean sync = false;
        boolean dsync = false;

        static Flags toFlags(Set<? extends OpenOption> options) {
            Flags flags = new Flags();
            for (OpenOption option: options) {
                if (option instanceof StandardOpenOption) {
                    switch ((StandardOpenOption)option) {
                        case READ : flags.read = true; break;
                        case WRITE : flags.write = true; break;
                        case APPEND : flags.append = true; break;
                        case TRUNCATE_EXISTING : flags.truncateExisting = true; break;
                        case CREATE : flags.create = true; break;
                        case CREATE_NEW : flags.createNew = true; break;
                        case DELETE_ON_CLOSE : flags.deleteOnClose = true; break;
                        case SYNC : flags.sync = true; break;
                        case DSYNC : flags.dsync = true; break;
                        default: throw new UnsupportedOperationException();
                    }
                    continue;
                }

                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException();
            }
            return flags;
        }
    }


}