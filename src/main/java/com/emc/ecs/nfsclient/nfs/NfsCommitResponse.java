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
package com.emc.ecs.nfsclient.nfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure COMMIT forces or flushes data to stable storage that was previously
 * written with a WRITE procedure call with the stable field set to UNSTABLE.
 * </p>
 * 
 * @author seibed
 */
public class NfsCommitResponse extends NfsResponseBase {

    private static final Logger _log = LoggerFactory.getLogger(NfsCommitResponse.class);

    /**
     * Weak cache consistency data for the file.
     */
    private NfsWccData _fileWccData = new NfsWccData();

    /**
     * This is a cookie that the client can use to determine whether the server
     * has rebooted between a call to WRITE and a subsequent call to COMMIT.
     * This cookie must be consistent during a single boot session and must be
     * unique between instances of the NFS version 3 protocol server where
     * uncommitted data may be lost.
     */
    private long _verf;

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsCommitResponse(int nfsVersion) {
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
            _verf = xdr.getLong();
            _log.trace("verf {}", _verf);
        }
    }

    /**
     * @return A cookie that the client can use to determine whether the server
     *         has rebooted between a call to WRITE and a subsequent call to
     *         COMMIT. This cookie must be consistent during a single boot
     *         session and must be unique between instances of the NFS version 3
     *         protocol server where uncommitted data may be lost.
     */
    public long getVerf() {
        return _verf;
    }

    /**
     * @return Weak cache consistency data for the file.
     */
    public NfsWccData getFileWccData() {
        return _fileWccData;
    }

}
