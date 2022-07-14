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
public class ReplyStatus extends RpcStatus {

    /**
     * Accepted - reply status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    public static final ReplyStatus MSG_ACCEPTED = new ReplyStatus(0);

    /**
     * Denied - reply status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    public static final ReplyStatus MSG_DENIED = new ReplyStatus(1);

    /**
     * Preset values.
     */
    private static final Map<Integer, ReplyStatus> VALUES = new HashMap<Integer, ReplyStatus>();
    static {
        addValues(new ReplyStatus[] { MSG_ACCEPTED, MSG_DENIED });
    }

    /**
     * Convenience function to get the instance from the int status value.
     * 
     * @param value
     *            The int status value.
     * @return The instance.
     */
    public static ReplyStatus fromValue(int value) {
        ReplyStatus status = VALUES.get(value);
        if (status == null) {
            status = new ReplyStatus(value);
            VALUES.put(value, status);
        }
        return status;
    }

    /**
     * @param values
     *            Instances to add.
     */
    private static void addValues(ReplyStatus[] values) {
        for (ReplyStatus value : values) {
            VALUES.put(value.getValue(), value);
        }
    }

    /**
     * Create the instance from the int status value.
     * 
     * @param value
     */
    private ReplyStatus(int value) {
        super(value);
    }

    @Override
    public String toString() {
        return "ReplyStatus:" + getValue();
    }
}
