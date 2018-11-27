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
package com.emc.ecs.nfsclient.nfs.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.emc.ecs.nfsclient.nfs.*;

/**
 * This interface is the NFS client equivalent of <code>java.io.File</code>. It
 * has been modified appropriately for manipulations of NFS files, as specified
 * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public interface NfsFile<N extends Nfs<?>, F extends NfsFile<N, F>> extends Comparable<F> {

    /**
     * Character between names in the file path. Equivalent of the File
     * interface <code>separatorChar</code>.
     */
    final static char separatorChar = '/';

    /**
     * String between names in the file path. Equivalent of the File interface
     * <code>separator</code>.
     */
    final static String separator = "/";

    /**
     * Set user ID on execution. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long setUserIdOnExecuteModeBit = 0x00800;

    /**
     * Set group ID on execution. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long setGroupIdOnExecuteModeBit = 0x00400;

    /**
     * Save swapped text (not defined in POSIX). Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long saveSwappedTextModeBit = 0x00200;

    /**
     * Read permission for owner. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long ownerReadModeBit = 0x00100;

    /**
     * Write permission for owner. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long ownerWriteModeBit = 0x00080;

    /**
     * Execute permission for owner on a file. Or lookup (search) permission for
     * owner in directory. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long ownerExecuteModeBit = 0x00040;

    /**
     * Read permission for group. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long groupReadModeBit = 0x00020;

    /**
     * Write permission for group. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long groupWriteModeBit = 0x00010;

    /**
     * Execute permission for group on a file. Or lookup (search) permission for
     * group in directory. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long groupExecuteModeBit = 0x00008;

    /**
     * Read permission for others. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long othersReadModeBit = 0x00004;

    /**
     * Write permission for others. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long othersWriteModeBit = 0x00002;

    /**
     * Execute permission for others on a file. Or lookup (search) permission
     * for others in directory. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    long othersExecuteModeBit = 0x00001;

    /**
     * Tests whether this client can delete an existing directory entry.
     * Specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     *
     * @return <code>true</code> if it can, <code>false</code> if it cannot
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    boolean canDelete() throws IOException;

    /**
     * Tests whether this client can execute this file (no meaning for a
     * directory). Specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     *
     * @return <code>true</code> if it can, <code>false</code> if it cannot
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    boolean canExecute() throws IOException;

    /**
     * Tests whether this client can write new data or add directory entries.
     * Specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     *
     * @return <code>true</code> if it can, <code>false</code> if it cannot
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    boolean canExtend() throws IOException;

    /**
     * Tests whether this client can look up a name in a directory (no meaning
     * for non-directory objects). Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     *
     * @return <code>true</code> if it can, <code>false</code> if it cannot
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    boolean canLookup() throws IOException;

    /**
     * Tests whether this client can rewrite existing file data or modify
     * existing directory entries. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     *
     * @return <code>true</code> if it can, <code>false</code> if it cannot
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    boolean canModify() throws IOException;

    /**
     * Tests whether this client can read data from file or read a directory.
     * Specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     *
     * @return <code>true</code> if it can, <code>false</code> if it cannot
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    boolean canRead() throws IOException;

    /**
     * Atomically creates a new, empty file named by path if and only if a file
     * with this name does not yet exist. The test for the existence of the file
     * and the creation of the file if it does not exist are a single operation
     * that is atomic with respect to all other filesystem activities that might
     * affect the file.
     *
     * @return <code>true</code> if the named file did not exist and was
     *         successfully created; <code>false</code> if the named file
     *         already existed.
     * @throws IOException
     *             If an I/O error occurred
     */
    boolean createNewFile() throws IOException;

    /**
     * Deletes this file/directory.
     *
     * @throws IOException
     */
    void delete() throws IOException;

    /**
     * Tests whether this file exists on the NFS server.
     *
     * @return <code>true</code> if the file exists on the NFS server
     * @throws IOException
     */
    boolean exists() throws IOException;

    /**
     * @return The backing file obtained by following all symbolic links in the
     *         path, if any.
     * @throws IOException
     */
    F followLinks() throws IOException;

    /**
     * @param linkTracker
     *            The tracker to use. This must be passed so that monitoring is
     *            continued until the link resolves to a file that is not a
     *            symbolic link.
     * @return The backing file obtained by following all symbolic links in the
     *         path, if any.
     * @throws IOException
     */
    F followLinks(LinkTracker<N, F> linkTracker) throws IOException;

    /**
     * @return The fully qualified path to the file in this network, including
     *         the server name and the exported folders, using the usual form:
     *         <code>server:exported_folders/path_from_exported_filesystem_root</code>
     */
    String getAbsolutePath();

    /**
     * Tests whether this client has the specified access permissions for this
     * file. The bits for the request and reply are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813):
     * 
     * <pre>
    ACCESS3_READ    = 0x0001 - Read data from file or read a directory.
    ACCESS3_LOOKUP  = 0x0002 - Look up a name in a directory (no meaning for non-directory objects).
    ACCESS3_MODIFY  = 0x0004 - Rewrite existing file data or modify existing directory entries.
    ACCESS3_EXTEND  = 0x0008 - Write new data or add directory entries.
    ACCESS3_DELETE  = 0x0010 - Delete an existing directory entry.
    ACCESS3_EXECUTE = 0x0020 - Execute file (no meaning for a directory).
     * </pre>
     * 
     * In general, it is not sufficient for the client to attempt to deduce
     * access permissions by inspecting the uid, gid, and mode fields in the
     * file attributes, since the server may perform uid or gid mapping or
     * enforce additional access control restrictions. It is also possible that
     * the NFS version 3 protocol server may not be in the same ID space as the
     * NFS version 3 protocol client. In these cases (and perhaps others), the
     * NFS version 3 protocol client can not reliably perform an access check
     * with only current file attributes.
     * 
     * In the NFS version 2 protocol, the only reliable way to determine whether
     * an operation was allowed was to try it and see if it succeeded or failed.
     * Using the ACCESS procedure in the NFS version 3 protocol, the client can
     * ask the server to indicate whether or not one or more classes of
     * operations are permitted. The ACCESS operation is provided to allow
     * clients to check before doing a series of operations. This is useful in
     * operating systems (such as UNIX) where permission checking is done only
     * when a file or directory is opened. This procedure is also invoked by NFS
     * client access procedure (called possibly through access(2)). The intent
     * is to make the behavior of opening a remote file more consistent with the
     * behavior of opening a local file.
     *
     * @param accessToCheck
     *            A bit mask of access permissions to check, as specified below.
     * 
     *            <pre>
    ACCESS3_READ    = 0x0001 - Read data from file or read a directory.
    ACCESS3_LOOKUP  = 0x0002 - Look up a name in a directory (no meaning for non-directory objects).
    ACCESS3_MODIFY  = 0x0004 - Rewrite existing file data or modify existing directory entries.
    ACCESS3_EXTEND  = 0x0008 - Write new data or add directory entries.
    ACCESS3_DELETE  = 0x0010 - Delete an existing directory entry.
    ACCESS3_EXECUTE = 0x0020 - Execute file (no meaning for a directory).
     *            </pre>
     * 
     * @return The access bitmap, as specified below.
     * 
     *         <pre>
    ACCESS3_READ    = 0x0001 - Read data from file or read a directory.
    ACCESS3_LOOKUP  = 0x0002 - Look up a name in a directory (no meaning for non-directory objects).
    ACCESS3_MODIFY  = 0x0004 - Rewrite existing file data or modify existing directory entries.
    ACCESS3_EXTEND  = 0x0008 - Write new data or add directory entries.
    ACCESS3_DELETE  = 0x0010 - Delete an existing directory entry.
    ACCESS3_EXECUTE = 0x0020 - Execute file (no meaning for a directory).
     *         </pre>
     * 
     * @throws IOException
     *             if it does not exist or permissions cannot be read.
     */
    long getAccess(long accessToCheck) throws IOException;

    /**
     * @return The NFS attributes, as specified by RFC 1813
     *         (https://tools.ietf.org/html/rfc1813).
     * @throws IOException
     */
    NfsGetAttributes getAttributes() throws IOException;

    /**
     * @param childName
     * @return The child file.
     * @throws IOException
     */
    F getChildFile(String childName) throws IOException;

    /**
     * @return The number of unallocated bytes in the partition named by this
     *         abstract path name.
     * @throws IOException
     */
    long getFreeSpace() throws IOException;

    /**
     * Convenience method to access the protection mode bits, as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813):
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
     * 
     * @return The protection mode bits for the file.
     * @throws IOException
     *
     */
    long getMode() throws IOException;

    /**
     * @return The name
     */
    String getName();

    /**
     * @return The path of the parent folder, or null if there is none
     */
    String getParent();

    /**
     * @return The NfsFile of the parent folder, or null if there is none.
     */
    F getParentFile();

    /**
     * @return The absolute path, from the root of the exported folders.
     *         Synonymous with getAbsolutePath().
     */
    String getPath();

    /**
     * @return The size of the partition named by this abstract pathname.
     * @throws IOException
     */
    long getTotalSpace() throws IOException;

    /**
     * @return The number of free bytes available to this client on the
     *         partition named by this abstract pathname.
     * @throws IOException
     */
    long getUsableSpace() throws IOException;

    /**
     * @return true if it is, false if it is not
     * @throws IOException
     */
    boolean isDirectory() throws IOException;

    /**
     * @return true if it is a normal file, false if it is not
     * @throws IOException
     */
    boolean isFile() throws IOException;

    /**
     * @return true if it is the root file of the mount, false if it is not
     * @throws IOException
     */
    boolean isRootFile() throws IOException;

    /**
     * @return time of last modification
     * @throws IOException
     */
    long lastModified() throws IOException;

    /**
     * @return the length in bytes, or 0 if the file does not exist or the
     *         attributes cannot be read.
     */
    long length();

    /**
     * @return the length in bytes
     * @throws IOException
     *             if the file does not exist or the attributes cannot be read.
     */
    long lengthEx() throws IOException;

    /**
     * Lists all files in this directory.
     * 
     * @return the names of all files in the directory
     * @throws IOException
     */
    List<String> list() throws IOException;

    /**
     * Lists all files matching the filter in this directory.
     * 
     * @param filter
     * @return the names of all files matching the filter in the directory
     * @throws IOException
     */
    List<String> list(NfsFilenameFilter filter) throws IOException;

    /**
     * Lists all files in this directory.
     * 
     * @return NfsFile objects for all files in the directory
     * @throws IOException
     */
    List<F> listFiles() throws IOException;

    /**
     * Lists all files matching the filter in this directory.
     * 
     * @param filter
     * @return NfsFile objects for all files matching the filter in the
     *         directory
     * @throws IOException
     */
    List<F> listFiles(NfsFilenameFilter filter) throws IOException;

    /**
     * Lists all files matching the filter in this directory.
     * 
     * @param filter
     * @return NfsFile objects for all files matching the filter in the
     *         directory
     * @throws IOException
     */
    List<F> listFiles(NfsFileFilter filter) throws IOException;

    /**
     * Creates the directory if it does not exist.
     * 
     * @throws IOException
     *             if the parent directories do not exist or it cannot be
     *             created
     */
    void mkdir() throws IOException;

    /**
     * Creates the directory and all parent directories if they do not already
     * exist.
     * 
     * @throws IOException
     *             if it cannot create one of the directories.
     */
    void mkdirs() throws IOException;

    /**
     * Creates a new file with the current file as its parent directory. This
     * does not create the file on the NFS mount.
     * 
     * @param childName
     * @return the new file
     * @throws IOException
     */
    F newChildFile(String childName) throws IOException;

    /**
     * Renames the file denoted by this abstract pathname.
     * 
     * <p>
     * Many aspects of the behavior of this method are inherently
     * platform-dependent: The rename operation might not be able to move a file
     * from one filesystem to another, it might not be atomic, and it might not
     * succeed if a file with the destination abstract pathname already exists.
     * The return value should always be checked to make sure that the rename
     * operation was successful.
     * </p>
     * 
     * @param destination
     * @throws IOException
     */
    boolean renameTo(F destination) throws IOException;

    /**
     * Sets NFS file attributes, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @param nfsSetAttributes
     * @throws IOException
     */
    public void setAttributes(NfsSetAttributes nfsSetAttributes) throws IOException;

    /**
     * Sets the last-modified time of the file or directory named by this
     * abstract pathname.
     * 
     * <p>
     * All platforms support file-modification times to the nearest second, but
     * some provide more precision. The argument will be truncated to fit the
     * supported precision. If the operation succeeds and no intervening
     * operations on the file take place, then the next invocation of the
     * lastModified() method will return the (possibly truncated) time argument
     * that was passed to this method.
     * </p>
     * 
     * @param millis
     *            - The new last-modified time, measured in milliseconds since
     *            the epoch (00:00:00 GMT, January 1, 1970)
     * @throws IOException
     */
    void setLastModified(long millis) throws IOException;

    /**
     * Sets the protection mode bits, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This replaces the
     * <code>java.io.File</code> methods <code>setExecutable</code>,
     * <code>setReadable</code>, and <code>setWritable</code>. The protection
     * mode bits are defined as follows:
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
     *
     * @param mode
     *            the protection mode bits
     * @throws IOException
     */
    void setMode(long mode) throws IOException;

    /**
     * @return The low-level NFS client supporting this NfsFile instance.
     */
    public N getNfs();

    /**
     * @return The NFS file handle, as specified by RFC 1813
     *         (https://tools.ietf.org/html/rfc1813).
     * @throws IOException
     */
    byte[] getFileHandle() throws IOException;

    /*
     * The following methods all give access to wrapped NFS functionality based
     * on the NfsFile data. This wrapping implementation (provided by the Nfs
     * client) includes repeated attempts, error checking, and logging.
     */

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsGetAttrResponse getattr() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsGetAttrRequest makeGetAttrRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure SETATTR changes one or more of the attributes of a file system
     * object on the server.
     * </p>
     * 
     * @param attributes
     *            The attributes to set.
     * @param guardTime
     *            If <code>guardTime</code> is not null, the server must compare
     *            the value of <code>guardTime</code> to the current ctime of
     *            the object. If the values are different, the server must
     *            preserve the object attributes and must return a status of
     *            NFS3ERR_NOT_SYNC.
     * @return the response
     * @throws IOException
     */
    NfsSetAttrResponse setattr(NfsSetAttributes attributes, NfsTime guardTime) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SETATTR changes one or more of the attributes of a file system
     * object on the server.
     * </p>
     * 
     * @param attributes
     *            The attributes to set.
     * @param guardTime
     *            If <code>guardTime</code> is not null, the server must compare
     *            the value of <code>guardTime</code> to the current ctime of
     *            the object. If the values are different, the server must
     *            preserve the object attributes and must return a status of
     *            NFS3ERR_NOT_SYNC.
     * @return the request
     * @throws IOException
     */
    NfsSetAttrRequest makeSetAttrRequest(NfsSetAttributes attributes, NfsTime guardTime) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsLookupResponse lookup() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsLookupRequest makeLookupRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure ACCESS determines the access rights that a user, as identified
     * by the credentials in the request, has with respect to a file system
     * object. The client encodes the set of permissions that are to be checked
     * in a bit mask. The server checks the permissions encoded in the bit mask.
     * A status of NFS3_OK is returned along with a bit mask encoded with the
     * permissions that the client is allowed.
     * </p>
     * 
     * <p>
     * The results of this procedure are necessarily advisory in nature. That
     * is, a return status of NFS3_OK and the appropriate bit set in the bit
     * mask does not imply that such access will be allowed to the file system
     * object in the future, as access rights can be revoked by the server at
     * any time.
     * </p>
     * 
     * @param accessToCheck
     *            A bit mask of access permissions to check, as specified below.
     * 
     *            <pre>
    ACCESS3_READ    = 0x0001 - Read data from file or read a directory.
    ACCESS3_LOOKUP  = 0x0002 - Look up a name in a directory (no meaning for non-directory objects).
    ACCESS3_MODIFY  = 0x0004 - Rewrite existing file data or modify existing directory entries.
    ACCESS3_EXTEND  = 0x0008 - Write new data or add directory entries.
    ACCESS3_DELETE  = 0x0010 - Delete an existing directory entry.
    ACCESS3_EXECUTE = 0x0020 - Execute file (no meaning for a directory).
     *            </pre>
     * 
     * @return the response
     * @throws IOException
     */
    NfsAccessResponse access(long accessToCheck) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure ACCESS determines the access rights that a user, as identified
     * by the credentials in the request, has with respect to a file system
     * object. The client encodes the set of permissions that are to be checked
     * in a bit mask. The server checks the permissions encoded in the bit mask.
     * A status of NFS3_OK is returned along with a bit mask encoded with the
     * permissions that the client is allowed.
     * </p>
     * 
     * <p>
     * The results of this procedure are necessarily advisory in nature. That
     * is, a return status of NFS3_OK and the appropriate bit set in the bit
     * mask does not imply that such access will be allowed to the file system
     * object in the future, as access rights can be revoked by the server at
     * any time.
     * </p>
     * 
     * @param accessToCheck
     *            A bit mask of access permissions to check, as specified below.
     * 
     *            <pre>
    ACCESS3_READ    = 0x0001 - Read data from file or read a directory.
    ACCESS3_LOOKUP  = 0x0002 - Look up a name in a directory (no meaning for non-directory objects).
    ACCESS3_MODIFY  = 0x0004 - Rewrite existing file data or modify existing directory entries.
    ACCESS3_EXTEND  = 0x0008 - Write new data or add directory entries.
    ACCESS3_DELETE  = 0x0010 - Delete an existing directory entry.
    ACCESS3_EXECUTE = 0x0020 - Execute file (no meaning for a directory).
     *            </pre>
     * 
     * @return the request
     * @throws IOException
     */
    NfsAccessRequest makeAccessRequest(long accessToCheck) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * This method reads the data associated with a symbolic link. The data is
     * an ASCII string that is opaque to the server. That is, whether created by
     * the NFS version 3 protocol software from a client or created locally on
     * the server, the data in a symbolic link is not interpreted when created,
     * but is simply stored.
     * </p>
     * 
     * <p>
     * A symbolic link is nominally a pointer to another file. The data is not
     * necessarily interpreted by the server, just stored in the file. It is
     * possible for a client implementation to store a path name that is not
     * meaningful to the server operating system in a symbolic link. A READLINK
     * operation returns the data to the client for interpretation. If different
     * implementations want to share access to symbolic links, then they must
     * agree on the interpretation of the data in the symbolic link.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsReadlinkResponse readlink() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * This method reads the data associated with a symbolic link. The data is
     * an ASCII string that is opaque to the server. That is, whether created by
     * the NFS version 3 protocol software from a client or created locally on
     * the server, the data in a symbolic link is not interpreted when created,
     * but is simply stored.
     * </p>
     * 
     * <p>
     * A symbolic link is nominally a pointer to another file. The data is not
     * necessarily interpreted by the server, just stored in the file. It is
     * possible for a client implementation to store a path name that is not
     * meaningful to the server operating system in a symbolic link. A READLINK
     * operation returns the data to the client for interpretation. If different
     * implementations want to share access to symbolic links, then they must
     * agree on the interpretation of the data in the symbolic link.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsReadlinkRequest makeReadlinkRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure READ reads data from a file.
     * </p>
     * 
     * @param offset
     *            The position within the file at which the read is to begin. An
     *            <code>offset</code> of 0 means to read data starting at the
     *            beginning of the file. If <code>offset</code> is greater than
     *            or equal to the size of the file, the status, NFS3_OK, is
     *            returned with <code>size</code> set to 0 and <code>eof</code>
     *            set to <code>true</code>, subject to access permissions
     *            checking.
     * @param size
     *            The number of bytes of data that are to be read. If
     *            <code>size</code> is 0, the READ will succeed and return 0
     *            bytes of data, subject to access permissions checking.
     *            <code>size</code> must be less than or equal to the value of
     *            the <code>rtmax</code> field in the FSINFO reply structure for
     *            the file system that contains file. If greater, the server may
     *            return only <code>rtmax</code> bytes, resulting in a short
     *            read.
     * @param bytes
     *            optional externally provided buffer - if not provided, the
     *            method will create a new, empty buffer for receiving the data.
     * @param position
     *            position to start writing, defaults to 0 if buffer is null
     * @return the response
     * @throws IOException
     */
    NfsReadResponse read(long offset, int size, byte[] bytes, int position) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READ reads data from a file.
     * </p>
     * 
     * @param offset
     *            The position within the file at which the read is to begin. An
     *            <code>offset</code> of 0 means to read data starting at the
     *            beginning of the file. If <code>offset</code> is greater than
     *            or equal to the size of the file, the status, NFS3_OK, is
     *            returned with <code>size</code> set to 0 and <code>eof</code>
     *            set to <code>true</code>, subject to access permissions
     *            checking.
     * @param size
     *            The number of bytes of data that are to be read. If
     *            <code>size</code> is 0, the READ will succeed and return 0
     *            bytes of data, subject to access permissions checking.
     *            <code>size</code> must be less than or equal to the value of
     *            the <code>rtmax</code> field in the FSINFO reply structure for
     *            the file system that contains file. If greater, the server may
     *            return only <code>rtmax</code> bytes, resulting in a short
     *            read.
     * @return the request
     * @throws IOException
     */
    NfsReadRequest makeReadRequest(long offset, int size) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param offset
     *            The position within the file at which the write is to begin.
     *            An offset of 0 means to write data starting at the beginning
     *            of the file.
     * @param payload
     *            The data to be written to the file.
     * @param syncType
     *            One of the values below.
     * 
     *            <pre>
    UNSTABLE  = 0 - Best effort, no promises.
    DATA_SYNC = 1 - Commit all data to stable storage, plus enough metadata for retrieval, before returning.
    FILE_SYNC = 2 - Commit all data and metadata to stable storage before returning.
     *            </pre>
     * 
     * @return the response
     * @throws IOException
     */
    NfsWriteResponse write(long offset, List<ByteBuffer> payload, int syncType) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param offset
     *            The position within the file at which the write is to begin.
     *            An offset of 0 means to write data starting at the beginning
     *            of the file.
     * @param payload
     *            The data to be written to the file.
     * @param syncType
     *            One of the values below.
     *            <ul>
     *            <li>UNSTABLE = 0 - Best effort, no promises.</li>
     *            <li>DATA_SYNC = 1 - Commit all data to stable storage, plus
     *            enough metadata for retrieval, before returning.</li>
     *            <li>FILE_SYNC = 2 - Commit all data and metadata to stable
     *            storage before returning.</li>
     *            </ul>
     * @param verifier
     *            This is a cookie, previously returned from the server, that
     *            the client can use to determine whether the server has changed
     *            state between a call to WRITE and a subsequent call to either
     *            WRITE or COMMIT.
     * @return the response
     * @throws IOException
     */
    NfsWriteResponse write(long offset, List<ByteBuffer> payload, int syncType, Long verifier) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param offset
     *            The position within the file at which the write is to begin.
     *            An offset of 0 means to write data starting at the beginning
     *            of the file.
     * @param payload
     *            The data to be written to the file.
     * @param syncType
     *            One of the values below.
     *            <ul>
     *            <li>UNSTABLE = 0 - Best effort, no promises.</li>
     *            <li>DATA_SYNC = 1 - Commit all data to stable storage, plus
     *            enough metadata for retrieval, before returning.</li>
     *            <li>FILE_SYNC = 2 - Commit all data and metadata to stable
     *            storage before returning.</li>
     *            </ul>
     * @return the request
     * @throws IOException
     */
    NfsWriteRequest makeWriteRequest(long offset, List<ByteBuffer> payload, int syncType) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure CREATE creates a regular file.
     * </p>
     * 
     * @param createMode
     *            The instance describing how the file is to be created.
     *            UNCHECKED means that the file should be created without
     *            checking for the existence of a duplicate file in the same
     *            directory. In this case, how.obj_attributes is a sattr3
     *            describing the initial attributes for the file. GUARDED
     *            specifies that the server should check for the presence of a
     *            duplicate file before performing the create and should fail
     *            the request with NFS3ERR_EXIST if a duplicate file exists. If
     *            the file does not exist, the request is performed as described
     *            for UNCHECKED. EXCLUSIVE specifies that the server is to
     *            follow exclusive creation semantics, using the verifier to
     *            ensure exclusive creation of the target. No attributes may be
     *            provided in this case, since the server may use the target
     *            file metadata to store the createverf3 verifier.
     * @param attributes
     *            The initial attributes for the new file.
     * @param verifier
     *            A unique identifier to ensure exclusive creation of the new
     *            file. If no value is provided for exclusive creation, this is
     *            generated.
     * @return the response
     * @throws IOException
     */
    NfsCreateResponse create(NfsCreateMode createMode, NfsSetAttributes attributes, byte[] verifier) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure CREATE creates a regular file.
     * </p>
     * 
     * @param createMode
     *            The instance describing how the file is to be created.
     *            UNCHECKED means that the file should be created without
     *            checking for the existence of a duplicate file in the same
     *            directory. In this case, how.obj_attributes is a sattr3
     *            describing the initial attributes for the file. GUARDED
     *            specifies that the server should check for the presence of a
     *            duplicate file before performing the create and should fail
     *            the request with NFS3ERR_EXIST if a duplicate file exists. If
     *            the file does not exist, the request is performed as described
     *            for UNCHECKED. EXCLUSIVE specifies that the server is to
     *            follow exclusive creation semantics, using the verifier to
     *            ensure exclusive creation of the target. No attributes may be
     *            provided in this case, since the server may use the target
     *            file metadata to store the createverf3 verifier.
     * @param attributes
     *            The initial attributes for the new file.
     * @param verifier
     *            A unique identifier to ensure exclusive creation of the new
     *            file. If no value is provided for exclusive creation, this is
     *            generated.
     * @return the request
     * @throws IOException
     */
    NfsCreateRequest makeCreateRequest(NfsCreateMode createMode, NfsSetAttributes attributes, byte[] verifier)
            throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure MKDIR creates a new subdirectory.
     * </p>
     * 
     * @param attributes
     *            The initial attributes for the subdirectory.
     * @return the response
     * @throws IOException
     */
    NfsMkdirResponse mkdir(NfsSetAttributes attributes) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKDIR creates a new subdirectory.
     * </p>
     * 
     * @param attributes
     *            The initial attributes for the subdirectory.
     * @return the request
     * @throws IOException
     */
    NfsMkdirRequest makeMkdirRequest(NfsSetAttributes attributes) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure SYMLINK creates a new symbolic link.
     * </p>
     * 
     * @param symbolicLinkData
     *            The string containing the symbolic link data.
     * @param attributes
     *            The initial attributes for the symbolic link.
     * @return the response
     * @throws IOException
     */
    NfsSymlinkResponse symlink(String symbolicLinkData, NfsSetAttributes attributes) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SYMLINK creates a new symbolic link.
     * </p>
     * 
     * @param symbolicLinkData
     *            The string containing the symbolic link data.
     * @param attributes
     *            The initial attributes for the symbolic link.
     * @return the request
     * @throws IOException
     */
    NfsSymlinkRequest makeSymlinkRequest(String symbolicLinkData, NfsSetAttributes attributes) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param type
     *            The type of the object to be created.
     * @param attributes
     *            The initial attributes for the special file.
     * @param rdev
     *            When creating a character special file (<code>type</code> is
     *            <code>NF3CHR</code>) or a block special file (
     *            <code>type</code> is <code>NF3BLK</code>),
     *            <code>rdev[0]</code> is the major number and
     *            <code>rdev[1]</code> is the minor number.
     * @return the response
     * @throws IOException
     */
    NfsMknodResponse mknod(NfsType type, NfsSetAttributes attributes, long[] rdev) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param type
     *            The type of the object to be created.
     * @param attributes
     *            The initial attributes for the special file.
     * @param rdev
     *            When creating a character special file (<code>type</code> is
     *            <code>NF3CHR</code>) or a block special file (
     *            <code>type</code> is <code>NF3BLK</code>),
     *            <code>rdev[0]</code> is the major number and
     *            <code>rdev[1]</code> is the minor number.
     * @return the request
     * @throws IOException
     */
    NfsMknodRequest makeMknodRequest(NfsType type, NfsSetAttributes attributes, long[] rdev) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure REMOVE removes (deletes) an entry from a directory. If the
     * entry in the directory was the last reference to the corresponding file
     * system object, the object may be destroyed.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsRemoveResponse remove() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure REMOVE removes (deletes) an entry from a directory. If the
     * entry in the directory was the last reference to the corresponding file
     * system object, the object may be destroyed.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsRemoveRequest makeRemoveRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
     * directory entry of the subdirectory is the last reference to the
     * subdirectory, the subdirectory may be destroyed.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsRmdirResponse rmdir() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
     * directory entry of the subdirectory is the last reference to the
     * subdirectory, the subdirectory may be destroyed.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsRmdirRequest makeRmdirRequest() throws IOException;

    /**
     * Makes the RPC call for these files, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure RENAME changes the path of this file to the path specified by
     * <code>toFile</code>. The operation is required to be atomic to the
     * client. This file and <code>toFile</code> must reside on the same file
     * system and server.
     * </p>
     * 
     * @param toFile
     *            A file object with the new path for this object.
     * @return the response
     * @throws IOException
     */
    NfsRenameResponse rename(F toFile) throws IOException;

    /**
     * Convenience method for creating the request for these files, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RENAME changes the path of this file to the path specified by
     * <code>toFile</code>. The operation is required to be atomic to the
     * client. This file and <code>toFile</code> must reside on the same file
     * system and server.
     * </p>
     * 
     * @param toFile
     *            A file object with the new path for this object.
     * @return the request
     * @throws IOException
     */
    NfsRenameRequest makeRenameRequest(F toFile) throws IOException;

    /**
     * Makes the RPC call for these files, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>source</code> to this file.
     * This file and <code>source</code> must reside on the same file system and
     * server.
     * </p>
     * 
     * @param source
     *            The existing file system object to which this file should be
     *            linked.
     * @return the response
     * @throws IOException
     */
    NfsLinkResponse link(F source) throws IOException;

    /**
     * Convenience method for creating the request for these files, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>source</code> to this file.
     * This file and <code>source</code> must reside on the same file system and
     * server.
     * </p>
     * 
     * @param source
     *            The existing file system object to which this file should be
     *            linked.
     * @return the request
     * @throws IOException
     */
    NfsLinkRequest makeLinkRequest(F source) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIR in which the <code>cookie</code> was acquired.
     * @param count
     *            The maximum size of the READDIR3resok structure, in bytes. The
     *            size must include all XDR overhead. The server is free to
     *            return less than count bytes of data.
     * @return the response
     * @throws IOException
     */
    NfsReaddirResponse readdir(long cookie, long cookieverf, int count) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIR in which the <code>cookie</code> was acquired.
     * @param count
     *            The maximum size of the READDIR3resok structure, in bytes. The
     *            size must include all XDR overhead. The server is free to
     *            return less than count bytes of data.
     * @param entries
     *            A list to which the incoming data should be appended.
     * @return the response
     * @throws IOException
     */
    NfsReaddirResponse readdir(long cookie, long cookieverf, int count, List<NfsDirectoryEntry> entries)
            throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIR in which the <code>cookie</code> was acquired.
     * @param count
     *            The maximum size of the READDIR3resok structure, in bytes. The
     *            size must include all XDR overhead. The server is free to
     *            return less than count bytes of data.
     * @return the request
     * @throws IOException
     */
    NfsReaddirRequest makeReaddirRequest(long cookie, long cookieverf, int count) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure READDIRPLUS retrieves a variable number of entries from a file
     * system directory and returns complete information about each along with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIRPLUS. READDIRPLUS differs from READDIR only in the
     * amount of information returned for each entry. In READDIR, each entry
     * returns the filename and the fileid. In READDIRPLUS, each entry returns
     * the name, the fileid, attributes (including the fileid), and file handle.
     * </p>
     * 
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIRPLUS in which the <code>cookie</code> was acquired.
     * @param dircount
     *            The maximum number of bytes of directory information returned.
     *            This number should not include the size of the attributes and
     *            file handle portions of the result.
     * @param maxcount
     *            The maximum size of the READDIRPLUS3resok structure, in bytes.
     *            The size must include all XDR overhead. The server is free to
     *            return less than <code>maxcount</code> bytes of data.
     * @return the response
     * @throws IOException
     */
    NfsReaddirplusResponse readdirplus(long cookie, long cookieverf, int dircount, int maxcount) throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure READDIRPLUS retrieves a variable number of entries from a file
     * system directory and returns complete information about each along with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIRPLUS. READDIRPLUS differs from READDIR only in the
     * amount of information returned for each entry. In READDIR, each entry
     * returns the filename and the fileid. In READDIRPLUS, each entry returns
     * the name, the fileid, attributes (including the fileid), and file handle.
     * </p>
     * 
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIRPLUS in which the <code>cookie</code> was acquired.
     * @param dircount
     *            The maximum number of bytes of directory information returned.
     *            This number should not include the size of the attributes and
     *            file handle portions of the result.
     * @param maxcount
     *            The maximum size of the READDIRPLUS3resok structure, in bytes.
     *            The size must include all XDR overhead. The server is free to
     *            return less than <code>maxcount</code> bytes of data.
     * @param entries
     *            A list to which the incoming data should be appended.
     * @return the response
     * @throws IOException
     */
    NfsReaddirplusResponse readdirplus(long cookie, long cookieverf, int dircount, int maxcount,
            List<NfsDirectoryPlusEntry> entries) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIRPLUS retrieves a variable number of entries from a file
     * system directory and returns complete information about each along with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIRPLUS. READDIRPLUS differs from READDIR only in the
     * amount of information returned for each entry. In READDIR, each entry
     * returns the filename and the fileid. In READDIRPLUS, each entry returns
     * the name, the fileid, attributes (including the fileid), and file handle.
     * </p>
     * 
     * @param cookie
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookie</code> as returned by the server.
     * @param cookieverf
     *            This should be set to 0 in the first request to read the
     *            directory. On subsequent requests, it should be a
     *            <code>cookieverf</code> as returned by the server. The
     *            <code>cookieverf</code> must match that returned by the
     *            READDIRPLUS in which the <code>cookie</code> was acquired.
     * @param dircount
     *            The maximum number of bytes of directory information returned.
     *            This number should not include the size of the attributes and
     *            file handle portions of the result.
     * @param maxcount
     *            The maximum size of the READDIRPLUS3resok structure, in bytes.
     *            The size must include all XDR overhead. The server is free to
     *            return less than <code>maxcount</code> bytes of data.
     * @return the request
     * @throws IOException
     */
    NfsReaddirplusRequest makeReaddirplusRequest(long cookie, long cookieverf, int dircount, int maxcount)
            throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure FSSTAT retrieves volatile file system state information.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsFsStatResponse fsstat() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSSTAT retrieves volatile file system state information.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsFsStatRequest makeFsStatRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsFsInfoResponse fsinfo() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsFsInfoRequest makeFsInfoRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure PATHCONF retrieves the pathconf information for a file or
     * directory. If the FSF_HOMOGENEOUS bit is set in FSFINFO3resok.properties,
     * the pathconf information will be the same for all files and directories
     * in the exported file system in which this file or directory resides.
     * </p>
     * 
     * @return the response
     * @throws IOException
     */
    NfsPathconfResponse pathconf() throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure PATHCONF retrieves the pathconf information for a file or
     * directory. If the FSF_HOMOGENEOUS bit is set in FSFINFO3resok.properties,
     * the pathconf information will be the same for all files and directories
     * in the exported file system in which this file or directory resides.
     * </p>
     * 
     * @return the request
     * @throws IOException
     */
    NfsPathconfRequest makePathconfRequest() throws IOException;

    /**
     * Makes the RPC call for this file, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This call is wrapped as provided
     * by the Nfs client to include repeated attempts, error checking, and
     * logging.
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param offsetToCommit
     *            The position within the file at which the flush is to begin.
     *            An offset of 0 means to flush data starting at the beginning
     *            of the file.
     * @param dataSizeToCommit
     *            The number of bytes of data to flush. If count is 0, a flush
     *            from offset to the end of file is done.
     * @return the response
     * @throws IOException
     */
    NfsCommitResponse commit(long offsetToCommit, int dataSizeToCommit) throws IOException;

    /**
     * Convenience method for creating the request for this file, as specified
     * by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param offsetToCommit
     *            The position within the file at which the flush is to begin.
     *            An offset of 0 means to flush data starting at the beginning
     *            of the file.
     * @param dataSizeToCommit
     *            The number of bytes of data to flush. If count is 0, a flush
     *            from offset to the end of file is done.
     * @return the request
     * @throws IOException
     */
    NfsCommitRequest makeCommitRequest(long offsetToCommit, int dataSizeToCommit) throws IOException;

}
