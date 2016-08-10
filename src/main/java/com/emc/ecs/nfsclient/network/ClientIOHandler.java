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

import com.emc.ecs.nfsclient.rpc.Xdr;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;

/**
 * Each TCP connection has a corresponding ClientIOHandler instance.
 * ClientIOHandler includes the handler to receiving data and connection
 * establishment
 * 
 * @author seibed
 */
public class ClientIOHandler extends SimpleChannelHandler {

    /**
     * The usual logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientIOHandler.class);

    /**
     * The Netty helper class object.
     */
    private final ClientBootstrap _clientBootstrap;

    /**
     * The connection instance
     */
    private final Connection _connection;

    /**
     * The only constructor.
     * 
     * @param bootstrap
     *            A Netty helper instance.
     */
    public ClientIOHandler(ClientBootstrap bootstrap) {
        _clientBootstrap = bootstrap;
        _connection = (Connection) bootstrap.getOption(Connection.CONNECTION_OPTION);
    }

    /**
     * Convenience getter method.
     * 
     * @return The address.
     */
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) _clientBootstrap.getOption(Connection.REMOTE_ADDRESS_OPTION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.
     * netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        LOG.info("Connected to: {}", getRemoteAddress());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.
     * jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        closeConnection("Channel disconnected");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.
     * netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        closeConnection("Channel closed");
    }

    /**
     * Convenience method to standardize connection closing.We never try to
     * reconnect the tcp connections. the new connection will be launched when
     * new request is received. Reasons:
     * <ol>
     * <li>Portmap service will disconnect a tcp connection once it has been
     * idle for a few seconds.</li>
     * <li>Mounting service is listening to a temporary port, the port will
     * change after nfs server restart.</li>
     * <li>Even Nfs server may be listening to a temporary port.</li>
     * </ol>
     * 
     * @param messageStart
     *            A string used to start the log message.
     */
    private void closeConnection(String messageStart) {
        LOG.warn(messageStart + ": {}", getRemoteAddress());
        _connection.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.
     * netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.MessageEvent)
     */
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        byte[] rpcResponse = (byte[]) e.getMessage();
        // remove marking
        Xdr x = RecordMarkingUtil.removeRecordMarking(rpcResponse);
        // remove the request from timeout manager map
        int xid = x.getXid();
        _connection.notifySender(Integer.valueOf(xid), x);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.
     * netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ExceptionEvent)
     */
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Throwable cause = e.getCause();

        // don't print exception if it is BindException.
        // we are trying to search available port below 1024. It is not good to
        // print a flood
        // of error logs during the searching.
        if (cause instanceof java.net.BindException) {
            return;
        }

        LOG.error("Exception on connection to " + getRemoteAddress(), e.getCause());

        // close the channel unless we are connecting and it is
        // NotYetConnectedException
        if (!((cause instanceof NotYetConnectedException)
                && _connection.getConnectionState().equals(Connection.State.CONNECTING))) {
            ctx.getChannel().close();
        }
    }
}
