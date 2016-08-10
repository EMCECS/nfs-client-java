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
package com.emc.ecs.nfsclient.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.ecs.nfsclient.nfs.NfsStatus;
import com.emc.ecs.nfsclient.network.NetMgr;
import com.emc.ecs.nfsclient.nfs.NfsException;
import com.emc.ecs.nfsclient.nfs.NfsRequestBase;
import com.emc.ecs.nfsclient.nfs.NfsResponseBase;

/**
 * Wrapper for NFS Server RPC calls, to handle unmarshalling, retries, and
 * status checking.
 * 
 * @author seibed
 */
public class RpcWrapper<S extends NfsRequestBase, T extends NfsResponseBase> {

    /**
     * The usual logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RpcWrapper.class);

    /**
     * The remote server being called.
     */
    private final String _server;

    /**
     * The port on the remote server being used for this communication.
     */
    private int _port;

    /**
     * The wait in milliseconds.
     */
    private final int _retryWait;

    /**
     * The maximum number of retries.
     */
    private final int _maximumRetries;

    /**
     * The maximum request size in bytes.
     */
    private final int _maximumRequestSize;

    /**
     * The timeout in seconds.
     */
    private final int _rpcTimeout;

    /**
     * <ul>
     * <li>If <code>true</code>, include extra diagnostic and informational log
     * entries.</li>
     * <li>If <code>false</code>, omit non-critical log entries.</li>
     * </ul>
     */
    private final boolean _verbose;

    /**
     * Discovered IP addresses for the remote server.
     */
    private String[] _ips;

    /**
     * @param server
     *            The remote server being called.
     * @param port
     *            The port on the remote server being used for this
     *            communication.
     * @param retryWait
     *            The wait in milliseconds.
     * @param maximumRetries
     *            The maximum number of retries.
     * @param maximumRequestSize
     *            The maximum request size in bytes.
     * @param rpcTimeout
     *            The timeout in seconds.
     * @param verbose
     *            <ul>
     *            <li>If <code>true</code>, include extra diagnostic and
     *            informational log entries.</li>
     *            <li>If <code>false</code>, omit non-critical log entries.</li>
     *            </ul>
     */
    public RpcWrapper(String server, int port, int retryWait, int maximumRetries,
            int maximumRequestSize, int rpcTimeout, boolean verbose) {
        _server = server;
        _port = port;
        _retryWait = retryWait;
        _maximumRetries = maximumRetries;
        _maximumRequestSize = maximumRequestSize;
        _rpcTimeout = rpcTimeout;
        _verbose = verbose;
    }

    /**
     * Reset the port as necessary - used if the port changes.
     * 
     * @param port
     *            The port on the remote server being used for this
     *            communication.
     */
    public void setPort(int port) {
        _port = port;
        _ips = probeIps();
    }

    /**
     * Make the wrapped call and unmarshall the returned Xdr to a response,
     * getting the IP key from the request. If an RPC Exception is being thrown,
     * and retries remain, then log the exception and retry.
     * 
     * @param request
     *            The request to send.
     * @param responseHandler
     *            A response handler.
     * @throws IOException
     */
    public void callRpcWrapped(S request, RpcResponseHandler<? extends T> responseHandler) throws IOException {
        for (int i = 0; i < _maximumRetries; ++i) {
            try {
                callRpcChecked(request, responseHandler);
                return;
            } catch (RpcException e) {
                handleRpcException(e, i);
            }
        }
    }

    /**
     * Make the wrapped call and unmarshall the returned Xdr to a response,
     * using the given <code>ip</code>. If an RPC Exception is being thrown, and
     * retries remain, then log the exception and retry.
     * 
     * @param request
     *            The request to send.
     * @param responseHandler
     *            A response handler.
     * @param ip
     *            The IP address to use for communication.
     * @throws IOException
     */
    public void callRpcWrapped(S request, RpcResponseHandler<? extends T> responseHandler, String ip)
            throws IOException {
        for (int i = 0; i < _maximumRetries; ++i) {
            try {
                callRpcChecked(request, responseHandler, ip);
                return;
            } catch (RpcException e) {
                handleRpcException(e, i);
            }
        }
    }

    /**
     * Convenience wrapper for NFS RPC calls where the IP is determined by a
     * byte[] key. This method just determines the IP address and calls the
     * basic method.
     * 
     * @param request
     *            The request to send.
     * @param responseHandler
     *            A response handler.
     * @throws IOException
     */
    public void callRpcChecked(S request, RpcResponseHandler<? extends T> responseHandler) throws IOException {
        callRpcChecked(request, responseHandler, chooseIP(request.getIpKey()));
    }

    /**
     * Make the call using the Request ip key to determine the IP address for
     * communication.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            A response to hold the returned data.
     * @throws IOException
     */
    public void callRpcNaked(S request, T response) throws IOException {
        callRpcNaked(request, response, chooseIP(request.getIpKey()));
    }

    /**
     * Make the call to a specified IP address.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            A response to hold the returned data.
     * @param ipAddress
     *            The IP address to use for communication.
     * @throws RpcException
     */
    public void callRpcNaked(S request, T response, String ipAddress) throws RpcException {
        Xdr xdr = new Xdr(_maximumRequestSize);
        request.marshalling(xdr);
        response.unmarshalling(callRpc(ipAddress, xdr, request.isUsePrivilegedPort()));
    }

    /**
     * Basic RPC call functionality only.
     * 
     * @param serverIP
     *            The endpoint of the server being called.
     * @param xdrRequest
     *            The Xdr data for the request.
     * @param usePrivilegedPort
     *            <ul>
     *            <li>If <code>true</code>, use a privileged local port (below
     *            1024) for RPC communication.</li>
     *            <li>If <code>false</code>, use any non-privileged local port
     *            for RPC communication.</li>
     *            </ul>
     * @return The Xdr data for the response.
     * @throws RpcException
     */
    public Xdr callRpc(String serverIP, Xdr xdrRequest, boolean usePrivilegedPort) throws RpcException {
        return NetMgr.getInstance().sendAndWait(serverIP, _port, usePrivilegedPort, xdrRequest, _rpcTimeout);
    }

    /**
     * Select an IP address to use for communication, based on the
     * <code>key</code> and <code>policy</code>.
     * 
     * @param key
     *            The key to use when selecting an IP address.
     * @return The IP address to use for communication.
     * @throws IOException
     */
    public String chooseIP(byte[] key) throws IOException {
        if (_ips == null || _ips.length == 0) {
            if (_server != null) {
                LOG.warn("ip list is not initialized, fallback to server");
                return _server;
            }
            throw new IOException("ip list is not initialized");
        }

        return _ips[Math.abs(Arrays.hashCode(key)) % _ips.length];
    }

    /**
     * The base functionality used by all NFS calls, which does basic return
     * code checking and throws an exception if this does not pass. Verbose
     * logging is also handled here. This method is not used by Portmap, Mount,
     * and Unmount calls.
     * 
     * @param request
     *            The request to send.
     * @param responseHandler
     *            A response handler.
     * @param ipAddress
     *            The IP address to use for communication.
     * @throws IOException
     */
    private void callRpcChecked(S request, RpcResponseHandler<? extends T> responseHandler, String ipAddress)
            throws IOException {
        if (_verbose) {
            LOG.info("server: %s port: %s %s", new Object[] { _server, _port, request.toString() });
        }

        callRpcNaked(request, responseHandler.getNewResponse(), ipAddress);

        if (_verbose) {
            String msg = String.format("server: %s port: %s %s", _server, _port,
                    responseHandler.getResponse().toString());
            LOG.info(msg);
        }

        responseHandler.checkResponse(request);
    }

    /**
     * Decide whether to retry or throw an exception.
     * 
     * @param e
     *            The exception.
     * @param attemptNumber
     *            The number of attempts so far.
     * @throws IOException
     */
    private void handleRpcException(RpcException e, int attemptNumber) throws IOException {
        String messageStart;

        if (!(e.getStatus().equals(RpcStatus.NETWORK_ERROR))) {
            messageStart = "rpc";
        } else {
            // check whether to retry
            if (attemptNumber + 1 < _maximumRetries) {
                try {
                    int waitTime = _retryWait * (attemptNumber + 1);
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    // restore the interrupt status
                    Thread.currentThread().interrupt();
                }
                LOG.info("network error happens, server {}, attemptNumber {}", new Object[] { _server, attemptNumber });
                return;
            }

            messageStart = "network";
        }

        throw new NfsException(NfsStatus.NFS3ERR_IO,
                String.format("%s error, server: %s, RPC error: %s", messageStart, _server, e.getMessage()), e);
    }

    /**
     * Find possible IP addresses for communicating with the server.
     * 
     * @return The array of addresses.
     */
    private String[] probeIps() {
        Set<String> ips = new TreeSet<String>();
        for (int i = 0; i < 32; ++i) {
            InetSocketAddress sa = new InetSocketAddress(_server, _port);
            ips.add(sa.getAddress().getHostAddress());
        }

        StringBuffer sb = new StringBuffer();
        for (String ip : ips) {
            sb.append(ip);
            sb.append(" ");
        }
        LOG.info(sb.toString());

        return (String[]) ips.toArray(new String[0]);
    }

}
