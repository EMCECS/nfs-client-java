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
package com.emc.ecs.nfsclient.nfs;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.List;

import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure WRITE writes data to a file.
 * </p>
 * 
 * @author seibed
 */
public class NfsWriteRequest extends NfsRequestBase {

    /**
     * The server is free to commit any part of the data and the metadata to
     * stable storage, including all or none, before returning a reply to the
     * client.
     */
    public final static int UNSTABLE = 0;

    /**
     * The server must commit all of the data to stable storage and enough of
     * the metadata to retrieve the data before returning.
     */
    public final static int DATA_SYNC = 1;

    /**
     * The server must commit the data written plus all file system metadata to
     * stable storage before returning results.
     */
    public final static int FILE_SYNC = 2;

    /**
     * The position within the file at which the write is to begin. An offset of
     * 0 means to write data starting at the beginning of the file.
     */
    private final long _offset;

    /**
     * The number of bytes of data to be written (calculated from the
     * <code>payload</code>). If <code>size</code> is 0, the WRITE will succeed
     * and return a <code>count</code> of 0, barring errors due to permissions
     * checking. The <code>size</code> of data must be less than or equal to the
     * value of the <code>wtmax</code> field in the FSINFO reply structure for
     * the file system that contains file. If greater, the server may write only
     * <code>wtmax</code> bytes, resulting in a short write.
     */
    private final int _size;

    /**
     * One of the values below.
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
     * The data to be written to the file.
     */
    private final List<ByteBuffer> _payload;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the file to which data is to be written.
     *            This must identify a file system object of type, NF3REG.
     * @param offset
     *            The position within the file at which the write is to begin.
     *            An offset of 0 means to write data starting at the beginning
     *            of the file.
     * @param payload
     *            The data to be written to the file.
     * @param syncType
     *            One of the values below.
     *            <ul>
     *            <li>UNSTABLE = 0 - Best effort, no promises.</li>
     *            <li>DATA_SYNC = 1 - Commit all data to stable storage, plus
     *            enough metadata for retrieval, before returning.</li>
     *            <li>FILE_SYNC = 2 - Commit all data and metadata to stable
     *            storage before returning.</li>
     *            </ul>
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsWriteRequest(byte[] fileHandle, long offset, List<ByteBuffer> payload, int syncType,
            Credential credential, int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_WRITE, credential, fileHandle);
        if ((syncType < 0) || (syncType > 2)) {
            throw new IllegalArgumentException("Invalid syncType: " + syncType);
        }

        _offset = offset;
        _syncType = syncType;
        _payload = payload;
        int payload_size = 0;
        if (_payload != null) {
            for (ByteBuffer b : _payload) {
                payload_size += b.remaining();
            }
        }
        _size = payload_size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsRequestBase#marshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        xdr.putLong(_offset);
        xdr.putUnsignedInt(_size);
        xdr.putInt(_syncType);
        // opaque payload, pass payload size down to avoid enumerating the list
        // again
        if (_payload != null) {
            xdr.putPayloads(_payload, _size);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsWriteRequest").append(" offset:").append(_offset).append(" size:").append(_size)
                .append(" syncType:").append(_syncType).toString();
    }

    /**
     * @return The number of bytes of data to be written (calculated from the
     *         <code>payload</code>). If <code>size</code> is 0, the WRITE will
     *         succeed and return a <code>count</code> of 0, barring errors due
     *         to permissions checking. The <code>size</code> of data must be
     *         less than or equal to the value of the <code>wtmax</code> field
     *         in the FSINFO reply structure for the file system that contains
     *         file. If greater, the server may write only <code>wtmax</code>
     *         bytes, resulting in a short write.
     */
    public int getSize() {
        return _size;
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if data is synced</li>
     *         <li><code>false</code> if it is not.</li>
     *         </ul>
     */
    public boolean isSync() {
        return _syncType == FILE_SYNC;
    }

}
