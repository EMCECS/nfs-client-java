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
package com.emc.ecs.nfsclient.nfs;

import java.io.IOException;

import com.emc.ecs.nfsclient.rpc.RpcException;

/**
 * Nfs exceptions, with error codes as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsException extends IOException {

    /**
     * The default.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The status instance.
     */
    private NfsStatus _status;

    /**
     * Construct a new NFS exception with the given status instance and message
     * 
     * @param status
     * @param message
     */
    public NfsException(NfsStatus status, String message) {
        super(message);
        _status = status;
    }

    /**
     * Construct a new NFS exception with the given status instance, message,
     * and exception
     * 
     * @param status
     * @param message
     * @param e
     */
    public NfsException(NfsStatus status, String message, RpcException e) {
        super(message, e);
        _status = status;
    }

    /**
     * @return The status instance.
     */
    public NfsStatus getStatus() {
        return _status;
    }

}
