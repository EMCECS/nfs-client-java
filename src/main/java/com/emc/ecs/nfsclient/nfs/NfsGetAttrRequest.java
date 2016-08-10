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
 * Procedure GETATTR retrieves the attributes for a specified file system
 * object. The object is identified by the file handle that the server returned
 * as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK, MKNOD, or
 * READDIRPLUS procedure (or from the MOUNT service, described elsewhere).
 * </p>
 * 
 * @author seibed
 */
public class NfsGetAttrRequest extends NfsRequestBase {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     * 
     * @param fileHandle
     *            The file handle of an object whose attributes are to be
     *            retrieved.
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsGetAttrRequest(byte[] fileHandle, Credential credential, int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_GETATTR, credential, fileHandle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsGetAttrRequest").toString();
    }

}
