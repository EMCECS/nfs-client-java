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
package com.emc.ecs.nfsclient.network;

import com.emc.ecs.nfsclient.rpc.RpcException;
import com.emc.ecs.nfsclient.rpc.RpcStatus;
import com.emc.ecs.nfsclient.rpc.Xdr;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioSocketChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Each Connection instance manages a tcp connection. The class is used to send
 * data and track the status of a connection.
 * 
 * @author seibed
 */
public class Connection {

    /**
     * Key for getting the connection from the helper map.
     */
    static final String CONNECTION_OPTION = "bourneLocalConn";

    /**
     * Key for getting the remote address from the helper map.
     */
    static final String REMOTE_ADDRESS_OPTION = "remoteAddress";

    /**
     * The usual logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    /**
     * The value in milliseconds.
     */
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds

    /**
     * internal sending queue (not tcp sending buffer) the rename/lookup/readdir
     * has size between 256-516bytes, 5M can hold 10000 pending requests for
     * data writing, each request is 512K and maximum 40 pending requests are
     * allowed. (512k is the preferred size in a single request. For object >
     * 512K, it should be split to 512K-size chunks.) the queue size need be
     * 20M.
     */
    private static final int MAX_SENDING_QUEUE_SIZE = 1 * 1024 * 1024 * 1024;// bytes - total
                                                                // 1G

    /**
     * Netty helper instance.
     */
    private final ClientBootstrap _clientBootstrap;

    /**
     * Netty channel representing a tcp connection.
     */
    private Channel _channel;

    /**
     * Netty helper instance.
     */
    ChannelFuture _channelFuture = Channels.future(null, true);

    /**
     * The remote server address, in any form.
     */
    private final String _remoteHost;

    /**
     * The remote server port being used.
     */
    private final int _port;

    /**
     * <ul><li>
     * If <code>true</code>, use a privileged local port (below 1024) for RPC communication.
     * </li><li>
     * If <code>false</code>, use any non-privileged local port for RPC communication.
     * </li></ul>
     */
    private final boolean _usePrivilegedPort;

    /**
     * Store the ChannelFuture instances while they are in progress. The map is final, but the content will change.
     */
    private final ConcurrentHashMap<Integer, ChannelFuture> _futureMap = new ConcurrentHashMap<Integer, ChannelFuture>();

    /**
     * Store the Xdr response instances while they are in progress. The map is final, but the content will change.
     */
    private final ConcurrentHashMap<Integer, Xdr> _responseMap = new ConcurrentHashMap<Integer, Xdr>();

    /**
     * Simple enums for communicating connection states.
     * 
     * @author seibed
     */
    public enum State {
        DISCONNECTED, CONNECTING, CONNECTED;
    }

    /**
     * The current state.
     */
    private State _state = State.DISCONNECTED;

    /**
     * @param remoteHost A unique name for the host to which the connection is being made.
     * @param port The remote host port being used for the connection.
     * @param usePrivilegedPort
     *            <ul>
     *            <li>If <code>true</code>, use a privileged port (below 1024)
     *            for RPC communication.</li>
     *            <li>If <code>false</code>, use any non-privileged port for RPC
     *            communication.</li>
     *            </ul>
     */
    public Connection(String remoteHost, int port, boolean usePrivilegedPort) {
        _remoteHost = remoteHost;
        _port = port;
        _usePrivilegedPort = usePrivilegedPort;
        _clientBootstrap = new ClientBootstrap(NetMgr.getInstance().getFactory());
        // Configure the client.
        _clientBootstrap.setOption(REMOTE_ADDRESS_OPTION, new InetSocketAddress(_remoteHost, _port));
        _clientBootstrap.setOption("connectTimeoutMillis", CONNECT_TIMEOUT);  // set
                                                                              // connection
                                                                              // timeout
                                                                              // value
                                                                              // to
                                                                              // 10
                                                                              // seconds
        _clientBootstrap.setOption("tcpNoDelay", true);
        _clientBootstrap.setOption("keepAlive", true);
        _clientBootstrap.setOption(CONNECTION_OPTION, this);

        // Configure the pipeline factory.
        _clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            /**
             * Netty helper instance.
             */
            private final ChannelHandler ioHandler = new ClientIOHandler(_clientBootstrap);

            /* (non-Javadoc)
             * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
             */
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new RPCRecordDecoder(), ioHandler);
            }
        });
    }

    /**
     * Convenience getter method.
     * 
     * @return The remote server internet address.
     */
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) _clientBootstrap.getOption(REMOTE_ADDRESS_OPTION);
    }

    /**
     * Convenience getter method.
     * 
     * @return The state.
     */
    public State getConnectionState() {
        return _state;
    }

    /**
     * Send a RPC request and wait until a response is received or timeout. The
     * function will not retry. It is the responsibility of the application to
     * do retry. The behaviours: a. If the tcp connection is not established
     * yet: (1). sendAndWait will wait until the connection is established or
     * timeout or network error occurs. (2). Once the connection is established,
     * sendAndWait can send data. b. If the tcp connection is established: (1).
     * the request is put in the internal queue of netty. Netty will send it
     * asap. If there are already lots of pending requests in the queue,
     * sendAndWait return error. (2). sendAndWait will wait until it gets a
     * response from NFS server or timeout. (3). If the tcp connection is
     * broken, the function return error with network error. c. If the tcp
     * connection is broken suddenly: (1) the old sendAndWait will get the
     * network error or timeout (2) The new sendAndWait will follow (a).
     * 
     * @param timeout
     *            The timeout in seconds.
     * @param xdrRequest
     *            The generic RPC data and protocol-specific data.
     * @return The Xdr data for the response.
     * @throws RpcException
     */
    public Xdr sendAndWait(int timeout, Xdr xdrRequest) throws RpcException {
        // no lock is required here.
        // The status may be changed after the checking,
        // or there exists a small window that the status is not consistent to
        // the actual tcp connection state.
        // Both above cases will not cause any issues.
        if (_state.equals(State.CONNECTED) == false) {
            _channelFuture.awaitUninterruptibly();
            if (_channelFuture.isSuccess() == false) {

                String msg = String.format("waiting for connection to be established, but failed %s",
                        getRemoteAddress());
                LOG.error(msg);

                // return RpcException, the exact reason should already be
                // logged in IOHandler::exceptionCaught()
                throw new RpcException(RpcStatus.NETWORK_ERROR, msg);
            }
        }

        // check whether the internal queue of netty has enough spaces to hold
        // the request
        // False means that the too many pending requests are in the queue or
        // the connection is closed.
        if (_channel.isWritable() == false) {
            String msg;
            if (_channel.isConnected()) {
                msg = String.format("too many pending requests for the connection: %s", getRemoteAddress());
            } else {
                msg = String.format("the connection is broken: %s", getRemoteAddress());
            }

            // too many pending request are in the queue, return error
            throw new RpcException(RpcStatus.NETWORK_ERROR, msg);
        }

        // put the request into a map for timeout management
        ChannelFuture timeoutFuture = Channels.future(_channel);
        Integer xid = Integer.valueOf(xdrRequest.getXid());
        _futureMap.put(xid, timeoutFuture);

        // put the request into the queue of the netty, netty will send data
        // asynchronously
        RecordMarkingUtil.putRecordMarkingAndSend(_channel, xdrRequest);

        timeoutFuture.awaitUninterruptibly(timeout, TimeUnit.SECONDS);

        // remove the response from timeout maps
        Xdr response = _responseMap.remove(xid);
        _futureMap.remove(xid);

        if (timeoutFuture.isSuccess() == false) {

            LOG.info("cause:", timeoutFuture.getCause());

            if (timeoutFuture.isDone()) {
                String msg = String.format("tcp IO error on the connection: %s", getRemoteAddress());
                throw new RpcException(RpcStatus.NETWORK_ERROR, msg);
            } else {
                String msg = String.format("rpc request timeout on the connection: %s", getRemoteAddress());
                throw new RpcException(RpcStatus.NETWORK_ERROR, msg);
            }
        }

        return response;
    }

    /**
     * If there is no current connection, start a new tcp connection asynchronously.
     * 
     * @throws RpcException
     */
    protected void connect() throws RpcException {
        if (_state.equals(State.CONNECTED)) {
            return;
        }

        final ChannelFuture oldChannelFuture = _channelFuture;

        String logPrefix = _usePrivilegedPort ? "usePrivilegedPort " : "";
        LOG.info(logPrefix + "connecting to {}", getRemoteAddress());
        _state = State.CONNECTING;

        if (_usePrivilegedPort) {
            _channel = bindToPrivilegedPort();
            _channelFuture = _channel.connect(getRemoteAddress());
        } else {
            _channelFuture = _clientBootstrap.connect();
            _channel = _channelFuture.getChannel();
        }

        NioSocketChannelConfig cfg = (NioSocketChannelConfig) _channel.getConfig();
        cfg.setWriteBufferHighWaterMark(MAX_SENDING_QUEUE_SIZE);

        _channelFuture.addListener(new ChannelFutureListener() {
            /* (non-Javadoc)
             * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
             */
            public void operationComplete(ChannelFuture future) {
                if (_channelFuture.isSuccess()) {
                    _state = State.CONNECTED;
                    oldChannelFuture.setSuccess();
                } else {
                    _state = State.DISCONNECTED;
                    oldChannelFuture.cancel();
                }

            }
        });

    }

    /**
     * This is called when the application is shutdown or the channel is closed.
     */
    protected void shutdown() {
        if (_channel != null) {
            _channel.close();
        }
    }

    /**
     * This is called when the connection should be closed.
     */
    protected void close() {
        _state = State.DISCONNECTED;

        shutdown();

        // remove the connection from map
        NetMgr.getInstance().dropConnection(InetSocketAddress.createUnresolved(_remoteHost, _port));

        // notify all the pending requests in the timeout map
        notifyAllPendingSenders("Channel closed, connection closing.");
    }

    /**
     * Update the response map with the response and notify the thread waiting for the
     * response. Do nothing if the future has been removed.
     * 
     * @param xid
     * @param response
     */
    protected void notifySender(Integer xid, Xdr response) {
        ChannelFuture future = _futureMap.get(xid);
        if (future != null) {
            _responseMap.put(xid, response);
            future.setSuccess();
        }
    }

    /**
     * Notify all the senders of all pending requests
     */
    protected void notifyAllPendingSenders(String message) {
        for (ChannelFuture future : _futureMap.values()) {
            future.setFailure(new Error(message));
        }
    }

    /**
     * This attempts to bind to privileged ports, starting with 1023 and working downwards, and returns when the first binding succeeds.
     * 
     * <p>
     * Some NFS servers apparently may require that some requests originate on
     * an Internet port below IPPORT_RESERVED (1024). This is generally not
     * used, though, as the client then has to run as a user authorized for
     * privileged, which is dangerous. It is also not generally needed.
     * </p>
     * 
     * @return
     *         <ul>
     *         <li><code>true</code> if the binding succeeds,</li>
     *         <li><code>false</code> otherwise.</li>
     *         </ul>
     * @throws RpcException If an exception occurs, or if no binding succeeds.
     */
    private Channel bindToPrivilegedPort() throws RpcException {
        System.out.println("Attempting to use privileged port.");
        for (int port = 1023; port > 0; --port) {
            try {
                ChannelPipeline pipeline = _clientBootstrap.getPipelineFactory().getPipeline();
                Channel channel = _clientBootstrap.getFactory().newChannel(pipeline);
                channel.getConfig().setOptions(_clientBootstrap.getOptions());
                ChannelFuture bindFuture = channel.bind(new InetSocketAddress(port)).awaitUninterruptibly();
                if (bindFuture.isSuccess()) {
                    System.out.println("Success! Bound to port " + port);
                    return bindFuture.getChannel();
                }
            } catch (Exception e) {
                String msg = String.format("rpc request bind error for address: %s", 
                        getRemoteAddress());
                throw new RpcException(RpcStatus.NETWORK_ERROR, msg, e);
            }
        }

        throw new RpcException(RpcStatus.LOCAL_BINDING_ERROR, String.format("Cannot bind a port < 1024: %s", getRemoteAddress()));
    }

}
