/**
 * Copyright 2016-2018 EMC Corporation. All Rights Reserved.
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
 * Procedure SETATTR changes one or more of the attributes of a file system
 * object on the server.
 * </p>
 * 
 * @author seibed
 */
public class NfsSetAttrRequest extends NfsRequestBase {

    private final NfsSetAttributes _attributes;

    private final NfsTime _guardTime;

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
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsSetAttrRequest(byte[] fileHandle, NfsSetAttributes attributes, NfsTime guardTime, Credential credential,
            int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_SETATTR, credential, fileHandle);
        _attributes = attributes;
        if ( ( guardTime != null ) && ( ! guardTime.isBareTime() ) ) {
            throw new IllegalArgumentException("The guard time cannot be a time setting time");
        }
        _guardTime = guardTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsRequestBase#marshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        _attributes.marshalling(xdr);
        if (_guardTime == null) {
            xdr.putBoolean(false);
        } else {
            xdr.putBoolean(true);
            _guardTime.marshalling(xdr);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsSetAttrRequest").append(" attributes:").append(_attributes.toString()).toString();
    }

}
