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

import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Holder for entries returned by READDIR calls, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsDirectoryEntry {

    /**
     * The cookie from the server, for use in future RPC calls.
     */
    private final long _cookie;

    /**
     * A number which uniquely identifies the file within its file system (on
     * UNIX this would be the inumber).
     */
    private final long _fileId;

    /**
     * The file name from the directory listing.
     */
    private final String _fileName;

    /**
     * Create a new entry.
     * 
     * @param xdr The response.
     */
    public NfsDirectoryEntry(Xdr xdr) {
        _fileId = xdr.getLong();
        _fileName = xdr.getString();
        _cookie = xdr.getLong();
    }

    /**
     * @return The cookie from the server, for use in future RPC calls.
     */
    public final long getCookie() {
        return _cookie;
    }

    /**
     * @return
     * A number which uniquely identifies the file within its file system (on
     * UNIX this would be the inumber).
     */
    public final long getFileId() {
        return _fileId;
    }

    /**
     * @return The file name from the directory listing.
     */
    public final String getFileName() {
        return _fileName;
    }

}
