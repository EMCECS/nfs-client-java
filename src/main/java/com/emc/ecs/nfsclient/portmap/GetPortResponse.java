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
package com.emc.ecs.nfsclient.portmap;

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.RpcResponse;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * A Portmapper GETPORT response, as specified by https://tools.ietf.org/html/rfc1833.
 * 
 * @author seibed
 */
public class GetPortResponse extends RpcResponse {

    /**
     * The port number on which the program is awaiting call requests. A port
     * value of zeros means the program has not been registered. Specified by
     * https://tools.ietf.org/html/rfc1833.
     */
    private int _port;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcResponse#unmarshalling(com.emc.ecs.nfsclient
     * .rpc.Xdr)
     */
    public void unmarshalling(Xdr x) throws RpcException {
        super.unmarshalling(x);
        _port = x.getInt();
    }

    /**
     * @return The port number on which the program is awaiting call requests. A
     *         port value of zeros means the program has not been registered.
     *         Specified by https://tools.ietf.org/html/rfc1833.
     */
    public int getPort() {
        return _port;
    }

}
