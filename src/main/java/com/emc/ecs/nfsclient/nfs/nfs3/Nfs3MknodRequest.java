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

import com.emc.ecs.nfsclient.nfs.NfsMknodRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsType;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure MKNOD creates a new special file of the type, <code>type</code> .
 * Special files can be device files or named pipes.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3MknodRequest extends NfsMknodRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory in which the special file is
     *            to be created.
     * @param name
     *            The name that is to be associated with the created special
     *            file.
     * @param type
     *            The type of the object to be created.
     * @param attributes
     *            The initial attributes for the special file.
     * @param rdev
     *            When creating a character special file (<code>type</code> is
     *            <code>NF3CHR</code>) or a block special file (
     *            <code>type</code> is <code>NF3BLK</code>),
     *            <code>rdev[0]</code> is the major number and
     *            <code>rdev[1]</code> is the minor number.
     * @param credential
     *            The credential used for RPC authentication.
     * @throws FileNotFoundException
     */
    public Nfs3MknodRequest(byte[] fileHandle, String name, NfsType type, NfsSetAttributes attributes, long[] rdev,
            Credential credential) throws FileNotFoundException {
        super(fileHandle, name, type, attributes, rdev, credential, Nfs3.VERSION);
    }

}
