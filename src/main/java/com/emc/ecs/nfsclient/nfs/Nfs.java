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

import com.emc.ecs.nfsclient.nfs.io.NfsFile;
import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.Xdr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Client for an NFS RPC server, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public interface Nfs<F extends NfsFile<?, ?>> {

    /**
     * Program number for Nfs calls
     */
    static final int RPC_PROGRAM = 100003;

    /**
     * Program number for Mount calls
     */
    public final static int MOUNTPROG = 100005;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_NULL = 0;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_GETATTR = 1;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_SETATTR = 2;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_LOOKUP = 3;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_ACCESS = 4;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_READLINK = 5;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_READ = 6;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_WRITE = 7;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_CREATE = 8;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_MKDIR = 9;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_SYMLINK = 10;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_MKNOD = 11;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_REMOVE = 12;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_RMDIR = 13;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_RENAME = 14;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_LINK = 15;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_READDIR = 16;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_READDIRPLUS = 17;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_FSSTAT = 18;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_FSINFO = 19;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_PATHCONF = 20;

    /**
     * NFS version 3 procedure number. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    final static int NFSPROC3_COMMIT = 21;

    /**
     * Read data from file or read a directory. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    static final long ACCESS3_READ = 0x0001;

    /**
     * Look up a name in a directory (no meaning for non-directory objects).
     * Specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     */
    static final long ACCESS3_LOOKUP = 0x0002;

    /**
     * Rewrite existing file data or modify existing directory entries.
     * Specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
     */
    static final long ACCESS3_MODIFY = 0x0004;

    /**
     * Write new data or add directory entries. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    static final long ACCESS3_EXTEND = 0x0008;

    /**
     * Delete an existing directory entry. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    static final long ACCESS3_DELETE = 0x0010;

    /**
     * Execute file (no meaning for a directory). Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    static final long ACCESS3_EXECUTE = 0x0020;

    // Basic API calls for managing the client and NFS connection

    /**
     * Return to the default Credential.
     * 
     * @throws IOException
     */
    void disableSudo() throws IOException;

    /**
     * Use a custom Credential.
     * 
     * @param uid
     *            Unix uid
     * @param gid
     *            Unix gid
     */
    void enableSudo(int uid, int gid);

    /**
     * @return The Credential used for RPC calls.
     */
    Credential getCredential();

    /**
     * @return The path on the filesystem that is exported by the NFS server.
     */
    String getExportedPath();

    /**
     * @return The root file handle of the exported filesystem, for debugging
     *         and naked API calls
     */
    byte[] getRootFileHandle();

    /**
     * @return The port being used for the NFS server
     */
    int getPort();

    /**
     * @return The address of the machine hosting the NFS server.
     */
    String getServer();

    /**
     * Convenience method to create an nfs file from this client and the path.
     * 
     * @param path
     *            The file's path from the mount point.
     * @return the nfs file object
     * @throws IOException 
     */
    F newFile(String path) throws IOException;

    // RFC 1813 implementation

    /**
     * Procedure NULL does not do any work. It is made available to allow server
     * response testing and timing. For details, see RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * @return the returned Xdr
     * @throws IOException
     */
    Xdr nullCall() throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     * 
     * @param fileHandle
     *            The file handle of an object whose attributes are to be
     *            retrieved.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsGetAttrRequest makeGetAttrRequest(byte[] fileHandle) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsGetAttrResponse getAttr(NfsGetAttrRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure GETATTR retrieves the attributes for a specified file system
     * object. The object is identified by the file handle that the server
     * returned as part of the response from a LOOKUP, CREATE, MKDIR, SYMLINK,
     * MKNOD, or READDIRPLUS procedure (or from the MOUNT service, described
     * elsewhere).
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsGetAttrResponse wrapped_getAttr(NfsGetAttrRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SETATTR changes one or more of the attributes of a file system
     * object on the server.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the object.
     * @param attributes
     *            The attributes to set.
     * @param guardTime
     *            If <code>guardTime</code> is not null, the server must compare
     *            the value of <code>guardTime</code> to the current ctime of
     *            the object. If the values are different, the server must
     *            preserve the object attributes and must return a status of
     *            NFS3ERR_NOT_SYNC.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsSetAttrRequest makeSetAttrRequest(byte[] fileHandle, NfsSetAttributes attributes, NfsTime guardTime)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SETATTR changes one or more of the attributes of a file system
     * object on the server.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsSetAttrResponse setAttr(NfsSetAttrRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SETATTR changes one or more of the attributes of a file system
     * object on the server.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsSetAttrResponse wrapped_setAttr(NfsSetAttrRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory to search.
     * @param name
     *            The filename to be searched for.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsLookupRequest makeLookupRequest(byte[] fileHandle, String name) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsLookupResponse getLookup(NfsLookupRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LOOKUP searches a directory for a specific name and returns the
     * file handle for the corresponding file system object.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsLookupResponse wrapped_getLookup(NfsLookupRequest request) throws IOException;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param fileHandle
     *            The file handle for the file system object to which access is
     *            to be checked.
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
     * @throws FileNotFoundException
     */
    NfsAccessRequest makeAccessRequest(byte[] fileHandle, long accessToCheck) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsAccessResponse getAccess(NfsAccessRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsAccessResponse wrapped_getAccess(NfsAccessRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param fileHandle
     *            the fileHandle for the symbolic link
     * @return the request
     * @throws FileNotFoundException
     */
    NfsReadlinkRequest makeReadlinkRequest(byte[] fileHandle) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsReadlinkResponse getReadlink(NfsReadlinkRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsReadlinkResponse wrapped_getReadlink(NfsReadlinkRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READ reads data from a file.
     * </p>
     * 
     * @param fileHandle
     *            The file handle of the file from which data is to be read.
     *            This must identify a file system object of type, NF3REG.
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
     * @throws FileNotFoundException
     */
    NfsReadRequest makeReadRequest(byte[] fileHandle, long offset, int size) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READ reads data from a file.
     * </p>
     * 
     * @param request
     * @param bytes
     *            optional externally provided buffer - if not provided, the
     *            method will create a new, empty buffer for receiving the data.
     * @param position
     *            position to start writing, defaults to 0 if buffer is null
     * @return the response
     * @throws IOException
     */
    NfsReadResponse getRead(NfsReadRequest request, byte[] bytes, int position) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READ reads data from a file.
     * </p>
     * 
     * @param request
     * @param bytes
     *            optional externally provided buffer - if not provided, the
     *            method will create a new, empty buffer for receiving the data.
     * @param position
     *            position to start writing, defaults to 0 if buffer is null
     * @return the response
     * @throws IOException
     */
    NfsReadResponse wrapped_getRead(NfsReadRequest request, byte[] bytes, int position) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the file to which data is to be written.
     *            This must identify a file system object of type, NF3REG.
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
     * @throws FileNotFoundException
     */
    NfsWriteRequest makeWriteRequest(byte[] fileHandle, long offset, List<ByteBuffer> payload, int syncType)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsWriteResponse sendWrite(NfsWriteRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsWriteResponse wrapped_sendWrite(NfsWriteRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param request
     * @param verifier
     *            This is a cookie, previously returned from the server, that
     *            the client can use to determine whether the server has changed
     *            state between a call to WRITE and a subsequent call to either
     *            WRITE or COMMIT.
     * @return the response
     * @throws IOException
     */
    NfsWriteResponse wrapped_sendWrite(NfsWriteRequest request, Long verifier) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param parentDirectoryFileHandle
     *            The file handle for the directory in which the file is to be
     *            created.
     * @param name
     *            The name that is to be associated with the created file.
     * @param attributes
     *            the initial attributes for the new file.
     * @param verifier
     *            A unique identifier to ensure exclusive creation of the new
     *            file. If no value is provided for exclusive creation, this is
     *            generated.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsCreateRequest makeCreateRequest(NfsCreateMode createMode, byte[] parentDirectoryFileHandle, String name,
            NfsSetAttributes attributes, byte[] verifier) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure CREATE creates a regular file.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsCreateResponse sendCreate(NfsCreateRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure CREATE creates a regular file.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsCreateResponse wrapped_sendCreate(NfsCreateRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKDIR creates a new subdirectory.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory in which the subdirectory is
     *            to be created.
     * @param name
     *            The name that is to be associated with the created
     *            subdirectory.
     * @param attributes
     *            The initial attributes for the subdirectory.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsMkdirRequest makeMkdirRequest(byte[] fileHandle, String name, NfsSetAttributes attributes)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKDIR creates a new subdirectory.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsMkdirResponse sendMkdir(NfsMkdirRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKDIR creates a new subdirectory.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsMkdirResponse wrapped_sendMkdir(NfsMkdirRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SYMLINK creates a new symbolic link.
     * </p>
     * 
     * @param symbolicLinkData
     *            The string containing the symbolic link data.
     * @param fileHandle
     *            The file handle for the directory in which the symbolic link
     *            is to be created.
     * @param name
     *            The name that is to be associated with the created symbolic
     *            link.
     * @param attributes
     *            The initial attributes for the symbolic link.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsSymlinkRequest makeSymlinkRequest(String symbolicLinkData, byte[] fileHandle, String name,
            NfsSetAttributes attributes) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SYMLINK creates a new symbolic link.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsSymlinkResponse sendSymlink(NfsSymlinkRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure SYMLINK creates a new symbolic link.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsSymlinkResponse wrapped_sendSymlink(NfsSymlinkRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory in which the special file is
     *            to be created.
     * @param name
     *            The name that is to be associated with the created special
     *            file.
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
     * @throws FileNotFoundException
     */
    NfsMknodRequest makeMknodRequest(byte[] fileHandle, String name, NfsType type, NfsSetAttributes attributes,
            long[] rdev) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsMknodResponse sendMknod(NfsMknodRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure MKNOD creates a new special file of the type, <code>type</code>
     * . Special files can be device files or named pipes.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsMknodResponse wrapped_sendMknod(NfsMknodRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure REMOVE removes (deletes) an entry from a directory. If the
     * entry in the directory was the last reference to the corresponding file
     * system object, the object may be destroyed.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory from which the entry is to
     *            be removed.
     * @param name
     *            The name of the entry to be removed.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsRemoveRequest makeRemoveRequest(byte[] fileHandle, String name) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure REMOVE removes (deletes) an entry from a directory. If the
     * entry in the directory was the last reference to the corresponding file
     * system object, the object may be destroyed.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsRemoveResponse sendRemove(NfsRemoveRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure REMOVE removes (deletes) an entry from a directory. If the
     * entry in the directory was the last reference to the corresponding file
     * system object, the object may be destroyed.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsRemoveResponse wrapped_sendRemove(NfsRemoveRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
     * directory entry of the subdirectory is the last reference to the
     * subdirectory, the subdirectory may be destroyed.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory from which the subdirectory
     *            is to be removed.
     * @param name
     *            The name of the subdirectory to be removed.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsRmdirRequest makeRmdirRequest(byte[] fileHandle, String name) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
     * directory entry of the subdirectory is the last reference to the
     * subdirectory, the subdirectory may be destroyed.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsRmdirResponse sendRmdir(NfsRmdirRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RMDIR removes (deletes) a subdirectory from a directory. If the
     * directory entry of the subdirectory is the last reference to the
     * subdirectory, the subdirectory may be destroyed.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsRmdirResponse wrapped_sendRmdir(NfsRmdirRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RENAME renames the file identified by <code>name</code> in the
     * directory, <code>fileHandle</code>, to <code>toName</code> in the
     * directory, <code>toFileHandle</code>. The operation is required to be
     * atomic to the client. <code>toFileHandle</code> and
     * <code>fileHandle</code> must reside on the same file system and server.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory from which the entry is to
     *            be renamed
     * @param name
     *            The name of the entry that identifies the object to be renamed
     * @param toFileHandle
     *            The file handle for the directory to which the object is to be
     *            renamed.
     * @param toName
     *            The new name for the object.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsRenameRequest makeRenameRequest(byte[] fileHandle, String name, byte[] toFileHandle, String toName)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RENAME renames the file identified by <code>name</code> in the
     * directory, <code>fileHandle</code>, to <code>toName</code> in the
     * directory, <code>toFileHandle</code>. The operation is required to be
     * atomic to the client. <code>toFileHandle</code> and
     * <code>fileHandle</code> must reside on the same file system and server.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsRenameResponse sendRename(NfsRenameRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure RENAME renames the file identified by <code>name</code> in the
     * directory, <code>fileHandle</code>, to <code>toName</code> in the
     * directory, <code>toFileHandle</code>. The operation is required to be
     * atomic to the client. <code>toFileHandle</code> and
     * <code>fileHandle</code> must reside on the same file system and server.
     * </p>
     *
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsRenameResponse wrapped_sendRename(NfsRenameRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>fileHandle</code> to
     * <code>name</code>, in the directory,
     * <code>parentDirectoryFileHandle</code>. <code>fileHandle</code> and
     * <code>parentDirectoryFileHandle</code> must reside on the same file
     * system and server.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the existing file system object.
     * @param parentDirectoryFileHandle
     *            The file handle for the directory in which the link is to be
     *            created.
     * @param name
     *            The name that is to be associated with the created link.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsLinkRequest makeLinkRequest(byte[] fileHandle, byte[] parentDirectoryFileHandle, String name)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>fileHandle</code> to
     * <code>name</code>, in the directory,
     * <code>parentDirectoryFileHandle</code>. <code>fileHandle</code> and
     * <code>parentDirectoryFileHandle</code> must reside on the same file
     * system and server.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsLinkResponse sendLink(NfsLinkRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure LINK creates a hard link from <code>fileHandle</code> to
     * <code>name</code>, in the directory,
     * <code>parentDirectoryFileHandle</code>. <code>fileHandle</code> and
     * <code>parentDirectoryFileHandle</code> must reside on the same file
     * system and server.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsLinkResponse wrapped_sendLink(NfsLinkRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the directory to be read.
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
     * @throws FileNotFoundException
     */
    NfsReaddirRequest makeReaddirRequest(byte[] fileHandle, long cookie, long cookieverf, int count)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsReaddirResponse getReaddir(NfsReaddirRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsReaddirResponse wrapped_getReaddir(NfsReaddirRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure READDIR retrieves a variable number of entries, in sequence,
     * from a directory and returns the name and file identifier for each, with
     * information to allow the client to request additional directory entries
     * in a subsequent READDIR request.
     * </p>
     * 
     * @param request
     * @param entries
     *            A list of entries to which the returned entries should be
     *            appended.
     * @return the response
     */
    NfsReaddirResponse wrapped_getReaddir(NfsReaddirRequest request, List<NfsDirectoryEntry> entries)
            throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param fileHandle
     *            The file handle for the directory to be read.
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
     * @throws FileNotFoundException
     */
    NfsReaddirplusRequest makeReaddirplusRequest(byte[] fileHandle, long cookie, long cookieverf, int dircount,
            int maxcount) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsReaddirplusResponse getReaddirplus(NfsReaddirplusRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsReaddirplusResponse wrapped_getReaddirplus(NfsReaddirplusRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
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
     * @param request
     * @param entries
     *            A list of entries to which the returned entries should be
     *            appended.
     * @return the response
     */
    NfsReaddirplusResponse wrapped_getReaddirplus(NfsReaddirplusRequest request, List<NfsDirectoryPlusEntry> entries)
            throws IOException;

    /**
     * Procedure FSSTAT retrieves volatile file system state information, as
     * specified by RFC 1813 (https://tools.ietf.org/html/rfc1813), using the
     * root handle for the exported filesystem.
     * 
     * @return the data
     * @throws IOException
     */
    NfsFsStat getNfsFsStat() throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSSTAT retrieves volatile file system state information.
     * </p>
     * 
     * @param fileHandle
     *            A file handle identifying a object in the file system. This is
     *            normally a file handle for a mount point for a file system, as
     *            originally obtained from the MOUNT service on the server.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsFsStatRequest makeFsStatRequest(byte[] fileHandle) throws FileNotFoundException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This method uses the root file
     * handle for this client.
     * 
     * <p>
     * Procedure FSSTAT retrieves volatile file system state information.
     * </p>
     * 
     * @return the request
     * @throws FileNotFoundException
     */
    NfsFsStatRequest makeFsStatRequest() throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSSTAT retrieves volatile file system state information.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsFsStatResponse getFsStat(NfsFsStatRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSSTAT retrieves volatile file system state information.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsFsStatResponse wrapped_getFsStat(NfsFsStatRequest request) throws IOException;

    /**
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813), using the root handle for the
     * exported filesystem.
     * 
     * @return the data
     * @throws IOException
     */
    NfsFsInfo getNfsFsInfo() throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @param fileHandle
     *            A file handle identifying a file object. Normal usage is to
     *            provide a file handle for a mount point for a file system, as
     *            originally obtained from the MOUNT service on the server.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsFsInfoRequest makeFsInfoRequest(byte[] fileHandle) throws FileNotFoundException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813). This method uses the root file
     * handle for this client.
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @return the request
     * @throws FileNotFoundException
     */
    NfsFsInfoRequest makeFsInfoRequest() throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsFsInfoResponse getFsInfo(NfsFsInfoRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure FSINFO retrieves nonvolatile file system state information and
     * general information about the NFS version 3 protocol server
     * implementation.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsFsInfoResponse wrapped_getFsInfo(NfsFsInfoRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure PATHCONF retrieves the pathconf information for a file or
     * directory. If the FSF_HOMOGENEOUS bit is set in FSFINFO3resok.properties,
     * the pathconf information will be the same for all files and directories
     * in the exported file system in which this file or directory resides.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the file system object.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsPathconfRequest makePathconfRequest(byte[] fileHandle) throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure PATHCONF retrieves the pathconf information for a file or
     * directory. If the FSF_HOMOGENEOUS bit is set in FSFINFO3resok.properties,
     * the pathconf information will be the same for all files and directories
     * in the exported file system in which this file or directory resides.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsPathconfResponse getPathconf(NfsPathconfRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure PATHCONF retrieves the pathconf information for a file or
     * directory. If the FSF_HOMOGENEOUS bit is set in FSFINFO3resok.properties,
     * the pathconf information will be the same for all files and directories
     * in the exported file system in which this file or directory resides.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsPathconfResponse wrapped_getPathconf(NfsPathconfRequest request) throws IOException;

    /**
     * Convenience method for creating the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the file to which data is to be flushed
     *            (committed). This must identify a file system object of type,
     *            NF3REG.
     * @param offsetToCommit
     *            The position within the file at which the flush is to begin.
     *            An offset of 0 means to flush data starting at the beginning
     *            of the file.
     * @param dataSizeToCommit
     *            The number of bytes of data to flush. If count is 0, a flush
     *            from offset to the end of file is done.
     * @return the request
     * @throws FileNotFoundException
     */
    NfsCommitRequest makeCommitRequest(byte[] fileHandle, long offsetToCommit, int dataSizeToCommit)
            throws FileNotFoundException;

    /**
     * Bare implementation of the NFS RPC call. The details are as specified by
     * RFC 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsCommitResponse sendCommit(NfsCommitRequest request) throws IOException;

    /**
     * Implementation of the NFS RPC call, wrapped to include repeated attempts,
     * error checking, and logging. The details are as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param request
     * @return the response
     * @throws IOException
     */
    NfsCommitResponse wrapped_sendCommit(NfsCommitRequest request) throws IOException;

}
