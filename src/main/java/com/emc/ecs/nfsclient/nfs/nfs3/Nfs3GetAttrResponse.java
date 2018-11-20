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

import com.emc.ecs.nfsclient.nfs.NfsGetAttrResponse;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure GETATTR retrieves the attributes for a specified file system
 * object. The object is identified by the file handle that the server returned
 * as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK, MKNOD, or
 * READDIRPLUS procedure (or from the MOUNT service, described elsewhere).
 * </p>
 * 
 * @author seibed
 */
public class Nfs3GetAttrResponse extends NfsGetAttrResponse {

    /**
     * The response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     */
    public Nfs3GetAttrResponse() {
        super(Nfs3.VERSION);
    }

}
