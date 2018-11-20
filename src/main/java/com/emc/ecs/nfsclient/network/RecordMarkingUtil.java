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
package com.emc.ecs.nfsclient.network;

import com.emc.ecs.nfsclient.rpc.Xdr;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * RFC1831: RECORD MARKING STANDARD When RPC messages are passed on top of a
 * byte stream transport protocol (like TCP), it is necessary to delimit one
 * message from another in order to detect and possibly recover from protocol
 * errors. This is called record marking (RM).
 * 
 * @author seibed
 */
public class RecordMarkingUtil {

    /**
     * Special constant used for the last fragment size. This is a number bigger
     * than the largest unsigned int, so it cannot be a real fragment size..
     */
    private static final int LAST_FRAG = 0x80000000;

    /**
     * The size mask is applied to the long fragment size to get the real
     * unsigned int size. When this mask is applied to <code>LAST_FRAG</code>,
     * the resulting real fragment size is 0. For all real fragment sizes, this
     * mask has no effect.
     */
    private static final int SIZE_MASK = 0x7fffffff;

    /**
     * RFC suggest setting the record size to MTU (Ethernet: MTU=1500 - 40(ip
     * and tcp header). But, When we send multiple fragments continuously, some
     * NFS server kill the connection and the report the error below:
     * "RPC: multiple fragments per record not supported" To bypass this
     * limitation, set a big MTU_SIZE number now.
     */
    private static final int MTU_SIZE = 1024 * 1024;

    /**
     * The usual logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecordMarkingUtil.class);

    /**
     * Insert record marking into rpcRequest and then send to tcp stream.
     * 
     * @param channel The Channel to use for sending.
     * @param rpcRequest The request to send.
     */
    static void putRecordMarkingAndSend(Channel channel, Xdr rpcRequest) {
        // XDR header buffer
        List<ByteBuffer> buffers = new LinkedList<>();
        buffers.add(ByteBuffer.wrap(rpcRequest.getBuffer(), 0, rpcRequest.getOffset()));

        // payload buffer
        if (rpcRequest.getPayloads() != null) {
            buffers.addAll(rpcRequest.getPayloads());
        }

        List<ByteBuffer> outBuffers = new ArrayList<>();

        int bytesToWrite = 0;
        int remainingBuffers = buffers.size();
        boolean isLast = false;

        for (ByteBuffer buffer : buffers) {

            if (bytesToWrite + buffer.remaining() > MTU_SIZE) {
                if (outBuffers.isEmpty()) {
                    LOG.error("too big single byte buffer {}", buffer.remaining());
                    throw new IllegalArgumentException(
                            String.format("too big single byte buffer %d", buffer.remaining()));
                } else {

                    sendBuffers(channel, bytesToWrite, outBuffers, isLast);

                    bytesToWrite = 0;
                    outBuffers.clear();
                }
            }

            outBuffers.add(buffer);
            bytesToWrite += buffer.remaining();
            remainingBuffers -= 1;
            isLast = (remainingBuffers == 0);
        }

        // send out remaining buffers
        if (!outBuffers.isEmpty()) {
            sendBuffers(channel, bytesToWrite, outBuffers, true);
        }
    }

    /**
     * Remove record marking from the byte array and convert to an Xdr.
     * 
     * @param bytes The byte array.
     * @return The Xdr.
     */
    static Xdr removeRecordMarking(byte[] bytes) {
        Xdr toReturn = new Xdr(bytes.length);
        Xdr input = new Xdr(bytes);

        long fragSize;
        boolean lastFragment = false;

        input.setOffset(0);
        int inputOff = input.getOffset();

        while (!lastFragment) {

            fragSize = input.getUnsignedInt();
            lastFragment = isLastFragment(fragSize);
            fragSize = maskFragmentSize(fragSize);

            toReturn.putBytes(input.getBuffer(), input.getOffset(), (int) fragSize);
            inputOff += fragSize;
            input.setOffset(inputOff);
        }

        // get xid
        int off = toReturn.getOffset();
        toReturn.setOffset(0);
        int xid = toReturn.getInt();
        toReturn.setXid(xid);
        toReturn.setOffset(off);

        return toReturn;
    }

    /**
     * @param channel
     * @param bytesToWrite
     * @param outBuffers
     * @param isLast
     */
    private static void sendBuffers(Channel channel, int bytesToWrite, List<ByteBuffer> outBuffers, boolean isLast) {
        ByteBuffer recSizeBuf = ByteBuffer.allocate(4);

        if (isLast) {
            recSizeBuf.putInt(LAST_FRAG | bytesToWrite);
        } else {
            recSizeBuf.putInt(bytesToWrite);
        }
        recSizeBuf.rewind();
        outBuffers.add(0, recSizeBuf);

        ByteBuffer[] outArray = outBuffers.toArray(new ByteBuffer[outBuffers.size()]);
        ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(outArray);
        channel.write(channelBuffer);
    }

    /**
     * @param fragmentSize
     *            A long that contains either the int number of bytes in the
     *            fragment, or a special value if this is the last fragment.
     * @return <code>true</code> if it is, <code>false</code> if it is not.
     */
    static boolean isLastFragment(long fragmentSize) {
        return ((fragmentSize & LAST_FRAG) != 0);
    }

    /**
     * @param fragmentSize
     *            A long that contains either the int number of bytes in the
     *            fragment, or a special value if this is the last fragment.
     * @return The masked fragment size, which is the real size of the fragment.
     */
    static long maskFragmentSize(long fragmentSize) {
        return fragmentSize & SIZE_MASK;
    }

    /**
     * Should never be used.
     */
    private RecordMarkingUtil() {
        throw new NotImplementedException("No class instances should be needed.");
    }

}
