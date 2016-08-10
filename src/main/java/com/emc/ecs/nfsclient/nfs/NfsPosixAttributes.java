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
 * Holder for the Posix attributes returned by the Pathconf RPC call, as
 * specified by RFC 1813 (https://tools.ietf.org/html/rfc1813)
 * 
 * @author seibed
 */
public class NfsPosixAttributes {

    /**
     * <ul>
     * <li><code>true</code> if the properties have been loaded from NFS server
     * data.</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean _loaded = false;  // if attribute _loaded

    /**
     * The maximum number of hard links to an object.
     */
    private long linkMaximum; // uint32

    /**
     * The maximum length of a component of a filename.
     */
    private long nameMaximum; // uint32

    /**
     * <ul>
     * <li><code>true</code> if the server will reject any request that includes
     * a name longer than getNameMaximum() with the error, NFS3ERR_NAMETOOLONG.
     * </li>
     * <li><code>false</code> if any length name over getNameMaximum() bytes
     * will be silently truncated to getNameMaximum() bytes.</li>
     * </ul>
     */
    private boolean noTruncation;

    /**
     * <ul>
     * <li><code>true</code> if the server will reject any request to change
     * either the owner or the group associated with a file if the caller is not
     * the privileged user (uid 0).</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean chownRestricted;

    /**
     * <ul>
     * <li><code>true</code> if the server file system does not distinguish case
     * when interpreting filenames.</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean caseInsensitive;

    /**
     * <ul>
     * <li><code>true</code> if the server file system will preserve the case of
     * a name during a CREATE, MKDIR, MKNOD, SYMLINK, RENAME, or LINK operation.
     * </li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    private boolean casePreserving;

    /**
     * Reads the Xdr response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @param xdr
     */
    public void unmarshalling(Xdr xdr) {
        _loaded = true;
        linkMaximum = xdr.getUnsignedInt();
        nameMaximum = xdr.getUnsignedInt();
        noTruncation = xdr.getBoolean();
        chownRestricted = xdr.getBoolean();
        caseInsensitive = xdr.getBoolean();
        casePreserving = xdr.getBoolean();
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the properties have been loaded from NFS
     *         server data.</li>
     *         <li><code>false</code> otherwise.</li>
     *         </ul>
     * 
     */
    public boolean isLoaded() {
        return _loaded;
    }

    /**
     * @return The maximum number of hard links to an object.
     */
    public long getLinkMaximum() {
        return linkMaximum;
    }

    /**
     * @return The maximum length of a component of a filename.
     */
    public long getNameMaximum() {
        return nameMaximum;
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the server file system does not
     *         distinguish case when interpreting filenames.</li>
     *         <li><code>false</code> otherwise.</li>
     *         </ul>
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the server file system will preserve the
     *         case of a name during a CREATE, MKDIR, MKNOD, SYMLINK, RENAME, or
     *         LINK operation.</li>
     *         <li><code>false</code> otherwise.</li>
     *         </ul>
     */
    public boolean isCasePreserving() {
        return casePreserving;
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the server will reject any request to
     *         change either the owner or the group associated with a file if
     *         the caller is not the privileged user (uid 0).</li>
     *         <li><code>false</code> otherwise.</li>
     *         </ul>
     */
    public boolean isChownRestricted() {
        return chownRestricted;
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the server will reject any request that
     *         includes a name longer than getNameMaximum() with the error,
     *         NFS3ERR_NAMETOOLONG.</li>
     *         <li><code>false</code> if any length name over getNameMaximum()
     *         bytes will be silently truncated to getNameMaximum() bytes.</li>
     *         </ul>
     */
    public boolean isNoTruncation() {
        return noTruncation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "NfsPosixAttributes [loaded=" + _loaded + ", linkMaximum=" + linkMaximum + ", nameMaximum=" + nameMaximum
                + ", noTruncation=" + noTruncation + ", chownRestricted=" + chownRestricted + ", caseInsensitive="
                + caseInsensitive + ", casePreserving=" + casePreserving + "]";
    }

}
