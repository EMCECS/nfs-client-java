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
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure READ reads data from a file.
 * </p>
 * 
 * @author seibed
 */
public class NfsReadResponse extends NfsResponseBase {

    /**
     * The number read.
     */
    private int _bytesRead;

    /**
     * <ul>
     * <li><code>true</code> if the end of file is reached.</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean _eof;

    /**
     * The buffer for storing bytes.
     */
    private byte[] _bytes;

    /**
     * The beginning position of the buffer to store bytes
     */
    private int _position;

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READ reads data from a file.
     * </p>
     * 
     * @param bytes
     *            the buffer for storing bytes
     * @param position
     *            the beginning position of buffer to store bytes
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsReadResponse(byte[] bytes, int position, int nfsVersion) {
        super();
        _bytes = bytes;
        _position = position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsResponseBase#unmarshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void unmarshalling(Xdr xdr) throws RpcException {
        super.unmarshalling(xdr);
        unmarshallingAttributes(xdr);
        if (stateIsOk()) {
            _bytesRead = xdr.getInt();
            _eof = xdr.getBoolean();
            if (_bytes == null) {
                _bytes = new byte[_bytesRead];
                _position = 0;
            }
            xdr.getBytes(xdr.getInt(), _bytes, _position);
        }
    }

    /**
     * @return The number read.
     */
    public int getBytesRead() {
        return _bytesRead;
    }

    /**
     * @return 
     * <ul>
     * <li><code>true</code> if the end of file is reached.</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    public boolean isEof() {
        return _eof;
    }

    /**
     * @return The buffer, or null if there was none.
     */
    public byte[] getBytes() {
        return (_bytes == null) ? null : _bytes.clone();
    }

}
