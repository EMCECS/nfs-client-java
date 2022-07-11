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

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.Xdr;

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
public class NfsReaddirRequest extends NfsRequestBase {

    /**
     * This should be set to 0 in the first request to read the directory. On
     * subsequent requests, it should be a cookie as returned by the server.
     */
    private final long _cookie;

    /**
     * This should be set to 0 in the first request to read the directory. On
     * subsequent requests, it should be a cookieverf as returned by the server.
     * The cookieverf must match that returned by the READDIR in which the
     * cookie was acquired.
     */
    private final long _cookieverf;

    /**
     * The maximum size of the READDIR3resok structure, in bytes. The size must
     * include all XDR overhead. The server is free to return less than count
     * bytes of data.
     */
    private final int _count;

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
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsReaddirRequest(byte[] fileHandle, long cookie, long cookieverf, int count, Credential credential,
            int nfsVersion) throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_READDIR, credential, fileHandle);
        _cookie = cookie;
        _cookieverf = cookieverf;
        _count = count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsRequestBase#marshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        xdr.putLong(_cookie);
        xdr.putLong(_cookieverf);
        xdr.putUnsignedInt(_count);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsReaddirRequest").append(" cookie:").append(String.valueOf(_cookie))
                .append(" cookieverf:").append(String.valueOf(_cookieverf)).toString();
    }

}
