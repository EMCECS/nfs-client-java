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
 * Procedure RENAME renames the file identified by <code>name</code> in the
 * directory, <code>fileHandle</code>, to <code>toName</code> in the directory,
 * <code>toFileHandle</code>. The operation is required to be atomic to the
 * client. <code>toFileHandle</code> and <code>fileHandle</code> must reside on
 * the same file system and server.
 * </p>
 * 
 * @author seibed
 */
public class NfsRenameRequest extends NfsRequestBase {

    /**
     * The name of the entry that identifies the object to be renamed
     */
    private final String _name;

    /**
     * The file handle for the directory to which the object is to be renamed.
     */
    private final byte[] _toFileHandle;

    /**
     * The new name for the object.
     */
    private final String _toName;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RENAME renames the file identified by <code>name</code> in the
     * directory, <code>fileHandle</code>, to <code>toName</code> in the
     * directory, <code>toFileHandle</code>. The operation is required to be
     * atomic to the client. <code>toFileHandle</code> and
     * <code>fileHandle</code> must reside on the same file system and server.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory from which the entry is to
     *            be renamed
     * @param name
     *            The name of the entry that identifies the object to be renamed
     * @param toFileHandle
     *            The file handle for the directory to which the object is to be
     *            renamed.
     * @param toName
     *            The new name for the object.
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsRenameRequest(byte[] fileHandle, String name, byte[] toFileHandle, String toName, Credential credential,
            int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_RENAME, credential, fileHandle);
        _toFileHandle = cloneFileHandle(toFileHandle);
        _name = trimFileName(name);
        _toName = trimFileName(toName);
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
        xdr.putByteArray(_toFileHandle);
        xdr.putString(_toName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsRenameRequest").append(" name:").append(_name).append(" toFileHandle:")
                .append(Arrays.toString(_toFileHandle)).append(" toName:").append(_toName).toString();
    }

}
