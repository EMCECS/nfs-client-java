/**
link * Copyright 2016-2018 Dell Inc. or its subsidiaries. All rights reserved.
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

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure LINK creates a hard link from <code>fileHandle</code> to
 * <code>name</code>, in the directory, <code>parentDirectoryFileHandle</code>.
 * <code>fileHandle</code> and <code>parentDirectoryFileHandle</code> must
 * reside on the same file system and server.
 * </p>
 * 
 * @author seibed
 */
public class NfsLinkResponse extends NfsResponseBase {

    /**
     * Weak cache consistency data for the directory,
     * <code>parentDirectoryFileHandle</code>.
     */
    private NfsWccData _directoryWccData = new NfsWccData();

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>fileHandle</code> to
     * <code>name</code>, in the directory,
     * <code>parentDirectoryFileHandle</code>. <code>fileHandle</code> and
     * <code>parentDirectoryFileHandle</code> must reside on the same file
     * system and server.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsLinkResponse(int nfsVersion) {
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
        unmarshallingAttributes(xdr);
        _directoryWccData = new NfsWccData(xdr);
    }

    /**
     * @return Weak cache consistency data for the directory,
     *         <code>parentDirectoryFileHandle</code>.
     */
    public NfsWccData getDirectoryWccData() {
        return _directoryWccData;
    }

}
