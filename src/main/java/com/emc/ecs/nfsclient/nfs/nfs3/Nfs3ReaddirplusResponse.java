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
package com.emc.ecs.nfsclient.nfs.nfs3;

import com.emc.ecs.nfsclient.nfs.NfsDirectoryPlusEntry;
import com.emc.ecs.nfsclient.nfs.NfsReaddirplusResponse;

import java.util.List;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure READDIRPLUS retrieves a variable number of entries from a file
 * system directory and returns complete information about each along with
 * information to allow the client to request additional directory entries in a
 * subsequent READDIRPLUS. READDIRPLUS differs from READDIR only in the amount
 * of information returned for each entry. In READDIR, each entry returns the
 * filename and the fileid. In READDIRPLUS, each entry returns the name, the
 * fileid, attributes (including the fileid), and file handle.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3ReaddirplusResponse extends NfsReaddirplusResponse {

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIRPLUS retrieves a variable number of entries from a file
     * system directory and returns complete information about each along with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIRPLUS. READDIRPLUS differs from READDIR only in the
     * amount of information returned for each entry. In READDIR, each entry
     * returns the filename and the fileid. In READDIRPLUS, each entry returns
     * the name, the fileid, attributes (including the fileid), and file handle.
     * </p>
     */
    public Nfs3ReaddirplusResponse() {
        super(Nfs3.VERSION);
    }

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIRPLUS retrieves a variable number of entries from a file
     * system directory and returns complete information about each along with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIRPLUS. READDIRPLUS differs from READDIR only in the
     * amount of information returned for each entry. In READDIR, each entry
     * returns the filename and the fileid. In READDIRPLUS, each entry returns
     * the name, the fileid, attributes (including the fileid), and file handle.
     * </p>
     * 
     * @param entries
     *            A list in which to store the entries.
     */
    public Nfs3ReaddirplusResponse(List<NfsDirectoryPlusEntry> entries) {
        super(entries, Nfs3.VERSION);
    }

}
