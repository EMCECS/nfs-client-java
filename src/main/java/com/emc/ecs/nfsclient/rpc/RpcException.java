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

import java.io.IOException;

/**
 * Represent the error caused by a RPC request.
 * 
 * @author seibed
 */
public class RpcException extends IOException {

    /**
     * The default.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The status.
     */
    private RpcStatus _status;

    /**
     * Construct a new RPC exception with the given status and error message.
     * 
     * @param status
     *            The status.
     * @param msg
     *            The error message.
     */
    public RpcException(RpcStatus status, String msg) {
        super(msg);
        _status = status;
    }

    /**
     * Construct a new RPC exception with the given status, error message, and
     * exception.
     * 
     * @param status
     *            The status.
     * @param msg
     *            The error message.
     * @param e
     *            The exception.
     */
    public RpcException(RpcStatus status, String msg, Exception e) {
        super(msg, e);
        _status = status;
    }

    /**
     * @return The status.
     */
    public RpcStatus getStatus() {
        return _status;
    }
}
