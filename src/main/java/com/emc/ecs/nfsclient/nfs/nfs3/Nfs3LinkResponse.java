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

import com.emc.ecs.nfsclient.nfs.NfsLinkResponse;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
public class Nfs3LinkResponse extends NfsLinkResponse {

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>fileHandle</code> to
     * <code>name</code>, in the directory,
     * <code>parentDirectoryFileHandle</code>. <code>fileHandle</code> and
     * <code>parentDirectoryFileHandle</code> must reside on the same file
     * system and server.
     * </p>
     */
    public Nfs3LinkResponse() {
        super(Nfs3.VERSION);
    }

}
