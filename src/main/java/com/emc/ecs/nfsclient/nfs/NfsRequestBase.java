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
package com.emc.ecs.nfsclient.nfs;

import java.io.FileNotFoundException;
import java.util.Arrays;

import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.RpcRequest;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The common base class for all requests specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public abstract class NfsRequestBase extends RpcRequest implements NfsRequest {

    /**
     * The primary file handle, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * <ul>
     * <li>For operations GETATTR, SETATTR, ACCESS, READLINK, READ, WRITE, LINK,
     * READDIR, READDIRPLUS, SSTAT, FSINFO, PATHCONF, and COMMIT, this is the
     * file handle for the object on which the operation will be performed.</li>
     * <li>For operations LOOKUP, CREATE, MKDIR, SYMLINK, MKNOD, REMOVE, RMDIR,
     * RENAME, this is the file handle for the directory on which the operation
     * will be performed.</li>
     * </ul>
     */
    private final byte[] _fileHandle;

    /**
     * Creates the basic request structure.
     * 
     * @param serviceProgram
     *            The program number of the service.
     * @param serviceVersion
     *            The version number of the service.
     * @param serviceProcedure
     *            The service procedure number to be called.
     * @param credential
     *            The credential.
     * @param fileHandle
     *            The primary file handle, as specified by RFC 1813
     *            (https://tools.ietf.org/html/rfc1813).
     *            <ul>
     *            <li>For operations GETATTR, SETATTR, ACCESS, READLINK, READ,
     *            WRITE, LINK, READDIR, READDIRPLUS, SSTAT, FSINFO, PATHCONF,
     *            and COMMIT, this is the file handle for the object on which
     *            the operation will be performed.</li>
     *            <li>For operations LOOKUP, CREATE, MKDIR, SYMLINK, MKNOD,
     *            REMOVE, RMDIR, RENAME, this is the file handle for the
     *            directory on which the operation will be performed.</li>
     *            </ul>
     * @throws FileNotFoundException
     */
    public NfsRequestBase(int serviceProgram, int serviceVersion, int serviceProcedure, Credential credential,
            byte[] fileHandle) throws FileNotFoundException {
        super(serviceProgram, serviceVersion, serviceProcedure, credential);
        _fileHandle = cloneFileHandle(fileHandle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.rpc.RpcRequest#getErrorMessage()
     */
    public final String getErrorMessage() {
        return "Error in " + toString();
    }

    /**
     * @return The primary file handle, as specified by RFC 1813
     *         (https://tools.ietf.org/html/rfc1813).
     *         <ul>
     *         <li>For operations GETATTR, SETATTR, ACCESS, READLINK, READ,
     *         WRITE, LINK, READDIR, READDIRPLUS, SSTAT, FSINFO, PATHCONF, and
     *         COMMIT, this is the file handle for the object on which the
     *         operation will be performed.</li>
     *         <li>For operations LOOKUP, CREATE, MKDIR, SYMLINK, MKNOD, REMOVE,
     *         RMDIR, RENAME, this is the file handle for the directory on which
     *         the operation will be performed.</li>
     *         </ul>
     */
    public final byte[] getFileHandle() {
        return _fileHandle;
    }

    /**
     * @return the key, used in determining IP addresses for RPC calls.
     */
    public final byte[] getIpKey() {
        return getFileHandle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcRequest#marshalling(com.emc.ecs.nfsclient.
     * rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        xdr.putByteArray(_fileHandle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.rpc.RpcRequest#startToString(java.lang.String)
     */
    protected final StringBuilder startToString(String requestlabel) {
        return super.startToString(requestlabel).append(" fileHandle:").append(Arrays.toString(_fileHandle));
    }

}
