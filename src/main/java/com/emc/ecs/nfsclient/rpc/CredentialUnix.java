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
package com.emc.ecs.nfsclient.rpc;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * implement AUTH_UNIX for RPC, as specified by RFC 1831
 * (https://tools.ietf.org/html/rfc1831).
 */
public class CredentialUnix extends CredentialBase {

    /**
     * Discriminant for a caller identifying itself as it is identified on a
     * UNIX(tm) system. Specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private static final int AUTH_UNIX = 1;

    /**
     * The caller's effective user ID.
     */
    private final int _uid;

    /**
     * The caller's effective group ID.
     */
    private final int _gid;

    /**
     * A counted array of groups which contain the caller as a member.
     */
    private final int[] _gids;

    /**
     * The machine name.
     */
    private final String _host;

    /**
     * The number of bytes needed for the Xdr buffer.
     */
    private final int _maximumXdrBytes;

    /**
     * Create the Credential.
     * 
     * @param uid
     *            The caller's effective user ID.
     * @param gid
     *            The caller's effective group ID.
     * @param gids
     *            The set of groups which contain the caller as a
     *            member.
     */
    public CredentialUnix(int uid, int gid, Set<Integer> gids) {
        _uid = uid;
        _gid = gid;
        if (gids == null) {
            gids = new HashSet<Integer>(0);
        }
        _gids = new int[gids.size()];
        int index = 0;
        for (Integer id : gids) {
            _gids[index] = id.intValue();
            ++index;
        }
        _host = getHostname();
        _maximumXdrBytes = 24 + (4 * _gids.length) + _host.getBytes(RpcRequest.CHARSET).length;
    }

    /**
     * Default constructor, with uid = 0 and gid = 0.
     */
    public CredentialUnix() {
        this(0, 0, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.rpc.CredentialBase#getCredentialFlavor()
     */
    protected int getCredentialFlavor() {
        return AUTH_UNIX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.rpc.CredentialBase#getCredential()
     */
    protected Xdr getCredential() {
        Xdr credentials = new Xdr(_maximumXdrBytes);
        credentials.setOffset(0);
        credentials.putInt((int) (System.currentTimeMillis() / 1000));
        credentials.putString(_host);
        credentials.putInt(_uid);
        credentials.putInt(_gid);
        if (_gids == null) {
            credentials.putInt(0);
        } else {
            credentials.putInt(_gids.length);
            for (int i = 0; i < _gids.length; i++)
                credentials.putInt(_gids[i]);
        }
        return credentials;
    }

    /**
     * @return The hostname, or "localhost" if it can't be determined.
     */
    private static String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // do nothing
        }

        // the hostname doesn't affect the correctness of RPC call, so
        // just return "localhost" if cannot get the host name
        return "localhost";
    }

}
