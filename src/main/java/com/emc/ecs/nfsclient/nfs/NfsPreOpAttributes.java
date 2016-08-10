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
 * These pre-operation attributes are part of weak cache consistency data, as specified by RFC
 * 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsPreOpAttributes {

    // for now we use long to represent uint64, it should be safe as 'signed
    // long' is big enough for file size
    /**
     * The number of bytes of disk space that the file actually uses (which can
     * be smaller than the size because the file may have holes or it may be
     * larger due to fragmentation).
     */
    private final long _size;

    /**
     * The time when the file data was last modified.
     */
    private final NfsTime _mtime;

    /**
     * The time when the attributes of the file were last changed. Writing to
     * the file changes the ctime in addition to the mtime.
     */
    private final NfsTime _ctime;

    /**
     * Creates a new structure from the Xdr response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @param xdr
     */
    public NfsPreOpAttributes(Xdr xdr) {
        _size = xdr.getLong();
        _mtime = new NfsTime();
        _ctime = new NfsTime();
        _mtime.unmarshalling(xdr);
        _ctime.unmarshalling(xdr);
    }

    /**
     * @return
     * The number of bytes of disk space that the file actually uses (which can
     * be smaller than the size because the file may have holes or it may be
     * larger due to fragmentation).
     */
    public long getSize() {
        return _size;
    }

    /**
     * @return
     * The time when the file data was last modified.
     */
    public NfsTime getMTime() {
        return _mtime;
    }

    /**
     * @return
     * The time when the attributes of the file were last changed. Writing to
     * the file changes the ctime in addition to the mtime.
     */
    public NfsTime getCTime() {
        return _ctime;
    }

}
