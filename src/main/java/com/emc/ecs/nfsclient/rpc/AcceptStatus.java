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

import java.util.HashMap;
import java.util.Map;

/**
 * @author seibed
 *
 */
public class AcceptStatus extends RpcStatus {

    /**
     * Accept status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final AcceptStatus SUCCESS = new AcceptStatus(0);

    /**
     * Accept status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final AcceptStatus PROG_UNAVAIL = new AcceptStatus(1);

    /**
     * Accept status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final AcceptStatus PROG_MISMATCH = new AcceptStatus(2);

    /**
     * Accept status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final AcceptStatus PROC_UNAVAIL = new AcceptStatus(3);

    /**
     * Accept status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final AcceptStatus GARBAGE_ARGS = new AcceptStatus(4);

    /**
     * Accept status specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final AcceptStatus SYSTEM_ERR = new AcceptStatus(5);

    /**
     * Preset values.
     */
    private static final Map<Integer, AcceptStatus> VALUES = new HashMap<Integer, AcceptStatus>();
    static {
        addValues(new AcceptStatus[] { SUCCESS, PROG_UNAVAIL, PROG_MISMATCH, PROC_UNAVAIL, GARBAGE_ARGS, SYSTEM_ERR });
    }

    /**
     * Convenience function to get the instance from the int status value.
     * 
     * @param value
     *            The int status value.
     * @return The instance.
     */
    public static AcceptStatus fromValue(int value) {
        AcceptStatus status = VALUES.get(value);
        if (status == null) {
            status = new AcceptStatus(value);
            VALUES.put(value, status);
        }
        return status;
    }

    /**
     * @param values
     *            Instances to add.
     */
    private static void addValues(AcceptStatus[] values) {
        for (AcceptStatus value : values) {
            VALUES.put(value.getValue(), value);
        }
    }

    /**
     * Create the instance from the int status value.
     * 
     * @param value
     */
    private AcceptStatus(int value) {
        super(value);
    }

}
