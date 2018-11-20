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

import com.emc.ecs.nfsclient.nfs.NfsCreateMode;
import com.emc.ecs.nfsclient.nfs.NfsCreateRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure CREATE creates a regular file.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3CreateRequest extends NfsCreateRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure CREATE creates a regular file.
     * </p>
     * 
     * @param createMode
     *            The instance describing how the file is to be created.
     *            UNCHECKED means that the file should be created without
     *            checking for the existence of a duplicate file in the same
     *            directory. In this case, how.obj_attributes is a sattr3
     *            describing the initial attributes for the file. GUARDED
     *            specifies that the server should check for the presence of a
     *            duplicate file before performing the create and should fail
     *            the request with NFS3ERR_EXIST if a duplicate file exists. If
     *            the file does not exist, the request is performed as described
     *            for UNCHECKED. EXCLUSIVE specifies that the server is to
     *            follow exclusive creation semantics, using the verifier to
     *            ensure exclusive creation of the target. No attributes may be
     *            provided in this case, since the server may use the target
     *            file metadata to store the createverf3 verifier.
     * @param parentDirectoryFileHandle
     *            The file handle for the directory in which the file is to be
     *            created.
     * @param name
     *            The name that is to be associated with the created file.
     * @param attributes
     *            The initial attributes for the new file.
     * @param verifier
     *            A unique identifier to ensure exclusive creation of the new
     *            file. If no value is provided for exclusive creation, this is
     *            generated.
     * @param credential
     *            The credential to use for the request
     * @throws FileNotFoundException
     */
    public Nfs3CreateRequest(NfsCreateMode createMode, byte[] parentDirectoryFileHandle, String name,
            NfsSetAttributes attributes, byte[] verifier, Credential credential) throws FileNotFoundException {
        super(createMode, parentDirectoryFileHandle, name, attributes, verifier, credential, Nfs3.VERSION);
    }

}
