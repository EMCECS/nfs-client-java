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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the serialization and deserialization of primitive types
 * into and out of a buffer, as specified in RFC 1014
 * (https://tools.ietf.org/html/rfc1014).
 * 
 * @author seibed
 */
public class Xdr {

    /**
     * The buffer used to hold XDR data.
     */
    private byte[] _buffer;

    /**
     * The size (capacity) of the buffer in bytes.
     */
    private int _size;

    /**
     * The current offset of the buffer in bytes.
     */
    private int _offset;

    /**
     * The xdr id - used only by the client.
     */
    private int _xid;

    /**
     * payload with this XDR
     */
    private List<ByteBuffer> _payloads;

    /**
     * The current payload size in bytes.
     */
    private int _payloadsSize;

    /**
     * Build an Xdr object for serialization.
     *
     * @param size
     *            Size of the buffer in bytes.
     */
    public Xdr(int size) {
        _buffer = new byte[size];
        _size = size;
        _offset = 0;
    }

    /**
     * Build an Xdr object for deserialization.
     * 
     * @param data
     *            The data for the buffer.
     */
    public Xdr(byte[] data) {
        _buffer = data.clone();
        _size = data.length;
        _offset = 0;
    }

    /**
     * Skip a number of bytes. Note that the count is rounded up to the next
     * BLOCK_SIZE.
     * 
     * @param count
     *            of the buffer in bytes
     */
    public void skip(int count) {
        int bytesToPad = getBytesOfPadding(_offset += count);
        _offset += bytesToPad;
    }

    /**
     * Return the entire Xdr buffer
     * 
     * @return The Xdr buffer
     */
    public byte[] getBuffer() {
        return _buffer.clone();
    }

    /**
     * Return the current offset
     * 
     * @return offset into XDR buffer
     */
    public int getOffset() {
        return _offset;
    }

    /**
     * Set the current offset
     * 
     * @param offset
     *            offset into XDR buffer
     */
    public void setOffset(int offset) {
        _offset = offset;
    }

    /**
     * Return the current size of the XDR buffer
     * 
     * @return size in bytes
     */
    public int getSize() {
        return _size;
    }

    /**
     * Set the current size of the XDR buffer
     * 
     * @param size
     *            Size of the buffer in bytes
     */
    public void setSize(int size) {
        _size = size;
    }

    /**
     * Get an integer from the buffer
     * 
     * @return integer
     */
    public int getInt() {
        return ((_buffer[_offset++] & 0xff) << 24 | (_buffer[_offset++] & 0xff) << 16 | (_buffer[_offset++] & 0xff) << 8
                | (_buffer[_offset++] & 0xff));
    }

    /**
     * Put an integer into the buffer
     * 
     * @param i
     *            Integer to store in XDR buffer.
     */
    public void putInt(int i) {
        _buffer[_offset++] = (byte) (i >>> 24);
        _buffer[_offset++] = (byte) (i >> 16);
        _buffer[_offset++] = (byte) (i >> 8);
        _buffer[_offset++] = (byte) i;
    }

    /**
     * Get an unsigned integer from the buffer Note that Java has no unsigned
     * integer type so we must return it as a long.
     * 
     * @return long
     */
    public long getUnsignedInt() {
        return ((_buffer[_offset++] & 0xff) << 24 | (_buffer[_offset++] & 0xff) << 16 | (_buffer[_offset++] & 0xff) << 8
                | (_buffer[_offset++] & 0xff));
    }

    /**
     * Put an unsigned integer into the buffer Note that Java has no unsigned
     * integer type so we must pass it as a long.
     * 
     * @param i
     *            unsigned integer to store in XDR buffer.
     */
    public void putUnsignedInt(long i) {
        _buffer[_offset++] = (byte) (i >>> 24 & 0xff);
        _buffer[_offset++] = (byte) (i >> 16);
        _buffer[_offset++] = (byte) (i >> 8);
        _buffer[_offset++] = (byte) i;
    }

    /**
     * Get a long from the buffer
     * 
     * @return long
     */
    public long getLong() {
        return ((long) (_buffer[_offset++] & 0xff) << 56 | (long) (_buffer[_offset++] & 0xff) << 48
                | (long) (_buffer[_offset++] & 0xff) << 40 | (long) (_buffer[_offset++] & 0xff) << 32
                | (long) (_buffer[_offset++] & 0xff) << 24 | (long) (_buffer[_offset++] & 0xff) << 16
                | (long) (_buffer[_offset++] & 0xff) << 8 | (long) (_buffer[_offset++] & 0xff));
    }

    /**
     * Put a long into the buffer
     * 
     * @param i
     *            long to store in XDR buffer
     */
    public void putLong(long i) {
        _buffer[_offset++] = (byte) (i >>> 56);
        _buffer[_offset++] = (byte) ((i >> 48) & 0xff);
        _buffer[_offset++] = (byte) ((i >> 40) & 0xff);
        _buffer[_offset++] = (byte) ((i >> 32) & 0xff);
        _buffer[_offset++] = (byte) ((i >> 24) & 0xff);
        _buffer[_offset++] = (byte) ((i >> 16) & 0xff);
        _buffer[_offset++] = (byte) ((i >> 8) & 0xff);
        _buffer[_offset++] = (byte) (i & 0xff);
    }

    /*
     * Note: we have no XDR routines for encoding/decoding unsigned longs. They
     * exist in XDR but not in Java hence we cannot represent them. Best just to
     * use xdr_hyper() and hope the sign bit is not used.
     */

    /**
     * Get a boolean from the buffer
     *
     * @return boolean
     */
    public boolean getBoolean() {
        return (getInt() != 0);
    }

    /**
     * Put a boolean into the buffer
     * 
     * @param b
     *            boolean
     */
    public void putBoolean(boolean b) {
        putInt(b ? 1 : 0);
    }

    /**
     * Get a floating point number from the buffer
     *
     * @return float
     */
    public float getFloat() {
        return (Float.intBitsToFloat(getInt()));
    }

    /**
     * Put a floating point number into the buffer
     * 
     * @param f
     *            float
     */
    public void putFloat(float f) {
        putInt(Float.floatToIntBits(f));
    }

    /**
     * Get a string from the buffer
     * 
     * @return string
     */
    public String getString() {
        int len = getInt();
        String s = new String(_buffer, _offset, len, RpcRequest.CHARSET);
        skip(len);
        return s;
    }

    /**
     * Put a string into the buffer
     * 
     * @param s
     *            string
     */
    public void putString(String s) {
        putByteArray(s.getBytes(RpcRequest.CHARSET));
    }

    /**
     * Get a counted array of bytes from the buffer
     * 
     * @return bytes
     */
    public byte[] getByteArray() {
        int lengthToCopy = getInt();
        byte[] byteArray = (lengthToCopy == 0) ? null : new byte[lengthToCopy];
        getBytes(lengthToCopy, byteArray, 0);
        return byteArray;
    }

    /**
     * Get bytes from the xdr buffer to the input buffer
     * 
     * @param lengthToCopy
     *            Number of bytes to copy.
     * @param copyArray
     *            Array to hold the copied data.
     * @param copyOffset
     *            Where to start the copy.
     */
    public void getBytes(int lengthToCopy, byte[] copyArray, int copyOffset) {
        if (lengthToCopy > 0) {
            System.arraycopy(_buffer, _offset, copyArray, copyOffset, lengthToCopy);
            skip(lengthToCopy);
        }

    }

    /**
     * Put a counted array of bytes into the buffer. Note that the entire byte
     * array is encoded.
     *
     * @param b
     *            byte array
     */
    public void putByteArray(byte[] b) {
        putByteArray(b, 0, b.length);
    }

    /**
     * Put a counted array of bytes into the buffer
     *
     * @param b
     *            byte array
     * @param len
     *            number of bytes to encode
     */
    public void putByteArray(byte[] b, int len) {
        putByteArray(b, 0, len);
    }

    /**
     * Put a counted array of bytes into the buffer
     * 
     * @param b
     *            byte array
     * @param boff
     *            offset into byte array
     * @param len
     *            number of bytes to encode
     */
    public void putByteArray(byte[] b, int boff, int len) {
        putInt(len);
        putBytes(b, boff, len);
    }

    /**
     * Put an Xdr buffer into the buffer This is used to encode the RPC
     * credentials
     *
     * @param x
     *            XDR buffer
     */
    public void putByteArray(Xdr x) {
        putByteArray(x.getBuffer(), x.getOffset());
    }

    /**
     * Put a counted array of bytes into the buffer. The length is not encoded.
     * 
     * @param b
     *            byte array
     * @param boff
     *            offset into byte array
     * @param len
     *            number of bytes to encode
     */
    public void putBytes(byte[] b, int boff, int len) {
        System.arraycopy(b, boff, _buffer, _offset, len);
        skip(len);
    }

    /**
     * @return the xid.
     */
    public int getXid() {
        return _xid;
    }

    /**
     * Set xid
     * 
     * @param xid
     */
    public void setXid(int xid) {
        _xid = xid;
    }

    /**
     * add payloads, more than one can be added.
     *
     * @param payloads
     * @param size
     */
    public void putPayloads(List<ByteBuffer> payloads, int size) {
        putInt(size);
        if (_payloads == null) {
            _payloads = payloads;
        } else {
            _payloads.addAll(payloads);
        }
        _payloadsSize += size;

    }

    /**
     * @return The terminated list of payloads, properly padded and ready for
     *         sending.
     */
    public List<ByteBuffer> getPayloads() {
        ByteBuffer terminalPadding = getTerminalPadding();
        if (terminalPadding != null) {
            List<ByteBuffer> paddingList = new ArrayList<ByteBuffer>(1);
            paddingList.add(terminalPadding);
            putPayloads(paddingList, terminalPadding.remaining());
        }
        return _payloads;
    }

    /**
     * The representation of all items requires a multiple of four bytes (or 32
     * bits) of data. The bytes are numbered 0 through n-1. The bytes are read
     * or written to some byte stream such that byte m always precedes byte m+1.
     * If the n bytes needed to contain the data are not a multiple of four,
     * then the n bytes are followed by enough (0 to 3) residual zero bytes, r,
     * to make the total byte count a multiple of 4.
     */
    private static int BLOCK_SIZE = 4;

    /**
     * The terminal padding - to avoid needing to reallocate this many times.
     */
    private static final byte[] MAXIMUM_TERMINAL_PADDING = new byte[BLOCK_SIZE];

    /**
     * The representation of all items requires a multiple of four bytes (or 32
     * bits) of data. The bytes are numbered 0 through n-1. The bytes are read
     * or written to some byte stream such that byte m always precedes byte m+1.
     * If the n bytes needed to contain the data are not a multiple of four,
     * then the n bytes are followed by enough (0 to 3) residual zero bytes, r,
     * to make the total byte count a multiple of 4.
     * 
     * <p>
     * Package access used to allow testing.
     * </p>
     * 
     * @return The proper amount of terminal padding.
     */
    ByteBuffer getTerminalPadding() {
        int bytesOfPadding = getBytesOfPadding(_offset + _payloadsSize);
        if (bytesOfPadding == 0) {
            return null;
        } else {
            return ByteBuffer.wrap(MAXIMUM_TERMINAL_PADDING, 0, bytesOfPadding);
        }

    }

    /**
     * The representation of each item requires a multiple of four bytes (or 32
     * bits) of data. The bytes are numbered 0 through n-1. The bytes are read
     * or written to some byte stream such that byte m always precedes byte m+1.
     * If the n bytes needed to contain the data are not a multiple of four,
     * then the n bytes are followed by enough (0 to 3) residual zero bytes, r,
     * to make the total byte count a multiple of 4.
     * 
     * @param currentBytes
     *            The current number of bytes in the structure.
     * @return The number of bytes of padding needed.
     */
    private static int getBytesOfPadding(int currentBytes) {
        int bytesOverLastXdrBlock = currentBytes % BLOCK_SIZE;
        return (BLOCK_SIZE - bytesOverLastXdrBlock) % BLOCK_SIZE;
    }

}
