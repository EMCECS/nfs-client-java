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

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure MKNOD creates a new special file of the type, <code>type</code> .
 * Special files can be device files or named pipes.
 * </p>
 * 
 * @author seibed
 */
public class NfsMknodResponse extends NfsResponseBase {

    /**
     * Weak cache consistency data for the parent directory.
     */
    private NfsWccData _directoryWccData = new NfsWccData();

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsMknodResponse(int nfsVersion) {
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
        if (stateIsOk()) {
            unmarshallingFileHandle(xdr);
            unmarshallingAttributes(xdr);
        }
        _directoryWccData = new NfsWccData(xdr);
    }

    /**
     * @return Weak cache consistency data for the parent directory.
     */
    public NfsWccData getDirectoryWccData() {
        return _directoryWccData;
    }

}
