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
package com.emc.ecs.nfsclient.nfs.nfs3;

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.nfs.NfsRmdirRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

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
public class Nfs3RmdirRequest extends NfsRmdirRequest {

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
     * @throws FileNotFoundException
     */
    public Nfs3RmdirRequest(byte[] fileHandle, String name, Credential credential) throws FileNotFoundException {
        super(fileHandle, name, credential, Nfs3.VERSION);
    }

}
