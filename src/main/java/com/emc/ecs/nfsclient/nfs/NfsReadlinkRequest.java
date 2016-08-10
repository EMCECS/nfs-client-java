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

import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
public class NfsReadlinkRequest extends NfsRequestBase {

    /**
     * Creates the request, as specified by RFC 1813
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
     * @param fileHandle
     *            the fileHandle for the symbolic link
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsReadlinkRequest(byte[] fileHandle, Credential credential, int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_READLINK, credential, fileHandle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsReadlinkRequest").toString();
    }

}
