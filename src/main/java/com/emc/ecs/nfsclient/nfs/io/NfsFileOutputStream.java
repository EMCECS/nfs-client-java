/**
 * Copyright 2016 EMC Corporation. All Rights Reserved.
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

import com.emc.ecs.nfsclient.nfs.NfsCreateMode;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsWriteRequest;
import com.emc.ecs.nfsclient.nfs.NfsWriteResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NFS equivalent of <code>java.io.FileOutputStream</code>.
 * 
 * @author seibed
 */
public class NfsFileOutputStream extends OutputStream {

    /**
     * The usual logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NfsFileOutputStream.class);

    private NfsFile<?, ?> _nfsFile;

    /**
     * The file offset used to write data that has not been committed. This is
     * advanced after each commit.
     */
    private long _offset;

    /**
     * The current value of the file offset to which we have written data. This
     * is advanced after each write.
     */
    private long _currentOffset;

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
     * The data currently being buffered.
     */
    private final byte[] _buffer;

    /**
     * The next position for writing in the buffer.
     */
    private int _bufferOffset = 0;

    /**
     * Flag to make sure operations are not run after closing the stream.
     */
    private boolean _closed = false;

    /**
     * Creates a file output stream to write to the file represented by the
     * specified <code>NfsFile</code> object, starting at
     * <code>offset = 0</code> and using <code>syncType = FILE_SYNC</code>
     * (commit all data and metadata to stable storage before returning).
     * <p>
     * If the file does not exist, it will first be created.
     *
     * @param nfsFile
     *            The file to be opened for writing.
     * @throws IOException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, or cannot be
     *             opened for any other reason
     */
    public NfsFileOutputStream(NfsFile<?, ?> nfsFile) throws IOException {
        this(nfsFile, 0, NfsWriteRequest.FILE_SYNC);
    }

    /**
     * Creates a file output stream to write to the file represented by the
     * specified <code>NfsFile</code> object, starting at
     * <code>offset = 0</code> and using <code>syncType</code> behavior.
     * <p>
     * If the file does not exist, it will first be created.
     *
     * @param nfsFile
     *            The file to be opened for writing.
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
    public NfsFileOutputStream(NfsFile<?, ?> nfsFile, int syncType) throws IOException {
        this(nfsFile, 0, syncType);
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
    public NfsFileOutputStream(NfsFile<?, ?> nfsFile, long offset, int syncType) throws IOException {
        // Validate the offset.
        if (offset < 0) {
            throw new IllegalArgumentException("Cannot start writing before offset 0: " + offset);
        }

        // Validate the syncType value.
        switch (syncType) {
        case NfsWriteRequest.DATA_SYNC:
        case NfsWriteRequest.FILE_SYNC:
        case NfsWriteRequest.UNSTABLE:
            break; // do nothing, these are fine.
        default:
            throw new IllegalArgumentException("The value of syncType is undefined: " + syncType);
        }

        _nfsFile = nfsFile;
        if (_nfsFile.exists()) {
            // Validate the file.
            if (!(_nfsFile.canExtend() && _nfsFile.canModify())) {
                throw new IllegalArgumentException(
                        "The file must be writable by the client: " + nfsFile.getAbsolutePath());
            }
        } else {
            // Create the file.
            NfsSetAttributes attributes = new NfsSetAttributes();
            attributes.setMode(NfsFile.ownerReadModeBit | NfsFile.ownerWriteModeBit);
            _nfsFile.create(NfsCreateMode.GUARDED, attributes, null);
        }
        _offset = offset;
        _currentOffset = offset;
        _syncType = syncType;
        _buffer = new byte[(int) Math.min(_nfsFile.fsinfo().getFsInfo().wtpref, Integer.MAX_VALUE)];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        if (!_closed) {
            try {
                flush();
            } catch (Throwable t) {
                LOG.debug(t.getMessage(), t);
            }
            _closed = true;
            super.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        checkForClosed();
        writeBufferToFile();
        if (_currentOffset > _offset) { // something to commit
            _nfsFile.commit(_offset, (int) (_currentOffset - _offset));
            _offset = _currentOffset;
        }
        super.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        checkForClosed();
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (len < 0) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        if (len > bytesLeftInBuffer()) {
            int bytesToWrite = bytesLeftInBuffer();
            write(b, off, bytesToWrite);
            write(b, off + bytesToWrite, len - bytesToWrite);
        } else {
            System.arraycopy(b, off, _buffer, _bufferOffset, len);
            _bufferOffset += len;
            if (bytesLeftInBuffer() == 0) {
                writeBufferToFile();
            }
        }
    }

    /**
     * @return The number of bytes that can be written to the buffer before
     *         overflow occurs.
     */
    private int bytesLeftInBuffer() {
        return _buffer.length - _bufferOffset;
    }

    /**
     * Convenience function.
     * 
     * @throws IOException
     *             If the stream has been closed.
     */
    private void checkForClosed() throws IOException {
        if (_closed) {
            throw new IOException("This stream has been closed.");
        }
    }

    /**
     * Write the buffer contents to the file and reset the buffer afterwards.
     * 
     * @throws IOException
     */
    private void writeBufferToFile() throws IOException {
        if (_bufferOffset > 0) {
            List<ByteBuffer> payload = new ArrayList<ByteBuffer>(1);
            payload.add(ByteBuffer.wrap(_buffer, 0, _bufferOffset));
            NfsWriteResponse response = _nfsFile.write(_currentOffset, payload, _syncType);
            int bytesWritten = response.getCount();
            _currentOffset += bytesWritten;
            _bufferOffset -= bytesWritten;
            if (0 != _bufferOffset) { // Everything was not written.
                // _bufferOffset should be the number of unwritten bytes.
                // Copy the unwritten bytes to the beginning of the buffer.
                System.arraycopy(_buffer, bytesWritten, _buffer, 0, _bufferOffset);
            }
        }
    }

}
