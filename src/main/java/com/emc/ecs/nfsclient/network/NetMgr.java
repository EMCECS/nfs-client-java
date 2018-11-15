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
import com.emc.ecs.nfsclient.rpc.Xdr;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Singleton class to manage all Connection instances
 * 
 * @author seibed
 */
public class NetMgr {

    /**
     * The single instance.
     */
    private static final NetMgr _instance = new NetMgr();

    /**
     * @return The instance.
     */
    public static NetMgr getInstance() {
        return _instance;
    }

    /**
     * Construct the private instance.
     */
    private NetMgr() {
        super();
    }

    /**
     * connection tracking map
     */
    private ConcurrentHashMap<InetSocketAddress, Connection> _connectionMap = new ConcurrentHashMap<InetSocketAddress, Connection>();

    /**
     * privileged connection tracking map
     */
    private ConcurrentHashMap<InetSocketAddress, Connection> _privilegedConnectionMap = new ConcurrentHashMap<InetSocketAddress, Connection>();

    /**
     * Netty helper instance.
     */
    private ChannelFactory _factory = new NioClientSocketChannelFactory(newThreadPool(), newThreadPool());

    /**
     * @return a thread pool instance using the proper factory to create daemon threads
     */
    private static final ExecutorService newThreadPool() {
        return Executors.newCachedThreadPool(getThreadFactory());
    }

    /**
     * @return a thread factory that creates daemon threads
     */
    private static ThreadFactory getThreadFactory() {
        return new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }

        };

    }

    /**
     * Basic RPC call functionality only. Send the request, creating a new
     * connection as necessary, and return the raw Xdr returned.
     * 
     * @param serverIP
     *            The endpoint of the server being called.
     * @param port
     *            The remote host port being called for this operation.
     * @param usePrivilegedPort
     *            <ul>
     *            <li>If <code>true</code>, use a privileged local port (below
     *            1024) for RPC communication.</li>
     *            <li>If <code>false</code>, use any non-privileged local port
     *            for RPC communication.</li>
     *            </ul>
     * @param xdrRequest
     *            The Xdr data for the request.
     * @param timeout
     *            The timeout in seconds.
     * @return The Xdr data for the response.
     * @throws RpcException
     */
    public Xdr sendAndWait(String serverIP, int port, boolean usePrivilegedPort, Xdr xdrRequest, int timeout) throws RpcException {
        InetSocketAddress key = InetSocketAddress.createUnresolved(serverIP, port);

        Map<InetSocketAddress, Connection> connectionMap = usePrivilegedPort ? _privilegedConnectionMap : _connectionMap;
        Connection connection = connectionMap.get(key);
        if (connection == null) {
            connection = new Connection(serverIP, port, usePrivilegedPort);
            connectionMap.put(key, connection);
            connection.connect();
        }

        return connection.sendAndWait(timeout, xdrRequest);
    }

    /**
     * Remove a dropped connection from the map.
     * 
     * @param key
     *            The key
     */
    public void dropConnection(InetSocketAddress key) {
        _connectionMap.remove(key);
        _privilegedConnectionMap.remove(key);
    }

    /**
     * Called when the application is being shut down.
     */
    public void shutdown() {
        for (Connection connection : _connectionMap.values()) {
            connection.shutdown();
        }

        for (Connection connection : _privilegedConnectionMap.values()) {
            connection.shutdown();
        }

        _factory.releaseExternalResources();
    }

    /**
     * Getter method for Factory access.
     * 
     * @return The factory.
     */
    public ChannelFactory getFactory() {
        return _factory;
    }
}
