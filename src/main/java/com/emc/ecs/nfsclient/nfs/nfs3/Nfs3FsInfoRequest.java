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

import com.emc.ecs.nfsclient.nfs.NfsFsInfoRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure FSINFO retrieves nonvolatile file system state information and
 * general information about the NFS version 3 protocol server implementation.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3FsInfoRequest extends NfsFsInfoRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @param fileHandle
     *            A file handle identifying a file object. Normal usage is to
     *            provide a file handle for a mount point for a file system, as
     *            originally obtained from the MOUNT service on the server.
     * @param credential
     *            The credential to use for the request
     * @throws FileNotFoundException
     */
    public Nfs3FsInfoRequest(byte[] fileHandle, Credential credential) throws FileNotFoundException {
        super(fileHandle, credential, Nfs3.VERSION);
    }

}
