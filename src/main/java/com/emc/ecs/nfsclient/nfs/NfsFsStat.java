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
 * Holder for volatile file system state information, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsFsStat {

    /**
     * The total size, in bytes, of the file system.
     */
    public long tbytes;

    /**
     * The amount of free space, in bytes, in the file system.
     */
    public long fbytes;

    /**
     * The amount of free space, in bytes, available to the user identified by
     * the authentication information in the RPC. (This reflects space that is
     * reserved by the file system; it does not reflect any quota system
     * implemented by the server.)
     */
    public long bytes;

    /**
     * The total number of file slots in the file system. (On a UNIX server,
     * this often corresponds to the number of inodes configured.)
     */
    public long tfiles;

    /**
     * The number of free file slots in the file system.
     */
    public long ffiles;

    /**
     * The number of free file slots that are available to the user
     * corresponding to the authentication information in the RPC. (This
     * reflects slots that are reserved by the file system; it does not reflect
     * any quota system implemented by the server.)
     */
    public long afiles;

    /**
     * A measure of file system volatility: this is the number of seconds for
     * which the file system is not expected to change. For a volatile,
     * frequently updated file system, this will be 0. For an immutable file
     * system, such as a CD-ROM, this would be the largest unsigned integer. For
     * file systems that are infrequently modified, for example, one containing
     * local executable programs and on-line documentation, a value
     * corresponding to a few hours or days might be used. The client may use
     * this as a hint in tuning its cache management. Note however, this measure
     * is assumed to be dynamic and may change at any time.
     */
    public long invarsec;

    /**
     * Unmarshalls volatile file system state information, as specified by RFC
     * 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * @param xdr
     */
    public NfsFsStat(Xdr xdr) {
        tbytes = xdr.getLong();
        fbytes = xdr.getLong();
        bytes = xdr.getLong();
        tfiles = xdr.getLong();
        ffiles = xdr.getLong();
        afiles = xdr.getLong();
        invarsec = xdr.getUnsignedInt();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(" total size:");
        sb.append(String.valueOf(tbytes));
        sb.append(" amount of free space:");
        sb.append(String.valueOf(fbytes));
        sb.append(" amount of free space to the user:");
        sb.append(String.valueOf(bytes));
        sb.append(" total number of file slots:");
        sb.append(String.valueOf(tbytes));
        sb.append(" number of free file slots:");
        sb.append(String.valueOf(tfiles));
        sb.append(" number of free file slots to the user:");
        sb.append(String.valueOf(afiles));
        sb.append(" file system volatility:");
        sb.append(String.valueOf(invarsec));

        return sb.toString();
    }
}
