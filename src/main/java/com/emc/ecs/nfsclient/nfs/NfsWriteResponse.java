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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure WRITE writes data to a file.
 * </p>
 * 
 * @author seibed
 */
public class NfsWriteResponse extends NfsResponseBase {

    private static final Logger _log = LoggerFactory.getLogger(NfsWriteResponse.class);

    /**
     * One of the values below.
     * <ul>
     * <li>UNSTABLE = 0 - Best effort was made, no promises.</li>
     * <li>DATA_SYNC = 1 - Committed all data to stable storage, plus enough
     * metadata for retrieval, before returning.</li>
     * <li>FILE_SYNC = 2 - Committed all data and metadata to stable storage
     * before returning.</li>
     * </ul>
     */
    private int _committed;

    /**
     * The number of bytes of data written to the file. The server may write
     * fewer bytes than requested. If so, the actual number of bytes written
     * starting at location, <code>offset</code>, is returned.
     */
    private int _count;

    /**
     * Weak cache consistency data for the file.
     */
    private NfsWccData _fileWccData = new NfsWccData();

    /**
     * This is a cookie that the client can use to determine whether the server
     * has changed state between a call to WRITE and a subsequent call to either
     * WRITE or COMMIT. This cookie must be consistent during a single instance
     * of the NFS version 3 protocol service and must be unique between
     * instances of the NFS version 3 protocol server, where uncommitted data
     * may be lost.
     */
    private long _verf;

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsWriteResponse(int nfsVersion) {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsResponseBase#unmarshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void unmarshalling(Xdr xdr) throws RpcException {
        super.unmarshalling(xdr);
        _fileWccData = new NfsWccData(xdr);
        if (stateIsOk()) {
            _count = xdr.getInt();
            _committed = xdr.getInt();
            _verf = xdr.getLong();
            _log.trace("committed {} verf {}", _committed, _verf);
        }
    }

    /**
     * @return One of the values below.
     *         <ul>
     *         <li>UNSTABLE = 0 - Best effort was made, no promises.</li>
     *         <li>DATA_SYNC = 1 - Committed all data to stable storage, plus
     *         enough metadata for retrieval, before returning.</li>
     *         <li>FILE_SYNC = 2 - Committed all data and metadata to stable
     *         storage before returning.</li>
     *         </ul>
     */
    public int getCommitted() {
        return _committed;
    }

    /**
     * @return The number of bytes of data written to the file. The server may
     *         write fewer bytes than requested. If so, the actual number of
     *         bytes written starting at location, <code>offset</code>, is
     *         returned.
     */
    public int getCount() {
        return _count;
    }

    /**
     * @return Weak cache consistency data for the file.
     */
    public NfsWccData getFileWccData() {
        return _fileWccData;
    }

    /**
     * @return This is a cookie that the client can use to determine whether the
     *         server has changed state between a call to WRITE and a subsequent
     *         call to either WRITE or COMMIT. This cookie must be consistent
     *         during a single instance of the NFS version 3 protocol service
     *         and must be unique between instances of the NFS version 3
     *         protocol server, where uncommitted data may be lost.
     */
    public long getVerf() {
        return _verf;
    }

}
