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

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.RpcResponse;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Representation of a Mount response, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure MNT maps a pathname on the server to a file handle.
 * </p>
 * 
 * @author seibed
 */
public class MountResponse extends RpcResponse {

    /**
     * A status indicator, expected to have one of the values below.
     * <ul>
     * <li>MNT3_OK = 0 (no error)</li>
     * <li>MNT3ERR_PERM = 1 (Not owner)</li>
     * <li>MNT3ERR_NOENT = 2 (No such file or directory)</li>
     * <li>MNT3ERR_IO = 5 (I/O error)</li>
     * <li>MNT3ERR_ACCES = 13 (Permission denied)</li>
     * <li>MNT3ERR_NOTDIR = 20 (Not a directory)</li>
     * <li>MNT3ERR_INVAL = 22 (Invalid argument)</li>
     * <li>MNT3ERR_NAMETOOLONG = 63 (Filename too long)</li>
     * <li>MNT3ERR_NOTSUPP = 10004 (Operation not supported)</li>
     * <li>MNT3ERR_SERVERFAULT = 10006 (A failure on the server)</li>
     * </ul>
     */
    private int _mountStatus;

    /**
     * The file handle for the exported directory.
     */
    private byte[] _rootFileHandle;

    /**
     * The array of supported authentication flavors, as defined in RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private int[] _authenticationFlavors;

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MNT maps a pathname on the server to a file handle.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public MountResponse(int nfsVersion) {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcResponse#unmarshalling(com.emc.ecs.nfsclient
     * .rpc.Xdr)
     */
    public void unmarshalling(Xdr xdr) throws RpcException {
        super.unmarshalling(xdr);
        _mountStatus = xdr.getInt();
        if (_mountStatus == MountStatus.MNT3_OK.getValue()) {
            _rootFileHandle = xdr.getByteArray();
            int numsec = xdr.getInt();
            _authenticationFlavors = new int[numsec];
            for (int i = 0; i < numsec; ++i) {
                _authenticationFlavors[i] = xdr.getInt();
            }
        }
    }

    /**
     * @return A status indicator, expected to have one of the MountStatus
     *         values as below.
     *         <ul>
     *         <li>MNT3_OK = 0 (no error)</li>
     *         <li>MNT3ERR_PERM = 1 (Not owner)</li>
     *         <li>MNT3ERR_NOENT = 2 (No such file or directory)</li>
     *         <li>MNT3ERR_IO = 5 (I/O error)</li>
     *         <li>MNT3ERR_ACCES = 13 (Permission denied)</li>
     *         <li>MNT3ERR_NOTDIR = 20 (Not a directory)</li>
     *         <li>MNT3ERR_INVAL = 22 (Invalid argument)</li>
     *         <li>MNT3ERR_NAMETOOLONG = 63 (Filename too long)</li>
     *         <li>MNT3ERR_NOTSUPP = 10004 (Operation not supported)</li>
     *         <li>MNT3ERR_SERVERFAULT = 10006 (A failure on the server)</li>
     *         </ul>
     */
    public int getMountStatus() {
        return _mountStatus;
    }

    /**
     * @return The file handle for the exported directory.
     */
    public byte[] getRootFileHandle() {
        return _rootFileHandle.clone();
    }

    /**
     * @return The array of supported authentication flavors, as defined in RFC
     *         1831 (https://tools.ietf.org/html/rfc1831).
     */
    public int[] getAuthenticationFlavors() {
        return (_authenticationFlavors == null) ? null : _authenticationFlavors.clone();
    }

}
