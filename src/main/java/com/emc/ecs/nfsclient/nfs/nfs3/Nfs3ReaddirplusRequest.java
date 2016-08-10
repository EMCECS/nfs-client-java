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

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.nfs.NfsReaddirplusRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
public class Nfs3ReaddirplusRequest extends NfsReaddirplusRequest {

    /**
     * Creates the request, as specified by RFC 1813
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
     * @param fileHandle
     *            The file handle for the directory to be read.
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIRPLUS in which the <code>cookie</code> was acquired.
     * @param dircount
     *            The maximum number of bytes of directory information returned.
     *            This number should not include the size of the attributes and
     *            file handle portions of the result.
     * @param maxcount
     *            The maximum size of the READDIRPLUS3resok structure, in bytes.
     *            The size must include all XDR overhead. The server is free to
     *            return less than <code>maxcount</code> bytes of data.
     * @param credential
     *            The credential used for RPC authentication.
     * @throws FileNotFoundException
     */
    public Nfs3ReaddirplusRequest(byte[] fileHandle, long cookie, long cookieverf, int dircount, int maxcount,
            Credential credential) throws FileNotFoundException {
        super(fileHandle, cookie, cookieverf, dircount, maxcount, credential, Nfs3.VERSION);

    }

}
