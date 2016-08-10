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
 * This method reads the data associated with a symbolic link. The data is an
 * ASCII string that is opaque to the server. That is, whether created by the
 * NFS version 3 protocol software from a client or created locally on the
 * server, the data in a symbolic link is not interpreted when created, but is
 * simply stored.
 * </p>
 * 
 * <p>
 * A symbolic link is nominally a pointer to another file. The data is not
 * necessarily interpreted by the server, just stored in the file. It is
 * possible for a client implementation to store a path name that is not
 * meaningful to the server operating system in a symbolic link. A READLINK
 * operation returns the data to the client for interpretation. If different
 * implementations want to share access to symbolic links, then they must agree
 * on the interpretation of the data in the symbolic link.
 * </p>
 * 
 * @author seibed
 */
public class NfsReadlinkResponse extends NfsResponseBase {

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * This method reads the data associated with a symbolic link. The data is
     * an ASCII string that is opaque to the server. That is, whether created by
     * the NFS version 3 protocol software from a client or created locally on
     * the server, the data in a symbolic link is not interpreted when created,
     * but is simply stored.
     * </p>
     * 
     * <p>
     * A symbolic link is nominally a pointer to another file. The data is not
     * necessarily interpreted by the server, just stored in the file. It is
     * possible for a client implementation to store a path name that is not
     * meaningful to the server operating system in a symbolic link. A READLINK
     * operation returns the data to the client for interpretation. If different
     * implementations want to share access to symbolic links, then they must
     * agree on the interpretation of the data in the symbolic link.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsReadlinkResponse(int nfsVersion) {
        super();
    }

    /**
     * The data associated with the symbolic link.
     */
    private String _data;

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsResponseBase#unmarshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void unmarshalling(Xdr xdr) throws RpcException {
        super.unmarshalling(xdr);
        unmarshallingAttributes(xdr);
        if (stateIsOk()) {
            _data = xdr.getString();
        }
    }

    /**
     * @return The data associated with the symbolic link.
     */
    public String getData() {
        return _data;
    }

}
