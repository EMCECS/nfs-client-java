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

import com.emc.ecs.nfsclient.nfs.NfsReadResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * The NFS equivalent of <code>java.io.FileInputStream</code>.
 * 
 * @author seibed
 */
public class NfsFileInputStream extends InputStream {

    /**
     * Constant for output.
     */
    public static final int EOF = -1;

    /**
     * The underlying file.
     */
    private NfsFile<?, ?> _file;

    /**
     * Current read position in the file, in bytes.
     */
    private long _offset;

    /**
     * The byte buffer, used to hold data from the file during reading.
     */
    private final byte[] _bytes;

    /**
     * The total number of bytes in the buffer.
     */
    private int _bytesInBuffer = 0;

    /**
     * Current reading position in the buffer.
     */
    private int _currentBufferPosition = 0;

    /**
     * <ul>
     * <li><code>true</code> if all data in the file has been returned and read,
     * </li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean _isEof = false;

    /**
     * Flag to prevent operations after closing.
     */
    private boolean _closed = false;

    /**
     * Creates a <code>NfsFileInputStream</code> by opening a connection to an
     * actual NFS file, using the specified <code>offset</code> and
     * <code>maximumBufferSize</code>.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     * </p>
     *
     * @param nfsFile
     *            The NFS file instance to be read.
     * @param offset
     *            The offset at which reading should start, in bytes.
     * @param maximumBufferSize
     *            The maximum buffer size to use in bytes.
     * @throws IOException
     *             If the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading.
     */
    public NfsFileInputStream(NfsFile<?, ?> nfsFile, long offset, int maximumBufferSize) throws IOException {
        // Validate the offset.
        if (offset < 0) {
            throw new IllegalArgumentException("Cannot start reading before offset 0: " + offset);
        }

        // Validate the maximum buffer size.
        if (maximumBufferSize <= 0) {
            throw new IllegalArgumentException("Cannot have a maximum buffer size <= 0: " + maximumBufferSize);
        }

        // Validate the file.
        if (!nfsFile.canRead()) {
            throw new IllegalArgumentException("The file must be readable by the client: " + nfsFile.getAbsolutePath());
        }

        _file = nfsFile;
        _offset = offset;
        maximumBufferSize = Math.min(maximumBufferSize,
                (int) Math.min(_file.fsinfo().getFsInfo().rtmax, Integer.MAX_VALUE));
        _bytes = makeBytes(maximumBufferSize);
    }

    /**
     * Creates a <code>NfsFileInputStream</code> by opening a connection to an
     * actual NFS file, starting to read at offset 0 and using the specified
     * maximum buffer size.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     * </p>
     *
     * @param nfsFile
     *            The NFS file instance to be read.
     * @param maximumBufferSize
     *            The maximum buffer size to use in bytes.
     * @throws IOException
     *             If the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading.
     */
    public NfsFileInputStream(NfsFile<?, ?> nfsFile, int maximumBufferSize) throws IOException {
        this(nfsFile, 0, maximumBufferSize);
    }

    /**
     * Creates a <code>NfsFileInputStream</code> by opening a connection to an
     * actual NFS file, starting to read at offset 0 and using the preferred
     * buffer size as the maximums. This constructor will generally give you the
     * best performance.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     * </p>
     *
     * @param nfsFile
     *            The NFS file instance to be read.
     * @throws IOException
     *             If the file does not exist, is a directory rather than a
     *             regular file, or for some other reason cannot be opened for
     *             reading.
     */
    public NfsFileInputStream(NfsFile<?, ?> nfsFile) throws IOException {
        this(nfsFile, (int) Math.min(nfsFile.fsinfo().getFsInfo().rtpref, Integer.MAX_VALUE));
    }

    /**
     * @param maximumBufferSize
     * @return the byte array
     * @throws IOException
     */
    private byte[] makeBytes(int maximumBufferSize) throws IOException {
        int bufferSize = Math.min((int) Math.min(_file.length() - _offset, Integer.MAX_VALUE), maximumBufferSize);
        return new byte[bufferSize];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        checkForClosed();
        return (int) (_file.length() - _offset + bytesLeftInBuffer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        _closed = true;
        super.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#mark(int)
     */
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        byte[] b = new byte[1];
        int bytesRead = read(b, 0, 1);
        if (bytesRead == EOF) {
            return EOF;
        } else {
        //byte type in Java is from -128 to +127 and we are supposed to return 0-255
            return b[0] & 0xFF;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        checkForClosed();
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (len < 0) || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        loadBytesAsNeeded();
        int bytesImmediatelyAvailable = bytesLeftInBuffer();
        if (len <= bytesImmediatelyAvailable) {
            System.arraycopy(_bytes, _currentBufferPosition, b, off, len);
            _currentBufferPosition += len;
            return len;
        }

        int bytesRead = EOF;
        if (bytesImmediatelyAvailable > 0) {
            bytesRead = read(b, off, bytesImmediatelyAvailable);
            if (bytesRead != EOF) {
                int furtherBytesRead = read(b, off + bytesRead, len - bytesRead);
                if (furtherBytesRead != EOF) {
                    bytesRead += furtherBytesRead;
                }
            }
        }
        return bytesRead;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#reset()
     */
    public synchronized void reset() throws IOException {
        checkForClosed();
        super.reset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long bytesToSkip) throws IOException {
        checkForClosed();
        long bytesSkipped = 0;
        while (bytesToSkip > bytesLeftInBuffer()) {
            bytesSkipped += bytesLeftInBuffer();
            bytesToSkip -= bytesLeftInBuffer();
            _currentBufferPosition = _bytesInBuffer;
            if (_isEof) {
                break;
            }
            loadBytesAsNeeded();
        }
        if ((bytesToSkip > 0) && (bytesToSkip <= bytesLeftInBuffer())) {
            _currentBufferPosition += (int) bytesToSkip;
            bytesSkipped += (int) bytesToSkip;
        }
        return bytesSkipped;
    }

    /**
     * @return The number of unread bytes in the buffer.
     */
    private int bytesLeftInBuffer() {
        return _bytesInBuffer - _currentBufferPosition;
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
     * If the buffer has no more bytes to be read, load more bytes.
     * 
     * @throws IOException
     */
    private void loadBytesAsNeeded() throws IOException {
        while ((!_isEof) && (bytesLeftInBuffer() <= 0)) {
            _currentBufferPosition = 0;
            NfsReadResponse response = _file.read(_offset, _bytes.length, _bytes, _currentBufferPosition);
            _bytesInBuffer = response.getBytesRead();
            _offset += _bytesInBuffer;
            _isEof = response.isEof();
        }
    }

}
