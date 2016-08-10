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

import com.emc.ecs.nfsclient.nfs.NfsSetAttrRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsTime;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure SETATTR changes one or more of the attributes of a file system
 * object on the server.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3SetAttrRequest extends NfsSetAttrRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SETATTR changes one or more of the attributes of a file system
     * object on the server.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the object.
     * @param attributes
     *            The attributes to set.
     * @param guardTime
     *            If <code>guardTime</code> is not null, the server must compare
     *            the value of <code>guardTime</code> to the current ctime of
     *            the object. If the values are different, the server must
     *            preserve the object attributes and must return a status of
     *            NFS3ERR_NOT_SYNC.
     * @param credential
     *            The credential used for RPC authentication.
     * @throws FileNotFoundException
     */
    public Nfs3SetAttrRequest(byte[] fileHandle, NfsSetAttributes attributes, NfsTime guardTime, Credential credential)
            throws FileNotFoundException {
        super(fileHandle, attributes, guardTime, credential, Nfs3.VERSION);
    }

}
