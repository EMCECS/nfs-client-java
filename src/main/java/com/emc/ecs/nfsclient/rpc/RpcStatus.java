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
package com.emc.ecs.nfsclient.rpc;

/**
 * @author seibed
 *
 */
public class RpcStatus {

    /**
     * Unable to bind to a privileged port as requested. NOT specified by RFC
     * 1831 (https://tools.ietf.org/html/rfc1831).
     */
    public static final RpcStatus LOCAL_BINDING_ERROR = new RpcStatus(-1001);

    /**
     * Network error blocking RFC request or response. NOT specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    public static final RpcStatus NETWORK_ERROR = new RpcStatus(-1001);

    /**
     * @return The int status value.
     */
    public final int getValue() {
        return _value;
    }

    /**
     * The int status value.
     */
    private final int _value;

    /**
     * Create the instance from the int status value.
     * 
     * @param value
     */
    protected RpcStatus(int value) {
        _value = value;
    }

}
