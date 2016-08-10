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

import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Used to set file or directory attributes, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 */
public class NfsSetAttributes {

    /**
     * The protection mode bits, defined as follows:
     * <ul>
     * <li><strong>0x00800</strong> - Set user ID on execution.</li>
     * <li><strong>0x00400</strong> - Set group ID on execution.</li>
     * <li><strong>0x00200</strong> - Save swapped text (not defined in POSIX).
     * </li>
     * <li><strong>0x00100</strong> - Read permission for owner.</li>
     * <li><strong>0x00080</strong> - Write permission for owner.</li>
     * <li><strong>0x00040</strong> - Execute permission for owner on a file. Or
     * lookup (search) permission for owner in directory.</li>
     * <li><strong>0x00020</strong> - Read permission for group.</li>
     * <li><strong>0x00010</strong> - Write permission for group.</li>
     * <li><strong>0x00008</strong> - Execute permission for group on a file. Or
     * lookup (search) permission for group in directory.</li>
     * <li><strong>0x00004</strong> - Read permission for others.</li>
     * <li><strong>0x00002</strong> - Write permission for others.</li>
     * <li><strong>0x00001</strong> - Execute permission for others on a file.
     * Or lookup (search) permission for others in directory.</li>
     * </ul>
     */
    private Long _mode; // uint32

    /**
     * the user ID of the owner of the file.
     */
    private Long _uid; // uint32

    /**
     * the group ID of the group of the file.
     */
    private Long _gid; // uint32

    /**
     * the size of the file in bytes.
     */
    private Long _size; // uint64

    /**
     * the time when the file data was last accessed.
     */
    private NfsTime _atime;

    /**
     * the time when the file data was last modified.
     */
    private NfsTime _mtime;

    /**
     * @param mode
     *            The protection mode bits, defined as follows:
     *            <ul>
     *            <li><strong>0x00800</strong> - Set user ID on execution.</li>
     *            <li><strong>0x00400</strong> - Set group ID on execution.</li>
     *            <li><strong>0x00200</strong> - Save swapped text (not defined
     *            in POSIX).</li>
     *            <li><strong>0x00100</strong> - Read permission for owner.</li>
     *            <li><strong>0x00080</strong> - Write permission for owner.
     *            </li>
     *            <li><strong>0x00040</strong> - Execute permission for owner on
     *            a file. Or lookup (search) permission for owner in directory.
     *            </li>
     *            <li><strong>0x00020</strong> - Read permission for group.</li>
     *            <li><strong>0x00010</strong> - Write permission for group.
     *            </li>
     *            <li><strong>0x00008</strong> - Execute permission for group on
     *            a file. Or lookup (search) permission for group in directory.
     *            </li>
     *            <li><strong>0x00004</strong> - Read permission for others.
     *            </li>
     *            <li><strong>0x00002</strong> - Write permission for others.
     *            </li>
     *            <li><strong>0x00001</strong> - Execute permission for others
     *            on a file. Or lookup (search) permission for others in
     *            directory.</li>
     *            </ul>
     */
    public void setMode(Long mode) {
        _mode = mode;
    }

    /**
     * @param uid
     *            the user ID of the owner of the file.
     */
    public void setUid(Long uid) {
        _uid = uid;
    }

    /**
     * @param gid
     *            the group ID of the group of the file.
     */
    public void setGid(Long gid) {
        _gid = gid;
    }

    /**
     * @param size
     *            the size of the file in bytes.
     */
    public void setSize(Long size) {
        _size = size;
    }

    /**
     * @param atime
     *            the time when the file data was last accessed.
     */
    public void setAtime(NfsTime atime) {
        _atime = atime;
    }

    /**
     * @param mtime
     *            the time when the file data was last modified.
     */
    public void setMtime(NfsTime mtime) {
        _mtime = mtime;
    }

    /**
     * Default constructor
     */
    public NfsSetAttributes() {
        this(null, null, null, null, NfsTime.DO_NOT_CHANGE, NfsTime.DO_NOT_CHANGE);
    }

    /**
     * Convenient constructor for multiple changes.
     * 
     * @param mode
     * @param uid
     * @param gid
     * @param atime
     * @param mtime
     */
    public NfsSetAttributes(Long mode, Long uid, Long gid, NfsTime atime, NfsTime mtime) {
        this(mode, uid, gid, null, atime, mtime);
    }

    /**
     * Convenient constructor for multiple changes.
     * 
     * @param mode
     * @param uid
     * @param gid
     * @param size
     * @param atime
     * @param mtime
     */
    public NfsSetAttributes(Long mode, Long uid, Long gid, Long size, NfsTime atime, NfsTime mtime) {
        _mode = mode;
        _uid = uid;
        _gid = gid;
        _size = size;
        _atime = atime;
        _mtime = mtime;
    }

    /**
     * Set Xdr fields for the rpc call.
     * 
     * @param xdr
     */
    public void marshalling(Xdr xdr) {
        marshalling(xdr, _mode);
        marshalling(xdr, _uid);
        marshalling(xdr, _gid);
        if (_size != null) {
            xdr.putBoolean(true);
            xdr.putLong(_size.longValue());
        } else {
            xdr.putBoolean(false);
        }
        marshalling(xdr, _atime);
        marshalling(xdr, _mtime);
    }

    /**
     * @param xdr
     * @param nfsTime
     */
    private void marshalling(Xdr xdr, NfsTime nfsTime) {
        if (nfsTime != null) {
            nfsTime.marshalling(xdr);
        } else {
            xdr.putInt(0);
        }
    }

    /**
     * @param xdr
     * @param longValue
     */
    private static void marshalling(Xdr xdr, Long longValue) {
        if (longValue != null) {
            xdr.putBoolean(true);
            xdr.putUnsignedInt(longValue.longValue());
        } else {
            xdr.putBoolean(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" [mode :").append(_mode);
        sb.append(" uid: ").append(_uid);
        sb.append(" gid: ").append(_gid);
        sb.append(" size: ").append(_size);
        sb.append(" atime: ").append(_atime);
        sb.append(" mtime: ").append(_mtime);
        sb.append("]");

        return sb.toString();
    }
}
