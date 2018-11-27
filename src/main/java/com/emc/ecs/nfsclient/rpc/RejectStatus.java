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

import java.util.HashMap;
import java.util.Map;

/**
 * @author seibed
 *
 */
public class RejectStatus extends RpcStatus {

    /**
     * Reject status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final RejectStatus RPC_MISMATCH = new RejectStatus(0);

    /**
     * Reject status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    public static final RejectStatus AUTH_ERROR = new RejectStatus(1);

    /**
     * Preset values.
     */
    private static final Map<Integer, RejectStatus> VALUES = new HashMap<Integer, RejectStatus>();
    static {
        addValues(new RejectStatus[] { RPC_MISMATCH, AUTH_ERROR });
    }

    /**
     * Convenience function to get the instance from the int status value.
     * 
     * @param value
     *            The int status value.
     * @return The instance.
     */
    public static RejectStatus fromValue(int value) {
        RejectStatus status = VALUES.get(value);
        if (status == null) {
            status = new RejectStatus(value);
            VALUES.put(value, status);
        }
        return status;
    }

    /**
     * @param values
     *            Instances to add.
     */
    private static void addValues(RejectStatus[] values) {
        for (RejectStatus value : values) {
            VALUES.put(value.getValue(), value);
        }
    }

    /**
     * Create the instance from the int status value.
     * 
     * @param value
     */
    private RejectStatus(int value) {
        super(value);
    }

}
