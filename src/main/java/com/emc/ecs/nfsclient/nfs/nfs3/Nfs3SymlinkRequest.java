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
package com.emc.ecs.nfsclient.nfs.nfs3;

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsSymlinkRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure SYMLINK creates a new symbolic link.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3SymlinkRequest extends NfsSymlinkRequest {

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
     * @throws FileNotFoundException
     */
    public Nfs3SymlinkRequest(String symbolicLinkData, byte[] fileHandle, String name, NfsSetAttributes attributes,
            Credential credential) throws FileNotFoundException {
        super(symbolicLinkData, fileHandle, name, attributes, credential, Nfs3.VERSION);
    }

}
