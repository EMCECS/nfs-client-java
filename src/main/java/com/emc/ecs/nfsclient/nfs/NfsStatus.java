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
 * Creates the nfs status values, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsStatus {

    /**
     * Indicates the call completed successfully.
     */
    public static final NfsStatus NFS3_OK = new NfsStatus(0);

    /**
     * Not owner. The operation was not allowed because the caller is either not
     * a privileged user (root) or not the owner of the target of the operation.
     */
    public static final NfsStatus NFS3ERR_PERM = new NfsStatus(1);

    /**
     * No such file or directory. The file or directory name specified does not
     * exist.
     */
    public static final NfsStatus NFS3ERR_NOENT = new NfsStatus(2);

    /**
     * I/O error. A hard error (for example, a disk error) occurred while
     * processing the requested operation.
     */
    public static final NfsStatus NFS3ERR_IO = new NfsStatus(5);

    /**
     * I/O error. No such device or address.
     */
    public static final NfsStatus NFS3ERR_NXIO = new NfsStatus(6);

    /**
     * Permission denied. The caller does not have the correct permission to
     * perform the requested operation. Contrast this with NFS3ERR_PERM, which
     * restricts itself to owner or privileged user permission failures.
     */
    public static final NfsStatus NFS3ERR_ACCES = new NfsStatus(13);

    /**
     * File exists. The file specified already exists.
     */
    public static final NfsStatus NFS3ERR_EXIST = new NfsStatus(17);

    /**
     * Attempt to do a cross-device hard link.
     */
    public static final NfsStatus NFS3ERR_XDEV = new NfsStatus(18);

    /**
     * No such device.
     */
    public static final NfsStatus NFS3ERR_NODEV = new NfsStatus(19);

    /**
     * Not a directory. The caller specified a non-directory in a directory
     * operation.
     */
    public static final NfsStatus NFS3ERR_NOTDIR = new NfsStatus(20);

    /**
     * Is a directory. The caller specified a directory in a non-directory
     * operation.
     */
    public static final NfsStatus NFS3ERR_ISDIR = new NfsStatus(21);

    /**
     * Invalid argument or unsupported argument for an operation. Two examples
     * are attempting a READLINK on an object other than a symbolic link or
     * attempting to SETATTR a time field on a server that does not support this
     * operation.
     */
    public static final NfsStatus NFS3ERR_INVAL = new NfsStatus(22);

    /**
     * File too large. The operation would have caused a file to grow beyond the
     * server's limit.
     */
    public static final NfsStatus NFS3ERR_FBIG = new NfsStatus(27);

    /**
     * No space left on device. The operation would have caused the server's
     * file system to exceed its limit.
     */
    public static final NfsStatus NFS3ERR_NOSPC = new NfsStatus(28);

    /**
     * Read-only file system. A modifying operation was attempted on a read-only
     * file system.
     */
    public static final NfsStatus NFS3ERR_ROFS = new NfsStatus(30);

    /**
     * Too many hard links.
     */
    public static final NfsStatus NFS3ERR_MLINK = new NfsStatus(31);

    /**
     * The filename in an operation was too long.
     */
    public static final NfsStatus NFS3ERR_NAMETOOLONG = new NfsStatus(63);

    /**
     * An attempt was made to remove a directory that was not empty.
     */
    public static final NfsStatus NFS3ERR_NOTEMPTY = new NfsStatus(66);

    /**
     * Resource (quota) hard limit exceeded. The user's resource limit on the
     * server has been exceeded.
     */
    public static final NfsStatus NFS3ERR_DQUOT = new NfsStatus(69);

    /**
     * Invalid file handle. The file handle given in the arguments was invalid.
     * The file referred to by that file handle no longer exists or access to it
     * has been revoked.
     */
    public static final NfsStatus NFS3ERR_STALE = new NfsStatus(70);

    /**
     * Too many levels of remote in path. The file handle given in the arguments
     * referred to a file on a non-local file system on the server.
     */
    public static final NfsStatus NFS3ERR_REMOTE = new NfsStatus(71);

    /**
     * Illegal NFS file handle. The file handle failed internal consistency
     * checks.
     */
    public static final NfsStatus NFS3ERR_BADHANDLE = new NfsStatus(10001);

    /**
     * Update synchronization mismatch was detected during a SETATTR operation.
     */
    public static final NfsStatus NFS3ERR_NOT_SYNC = new NfsStatus(10002);

    /**
     * READDIR or READDIRPLUS cookie is stale.
     */
    public static final NfsStatus NFS3ERR_BAD_COOKIE = new NfsStatus(10003);

    /**
     * Operation is not supported.
     */
    public static final NfsStatus NFS3ERR_NOTSUPP = new NfsStatus(10004);

    /**
     * Buffer or request is too small.
     */
    public static final NfsStatus NFS3ERR_TOOSMALL = new NfsStatus(10005);

    /**
     * An error occurred on the server which does not map to any of the legal
     * NFS version 3 protocol error values. The client should translate this
     * into an appropriate error. UNIX clients may choose to translate this to
     * EIO.
     */
    public static final NfsStatus NFS3ERR_SERVERFAULT = new NfsStatus(10006);

    /**
     * An attempt was made to create an object of a type not supported by the
     * server.
     */
    public static final NfsStatus NFS3ERR_BADTYPE = new NfsStatus(10007);

    /**
     * The server initiated the request, but was not able to complete it in a
     * timely fashion. The client should wait and then try the request with a
     * new RPC transaction ID. For example, this error should be returned from a
     * server that supports hierarchical storage and receives a request to
     * process a file that has been migrated. In this case, the server should
     * start the immigration process and respond to client with this error.
     */
    public static final NfsStatus NFS3ERR_JUKEBOX = new NfsStatus(10008);

    /**
     * The known values.
     */
    private static final Map<Integer, NfsStatus> VALUES = new HashMap<Integer, NfsStatus>();
    static {
        addValues(new NfsStatus[] { NFS3_OK, NFS3ERR_PERM, NFS3ERR_NOENT, NFS3ERR_IO, NFS3ERR_NXIO, NFS3ERR_ACCES,
                NFS3ERR_EXIST, NFS3ERR_XDEV, NFS3ERR_NODEV, NFS3ERR_NOTDIR, NFS3ERR_ISDIR, NFS3ERR_INVAL, NFS3ERR_FBIG,
                NFS3ERR_NOSPC, NFS3ERR_ROFS, NFS3ERR_MLINK, NFS3ERR_NAMETOOLONG, NFS3ERR_NOTEMPTY, NFS3ERR_DQUOT,
                NFS3ERR_STALE, NFS3ERR_REMOTE, NFS3ERR_BADHANDLE, NFS3ERR_NOT_SYNC, NFS3ERR_BAD_COOKIE, NFS3ERR_NOTSUPP,
                NFS3ERR_TOOSMALL, NFS3ERR_SERVERFAULT, NFS3ERR_BADTYPE, NFS3ERR_JUKEBOX });
    }

    /**
     * Convenience function to get the instance from the int status value.
     * 
     * @param value
     *            The int status value.
     * @return The instance.
     */
    public static NfsStatus fromValue(int value) {
        NfsStatus status = VALUES.get(value);
        if (status == null) {
            status = new NfsStatus(value);
            VALUES.put(value, status);
        }
        return status;
    }

    /**
     * @param values
     *            Instances to add.
     */
    private static void addValues(NfsStatus[] values) {
        for (NfsStatus value : values) {
            VALUES.put(value.getValue(), value);
        }
    }

    /**
     * @return The int status value.
     */
    public int getValue() {
        return _value;
    }

    /**
     * The int status value.
     */
    private int _value;

    /**
     * Creates the nfs status values, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @param value
     *            The int status value.
     */
    private NfsStatus(int value) {
        _value = value;
    }

    @Override
    public String toString() {
        return "NfsStatus:" + _value;
    }
}
