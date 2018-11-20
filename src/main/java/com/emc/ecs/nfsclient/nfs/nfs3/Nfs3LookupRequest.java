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

import com.emc.ecs.nfsclient.nfs.NfsLookupRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure LOOKUP searches a directory for a specific name and returns the
 * file handle for the corresponding file system object.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3LookupRequest extends NfsLookupRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory to search.
     * @param name
     *            The filename to be searched for.
     * @param credential
     *            The credential to use for the request
     * @throws FileNotFoundException
     */
    public Nfs3LookupRequest(byte[] fileHandle, String name, Credential credential) throws FileNotFoundException {
        super(fileHandle, name, credential, Nfs3.VERSION);
    }

}
