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

import java.util.ArrayList;
import java.util.List;

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The response, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
public class NfsReaddirResponse extends NfsResponseBase {

    /**
     * The last <code>cookie</code> from the server, for use in the next
     * request.
     */
    private long _cookie;

    /**
     * The <code>cookieverf</code> from the server, for use in the next request.
     */
    private long _cookieverf;

    /**
     * The list of entries.
     */
    List<NfsDirectoryEntry> _entries;

    /**
     * <ul>
     * <li><code>true</code> if the end of file is reached.</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean _eof;

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsReaddirResponse(int nfsVersion) {
        this(new ArrayList<NfsDirectoryEntry>(), nfsVersion);
    }

    /**
     * Creates the response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param entries
     *            A list in which to store the entries.
     * @param nfsVersion
     *            The NFS version number. This is ignored for now, as only NFSv3
     *            is supported, but is included to allow future support for
     *            other versions.
     */
    public NfsReaddirResponse(List<NfsDirectoryEntry> entries, int nfsVersion) {
        super();
        _entries = entries;
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
            _cookieverf = xdr.getLong();

            // get directory entries
            while (xdr.getBoolean()) {
                NfsDirectoryEntry entry = new NfsDirectoryEntry(xdr);
                _entries.add(entry);
                _cookie = entry.getCookie();
            }

            // check whether it is the end of directory
            _eof = xdr.getBoolean();
        }
    }

    /**
     * @return The last <code>cookie</code> from the server, for use in the next
     *         request.
     */
    public long getCookie() {
        return _cookie;
    }

    /**
     * @return The <code>cookieverf</code> from the server, for use in the next
     *         request.
     */
    public long getCookieverf() {
        return _cookieverf;
    }

    /**
     * @return The list of entries.
     */
    public List<NfsDirectoryEntry> getEntries() {
        return _entries;
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the end of file is reached.</li>
     *         <li><code>false</code> otherwise.</li>
     *         </ul>
     */
    public boolean isEof() {
        return _eof;
    }

}
