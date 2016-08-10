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

import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Holder for entries returned by READDIRPLUS calls, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsDirectoryPlusEntry extends NfsDirectoryEntry {

    /**
     * The attributes of the file.
     */
    private final NfsGetAttributes _attributes;

    /**
     * The file handle, for use in RPC calls.
     */
    private final byte[] _fileHandle;

    /**
     * Create the entry.
     * 
     * @param xdr
     *            The response.
     */
    public NfsDirectoryPlusEntry(Xdr xdr) {
        super(xdr);
        if (xdr.getBoolean()) {
            _attributes = new NfsGetAttributes();
            _attributes.unmarshalling(xdr);
        } else {
            _attributes = null;
        }
        if (xdr.getBoolean()) {
            _fileHandle = xdr.getByteArray();
        } else {
            _fileHandle = null;
        }
    }

    /**
     * @return The attributes of the file.
     */
    public final NfsGetAttributes getAttributes() {
        return _attributes;
    }

    /**
     * @return The file handle, for use in RPC calls.
     */
    public final byte[] getFileHandle() {
        return _fileHandle;
    }

}
