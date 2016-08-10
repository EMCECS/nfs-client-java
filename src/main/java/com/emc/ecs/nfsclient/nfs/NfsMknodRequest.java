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
package com.emc.ecs.nfsclient.nfs;

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.Xdr;

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
public class NfsMknodRequest extends NfsRequestBase {

    /**
     * The name that is to be associated with the created special file.
     */
    private final String _name;

    /**
     * The type of the object to be created.
     */
    private final NfsType _type;

    /**
     * The initial attributes for the special file.
     */
    private final NfsSetAttributes _attributes;

    /**
     * When creating a character special file (<code>type</code> is
     * <code>NF3CHR</code>) or a block special file ( <code>type</code> is
     * <code>NF3BLK</code>), <code>rdev[0]</code> is the major number and
     * <code>rdev[1]</code> is the minor number.
     */
    private final long[] _rdev;

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
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsMknodRequest(byte[] fileHandle, String name, NfsType type, NfsSetAttributes attributes, long[] rdev,
            Credential credential, int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_SYMLINK, credential, fileHandle);
        _name = trimFileName(name);
        _type = type;
        _attributes = attributes;
        _rdev = (rdev == null) ? null : rdev.clone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsRequestBase#marshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        xdr.putString(_name);
        xdr.putUnsignedInt(_type.getValue());
        if ((_type == NfsType.NFS_BLK) || (_type == NfsType.NFS_CHR)) {
            _attributes.marshalling(xdr);
            xdr.putUnsignedInt(_rdev[0]);
            xdr.putUnsignedInt(_rdev[1]);
        } else if ((_type == NfsType.NFS_SOCK) || (_type == NfsType.NFS_FIFO)) {
            _attributes.marshalling(xdr);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsMknodRequest").append(" name:").append(_name).append(" attributes:")
                .append(_attributes.toString()).toString();
    }

}
