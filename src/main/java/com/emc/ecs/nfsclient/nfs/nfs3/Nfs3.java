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
package com.emc.ecs.nfsclient.nfs.nfs3;

import com.emc.ecs.nfsclient.mount.MountException;
import com.emc.ecs.nfsclient.mount.MountRequest;
import com.emc.ecs.nfsclient.mount.MountResponse;
import com.emc.ecs.nfsclient.mount.MountStatus;
import com.emc.ecs.nfsclient.mount.UnmountRequest;
import com.emc.ecs.nfsclient.network.NetMgr;
import com.emc.ecs.nfsclient.nfs.Nfs;
import com.emc.ecs.nfsclient.nfs.NfsCreateMode;
import com.emc.ecs.nfsclient.nfs.NfsCreateRequest;
import com.emc.ecs.nfsclient.nfs.NfsDirectoryEntry;
import com.emc.ecs.nfsclient.nfs.NfsDirectoryPlusEntry;
import com.emc.ecs.nfsclient.nfs.NfsStatus;
import com.emc.ecs.nfsclient.nfs.NfsException;
import com.emc.ecs.nfsclient.nfs.NfsAccessRequest;
import com.emc.ecs.nfsclient.nfs.NfsCommitRequest;
import com.emc.ecs.nfsclient.nfs.NfsResponseBase;
import com.emc.ecs.nfsclient.nfs.NfsResponseHandler;
import com.emc.ecs.nfsclient.nfs.NfsRmdirRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttrRequest;
import com.emc.ecs.nfsclient.nfs.NfsLinkRequest;
import com.emc.ecs.nfsclient.nfs.NfsLookupRequest;
import com.emc.ecs.nfsclient.nfs.NfsMkdirRequest;
import com.emc.ecs.nfsclient.nfs.NfsMknodRequest;
import com.emc.ecs.nfsclient.nfs.NfsPathconfRequest;
import com.emc.ecs.nfsclient.nfs.NfsReadRequest;
import com.emc.ecs.nfsclient.nfs.NfsReaddirRequest;
import com.emc.ecs.nfsclient.nfs.NfsReaddirplusRequest;
import com.emc.ecs.nfsclient.nfs.NfsReadlinkRequest;
import com.emc.ecs.nfsclient.nfs.NfsRemoveRequest;
import com.emc.ecs.nfsclient.nfs.NfsRenameRequest;
import com.emc.ecs.nfsclient.nfs.NfsRequestBase;
import com.emc.ecs.nfsclient.nfs.NfsFsInfo;
import com.emc.ecs.nfsclient.nfs.NfsFsInfoRequest;
import com.emc.ecs.nfsclient.nfs.NfsFsStat;
import com.emc.ecs.nfsclient.nfs.NfsFsStatRequest;
import com.emc.ecs.nfsclient.nfs.NfsGetAttrRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsSymlinkRequest;
import com.emc.ecs.nfsclient.nfs.NfsTime;
import com.emc.ecs.nfsclient.nfs.NfsType;
import com.emc.ecs.nfsclient.nfs.NfsWriteRequest;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.portmap.Portmapper;
import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import com.emc.ecs.nfsclient.rpc.RejectStatus;
import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.RpcResponseHandler;
import com.emc.ecs.nfsclient.rpc.RpcStatus;
import com.emc.ecs.nfsclient.rpc.RpcRequest;
import com.emc.ecs.nfsclient.rpc.RpcWrapper;
import com.emc.ecs.nfsclient.rpc.Xdr;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Nfs3 client operations implementation, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813)
 *
 * @author seibed
 */
public class Nfs3 implements Nfs<Nfs3File> {

    /**
     * The usual logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Nfs3.class);

    // NFS version number for RPC use
    /**
     * The NFS version number.
     */
    public static final int VERSION = 3;

    /**
     * The timeout in seconds.
     */
    public static final int NFS_TIMEOUT = 10; // unit: seconds

    /**
     * The maximum size in bytes. 8K is enough for all requests except for data writing.
     */
    public static final int MAXIMUM_NFS_REQUEST_SIZE = 8 * 1024;

    /**
     * The remote NFS server name.
     */
    private final String _server;

    /**
     * The remote NFS server port, obtained dynamically from Portmap.
     */
    private int _port = 0;

    /**
     * The exported path handled by this client.
     */
    private final String _exportedPath;

    /**
     * The root file handle for the exported path, obtained by a MOUNT call.
     */
    private byte[] _rootFileHandle = null;

    /**
     * Maximum NFS call retries, used in the wrapped method calls.
     */
    private final int _maximumRetries;

    /**
     * The default wait time between retries in milliseconds.
     */
    private final int DEFAULT_WAIT_TIME_MILLIS = 1000;

    /**
     * The wait time between retries in milliseconds.
     */
    private final int _retryWait = DEFAULT_WAIT_TIME_MILLIS;

    /**
     * Lock for calls to determine the port and root file handle.
     */
    private final Lock _prepareLock = new ReentrantLock();

    /**
     * Credential used for RPC calls.
     */
    private Credential _credential = new CredentialUnix();

    /**
     * <ul><li>
     * If <code>true</code>, include extra diagnostic and informational log entries.
     * </li><li>
     * If <code>false</code>, omit non-critical log entries.
     * </li></ul>
     */
    private boolean _verbose = false;

    /**
     * The helper instance used to wrap RPC calls.
     */
    private final RpcWrapper<NfsRequestBase, NfsResponseBase> _rpcWrapper;

    /**
     * Timeout for MOUNT calls in seconds.
     */
    private final static int MOUNT_RPC_TIMEOUT = 10;   // seconds

    /**
     * Maximum retries for MOUNT calls.
     */
    private final static int MOUNT_MAX_RETRIES = 2;

    /**
     * maximum mount request size (bytes)
     */
    private final static int MOUNT_MAX_REQUEST_SIZE = 8 * 1024;

    /**
     * Convenience constructor
     * 
     * @param server
     * @param exportedPath
     * @param rootFileHandle
     * @param maximumRetries
     * @throws IOException
     */
    public Nfs3(String server, String exportedPath, byte[] rootFileHandle, int maximumRetries) throws IOException {
        this(server, exportedPath, rootFileHandle, null, maximumRetries);
    }

    /**
     * Convenience constructor
     * 
     * @param server
     * @param exportedPath
     * @param credential
     * @param maximumRetries
     * @throws IOException 
     */
    public Nfs3(String server, String exportedPath, Credential credential, int maximumRetries) throws IOException {
        this(server, exportedPath, null, credential, maximumRetries);
    }

    /**
     * Convenience constructor
     * 
     * @param absolutePath
     *            The fully qualified path to the export, e.g., 10.32.172.64:/store.
     * @param uid
     * @param gid
     * @param maximumRetries
     * @throws IOException
     */
    public Nfs3(String absolutePath, int uid, int gid, int maximumRetries) throws IOException {
        this(absolutePath, new CredentialUnix(uid, gid, null), maximumRetries);
    }

    /**
     * Convenience constructor
     * 
     * @param absolutePath
     *            The fully qualified path to the export, e.g., 10.32.172.64:/store.
     * @param credential
     * @param maximumRetries
     * @throws IOException
     */
    public Nfs3(String absolutePath, Credential credential, int maximumRetries) throws IOException {
        this(getServer(absolutePath), getExportedPath(absolutePath), credential, maximumRetries);
    }

    /**
     * Full constructor.
     * 
     * @param server The NFS server name or IP address.
     * @param exportedPath The exported path 
     * @param rootFileHandle
     * @param credential
     * @param maximumRetries
     * @throws IOException
     */
    public Nfs3(String server, String exportedPath, byte[] rootFileHandle, Credential credential, int maximumRetries) throws IOException {
        checkForBlank(server, "server");
        checkForBlank(exportedPath, "exportedPath");
        if (maximumRetries <= 0) {
            throw new IllegalArgumentException("maxRetry must be positive.");
        }

        _server = server;
        _exportedPath = exportedPath;
        _maximumRetries = maximumRetries;

        if (credential != null) {
            _credential = credential;
        }
        _rpcWrapper = new RpcWrapper<NfsRequestBase, NfsResponseBase>(_server, _port, _retryWait, _maximumRetries, MAXIMUM_NFS_REQUEST_SIZE, NFS_TIMEOUT, _verbose);

        if (rootFileHandle == null) {
            prepareRootFhAndNfsPort();
        } else {
            _rootFileHandle = rootFileHandle.clone();
            _port = getNfsPortFromServer();
            _rpcWrapper.setPort(_port);
        }

        if (_verbose) {
            LOG.info("nfs rpc verbose is enabled");
        } else {
            LOG.info("nfs rpc verbose is NOT enabled");
        }
    }

    /**
     * Convenience method to check String parameters that cannot be blank.
     * 
     * @param value The parameter value.
     * @param name The parameter name, for exception messages.
     */
    private void checkForBlank(String value, String name) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
    }

    /**
     * @param absolutePath
     *            The fully qualified path to the export, e.g., 10.32.172.64:/store.
     * @return The server.
     */
    private static String getServer(String absolutePath) {
        int index = absolutePath.indexOf(":");
        return absolutePath.substring(0, Math.max(index, 0));
    }

    /**
     * @param absolutePath
     *            The fully qualified path to the export, e.g., 10.32.172.64:/store.
     * @return The exported path.
     */
    private static String getExportedPath(String absolutePath) {
        int index = absolutePath.indexOf(":");
        return absolutePath.substring(index + 1);
    }

    /**
     * Query the port and root file handle for NFS server.
     * @throws IOException
     */
    private void prepareRootFhAndNfsPort() throws IOException {

        if (!_prepareLock.tryLock()) {
            return;
        }

        try {
            _port = getNfsPortFromServer();
            _rpcWrapper.setPort(_port);
            _rootFileHandle = lookupRootHandle();
        } finally {
            _prepareLock.unlock();
        }
    }

    /**
     * @return the root handle
     * @throws IOException
     */
    byte[] lookupRootHandle()
            throws IOException {
        int portOfMountService = Portmapper.queryPortFromPortMap(MOUNTPROG, VERSION, _server);

        MountResponse response = null;
        MountRequest request = new MountRequest(VERSION, _exportedPath, _credential);
        boolean usePrivilegedPort = false;
        for (int i = 0; i < MOUNT_MAX_RETRIES; ++i) {
            try {
                Xdr mountXdr = new Xdr(MOUNT_MAX_REQUEST_SIZE);
                request.marshalling(mountXdr);
                response = new MountResponse(VERSION);
                if (usePrivilegedPort) {
                    LOG.debug("Mounting with privileged port - attempt with unprivileged failed with an authentication error.");
                }
                response.unmarshalling(NetMgr.getInstance().sendAndWait(_server, portOfMountService, usePrivilegedPort, mountXdr, MOUNT_RPC_TIMEOUT));
                int status = response.getMountStatus();
                if (status != MountStatus.MNT3_OK.getValue()) {
                    String msg = String.format(
                            "mount failure, server: %s, export: %s, nfs version: %s, returned state: %s", _server,
                            _exportedPath, VERSION, status);
                    throw new MountException(MountStatus.fromValue(status), msg);
                }
            } catch (RpcException e) {
                usePrivilegedPort = handleRpcException(e, i);
            }
        }

        // unmount it, so the server knows we are not holding on to it
        UnmountRequest unmountRequest = new UnmountRequest(VERSION, _exportedPath, _credential);
        for (int i = 0; i < MOUNT_MAX_RETRIES; ++i) {
            try {
                Xdr unmountXdr = new Xdr(MOUNT_MAX_REQUEST_SIZE);
                unmountRequest.marshalling(unmountXdr);
                // RFC defines the response of a unmount request as void
                // If we mounted with a privileged port, use one to unmount.
                NetMgr.getInstance().sendAndWait(_server, portOfMountService, usePrivilegedPort, unmountXdr, MOUNT_RPC_TIMEOUT);
            } catch (RpcException e) {
                if (i+1 < MOUNT_MAX_RETRIES) {
                    LOG.warn(String.format(
                            "unmount failure, server: %s, export: %s, nfs version: %s", _server,
                            _exportedPath, VERSION), e);
                }
            }
        }

        // If this gets here, the response cannot be null.
        // log the security mode that NFS supports, though we just
        // support AUTH_UNIX now
        int[] authenticationFlavors = response.getAuthenticationFlavors();
        String msg = String.format("nfs server %s:%s support auth mode %s", _server, _exportedPath,
                Arrays.toString(authenticationFlavors));
        LOG.info(msg);

        return response.getRootFileHandle();
    }

    /**
     * Decide whether to retry or throw an exception
     * 
     * @param e
     *            The exception.
     * @param attemptNumber
     *            The number of attempts so far.
     * @return <ul><li><code>true</code> if there was an authentication failure and privileged ports should be tried,</li>
     * <li><code>false</code> otherwise.</li></ul>
     * @throws IOException
     */
    private boolean handleRpcException(RpcException e, int attemptNumber)
            throws IOException {
        boolean tryPrivilegedPort = e.getStatus().equals(RejectStatus.AUTH_ERROR);
        boolean networkError = e.getStatus().equals(RpcStatus.NETWORK_ERROR);
        boolean retry = (tryPrivilegedPort || networkError) &&
                        ((attemptNumber + 1) < MOUNT_MAX_RETRIES);
        if (!retry) {
            String messageStart = networkError ? "network" : "rpc";
            String msg = String.format("%s error, server: %s, export: %s, RPC error: %s", messageStart, _server, _exportedPath,
                        e.getMessage());
            throw new MountException(MountStatus.MNT3ERR_IO, msg, e);
        }
        System.out.println("retry " + (attemptNumber + 1));
        if (tryPrivilegedPort) {
            LOG.info("Next try will be with a privileged port.");
        }
        return tryPrivilegedPort;
    }

    /**
     * @return The NFS Server RPC port number.
     * @throws IOException
     */
    private int getNfsPortFromServer() throws IOException {
        return Portmapper.queryPortFromPortMap(RPC_PROGRAM, VERSION, _server);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#disableSudo()
     */
    public void disableSudo() throws IOException {
        _credential = new CredentialUnix();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#enableSudo(int, int)
     */
    public void enableSudo(int uid, int gid) {
        _credential = new CredentialUnix(uid, gid, null);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getCredential()
     */
    public Credential getCredential() {
        return _credential;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getExportedPath()
     */
    public String getExportedPath() {
        return _exportedPath;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#newFile(java.lang.String)
     */
    public Nfs3File newFile(String path) throws IOException {
        return new Nfs3File(this, path);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getPort()
     */
    public int getPort() {
        return _port;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getRootFileHandle()
     */
    public byte[] getRootFileHandle() {
        return _rootFileHandle.clone();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getServer()
     */
    public String getServer() {
        return _server;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#nullCall()
     */
    public Xdr nullCall() throws IOException {
        Xdr xdr = new Xdr(MAXIMUM_NFS_REQUEST_SIZE);
        new RpcRequest(RPC_PROGRAM, VERSION, NFSPROC3_NULL, _credential) {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcRequest#getErrorMessage()
             */
            public String getErrorMessage() {
                throw new NotImplementedException("This should never be used for a NULL call.");
            }

        }.marshalling(xdr);
        return _rpcWrapper.callRpc(_rpcWrapper.chooseIP(_server.getBytes(RpcRequest.CHARSET)), xdr, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeGetAttrRequest(byte[])
     */
    public Nfs3GetAttrRequest makeGetAttrRequest(byte[] fileHandle) throws FileNotFoundException {
        return new Nfs3GetAttrRequest(fileHandle, _credential);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.Nfs#getAttr(com.emc.ecs.nfsclient.nfs.NfsGetAttrRequest)
     */
    public Nfs3GetAttrResponse getAttr(NfsGetAttrRequest request) throws IOException {
        Nfs3GetAttrResponse response = new Nfs3GetAttrResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getAttr(com.emc.ecs.nfsclient.nfs.
     * NfsGetAttrRequest)
     */
    public Nfs3GetAttrResponse wrapped_getAttr(NfsGetAttrRequest request) throws IOException {
        NfsResponseHandler<Nfs3GetAttrResponse> responseHandler = new NfsResponseHandler<Nfs3GetAttrResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3GetAttrResponse makeNewResponse() {
                return new Nfs3GetAttrResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeSetAttrRequest(byte[], com.emc.ecs.nfsclient.nfs.NfsSetAttributes, com.emc.ecs.nfsclient.nfs.NfsTime)
     */
    public NfsSetAttrRequest makeSetAttrRequest(byte[] fileHandle, NfsSetAttributes attributes, NfsTime guardTime) throws FileNotFoundException {
        return new Nfs3SetAttrRequest(fileHandle, attributes, guardTime, _credential);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.Nfs#setAttr(com.emc.ecs.nfsclient.nfs.NfsSetAttrRequest)
     */
    public Nfs3SetAttrResponse setAttr(NfsSetAttrRequest request) throws IOException {
        Nfs3SetAttrResponse response = new Nfs3SetAttrResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.Nfs#wrapped_setAttr(com.emc.ecs.nfsclient.nfs.
     * NfsSetAttrRequest)
     */
    public Nfs3SetAttrResponse wrapped_setAttr(NfsSetAttrRequest request) throws IOException {
        NfsResponseHandler<Nfs3SetAttrResponse> responseHandler = new NfsResponseHandler<Nfs3SetAttrResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3SetAttrResponse makeNewResponse() {
                return new Nfs3SetAttrResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeLookupRequest(byte[], java.lang.String)
     */
    public Nfs3LookupRequest makeLookupRequest(byte[] fileHandle, String name) throws FileNotFoundException {
        return new Nfs3LookupRequest(fileHandle, name, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getLookup(com.emc.ecs.nfsclient.nfs.NfsLookupRequest)
     */
      public Nfs3LookupResponse getLookup(NfsLookupRequest request) throws IOException {
         Nfs3LookupResponse response = new Nfs3LookupResponse();
         _rpcWrapper.callRpcNaked(request, response);
         return response;
     }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getLookup(com.emc.ecs.nfsclient.nfs.NfsLookupRequest)
     */
    public Nfs3LookupResponse wrapped_getLookup(NfsLookupRequest request) throws IOException {
        RpcResponseHandler<Nfs3LookupResponse> responseHandler = new NfsResponseHandler<Nfs3LookupResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3LookupResponse makeNewResponse() {
                return new Nfs3LookupResponse();
            }

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.nfs.NfsResponseHandler#checkResponse(com.emc.ecs.nfsclient.rpc.RpcRequest)
             */
            public void checkResponse(RpcRequest request) throws IOException {
                // If the root file handle is bad, we should mount again.
                if ((getResponse().getState() == NfsStatus.NFS3ERR_BADHANDLE.getValue())
                 && (((NfsLookupRequest)request).getFileHandle() == _rootFileHandle)) {
                    prepareRootFhAndNfsPort();
                }
                super.checkResponse(request);
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeAccessRequest(byte[], long)
     */
    public Nfs3AccessRequest makeAccessRequest(byte[] fileHandle, long accessToCheck) throws FileNotFoundException {
        return new Nfs3AccessRequest(fileHandle, accessToCheck, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getAccess(com.emc.ecs.nfsclient.nfs.NfsAccessRequest)
     */
    public Nfs3AccessResponse getAccess(NfsAccessRequest request) throws IOException {
        Nfs3AccessResponse response = new Nfs3AccessResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getAccess(com.emc.ecs.nfsclient.nfs.NfsAccessRequest)
     */
    public Nfs3AccessResponse wrapped_getAccess(NfsAccessRequest request) throws IOException {
        NfsResponseHandler<Nfs3AccessResponse> responseHandler = new NfsResponseHandler<Nfs3AccessResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3AccessResponse makeNewResponse() {
                return new Nfs3AccessResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeReadlinkRequest(byte[])
     */
    public Nfs3ReadlinkRequest makeReadlinkRequest(byte[] fileHandle) throws FileNotFoundException {
        return new Nfs3ReadlinkRequest(fileHandle, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getReadlink(com.emc.ecs.nfsclient.nfs.NfsReadlinkRequest)
     */
    public Nfs3ReadlinkResponse getReadlink(NfsReadlinkRequest request) throws IOException {
        Nfs3ReadlinkResponse response = new Nfs3ReadlinkResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getReadlink(com.emc.ecs.nfsclient.nfs.NfsReadlinkRequest)
     */
    public Nfs3ReadlinkResponse wrapped_getReadlink(NfsReadlinkRequest request) throws IOException {
        NfsResponseHandler<Nfs3ReadlinkResponse> responseHandler = new NfsResponseHandler<Nfs3ReadlinkResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3ReadlinkResponse makeNewResponse() {
                return new Nfs3ReadlinkResponse();
            }
        };

        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeReadRequest(byte[], long, int)
     */
    public Nfs3ReadRequest makeReadRequest(byte[] fileHandle, long offset, int size) throws FileNotFoundException {
        return new Nfs3ReadRequest(fileHandle, offset, size, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getRead(com.emc.ecs.nfsclient.nfs.NfsReadRequest, byte[], int)
     */
    public Nfs3ReadResponse getRead(NfsReadRequest request, byte[] bytes, int position) throws IOException {
        Nfs3ReadResponse response = new Nfs3ReadResponse(bytes, position);
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getRead(com.emc.ecs.nfsclient.nfs.NfsReadRequest, byte[], int)
     */
    public Nfs3ReadResponse wrapped_getRead(NfsReadRequest request, final byte[] bytes, final int position) throws IOException {
        NfsResponseHandler<Nfs3ReadResponse> responseHandler = new NfsResponseHandler<Nfs3ReadResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3ReadResponse makeNewResponse() {
                return new Nfs3ReadResponse(bytes, position);
            }
        };

        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeWriteRequest(byte[], long, java.util.List, int)
     */
    public Nfs3WriteRequest makeWriteRequest(byte[] fileHandle, long offset, List<ByteBuffer> payload, int syncType) throws FileNotFoundException {
        return new Nfs3WriteRequest(fileHandle, offset, payload, syncType, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendWrite(com.emc.ecs.nfsclient.nfs.NfsWriteRequest)
     */
    public Nfs3WriteResponse sendWrite(NfsWriteRequest request) throws IOException {
        Nfs3WriteResponse response = new Nfs3WriteResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendWrite(com.emc.ecs.nfsclient.nfs.NfsWriteRequest)
     */
    public Nfs3WriteResponse wrapped_sendWrite(NfsWriteRequest request) throws IOException {
        // for async write, all the writes and commit should be sent to
        // the same NFS server
        String ip = request.isSync() ? _rpcWrapper.chooseIP(request.getIpKey()) : _server;
        NfsResponseHandler<Nfs3WriteResponse> responseHandler = new NfsResponseHandler<Nfs3WriteResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3WriteResponse makeNewResponse() {
                return new Nfs3WriteResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler, ip);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendWrite(com.emc.ecs.nfsclient.nfs.NfsWriteRequest, java.lang.Long)
     */
    public Nfs3WriteResponse wrapped_sendWrite(NfsWriteRequest request, final Long verifier) throws IOException {
        // for async write, all the writes and commit should be sent to
        // the same NFS server
        String ip = request.isSync() ? _rpcWrapper.chooseIP(request.getIpKey()) : _server;
        NfsResponseHandler<Nfs3WriteResponse> responseHandler = new NfsResponseHandler<Nfs3WriteResponse>() {

            protected Nfs3WriteResponse makeNewResponse() {
                return new Nfs3WriteResponse();
            }

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.nfs.NfsResponseHandler#checkResponse(com.emc.ecs.nfsclient.rpc.RpcRequest)
             */
            public void checkResponse(RpcRequest request) throws IOException {
                super.checkResponse(request);
                if (!((NfsWriteRequest)request).isSync() && verifier != null && getResponse().getVerf() != verifier) {
                    throw new NfsException(NfsStatus.NFS3ERR_SERVERFAULT, "server restart detected");
                }
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler, ip);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeCreateRequest(com.emc.ecs.nfsclient.nfs.NfsCreateMode, byte[], java.lang.String, com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public Nfs3CreateRequest makeCreateRequest(NfsCreateMode createMode, byte[] parentDirectoryFileHandle, String name,
            NfsSetAttributes attributes, byte[] verifier) throws FileNotFoundException {
        return new Nfs3CreateRequest(createMode, parentDirectoryFileHandle, name, attributes, verifier, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendCreate(com.emc.ecs.nfsclient.nfs.NfsCreateRequest)
     */
    public Nfs3CreateResponse sendCreate(NfsCreateRequest request) throws IOException {
        Nfs3CreateResponse response = new Nfs3CreateResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendCreate(com.emc.ecs.nfsclient.nfs.NfsCreateRequest)
     */
    public Nfs3CreateResponse wrapped_sendCreate(NfsCreateRequest request) throws IOException {
        NfsResponseHandler<Nfs3CreateResponse> responseHandler = new NfsResponseHandler<Nfs3CreateResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3CreateResponse makeNewResponse() {
                return new Nfs3CreateResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeMkdirRequest(byte[], java.lang.String, com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public Nfs3MkdirRequest makeMkdirRequest(byte[] parentDirectoryFileHandle, String name,
            NfsSetAttributes attributes) throws FileNotFoundException {
        return new Nfs3MkdirRequest(parentDirectoryFileHandle, name, attributes, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendMkdir(com.emc.ecs.nfsclient.nfs.NfsMkdirRequest)
     */
    public Nfs3MkdirResponse sendMkdir(NfsMkdirRequest request) throws IOException {
        Nfs3MkdirResponse response = new Nfs3MkdirResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendMkdir(com.emc.ecs.nfsclient.nfs.NfsMkdirRequest)
     */
    public Nfs3MkdirResponse wrapped_sendMkdir(NfsMkdirRequest request) throws IOException {
        RpcResponseHandler<Nfs3MkdirResponse> responseHandler = new NfsResponseHandler<Nfs3MkdirResponse>() {
    
            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3MkdirResponse makeNewResponse() {
                return new Nfs3MkdirResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeSymlinkRequest(java.lang.String, byte[], java.lang.String, com.emc.ecs.nfsclient.nfs.NfsSetAttributes)
     */
    public Nfs3SymlinkRequest makeSymlinkRequest(String symbolicLinkData, byte[] parentDirectoryFileHandle, String name,
            NfsSetAttributes attributes) throws FileNotFoundException {
        return new Nfs3SymlinkRequest(symbolicLinkData, parentDirectoryFileHandle, name, attributes, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendSymlink(com.emc.ecs.nfsclient.nfs.NfsSymlinkRequest)
     */
    public Nfs3SymlinkResponse sendSymlink(NfsSymlinkRequest request) throws IOException {
        Nfs3SymlinkResponse response = new Nfs3SymlinkResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendSymlink(com.emc.ecs.nfsclient.nfs.NfsSymlinkRequest)
     */
    public Nfs3SymlinkResponse wrapped_sendSymlink(NfsSymlinkRequest request) throws IOException {
        RpcResponseHandler<Nfs3SymlinkResponse> responseHandler = new NfsResponseHandler<Nfs3SymlinkResponse>() {
            
            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3SymlinkResponse makeNewResponse() {
                return new Nfs3SymlinkResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeMknodRequest(byte[], java.lang.String, com.emc.ecs.nfsclient.nfs.NfsType, com.emc.ecs.nfsclient.nfs.NfsSetAttributes, long[])
     */
    public Nfs3MknodRequest makeMknodRequest(byte[] parentDirectoryFileHandle, String name, NfsType type,
            NfsSetAttributes attributes, long[] rdev) throws FileNotFoundException {
        return new Nfs3MknodRequest(parentDirectoryFileHandle, name, type, attributes, rdev, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendMknod(com.emc.ecs.nfsclient.nfs.NfsMknodRequest)
     */
    public Nfs3MknodResponse sendMknod(NfsMknodRequest request) throws IOException {
        Nfs3MknodResponse response = new Nfs3MknodResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendMknod(com.emc.ecs.nfsclient.nfs.NfsMknodRequest)
     */
    public Nfs3MknodResponse wrapped_sendMknod(NfsMknodRequest request) throws IOException {
        RpcResponseHandler<Nfs3MknodResponse> responseHandler = new NfsResponseHandler<Nfs3MknodResponse>() {
            
            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3MknodResponse makeNewResponse() {
                return new Nfs3MknodResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeRemoveRequest(byte[], java.lang.String)
     */
    public Nfs3RemoveRequest makeRemoveRequest(byte[] parentDirectoryFileHandle, String name) throws FileNotFoundException {
        return new Nfs3RemoveRequest(parentDirectoryFileHandle, name, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendRemove(com.emc.ecs.nfsclient.nfs.NfsRemoveRequest)
     */
    public Nfs3RemoveResponse sendRemove(NfsRemoveRequest request) throws IOException {
        Nfs3RemoveResponse response = new Nfs3RemoveResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendRemove(com.emc.ecs.nfsclient.nfs.NfsRemoveRequest)
     */
    public Nfs3RemoveResponse wrapped_sendRemove(NfsRemoveRequest request) throws IOException {
        RpcResponseHandler<Nfs3RemoveResponse> responseHandler = new NfsResponseHandler<Nfs3RemoveResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3RemoveResponse makeNewResponse() {
                return new Nfs3RemoveResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeRmdirRequest(byte[], java.lang.String)
     */
    public Nfs3RmdirRequest makeRmdirRequest(byte[] parentDirectoryFileHandle, String name) throws FileNotFoundException {
        return new Nfs3RmdirRequest(parentDirectoryFileHandle, name, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendRmdir(com.emc.ecs.nfsclient.nfs.NfsRmdirRequest)
     */
    public Nfs3RmdirResponse sendRmdir(NfsRmdirRequest request) throws IOException {
        Nfs3RmdirResponse response = new Nfs3RmdirResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendRmdir(com.emc.ecs.nfsclient.nfs.NfsRmdirRequest)
     */
    public Nfs3RmdirResponse wrapped_sendRmdir(NfsRmdirRequest request) throws IOException {
        RpcResponseHandler<Nfs3RmdirResponse> responseHandler = new NfsResponseHandler<Nfs3RmdirResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3RmdirResponse makeNewResponse() {
                return new Nfs3RmdirResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeRenameRequest(byte[], java.lang.String, byte[], java.lang.String)
     */
    public Nfs3RenameRequest makeRenameRequest(byte[] fromDirectoryFileHandle, String fromName,
            byte[] toDirectoryFileHandle, String toName) throws FileNotFoundException {
        return new Nfs3RenameRequest(fromDirectoryFileHandle, fromName, toDirectoryFileHandle, toName, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendRename(com.emc.ecs.nfsclient.nfs.NfsRenameRequest)
     */
    public Nfs3RenameResponse sendRename(NfsRenameRequest request) throws IOException {
        Nfs3RenameResponse response = new Nfs3RenameResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendRename(com.emc.ecs.nfsclient.nfs.NfsRenameRequest)
     */
    public Nfs3RenameResponse wrapped_sendRename(NfsRenameRequest request) throws IOException {
        NfsResponseHandler<Nfs3RenameResponse> responseHandler = new NfsResponseHandler<Nfs3RenameResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3RenameResponse makeNewResponse() {
                return new Nfs3RenameResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeLinkRequest(byte[])
     */
    public Nfs3LinkRequest makeLinkRequest(byte[] fileHandle, byte[] parentDirectoryFileHandle, String name) throws FileNotFoundException {
        return new Nfs3LinkRequest(fileHandle, parentDirectoryFileHandle, name, _credential);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.Nfs#sendLink(com.emc.ecs.nfsclient.nfs.NfsLinkRequest)
     */
    public Nfs3LinkResponse sendLink(NfsLinkRequest request) throws IOException {
        Nfs3LinkResponse response = new Nfs3LinkResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendLink(com.emc.ecs.nfsclient.nfs.
     * NfsLinkRequest)
     */
    public Nfs3LinkResponse wrapped_sendLink(NfsLinkRequest request) throws IOException {
        NfsResponseHandler<Nfs3LinkResponse> responseHandler = new NfsResponseHandler<Nfs3LinkResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3LinkResponse makeNewResponse() {
                return new Nfs3LinkResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeReaddirRequest(byte[], long, long, int)
     */
    public NfsReaddirRequest makeReaddirRequest(byte[] directoryFileHandle, long cookie, long cookieverf, int count) throws FileNotFoundException {
        return new Nfs3ReaddirRequest(directoryFileHandle, cookie, cookieverf, count, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getReaddir(com.emc.ecs.nfsclient.nfs.NfsReaddirRequest)
     */
    public Nfs3ReaddirResponse getReaddir(NfsReaddirRequest request) throws IOException {
        Nfs3ReaddirResponse response = new Nfs3ReaddirResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getReaddir(com.emc.ecs.nfsclient.nfs.NfsReaddirRequest)
     */
    public Nfs3ReaddirResponse wrapped_getReaddir(NfsReaddirRequest request) throws IOException {
        NfsResponseHandler<Nfs3ReaddirResponse> responseHandler = new NfsResponseHandler<Nfs3ReaddirResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3ReaddirResponse makeNewResponse() {
                return new Nfs3ReaddirResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getReaddir(com.emc.ecs.nfsclient.nfs.NfsReaddirRequest, java.util.List)
     */
    public Nfs3ReaddirResponse wrapped_getReaddir(NfsReaddirRequest request, final List<NfsDirectoryEntry> entries)
            throws IOException {
        NfsResponseHandler<Nfs3ReaddirResponse> responseHandler = new NfsResponseHandler<Nfs3ReaddirResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3ReaddirResponse makeNewResponse() {
                return new Nfs3ReaddirResponse(entries);
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeReaddirplusRequest(byte[], long, long, int, int)
     */
    public NfsReaddirplusRequest makeReaddirplusRequest(byte[] directoryFileHandle, long cookie, long cookieverf,
            int dircount, int maxcount) throws FileNotFoundException {
        return new Nfs3ReaddirplusRequest(directoryFileHandle, cookie, cookieverf, dircount, maxcount, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getReaddirplus(com.emc.ecs.nfsclient.nfs.NfsReaddirplusRequest)
     */
    public Nfs3ReaddirplusResponse getReaddirplus(NfsReaddirplusRequest request) throws IOException {
        Nfs3ReaddirplusResponse response = new Nfs3ReaddirplusResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getReaddirplus(com.emc.ecs.nfsclient.nfs.NfsReaddirplusRequest)
     */
    public Nfs3ReaddirplusResponse wrapped_getReaddirplus(NfsReaddirplusRequest request) throws IOException {
        NfsResponseHandler<Nfs3ReaddirplusResponse> responseHandler = new NfsResponseHandler<Nfs3ReaddirplusResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3ReaddirplusResponse makeNewResponse() {
                return new Nfs3ReaddirplusResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getReaddirplus(com.emc.ecs.nfsclient.nfs.NfsReaddirplusRequest, java.util.List)
     */
    public Nfs3ReaddirplusResponse wrapped_getReaddirplus(NfsReaddirplusRequest request,
            final List<NfsDirectoryPlusEntry> entries) throws IOException {
        NfsResponseHandler<Nfs3ReaddirplusResponse> responseHandler = new NfsResponseHandler<Nfs3ReaddirplusResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3ReaddirplusResponse makeNewResponse() {
                return new Nfs3ReaddirplusResponse(entries);
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getNfsFsStat()
     */
    public NfsFsStat getNfsFsStat() throws IOException {
        return wrapped_getFsStat(makeFsStatRequest()).getFsStat();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeFsStatRequest(byte[])
     */
    public Nfs3FsStatRequest makeFsStatRequest(byte[] fileHandle) throws FileNotFoundException {
        return new Nfs3FsStatRequest(fileHandle, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeFsStatRequest()
     */
    public Nfs3FsStatRequest makeFsStatRequest() throws FileNotFoundException {
        return makeFsStatRequest(getRootFileHandle());
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getFsStat(com.emc.ecs.nfsclient.nfs.NfsFsStatRequest)
     */
    public Nfs3FsStatResponse getFsStat(NfsFsStatRequest request) throws IOException {
        Nfs3FsStatResponse response = new Nfs3FsStatResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getFsStat(com.emc.ecs.nfsclient.nfs.NfsFsStatRequest)
     */
    public Nfs3FsStatResponse wrapped_getFsStat(NfsFsStatRequest request) throws IOException {
        NfsResponseHandler<Nfs3FsStatResponse> responseHandler = new NfsResponseHandler<Nfs3FsStatResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3FsStatResponse makeNewResponse() {
                return new Nfs3FsStatResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getNfsFsInfo()
     */
    public NfsFsInfo getNfsFsInfo() throws IOException {
        return wrapped_getFsInfo(makeFsInfoRequest()).getFsInfo();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeFsInfoRequest(byte[])
     */
    public Nfs3FsInfoRequest makeFsInfoRequest(byte[] fileHandle) throws FileNotFoundException {
        return new Nfs3FsInfoRequest(fileHandle, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeFsInfoRequest()
     */
    public Nfs3FsInfoRequest makeFsInfoRequest() throws FileNotFoundException {
        return makeFsInfoRequest(getRootFileHandle());
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#getFsInfo(com.emc.ecs.nfsclient.nfs.NfsFsInfoRequest)
     */
    public Nfs3FsInfoResponse getFsInfo(NfsFsInfoRequest request) throws IOException {
        Nfs3FsInfoResponse response = new Nfs3FsInfoResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getFsInfo(com.emc.ecs.nfsclient.nfs.NfsFsInfoRequest)
     */
    public Nfs3FsInfoResponse wrapped_getFsInfo(NfsFsInfoRequest request) throws IOException {
        NfsResponseHandler<Nfs3FsInfoResponse> responseHandler = new NfsResponseHandler<Nfs3FsInfoResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3FsInfoResponse makeNewResponse() {
                return new Nfs3FsInfoResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makePathconfRequest(byte[])
     */
    public Nfs3PathconfRequest makePathconfRequest(byte[] fileHandle) throws FileNotFoundException {
        return new Nfs3PathconfRequest(fileHandle, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#PATHCONF(com.emc.ecs.nfsclient.nfs.NfsPathconfRequest)
     */
    public Nfs3PathconfResponse getPathconf(NfsPathconfRequest request) throws IOException {
        Nfs3PathconfResponse response = new Nfs3PathconfResponse();
        _rpcWrapper.callRpcNaked(request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_getPathconf(com.emc.ecs.nfsclient.nfs.NfsPathconfRequest)
     */
    public Nfs3PathconfResponse wrapped_getPathconf(NfsPathconfRequest request) throws IOException {
        NfsResponseHandler<Nfs3PathconfResponse> responseHandler = new NfsResponseHandler<Nfs3PathconfResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3PathconfResponse makeNewResponse() {
                return new Nfs3PathconfResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler);
        return responseHandler.getResponse();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#makeCommitRequest(byte[], long, int)
     */
    public Nfs3CommitRequest makeCommitRequest(byte[] fileHandle, long offsetToCommit, int dataSizeToCommit) throws FileNotFoundException {
        return new Nfs3CommitRequest(fileHandle, offsetToCommit, dataSizeToCommit, _credential);
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#sendCommit(com.emc.ecs.nfsclient.nfs.NfsCommitRequest)
     */
    public Nfs3CommitResponse sendCommit(NfsCommitRequest request) throws IOException {
        Nfs3CommitResponse response = new Nfs3CommitResponse();
        _rpcWrapper.callRpcNaked(request, response, _server);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.Nfs#wrapped_sendCommit(com.emc.ecs.nfsclient.nfs.NfsCommitRequest)
     */
    public Nfs3CommitResponse wrapped_sendCommit(NfsCommitRequest request) throws IOException {
        NfsResponseHandler<Nfs3CommitResponse> responseHandler = new NfsResponseHandler<Nfs3CommitResponse>() {

            /* (non-Javadoc)
             * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
             */
            protected Nfs3CommitResponse makeNewResponse() {
                return new Nfs3CommitResponse();
            }

        };
        _rpcWrapper.callRpcWrapped(request, responseHandler, _server);
        return responseHandler.getResponse();
    }

    // TODO: Figure out what to do with the rest of the code in this class (following this comment).

//  /**
//   * @param path
//   * @param fileHandle
//   * @param offset
//   * @param dataBuffer
//   * @param isSync
//   * @param serverIp
//   * @param verifier
//   * @return
//   * @throws IOException
//   */
//  private AsyncWriteResult _write(String path, byte[] fileHandle, long offset, List<ByteBuffer> dataBuffer,
//          boolean isSync, String serverIp, Long verifier) throws IOException {
//      return new AsyncWriteResult(response.getCount(), ip, response.getVerf());
//  }

  /**
   * Read data from a file handle
   * 
   * @param path
   *            the path of the file
   * @param fileHandle
   *            file handle of the file
   * @param offset
   *            offset of the file to read
   * @param length
   *            the length of the data buffer
   * @param data
   *            the data to be returned
   * @param eof
   *            is at the end-of-file
   * @return The number of bytes of data returned by the read. the number may
   *         be smaller than len
   */
  public int read(String path, byte[] fileHandle, long offset, int length, final byte[] data, final int pos, final MutableBoolean eof)
          throws IOException {
      Nfs3ReadRequest request = new Nfs3ReadRequest(fileHandle, offset, length, _credential);
      NfsResponseHandler<Nfs3ReadResponse> responseHandler = new NfsResponseHandler<Nfs3ReadResponse>() {


          /* (non-Javadoc)
           * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
           */
          protected Nfs3ReadResponse makeNewResponse() {
              return new Nfs3ReadResponse(data, pos);
          }

          /* (non-Javadoc)
           * @see com.emc.ecs.nfsclient.nfs.NfsResponseHandler#checkResponse(com.emc.ecs.nfsclient.rpc.RpcRequest)
           */
          public void checkResponse(RpcRequest request) throws IOException {
              super.checkResponse(request);
              eof.setValue(getResponse().isEof());
          }

      };
      _rpcWrapper.callRpcWrapped(request, responseHandler);
      return responseHandler.getResponse().getBytesRead();
  }

  public void fsync(String path, byte[] fh, final Long verifier) throws IOException {
      Nfs3CommitRequest request = new Nfs3CommitRequest(fh,
              0 /* from beginning of file */, 0 /* till the end of it */, _credential);

      NfsResponseHandler<Nfs3CommitResponse> responseHandler = new NfsResponseHandler<Nfs3CommitResponse>() {

          /* (non-Javadoc)
           * @see com.emc.ecs.nfsclient.rpc.RpcResponseHandler#makeNewResponse()
           */
          protected Nfs3CommitResponse makeNewResponse() {
              return new Nfs3CommitResponse();
          }

          /* (non-Javadoc)
           * @see com.emc.ecs.nfsclient.nfs.NfsResponseHandler#checkResponse(com.emc.ecs.nfsclient.rpc.RpcRequest)
           */
          public void checkResponse(RpcRequest request) throws IOException {
              super.checkResponse(request);
              if (verifier != null && getResponse().getVerf() != verifier) {
                  throw new NfsException(NfsStatus.NFS3ERR_SERVERFAULT, "server restart detected");
              }
          }

      };
      _rpcWrapper.callRpcWrapped(request, responseHandler, _server);
  }

}
