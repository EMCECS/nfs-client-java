/**
 * Copyright 2016-2018 Dell Inc. or its subsidiaries. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 * <p>
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.ecs.nfsclient.network;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * To receive the entire response. We do not actually decode the rpc packet here.
 * Just get the size from the packet and then put them in internal buffer until all data arrive.
 *
 * @author seibed
 */
public class RPCRecordDecoder extends FrameDecoder {

    /**
     * Holds the calculated record length for each channel until the Channel is ready for buffering.
     * Reset to 0 after that for the next channel.
     */
    private int _recordLength = 0;

    // To hold the real position of fragment starts
    private int _realReaderIndex = 0;

    /* (non-Javadoc)
     * @see org.jboss.netty.handler.codec.frame.FrameDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer)
     */
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, ChannelBuffer channelBuffer) throws Exception {
        // Wait until the length prefix is available.
        if (channelBuffer.readableBytes() < 4) {
            // If null is returned, it means there is not enough data yet.
            // FrameDecoder will call again when there is a sufficient amount of data available.
            return null;
        }

        //marking the current reading position
        channelBuffer.markReaderIndex();
        channelBuffer.skipBytes(_realReaderIndex);
        //get the fragment size and wait until the entire fragment is available.
        long fragSize = channelBuffer.readUnsignedInt();
        boolean lastFragment = RecordMarkingUtil.isLastFragment(fragSize);
        fragSize = RecordMarkingUtil.maskFragmentSize(fragSize);
        if (channelBuffer.readableBytes() < fragSize) {
            channelBuffer.resetReaderIndex();
            return null;
        }

        //seek to the beginning of the next fragment
        channelBuffer.skipBytes((int) fragSize);

        _recordLength += 4 + (int) fragSize;

        System.out.println("[test]current realindex=" + _realReaderIndex + " readerIndex=" + channelBuffer.readerIndex() + " length=" + _recordLength + " fragSize=" + fragSize + " readable=" + channelBuffer.readableBytes());

        //check the last fragment
        if (!lastFragment) {
            channelBuffer.resetReaderIndex();
            _realReaderIndex += 4 + (int) fragSize;
            //not the last fragment, the data is put in an internally maintained cumulative buffer
            return null;
        }
        System.out.println("[test]is last");
        byte[] rpcResponse = new byte[_recordLength];

        channelBuffer.readerIndex(channelBuffer.readerIndex() - _recordLength);

        channelBuffer.readBytes(rpcResponse, 0, _recordLength);

        _recordLength = 0;
        _realReaderIndex = 0;
        return rpcResponse;
    }
}
