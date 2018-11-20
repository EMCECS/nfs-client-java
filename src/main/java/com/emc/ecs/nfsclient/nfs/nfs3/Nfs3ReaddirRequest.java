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
package com.emc.ecs.nfsclient.nfs.nfs3;

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.nfs.NfsReaddirRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure READDIR retrieves a variable number of entries, in sequence, from a
 * directory and returns the name and file identifier for each, with information
 * to allow the client to request additional directory entries in a subsequent
 * READDIR request.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3ReaddirRequest extends NfsReaddirRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
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
     *            READDIR in which the <code>cookie</code> was acquired.
     * @param count
     *            The maximum size of the READDIR3resok structure, in bytes. The
     *            size must include all XDR overhead. The server is free to
     *            return less than count bytes of data.
     * @param credential
     *            The credential used for RPC authentication.
     * @throws FileNotFoundException
     */
    public Nfs3ReaddirRequest(byte[] fileHandle, long cookie, long cookieverf, int count, Credential credential)
            throws FileNotFoundException {
        super(fileHandle, cookie, cookieverf, count, credential, Nfs3.VERSION);
    }

}
