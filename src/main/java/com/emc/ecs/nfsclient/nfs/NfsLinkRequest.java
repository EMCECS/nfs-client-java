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
import java.util.Arrays;

import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
public class NfsLinkRequest extends NfsRequestBase {

    /**
     * The file handle for the directory in which the link is to be created.
     */
    private final byte[] _parentDirectoryFileHandle;

    /**
     * The name that is to be associated with the created link.
     */
    private final String _name;

    /**
     * Creates the request, as specified by RFC 1813
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
     * @param fileHandle
     *            The file handle for the existing file system object.
     * @param parentDirectoryFileHandle
     *            The file handle for the directory in which the link is to be
     *            created.
     * @param name
     *            The name that is to be associated with the created link.
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsLinkRequest(byte[] fileHandle, byte[] parentDirectoryFileHandle, String name, Credential credential,
            int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_LINK, credential, fileHandle);
        _parentDirectoryFileHandle = cloneFileHandle(parentDirectoryFileHandle);
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
        xdr.putByteArray(_parentDirectoryFileHandle);
        xdr.putString(_name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsLinkRequest").append(" parentDirectoryFileHandle:")
                .append(Arrays.toString(_parentDirectoryFileHandle)).append(" name:").append(_name).toString();
    }

}
