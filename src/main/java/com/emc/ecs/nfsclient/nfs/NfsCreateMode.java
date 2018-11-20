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

import java.util.HashMap;
import java.util.Map;

/**
 * Mode for creating new regular files, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsCreateMode {

    /**
     * The file should be created without checking for the existence of a
     * duplicate file in the same directory.
     */
    public static final NfsCreateMode UNCHECKED = new NfsCreateMode(0);

    /**
     * The server should check for the presence of a duplicate file before
     * performing the create and should fail the request with
     * <code>NFS3ERR_EXIST</code> if a duplicate file exists. If the file does
     * not exist, the request is performed as described for
     * <code>UNCHECKED</code>.
     */
    public static final NfsCreateMode GUARDED = new NfsCreateMode(1);

    /**
     * The server is to follow exclusive creation semantics, using the verifier
     * to ensure exclusive creation of the target.
     */
    public static final NfsCreateMode EXCLUSIVE = new NfsCreateMode(2);

    /**
     * The known values.
     */
    private static final Map<Integer, NfsCreateMode> VALUES = new HashMap<Integer, NfsCreateMode>();
    static {
        addValues(new NfsCreateMode[] { UNCHECKED, GUARDED, EXCLUSIVE });
    }

    /**
     * Convenience function to get the instance from the int create mode value.
     * 
     * @param value
     *            The int create mode value.
     * @return The instance.
     */
    public static NfsCreateMode fromValue(int value) {
        NfsCreateMode createMode = VALUES.get(value);
        if (createMode == null) {
            createMode = new NfsCreateMode(value);
            VALUES.put(value, createMode);
        }
        return createMode;
    }

    /**
     * @param values
     *            Instances to add.
     */
    private static void addValues(NfsCreateMode[] values) {
        for (NfsCreateMode value : values) {
            VALUES.put(value.getValue(), value);
        }
    }

    /**
     * The integer value used in the request.
     */
    private final int _value;

    /**
     * Create the instance.
     * 
     * @param value
     *            The integer value used in the request.
     */
    NfsCreateMode(int value) {
        _value = value;
    }

    /**
     * @return The integer value used in the request.
     */
    int getValue() {
        return _value;
    }

}
