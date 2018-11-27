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

import com.emc.ecs.nfsclient.nfs.NfsRenameResponse;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
public class Nfs3RenameResponse extends NfsRenameResponse {

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RENAME renames the file identified by <code>name</code> in the
     * directory, <code>fileHandle</code>, to <code>toName</code> in the
     * directory, <code>toFileHandle</code>. The operation is required to be
     * atomic to the client. <code>toFileHandle</code> and
     * <code>fileHandle</code> must reside on the same file system and server.
     * </p>
     */
    public Nfs3RenameResponse() {
        super(Nfs3.VERSION);
    }

}
