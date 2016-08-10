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

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.RpcResponse;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The base class for all NFS responses specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public abstract class NfsResponseBase extends RpcResponse {

    /**
     * Create the base class object.
     */
    public NfsResponseBase() {
        super();
    }

    /**
     * File system object post-operation attributes. These are provided for many
     * calls. For information on the object to which the attributes refer, or
     * for other details, see RFC 1813 (https://tools.ietf.org/html/rfc1813).
     */
    private NfsGetAttributes _attributes;

    /**
     * The file handle for the system object that was created for a successful
     * CREATE, MKDIR, MKNOD, or SYMLINK request, or looked up for a LOOKUP
     * request. This is null for all other requests, and also for failed
     * requests.
     */
    private byte[] _fileHandle;

    /**
     * The state returned in the response. For details, see RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    private int _state;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcResponse#unmarshalling(com.emc.ecs.nfsclient
     * .rpc.Xdr)
     */
    public void unmarshalling(Xdr xdr) throws RpcException {
        super.unmarshalling(xdr);
        _state = xdr.getInt();
    }

    /**
     * @return The state returned in the response. For details, see RFC 1813
     *         (https://tools.ietf.org/html/rfc1813).
     */
    public int getState() {
        return _state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("response");
        sb.append(" xid:");
        sb.append(String.valueOf(_xid));
        sb.append(" state:");
        sb.append(String.valueOf(_state));
        return sb.toString();
    }

    /**
     * @return File system object post-operation attributes. These are provided
     *         for many calls. For information on the object to which the
     *         attributes refer, or for other details, see RFC 1813
     *         (https://tools.ietf.org/html/rfc1813).
     */
    public final NfsGetAttributes getAttributes() {
        return _attributes;
    }

    /**
     * @return The file handle for the system object that was created for a
     *         successful CREATE, MKDIR, MKNOD, or SYMLINK request, or looked up
     *         for a LOOKUP request. This is null for all other requests, and
     *         also for failed requests.
     */
    public final byte[] getFileHandle() {
        return (_fileHandle == null) ? null : _fileHandle.clone();
    }

    /**
     * Check the state returned in the response. For details, see RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @return true if the state is NFS3_OK, false otherwise
     */
    public boolean stateIsOk() {
        return _state == NfsStatus.NFS3_OK.getValue();
    }

    /**
     * Unmarshall the object if it's there. Convenience method for use in
     * subclasses.
     * 
     * @param xdr
     */
    protected void unmarshallingAttributes(Xdr xdr) {
        unmarshallingAttributes(xdr, false);
    }

    /**
     * Unmarshall the object if it's there, or skip the existence check if
     * <code>force</code> is <code>true</code>. Convenience method for use in
     * subclasses.
     * 
     * @param xdr
     * @param force
     *            don't check whether it's there
     */
    protected void unmarshallingAttributes(Xdr xdr, boolean force) {
        _attributes = makeNfsGetAttributes(xdr, force);
    }

    /**
     * Unmarshall the object if it's there. Convenience method for use in
     * subclasses.
     * 
     * @param xdr
     */
    protected void unmarshallingFileHandle(Xdr xdr) {
        unmarshallingFileHandle(xdr, false);
    }

    /**
     * Unmarshall the object if it's there, or skip the existence check if
     * <code>force</code> is <code>true</code>. Convenience method for use in
     * subclasses.
     * 
     * @param xdr
     * @param force
     */
    protected void unmarshallingFileHandle(Xdr xdr, boolean force) {
        if (force || xdr.getBoolean()) {
            _fileHandle = xdr.getByteArray();
        }
    }

    /**
     * Create the object if it's there, return null if it isn't. Convenience
     * method for use in subclasses.
     * 
     * @param xdr
     * @return the created object or null
     */
    protected static NfsGetAttributes makeNfsGetAttributes(Xdr xdr) {
        return makeNfsGetAttributes(xdr, false);
    }

    /**
     * Create the object if it's there, or skip the existence check if
     * <code>force</code> is <code>true</code>. Convenience method for use in
     * subclasses.
     * 
     * @param xdr
     * @param force
     *            don't check whether it's there
     * @return the created object
     */
    protected static NfsGetAttributes makeNfsGetAttributes(Xdr xdr, boolean force) {
        NfsGetAttributes attributes = null;
        if (force || xdr.getBoolean()) {
            attributes = new NfsGetAttributes();
            attributes.unmarshalling(xdr);
        }
        return attributes;
    }

}
