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
 * Procedure SYMLINK creates a new symbolic link.
 * </p>
 * 
 * @author seibed
 */
public class NfsSymlinkRequest extends NfsRequestBase {

    /**
     * The string containing the symbolic link data.
     */
    private final String _symbolicLinkData;

    /**
     * The name that is to be associated with the created symbolic link.
     */
    private final String _name;

    /**
     * The initial attributes for the symbolic link.
     */
    private final NfsSetAttributes _attributes;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SYMLINK creates a new symbolic link.
     * </p>
     * 
     * @param symbolicLinkData
     *            The string containing the symbolic link data.
     * @param fileHandle
     *            The file handle for the directory in which the symbolic link
     *            is to be created.
     * @param name
     *            The name that is to be associated with the created symbolic
     *            link.
     * @param attributes
     *            The initial attributes for the symbolic link.
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsSymlinkRequest(String symbolicLinkData, byte[] fileHandle, String name, NfsSetAttributes attributes,
            Credential credential, int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_SYMLINK, credential, fileHandle);
        _symbolicLinkData = symbolicLinkData;
        _name = trimFileName(name);
        _attributes = attributes;
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
        _attributes.marshalling(xdr);
        xdr.putString(_symbolicLinkData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsSymbolicLinkRequest").append(" name:").append(_name).append(" symbolicLinkData")
                .append(_symbolicLinkData).append(" attributes:").append(_attributes.toString()).toString();
    }

}
