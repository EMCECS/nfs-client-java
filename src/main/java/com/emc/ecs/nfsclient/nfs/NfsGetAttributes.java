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
 * Holder for returned file system object attributes, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsGetAttributes {

    /**
     * <code>true</code> if this been populated.
     */
    private boolean _loaded = false;

    /**
     * The ftype3 enum of the file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).<br>
     * 
     * <pre>
      enum ftype3 {
         NF3REG    = 1,
         NF3DIR    = 2,
         NF3BLK    = 3,
         NF3CHR    = 4,
         NF3LNK    = 5,
         NF3SOCK   = 6,
         NF3FIFO   = 7
      };
     * </pre>
     * 
     * The enumeration, ftype3, gives the type of a file. The type, NF3REG, is a
     * regular file, NF3DIR is a directory, NF3BLK is a block special device
     * file, NF3CHR is a character special device file, NF3LNK is a symbolic
     * link, NF3SOCK is a socket, and NF3FIFO is a named pipe. Note that the
     * precise enum encoding must be followed.
     * 
     */
    private NfsType _type;

    /**
     * The protection mode bits, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813):
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
    private long _mode; // uint32

    /**
     * The number of hard links to the file - that is, the number of different
     * names for the same file.
     */
    private long _nlink;// uint32

    /**
     * The user ID of the owner of the file.
     */
    private long _uid;  // uint32

    /**
     * The group ID of the group of the file.
     */
    private long _gid;  // uint32

    // for now we use long to represent uint64, it should be safe as 'signed
    // long' is big enough for file size
    /**
     * The number of bytes of disk space that the file actually uses (which can
     * be smaller than the size because the file may have holes or it may be
     * larger due to fragmentation).
     */
    private long _size; // uint64

    /**
     * The number of bytes of disk space that the file actually uses (which can
     * be smaller than the size because the file may have holes or it may be
     * larger due to fragmentation).
     */
    private long _used; // uint64

    /**
     * A description of the device file if the file type is NF3CHR or NF3BLK.The
     * interpretation of rdev depends on the type of file system object. For a
     * block special (NF3BLK) or character special (NF3CHR) file, rdev[0] and
     * rdev[1] are the major and minor device numbers, respectively. (This is
     * obviously a UNIX-specific interpretation.) For all other file types,
     * these two elements should either be set to 0 or the values should be
     * agreed upon by the client and server. If the client and server do not
     * agree upon the values, the client should treat these fields as if they
     * are set to 0. This data field is returned as part of the fattr3 structure
     * and so is available from all replies returning attributes. Since these
     * fields are otherwise unused for objects which are not devices, out of
     * band information can be passed from the server to the client. However,
     * once again, both the server and the client must agree on the values
     * passed.
     * 
     */
    private long _rdev[] = new long[] { 0, 0 }; // uint32

    /**
     * The file system identifier for the file system.
     */
    private long _fsid; // uint64

    /**
     * A number which uniquely identifies the file within its file system (on
     * UNIX this would be the inumber).
     */
    private long _fileid; // uint64

    /**
     * The time when the file data was last accessed.
     */
    private NfsTime _atime;

    /**
     * The time when the file data was last modified.
     */
    private NfsTime _mtime;

    /**
     * The time when the attributes of the file were last changed. Writing to
     * the file changes the ctime in addition to the mtime.
     */
    private NfsTime _ctime;

    /**
     * Reads the Xdr response, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @param xdr
     */
    public void unmarshalling(Xdr xdr) {
        _loaded = true;
        _type = NfsType.fromValue((int) xdr.getUnsignedInt());
        _mode = xdr.getUnsignedInt();
        _nlink = xdr.getUnsignedInt();
        _uid = xdr.getUnsignedInt();
        _gid = xdr.getUnsignedInt();
        _size = xdr.getLong();
        _used = xdr.getLong();
        _rdev[0] = xdr.getUnsignedInt();
        _rdev[1] = xdr.getUnsignedInt();
        _fsid = xdr.getLong();
        _fileid = xdr.getLong();
        _atime = new NfsTime();
        _mtime = new NfsTime();
        _ctime = new NfsTime();
        _atime.unmarshalling(xdr);
        _mtime.unmarshalling(xdr);
        _ctime.unmarshalling(xdr);
    }

    /**
     * @return <code>true</code> if the properties have been loaded from NFS server data,
     *         <code>false</code> otherwise.
     */
    public boolean isLoaded() {
        return _loaded;
    }

    /**
     * @return the ftype3 enum of the file, as specified by RFC 1813
     *         (https://tools.ietf.org/html/rfc1813).<br>
     * 
     *         <pre>
      enum ftype3 {
         NF3REG    = 1,
         NF3DIR    = 2,
         NF3BLK    = 3,
         NF3CHR    = 4,
         NF3LNK    = 5,
         NF3SOCK   = 6,
         NF3FIFO   = 7
      };
     *         </pre>
     * 
     *         The enumeration, ftype3, gives the type of a file. The type,
     *         NF3REG, is a regular file, NF3DIR is a directory, NF3BLK is a
     *         block special device file, NF3CHR is a character special device
     *         file, NF3LNK is a symbolic link, NF3SOCK is a socket, and NF3FIFO
     *         is a named pipe. Note that the precise enum encoding must be
     *         followed.
     * 
     */
    public NfsType getType() {
        return _type;
    }

    /**
     * @return the protection mode bits, as specified by RFC 1813
     *         (https://tools.ietf.org/html/rfc1813):
     *         <ul>
     *         <li><strong>0x00800</strong> - Set user ID on execution.</li>
     *         <li><strong>0x00400</strong> - Set group ID on execution.</li>
     *         <li><strong>0x00200</strong> - Save swapped text (not defined in
     *         POSIX).</li>
     *         <li><strong>0x00100</strong> - Read permission for owner.</li>
     *         <li><strong>0x00080</strong> - Write permission for owner.</li>
     *         <li><strong>0x00040</strong> - Execute permission for owner on a
     *         file. Or lookup (search) permission for owner in directory.</li>
     *         <li><strong>0x00020</strong> - Read permission for group.</li>
     *         <li><strong>0x00010</strong> - Write permission for group.</li>
     *         <li><strong>0x00008</strong> - Execute permission for group on a
     *         file. Or lookup (search) permission for group in directory.</li>
     *         <li><strong>0x00004</strong> - Read permission for others.</li>
     *         <li><strong>0x00002</strong> - Write permission for others.</li>
     *         <li><strong>0x00001</strong> - Execute permission for others on a
     *         file. Or lookup (search) permission for others in directory.</li>
     *         </ul>
     */
    public long getMode() {
        return _mode;
    }

    /**
     * @return the number of hard links to the file - that is, the number of
     *         different names for the same file.
     */
    public long getNlink() {
        return _nlink;
    }

    /**
     * @return the user ID of the owner of the file.
     */
    public long getUid() {
        return _uid;
    }

    /**
     * @return the group ID of the group of the file.
     */
    public long getGid() {
        return _gid;
    }

    /**
     * @return the size of the file in bytes.
     */
    public long getSize() {
        return _size;
    }

    /**
     * @return the number of bytes of disk space that the file actually uses
     *         (which can be smaller than the size because the file may have
     *         holes or it may be larger due to fragmentation).
     */
    public long getUsed() {
        return _used;
    }

    /**
     * @return a description of the device file if the file type is NF3CHR or
     *         NF3BLK.The interpretation of rdev depends on the type of file
     *         system object. For a block special (NF3BLK) or character special
     *         (NF3CHR) file, rdev[0] and rdev[1] are the major and minor device
     *         numbers, respectively. (This is obviously a UNIX-specific
     *         interpretation.) For all other file types, these two elements
     *         should either be set to 0 or the values should be agreed upon by
     *         the client and server. If the client and server do not agree upon
     *         the values, the client should treat these fields as if they are
     *         set to 0. This data field is returned as part of the fattr3
     *         structure and so is available from all replies returning
     *         attributes. Since these fields are otherwise unused for objects
     *         which are not devices, out of band information can be passed from
     *         the server to the client. However, once again, both the server
     *         and the client must agree on the values passed.
     * 
     */
    public long[] getRdev() {
        return _rdev;
    }

    /**
     * @return the file system identifier for the file system.
     */
    public long getFsid() {
        return _fsid;
    }

    /**
     * @return a number which uniquely identifies the file within its file
     *         system (on UNIX this would be the inumber).
     */
    public long getFileid() {
        return _fileid;
    }

    /**
     * @return the time when the file data was last accessed.
     */
    public NfsTime getAtime() {
        return _atime;
    }

    /**
     * @return the time when the file data was last modified.
     */
    public NfsTime getMtime() {
        return _mtime;
    }

    /**
     * @return the time when the attributes of the file were last changed.
     *         Writing to the file changes the ctime in addition to the mtime.
     */
    public NfsTime getCtime() {
        return _ctime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "NfsAttr{" + "type=" + _type + ", _mode=" + _mode + ", _nlink=" + _nlink + ", _uid=" + _uid + ", _gid="
                + _gid + ", _size=" + _size + ", _used=" + _used + ", _specdata1=" + _rdev[0] + ", _specdata2="
                + _rdev[1] + ", _fsid=" + _fsid + ", _fileid=" + _fileid + ", _atime=" + _atime + ", _mtime=" + _mtime
                + ", _ctime=" + _ctime + '}';
    }

}
