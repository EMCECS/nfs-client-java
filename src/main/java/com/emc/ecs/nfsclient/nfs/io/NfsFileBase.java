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
package com.emc.ecs.nfsclient.nfs.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.emc.ecs.nfsclient.nfs.*;

/**
 * Basic implementation of NfsFile. Subclasses need only implement constructors
 * and newChildFile().
 * 
 * @author seibed
 */
public abstract class NfsFileBase<N extends Nfs<F>, F extends NfsFile<N, F>> implements NfsFile<N, F> {

    /**
     * The full network path to the file.
     */
    private String _absolutePath;

    /**
     * is it?
     */
    private boolean _isRootFile = false;

    /**
     * file handle for NFS calls
     */
    private byte[] _fileHandle;

    /**
     * The short name of the file, starting from the parent path.
     */
    private String _name;

    /**
     * The supporting NFS client.
     */
    private final N _nfs;

    /**
     * The full parent path of the file, starting with the mount point.
     */
    private String _parent;

    /**
     * The parent file, stored to reduce lookup overhead
     */
    private F _parentFile;

    /**
     * The full path of the file, starting with the mount point.
     */
    private String _path;

    /**
     * The basic constructor.
     * 
     * @param nfs
     *            The supporting NFS client.
     * @param path
     *            The full path of the file, starting with the mount point.
     */
    public NfsFileBase(N nfs, String path) {
        if (nfs == null) {
            throw new IllegalArgumentException("Nfs instance can not be null");
        }
        _nfs = nfs;

        setPath(path);
    }

    /**
     * The most efficient constructor if the parent file already exists.
     * 
     * @param parent
     *            The parent file, stored to reduce lookup overhead
     * @param child
     *            The short name of the file, starting from the parent path.
     */
    public NfsFileBase(F parent, String child) {
        this(parent.getNfs(), makeChildPath(parent.getPath(), child));
        setParentFile(parent);
    }

    /**
     * @param path
     * @return Everything up to and including the first separator character
     *         before the final name.
     */
    public static String makeParentPath(String path) {
        int firstParentSeparatorIndex = getParentSeparatorIndex(path);
        while ((firstParentSeparatorIndex > 0) && (separatorChar == path.charAt(firstParentSeparatorIndex))) {
            --firstParentSeparatorIndex;
        }
        int lengthOfParentName = (firstParentSeparatorIndex == 0) ? 1 : (firstParentSeparatorIndex + 2);
        return path.substring(0, lengthOfParentName);
    }

    /**
     * @param path
     * @return Everything after the last separator character.
     */
    public static String makeName(String path) {
        int nameStartIndex = getParentSeparatorIndex(path) + 1;
        int endOfNameIndex = path.indexOf(separatorChar, nameStartIndex);
        if (endOfNameIndex < 0) {
            endOfNameIndex = path.length();
        }
        return path.substring(nameStartIndex, endOfNameIndex);
    }

    /**
     * @param parent
     * @param child
     * @return the full child path
     */
    public static String makeChildPath(String parent, String child) {
        StringBuilder stringBuilder = new StringBuilder(parent.replaceAll("/+", separator));
        if (!parent.endsWith(separator)) {
            stringBuilder.append(separatorChar);
        }
        return stringBuilder.append(child).toString();
    }

    /**
     * @param path
     * @return the separator index
     */
    private static int getParentSeparatorIndex(String path) {
        int firstTerminalSeparatorIndex = path.length() - 1;
        while ((firstTerminalSeparatorIndex > 0) && (separatorChar == path.charAt(firstTerminalSeparatorIndex))) {
            --firstTerminalSeparatorIndex;
        }
        return path.lastIndexOf(separatorChar, firstTerminalSeparatorIndex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#canDelete()
     */
    public boolean canDelete() throws IOException {
        return canAccess(Nfs.ACCESS3_DELETE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#canExecute()
     */
    public boolean canExecute() throws IOException {
        return canAccess(Nfs.ACCESS3_EXECUTE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#canExtend()
     */
    public boolean canExtend() throws IOException {
        return canAccess(Nfs.ACCESS3_EXTEND);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#canLookup()
     */
    public boolean canLookup() throws IOException {
        return canAccess(Nfs.ACCESS3_LOOKUP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#canModify()
     */
    public boolean canModify() throws IOException {
        return canAccess(Nfs.ACCESS3_MODIFY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#canRead()
     */
    public boolean canRead() throws IOException {
        return canAccess(Nfs.ACCESS3_READ);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public final int compareTo(F o) {
        if (o == null) {
            return 1;
        }

        return getAbsolutePath().compareTo(o.getAbsolutePath());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#createNewFile()
     */
    public boolean createNewFile() throws IOException {
        try {
            create(NfsCreateMode.GUARDED, new NfsSetAttributes(), null);
        } catch (NfsException e) {
            if (e.getStatus() == NfsStatus.NFS3ERR_EXIST) {
                return false;
            }
            throw e;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#delete()
     */
    public void delete() throws IOException {
        if (isDirectory()) {
            rmdir();
        } else {
            remove();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(Object obj) {
        if (!(obj instanceof NfsFile)) {
            return false;
        }
        return getAbsolutePath().equals(((NfsFile<?, ?>) obj).getAbsolutePath());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#exists()
     */
    public boolean exists() throws IOException {
        boolean exists = false;
        try {
            // force lookup again
            setFileHandle(null);
            exists = (getFileHandle() != null);
        } catch (FileNotFoundException e) {
            // do nothing
        }
        return exists;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getAbsolutePath()
     */
    public final String getAbsolutePath() {
        return _absolutePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#hasAccess(long)
     */
    public long getAccess(long accessToCheck) throws IOException {
        return access(accessToCheck).getAccess();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getAttributes()
     */
    public NfsGetAttributes getAttributes() throws IOException {
        return getNfs().wrapped_getAttr(getNfs().makeGetAttrRequest(getFileHandle())).getAttributes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getChildFile(java.lang.String)
     */
    public F getChildFile(String childName) {
        return newChildFile(childName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getFileHandle()
     */
    public byte[] getFileHandle() throws IOException {
        if (_isRootFile) {
            return getNfs().getRootFileHandle();
        }
        if (_fileHandle == null) {
            setFileHandle();
        }
        return (_fileHandle == null) ? null : _fileHandle.clone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#getFreeSpace()
     */
    public long getFreeSpace() throws IOException {
        return fsstat().getFsStat().fbytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#getMode()
     */
    public long getMode() throws IOException {
        return getAttributes().getMode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getName()
     */
    public final String getName() {
        return _name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getNfs()
     */
    public final N getNfs() {
        return _nfs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getParent()
     */
    public final String getParent() {
        return _parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getParentFile()
     */
    public F getParentFile() {
        if (_parentFile == null) {
            _parentFile = getNfs().newFile(getParent());
        }
        return _parentFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#getPath()
     */
    public final String getPath() {
        return _path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#getTotalSpace()
     */
    public long getTotalSpace() throws IOException {
        return fsstat().getFsStat().tbytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#getUsableSpace()
     */
    public long getUsableSpace() throws IOException {
        return fsstat().getFsStat().bytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
        return getAbsolutePath().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#isDirectory()
     */
    public boolean isDirectory() throws IOException {
        return getAttributes().getType() == NfsType.NFS_DIR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#isFile()
     */
    public boolean isFile() throws IOException {
        return getAttributes().getType() == NfsType.NFS_REG;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#lastModified()
     */
    public long lastModified() throws IOException {
        return getAttributes().getMtime().getTimeInMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#length()
     */
    public long length() {
        try {
            return lengthEx();
        } catch (IOException e) {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#lengthEx()
     */
    public long lengthEx() throws IOException {
        return getAttributes().getSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#list()
     */
    public List<String> list() throws IOException {
        boolean eof = false;
        long cookie = 0;
        long cookieverf = 0;
        final int count = 8 * 1024;
        List<NfsDirectoryEntry> entries = new ArrayList<NfsDirectoryEntry>(32);
        do {
            NfsReaddirResponse response = readdir(cookie, cookieverf, count, entries);
            eof = response.isEof();
            cookie = response.getCookie();
            cookieverf = response.getCookieverf();
        } while (!eof);
        List<String> children = new ArrayList<String>();
        for (NfsDirectoryEntry entry : entries) {
            if (!(".".equals(entry.getFileName()) || "..".equals(entry.getFileName()))) {
                children.add(entry.getFileName());
            }
        }
        return children;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#list(com.emc.ecs.nfsclient.util.
     * NfsFilenameFilter)
     */
    public List<String> list(NfsFilenameFilter filter) throws IOException {
        List<String> children = list();
        if ((children == null) || (children.size() == 0) || (filter == null)) {
            return children;
        }
        List<String> filteredChildren = new ArrayList<String>();
        for (String child : children) {
            if (filter.accept(this, child)) {
                filteredChildren.add(child);
            }
        }
        return filteredChildren;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#listFiles()
     */
    public List<F> listFiles() throws IOException {
        return getChildFiles(list());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.util.NfsFile#listFiles(com.emc.ecs.nfsclient.util.
     * NfsFilenameFilter)
     */
    public List<F> listFiles(NfsFilenameFilter filter) throws IOException {
        return getChildFiles(list(filter));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.util.NfsFile#listFiles(com.emc.ecs.nfsclient.util.
     * NfsFileFilter)
     */
    public List<F> listFiles(NfsFileFilter filter) throws IOException {
        List<F> childFiles = listFiles();
        if ((childFiles == null) || (childFiles.size() == 0) || (filter == null)) {
            return childFiles;
        }
        List<F> filteredFiles = new ArrayList<F>();
        for (F childFile : childFiles) {
            if (filter.accept(childFile)) {
                filteredFiles.add(childFile);
            }
        }
        return filteredFiles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#mkdir()
     */
    public void mkdir() throws IOException {
        mkdir(new NfsSetAttributes());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#mkdirs()
     */
    public void mkdirs() throws IOException {
        if ((!getParent().equals(separator)) && (!getParentFile().exists())) {
            getParentFile().mkdirs();
        }
        mkdir();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#renameTo(com.emc.ecs.nfsclient.nfs.
     * NfsFile)
     */
    public boolean renameTo(F destination) throws IOException {
        return rename(destination).stateIsOk();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#setLastModified(long)
     */
    public void setLastModified(long millis) throws IOException {
        setAttributes(new NfsSetAttributes(null, null, null, NfsTime.DO_NOT_CHANGE, new NfsTime(millis)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.io.NfsFile#setAttributes(com.emc.ecs.nfsclient.
     * nfs.NfsSetAttributes)
     */
    public void setAttributes(NfsSetAttributes nfsSetAttr) throws IOException {
        setattr(nfsSetAttr, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.util.NfsFile#changeMode(long)
     */
    public void setMode(long mode) throws IOException {
        setAttributes(
                new NfsSetAttributes(Long.valueOf(mode), null, null, NfsTime.DO_NOT_CHANGE, NfsTime.DO_NOT_CHANGE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#readlink()
     */
    public NfsGetAttrResponse getattr() throws IOException {
        return getNfs().wrapped_getAttr(makeGetAttrRequest());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeGetAttrRequest()
     */
    public NfsGetAttrRequest makeGetAttrRequest() throws IOException {
        return getNfs().makeGetAttrRequest(getFileHandle());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#setattr(com.emc.ecs.nfsclient.nfs.
     * NfsSetAttributes, com.emc.ecs.nfsclient.nfs.NfsTime)
     */
    public NfsSetAttrResponse setattr(NfsSetAttributes attributes, NfsTime guardTime) throws IOException {
        return getNfs().wrapped_setAttr(makeSetAttrRequest(attributes, guardTime));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeSetAttrRequest(com.emc.ecs.
     * nfsclient.nfs.NfsSetAttributes, com.emc.ecs.nfsclient.nfs.NfsTime)
     */
    public NfsSetAttrRequest makeSetAttrRequest(NfsSetAttributes attributes, NfsTime guardTime) throws IOException {
        return getNfs().makeSetAttrRequest(getFileHandle(), attributes, guardTime);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#lookup()
     */
    public NfsLookupResponse lookup() throws IOException {
        return getNfs().wrapped_getLookup(makeLookupRequest());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeLookupRequest()
     */
    public NfsLookupRequest makeLookupRequest() throws IOException {
        return getNfs().makeLookupRequest(getParentFile().getFileHandle(), getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#access(long)
     */
    public NfsAccessResponse access(long accessToCheck) throws IOException {
        return getNfs().wrapped_getAccess(makeAccessRequest(accessToCheck));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeAccessRequest(long)
     */
    public NfsAccessRequest makeAccessRequest(long accessToCheck) throws IOException {
        return getNfs().makeAccessRequest(getFileHandle(), accessToCheck);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#readlink()
     */
    public NfsReadlinkResponse readlink() throws IOException {
        return getNfs().wrapped_getReadlink(makeReadlinkRequest());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeReadlinkRequest()
     */
    public NfsReadlinkRequest makeReadlinkRequest() throws IOException {
        return getNfs().makeReadlinkRequest(getFileHandle());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#read(long, int, byte[], int)
     */
    public NfsReadResponse read(long offset, int size, byte[] bytes, int position) throws IOException {
        return getNfs().wrapped_getRead(makeReadRequest(offset, size), bytes, position);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeReadRequest(long, int)
     */
    public NfsReadRequest makeReadRequest(long offset, int size) throws IOException {
        return getNfs().makeReadRequest(getFileHandle(), offset, size);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#write(long, java.util.List, int)
     */
    public NfsWriteResponse write(long offset, List<ByteBuffer> payload, int syncType) throws IOException {
        return getNfs().wrapped_sendWrite(makeWriteRequest(offset, payload, syncType));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#write(long, java.util.List, int,
     * java.lang.Long)
     */
    public NfsWriteResponse write(long offset, List<ByteBuffer> payload, int syncType, Long verifier)
            throws IOException {
        return getNfs().wrapped_sendWrite(makeWriteRequest(offset, payload, syncType), verifier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeWriteRequest(long,
     * java.util.List, int)
     */
    public NfsWriteRequest makeWriteRequest(long offset, List<ByteBuffer> payload, int syncType) throws IOException {
        return getNfs().makeWriteRequest(getFileHandle(), offset, payload, syncType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#create(com.emc.ecs.nfsclient.nfs.
     * NfsCreateMode, com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public NfsCreateResponse create(NfsCreateMode createMode, NfsSetAttributes attributes, byte[] verifier)
            throws IOException {
        NfsCreateResponse response = getNfs().wrapped_sendCreate(getNfs().makeCreateRequest(createMode,
                getParentFile().getFileHandle(), getName(), attributes, verifier));
        setFileHandle(response.getFileHandle());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#makeCreateRequest(com.emc.ecs.nfsclient
     * .nfs.NfsCreateMode, com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public NfsCreateRequest makeCreateRequest(NfsCreateMode createMode, NfsSetAttributes attributes, byte[] verifier)
            throws IOException {
        return getNfs().makeCreateRequest(createMode, getParentFile().getFileHandle(), getName(), attributes, verifier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#mkdir(com.emc.ecs.nfsclient.nfs.
     * NfsSetAttributes)
     */
    public NfsMkdirResponse mkdir(NfsSetAttributes attributes) throws IOException {
        NfsMkdirResponse response = getNfs().wrapped_sendMkdir(makeMkdirRequest(attributes));
        setFileHandle(response.getFileHandle());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#makeMkdirRequest(com.emc.ecs.nfsclient.
     * nfs.NfsSetAttributes)
     */
    public NfsMkdirRequest makeMkdirRequest(NfsSetAttributes attributes) throws IOException {
        return getNfs().makeMkdirRequest(getParentFile().getFileHandle(), getName(), attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#symlink(java.lang.String,
     * com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public NfsSymlinkResponse symlink(String symbolicLinkData, NfsSetAttributes attributes) throws IOException {
        NfsSymlinkResponse response = getNfs().wrapped_sendSymlink(makeSymlinkRequest(symbolicLinkData, attributes));
        setFileHandle(response.getFileHandle());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#makeSymlinkRequest(java.lang.String,
     * com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public NfsSymlinkRequest makeSymlinkRequest(String symbolicLinkData, NfsSetAttributes attributes)
            throws IOException {
        return getNfs().makeSymlinkRequest(symbolicLinkData, getParentFile().getFileHandle(), getName(), attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#mknod(com.emc.ecs.nfsclient.nfs.
     * NfsType, com.emc.ecs.nfsclient.nfs.NfsSetAttributes, long[])
     */
    public NfsMknodResponse mknod(NfsType type, NfsSetAttributes attributes, long[] rdev) throws IOException {
        NfsMknodResponse response = getNfs().wrapped_sendMknod(makeMknodRequest(type, attributes, rdev));
        setFileHandle(response.getFileHandle());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#makeMknodRequest(com.emc.ecs.nfsclient.
     * nfs.NfsType, com.emc.ecs.nfsclient.nfs.NfsSetAttributes, long[])
     */
    public NfsMknodRequest makeMknodRequest(NfsType type, NfsSetAttributes attributes, long[] rdev) throws IOException {
        return getNfs().makeMknodRequest(getParentFile().getFileHandle(), getName(), type, attributes, rdev);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#remove()
     */
    public NfsRemoveResponse remove() throws IOException {
        NfsRemoveResponse response = getNfs().wrapped_sendRemove(makeRemoveRequest());
        setFileHandle(null);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeRemoveRequest()
     */
    public NfsRemoveRequest makeRemoveRequest() throws IOException {
        return getNfs().makeRemoveRequest(getParentFile().getFileHandle(), getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#rmdir()
     */
    public NfsRmdirResponse rmdir() throws IOException {
        NfsRmdirResponse response = getNfs().wrapped_sendRmdir(makeRmdirRequest());
        setFileHandle(null);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeRmdirRequest()
     */
    public NfsRmdirRequest makeRmdirRequest() throws IOException {
        return getNfs().makeRmdirRequest(getParentFile().getFileHandle(), getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#rename(com.emc.ecs.nfsclient.nfs.
     * NfsFile)
     */
    public NfsRenameResponse rename(F toFile) throws IOException {
        NfsRenameResponse response = getNfs().wrapped_sendRename(makeRenameRequest(toFile));
        if (response.stateIsOk()) {
            setPath(toFile.getPath());
            _parentFile = toFile.getParentFile();
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#makeRenameRequest(com.emc.ecs.nfsclient
     * .nfs.NfsFile)
     */
    public NfsRenameRequest makeRenameRequest(F toFile) throws IOException {
        return getNfs().makeRenameRequest(getParentFile().getFileHandle(), getName(),
                toFile.getParentFile().getFileHandle(), toFile.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#link(com.emc.ecs.nfsclient.nfs.NfsFile)
     */
    public NfsLinkResponse link(F source) throws IOException {
        NfsLinkResponse response = getNfs().wrapped_sendLink(makeLinkRequest(source));
        setFileHandle(source.getFileHandle());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsFile#makeLinkRequest(com.emc.ecs.nfsclient.
     * nfs.NfsFile)
     */
    public NfsLinkRequest makeLinkRequest(F source) throws IOException {
        return getNfs().makeLinkRequest(source.getFileHandle(), getParentFile().getFileHandle(), getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#readdir(long, long, int)
     */
    public NfsReaddirResponse readdir(long cookie, long cookieverf, int count) throws IOException {
        return getNfs().wrapped_getReaddir(makeReaddirRequest(cookie, cookieverf, count));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#readdir(long, long, int,
     * java.util.List)
     */
    public NfsReaddirResponse readdir(long cookie, long cookieverf, int count, List<NfsDirectoryEntry> entries)
            throws IOException {
        return getNfs().wrapped_getReaddir(makeReaddirRequest(cookie, cookieverf, count), entries);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeReaddirRequest(long, long,
     * int)
     */
    public NfsReaddirRequest makeReaddirRequest(long cookie, long cookieverf, int count) throws IOException {
        return getNfs().makeReaddirRequest(getFileHandle(), cookie, cookieverf, count);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#readdirplus(long, long, int, int)
     */
    public NfsReaddirplusResponse readdirplus(long cookie, long cookieverf, int dircount, int maxcount)
            throws IOException {
        return getNfs().wrapped_getReaddirplus(makeReaddirplusRequest(cookie, cookieverf, dircount, maxcount));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#readdirplus(long, long, int, int,
     * java.util.List)
     */
    public NfsReaddirplusResponse readdirplus(long cookie, long cookieverf, int dircount, int maxcount,
            List<NfsDirectoryPlusEntry> entries) throws IOException {
        return getNfs().wrapped_getReaddirplus(makeReaddirplusRequest(cookie, cookieverf, dircount, maxcount), entries);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeReaddirplusRequest(long, long,
     * int, int)
     */
    public NfsReaddirplusRequest makeReaddirplusRequest(long cookie, long cookieverf, int dircount, int maxcount)
            throws IOException {
        return getNfs().makeReaddirplusRequest(getFileHandle(), cookie, cookieverf, dircount, maxcount);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#fsstat()
     */
    public NfsFsStatResponse fsstat() throws IOException {
        return getNfs().wrapped_getFsStat(getNfs().makeFsStatRequest());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeFsStatRequest()
     */
    public NfsFsStatRequest makeFsStatRequest() throws IOException {
        return getNfs().makeFsStatRequest(getFileHandle());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#fsinfo()
     */
    public NfsFsInfoResponse fsinfo() throws IOException {
        return getNfs().wrapped_getFsInfo(getNfs().makeFsInfoRequest());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeFsInfoRequest()
     */
    public NfsFsInfoRequest makeFsInfoRequest() throws IOException {
        return getNfs().makeFsInfoRequest(getFileHandle());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#pathconf()
     */
    public NfsPathconfResponse pathconf() throws IOException {
        return getNfs().wrapped_getPathconf(makePathconfRequest());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makePathconfRequest()
     */
    public NfsPathconfRequest makePathconfRequest() throws IOException {
        return getNfs().makePathconfRequest(getFileHandle());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#commit(long, int)
     */
    public NfsCommitResponse commit(long offsetToCommit, int dataSizeToCommit) throws IOException {
        return getNfs().wrapped_sendCommit(makeCommitRequest(offsetToCommit, dataSizeToCommit));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsFile#makeCommitRequest(long, int)
     */
    public NfsCommitRequest makeCommitRequest(long offsetToCommit, int dataSizeToCommit) throws IOException {
        return getNfs().makeCommitRequest(getFileHandle(), offsetToCommit, dataSizeToCommit);
    }

    /**
     * Conversion method.
     * 
     * @param childNames
     *            A list of child names.
     * @return The list of child files, one for each name.
     */
    protected List<F> getChildFiles(List<String> childNames) {
        if (childNames == null) {
            return null;
        }
        List<F> childFiles = new ArrayList<F>(childNames.size());
        for (String childName : childNames) {
            childFiles.add(getChildFile(childName));
        }
        return childFiles;
    }

    /**
     * @return true if it is, false if it isn't
     */
    protected final boolean isRootPath() {
        return (getPath() == null) || "".equals(getPath()) || separator.equals(getPath());
    }

    /**
     * @param fileHandle
     */
    protected final void setFileHandle(byte[] fileHandle) {
        _fileHandle = fileHandle;
    }

    /**
     * @param path
     */
    protected final void setPath(String path) {
        if (path == null || !path.startsWith(separator)) {
            throw new IllegalArgumentException("path must be absolute path");
        }

        _absolutePath = getNfs().getServer() + ":" + getNfs().getExportedPath() + path;
        _path = path;
        _parent = makeParentPath(path);
        _name = makeName(path);
    }

    /**
     * @param childName
     * @return the new nfs file
     */
    protected abstract F newChildFile(String childName);

    /**
     * @param parentFile
     */
    protected void setParentFile(F parentFile) {
        _parentFile = parentFile;
    }

    /**
     * @param accessToCheck
     * @return <code>true</code> if the access is allowed, <code>false</code> if
     *         it isn't
     * @throws IOException
     */
    private boolean canAccess(long accessToCheck) throws IOException {
        return (accessToCheck & getAccess(accessToCheck)) != 0;
    }

    /**
     * Set the file handle from the _path value
     */
    private void setFileHandle() {
        byte[] fileHandle = null;
        if (isRootPath()) {
            _isRootFile = true;
            fileHandle = getNfs().getRootFileHandle();
        } else {
            try {
                if (getParentFile().getFileHandle() != null) {
                    fileHandle = getNfs().wrapped_getLookup(makeLookupRequest()).getFileHandle();
                }
            } catch (IOException e) {
                // do nothing, this will be a common exception
            }
        }
        setFileHandle(fileHandle);
    }

}
