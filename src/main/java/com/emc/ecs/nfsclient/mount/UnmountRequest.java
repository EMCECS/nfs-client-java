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
package com.emc.ecs.nfsclient.mount;

import org.apache.commons.lang.NotImplementedException;

import com.emc.ecs.nfsclient.nfs.Nfs;
import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.RpcRequest;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Representation of an Unmount request, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure UMNT removes the mount list entry for the directory that was
 * previously the subject of a MNT call from this client.
 * </p>
 * 
 * @author seibed
 */
public class UnmountRequest extends RpcRequest {

    /**
     * Procedure number for RPC calls.
     */
    private final static int MOUNTPROC_UMNT = 3;

    /**
     * An ASCII string that describes a directory on the server.
     */
    private final String _exportPointPath;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure UMNT removes the mount list entry for the directory that was
     * previously the subject of a MNT call from this client.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number.
     * @param exportPointPath
     *            An ASCII string that describes a directory on the server.
     * @param credential
     *            The credential used for RPC authentication.
     */
    public UnmountRequest(int nfsVersion, String exportPointPath, Credential credential) {
        super(Nfs.MOUNTPROG, nfsVersion, MOUNTPROC_UMNT, credential);
        _exportPointPath = exportPointPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcRequest#marshalling(com.emc.ecs.nfsclient.
     * rpc.Xdr)
     */
    public void marshalling(Xdr x) {
        super.marshalling(x);
        x.putString(_exportPointPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.rpc.RpcRequest#getErrorMessage()
     */
    public String getErrorMessage() {
        throw new NotImplementedException("This method should never be used during unmount calls.");
    }

}
