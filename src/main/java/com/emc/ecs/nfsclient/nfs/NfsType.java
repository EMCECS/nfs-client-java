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
 * Enums for the NFS file types, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsType {

    /**
     * Regular file.
     */
    public static final NfsType NFS_REG = new NfsType(1);

    /**
     * Directory.
     */

    public static final NfsType NFS_DIR = new NfsType(2);

    /**
     * Block special device file.
     */
    public static final NfsType NFS_BLK = new NfsType(3);

    /**
     * Character special device file.
     */
    public static final NfsType NFS_CHR = new NfsType(4);

    /**
     * Symbolic link.
     */
    public static final NfsType NFS_LNK = new NfsType(5);

    /**
     * Socket.
     */
    public static final NfsType NFS_SOCK = new NfsType(6);

    /**
     * Named pipe.
     */
    public static final NfsType NFS_FIFO = new NfsType(7);

    /**
     * The known values.
     */
    private static final Map<Integer, NfsType> VALUES = new HashMap<Integer, NfsType>();
    static {
        addValues(new NfsType[] { NFS_REG, NFS_DIR, NFS_BLK, NFS_CHR, NFS_LNK, NFS_SOCK,
                NFS_FIFO });
    }

    /**
     * Convenience function to get the instance from the int type value.
     * 
     * @param value
     *            The int type value.
     * @return The instance.
     */
    public static NfsType fromValue(int value) {
        NfsType nfsType = VALUES.get(value);
        if (nfsType == null) {
            nfsType = new NfsType(value);
            VALUES.put(value, nfsType);
        }
        return nfsType;
    }

    /**
     * @param values
     *            Instances to add.
     */
    private static void addValues(NfsType[] values) {
        for (NfsType value : values) {
            VALUES.put(value.getValue(), value);
        }
    }

    /**
     * The integer value used in requests and responses.
     */
    private final int _value;

    /**
     * Create the instances.
     * 
     * @param value
     *            The integer value used in requests and responses.
     */
    private NfsType(int value) {
        _value = value;
    }

    /**
     * @return The integer value used in requests and responses.
     */
    public int getValue() {
        return _value;
    }

    @Override
    public String toString() {
        return "NfsType:" + _value;
    }
}
