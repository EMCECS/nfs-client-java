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
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
 * directory entry of the subdirectory is the last reference to the
 * subdirectory, the subdirectory may be destroyed.
 * </p>
 * 
 * @author seibed
 */
public class NfsRmdirRequest extends NfsRequestBase {

    /**
     * The name of the subdirectory to be removed.
     */
    private final String _name;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
     * directory entry of the subdirectory is the last reference to the
     * subdirectory, the subdirectory may be destroyed.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory from which the subdirectory
     *            is to be removed.
     * @param name
     *            The name of the subdirectory to be removed.
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsRmdirRequest(byte[] fileHandle, String name, Credential credential, int nfsVersion)
            throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_RMDIR, credential, fileHandle);
        _name = trimFileName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsRequestBase#marshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        xdr.putString(_name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsRmdirRequest").append(" name").append(_name).toString();
    }

}
