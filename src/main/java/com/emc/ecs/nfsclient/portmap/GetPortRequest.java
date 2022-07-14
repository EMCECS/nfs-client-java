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
package com.emc.ecs.nfsclient.portmap;

import org.apache.commons.lang3.NotImplementedException;

import com.emc.ecs.nfsclient.rpc.CredentialNone;
import com.emc.ecs.nfsclient.rpc.RpcRequest;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * A Portmapper GETPORT request, as specified by
 * https://tools.ietf.org/html/rfc1833.
 * 
 * @author seibed
 */
public class GetPortRequest extends RpcRequest {

    /**
     * TCP/IP Protocol number constant for Portmap requests.
     */
    public static final int IPPROTO_TCP = 6;

    /**
     * UDP/IP Protocol number constant for Portmap requests.
     */
    public static final int IPPROTO_UDP = 17;

    /**
     * The Portmapper program number for RPC calls, as specified by
     * https://tools.ietf.org/html/rfc1833.
     */
    private static final int PMAP_PROG = 100000;

    /**
     * The Portmapper version number for RPC calls, as specified by
     * https://tools.ietf.org/html/rfc1833.
     */
    private static final int PMAP_VERS = 2;

    /**
     * The Portmapper GETPORT procedure number for RPC calls, as specified by
     * https://tools.ietf.org/html/rfc1833.
     * 
     * <p>
     * Given a program number, version number, and transport protocol number,
     * this procedure returns the port number on which the program is awaiting
     * call requests. A port value of zeros means the program has not been
     * registered. The <code>port</code> field of the argument is ignored.
     * </p>
     */
    private static final int PMAPPROC_GETPORT = 3;

    /**
     * The RPC number for the program that will be used for later queries.
     */
    private final int _programToQuery;

    /**
     * The RPC number for the program version that will be used for later
     * queries.
     */
    private final int _programVersion;

    /**
     * The RPC number for the network protocol that will be used for later
     * queries.
     */
    private final int _networkProtocol;

    /**
     * The port number. This is ignored for GTEPORT requests.
     */
    private final int _port = 0;

    /**
     * Simple constructor for calls, using the default TCP/IP protocol.
     * 
     * @param programToQuery
     *            The RPC number for the program that will be used for later
     *            queries.
     * @param programVersion
     *            The RPC number for the program version that will be used for
     *            later queries.
     */
    public GetPortRequest(int programToQuery, int programVersion) {
        this(programToQuery, programVersion, IPPROTO_TCP);
    }

    /**
     * @param programToQuery
     *            The RPC number for the program that will be used for later
     *            queries.
     * @param programVersion
     *            The RPC number for the program version that will be used for
     *            later queries.
     * @param networkProtocol
     *            The RPC number for the network protocol that will be used for
     *            later queries.
     */
    public GetPortRequest(int programToQuery, int programVersion, int networkProtocol) {
        super(PMAP_PROG, PMAP_VERS, PMAPPROC_GETPORT, new CredentialNone());
        _programToQuery = programToQuery;
        _programVersion = programVersion;
        _networkProtocol = networkProtocol;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcRequest#marshalling(com.emc.ecs.nfsclient.
     * rpc.Xdr)
     */
    public void marshalling(Xdr x) {
        super.marshalling(x);
        x.putInt(_programToQuery);
        x.putInt(_programVersion);
        x.putInt(_networkProtocol);
        x.putInt(_port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.rpc.RpcRequest#getErrorMessage()
     */
    public String getErrorMessage() {
        throw new NotImplementedException("This method should never be used during Portmapper calls.");
    }

    @Override
    public String toString() {
        return startToString("GetPortRequest")
                .append(" programToQuery:").append(_programToQuery)
                .append(" programVersion:").append(_programVersion)
                .append(" networkProtocol:").append(_networkProtocol)
                .append(" port:").append(_port)
                .toString();
    }
}
