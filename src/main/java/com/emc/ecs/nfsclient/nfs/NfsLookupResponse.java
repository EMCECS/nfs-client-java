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

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure LOOKUP searches a directory for a specific name and returns the
 * file handle for the corresponding file system object.
 * </p>
 * 
 * @author seibed
 */
public class NfsLookupResponse extends NfsResponseBase {

    /**
     * The post-operation attributes for the directory.
     */
    private NfsGetAttributes _directoryAttributes;

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsLookupResponse(int nfsVersion) {
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
            unmarshallingFileHandle(xdr, true);
            unmarshallingAttributes(xdr);
        }
        _directoryAttributes = makeNfsGetAttributes(xdr);
    }

    /**
     * @return The post-operation attributes for the directory.
     */
    public NfsGetAttributes getDirectoryAttributes() {
        return _directoryAttributes;
    }

}
