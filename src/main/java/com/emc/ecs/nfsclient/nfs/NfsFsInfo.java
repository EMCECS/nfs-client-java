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

import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Holder for nonvolatile file system state information and general information
 * about the NFS version 3 protocol server implementation, as specified by RFC
 * 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsFsInfo {

    /**
     * The maximum size in bytes of a READ request supported by the server. Any
     * READ with a number greater than rtmax will result in a short read of
     * rtmax bytes or less.
     */
    public long rtmax;

    /**
     * The preferred size of a READ request. This should be the same as rtmax
     * unless there is a clear benefit in performance or efficiency.
     */
    public long rtpref;

    /**
     * The suggested multiple for the size of a READ request.
     */
    public long rtmult;

    /**
     * The maximum size of a WRITE request supported by the server. In general,
     * the client is limited by wtmax since there is no guarantee that a server
     * can handle a larger write. Any WRITE with a count greater than wtmax will
     * result in a short write of at most wtmax bytes.
     */
    public long wtmax;

    /**
     * The preferred size of a WRITE request. This should be the same as wtmax
     * unless there is a clear benefit in performance or efficiency.
     */
    public long wtpref;

    /**
     * The suggested multiple for the size of a WRITE request.
     */
    public long wtmult;

    /**
     * The preferred size of a READDIR request.
     */
    public long dtpref;

    /**
     * The maximum size of a file on the file system.
     */
    public long maxfilesize;

    /**
     * The server time granularity. When setting a file time using SETATTR, the
     * server guarantees only to preserve times to this accuracy. If this is {0,
     * 1}, the server can support nanosecond times, {0, 1000000} denotes
     * millisecond precision, and {1, 0} indicates that times are accurate only
     * to the nearest second.
     */
    public NfsTime time_delta = new NfsTime();

    /**
     * A bit mask of file system properties. The following values are defined:
     * 
     * <pre>
    FSF_LINK = 0x0001
        If this bit is 1 (TRUE), the file system supports
        hard links.
    FSF_SYMLINK = 0x0002
        If this bit is 1 (TRUE), the file system supports
        symbolic links.
    FSF_HOMOGENEOUS = 0x0008
        If this bit is 1 (TRUE), the information returned by
        PATHCONF is identical for every file and directory
        in the file system. If it is 0 (FALSE), the client
        should retrieve PATHCONF information for each file
        and directory as required.
    FSF_CANSETTIME = 0x0010
        If this bit is 1 (TRUE), the server will set the
        times for a file via SETATTR if requested (to the
        accuracy indicated by time_delta). If it is 0
        (FALSE), the server cannot set times as requested.
     * </pre>
     */
    public long properties;

    /**
     * Unmarshalls volatile file system state information, as specified by RFC
     * 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * @param xdr
     */
    public NfsFsInfo(Xdr xdr) {
        rtmax = xdr.getUnsignedInt();
        rtpref = xdr.getUnsignedInt();
        rtmult = xdr.getUnsignedInt();
        wtmax = xdr.getUnsignedInt();
        wtpref = xdr.getUnsignedInt();
        wtmult = xdr.getUnsignedInt();
        dtpref = xdr.getUnsignedInt();
        maxfilesize = xdr.getLong();
        time_delta.unmarshalling(xdr);
        properties = xdr.getUnsignedInt();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("The maximum size in bytes of a READ request supported: ");
        sb.append(String.valueOf(rtmax));
        sb.append("The preferred size of a READ request:");
        sb.append(String.valueOf(rtpref));
        sb.append("The suggested multiple for the size of a READ request: ");
        sb.append(String.valueOf(rtmult));
        sb.append("The maximum size of a WRITE request supported: ");
        sb.append(String.valueOf(wtmax));
        sb.append("The preferred size of a WRITE request: ");
        sb.append(String.valueOf(wtpref));
        sb.append("The suggested multiple for the size of a WRITE request: ");
        sb.append(String.valueOf(wtmult));
        sb.append("The preferred size of a READDIR request: ");
        sb.append(String.valueOf(dtpref));
        sb.append("The maximum size of a file on the file system: ");
        sb.append(String.valueOf(maxfilesize));

        return sb.toString();
    }
}
