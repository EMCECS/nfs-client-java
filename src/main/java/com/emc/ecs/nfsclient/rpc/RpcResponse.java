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

/**
 * An RPC response, as specified by RFC 1831
 * (https://tools.ietf.org/html/rfc1831).
 * 
 * @author seibed
 */
public class RpcResponse {

    /**
     * Accept status NOT specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final int NOT_ACCEPTED = -1;

    /**
     * Reject status NOT specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final int NOT_REJECTED = -1;

    /**
     * The <code>xid</code> from the request, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    protected int _xid;

    /**
     * The message type, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831). Should be 1 (REPLY).
     */
    private int _direction;

    /**
     * The reply status, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private int _replyStatus;

    /**
     * The accept status, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private int _acceptStatus = NOT_ACCEPTED;

    /**
     * The reject status, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private int _rejectStatus = NOT_REJECTED;

    /**
     * Load this structure from the response data, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     * 
     * @param xdr
     *            The xdr response data.
     * @throws RpcException
     */
    public void unmarshalling(Xdr xdr) throws RpcException {
        xdr.setOffset(0);
        _xid = xdr.getInt();
        _direction = xdr.getInt();
        _replyStatus = xdr.getInt();
        if (_replyStatus == ReplyStatus.MSG_ACCEPTED.getValue()) {
            xdr.skip(4);            // verifier flavor
            // byte[] verifier = x.xdr_bytes();
            xdr.getByteArray();
            _acceptStatus = xdr.getInt();
        } else {
            _rejectStatus = xdr.getInt();
        }

        checkRpcReply();
    }

    /**
     * Check whether the reply is successful. If not, log and throw exception.
     * the function is usually called after unmarshalling.
     * 
     * @throws RpcException
     */
    private void checkRpcReply() throws RpcException {

        if (_replyStatus != ReplyStatus.MSG_ACCEPTED.getValue()) {
            String msg = String.format("RPC call is REJECTED, rejectStat=%d", _rejectStatus);
            throw new RpcException(RejectStatus.fromValue(_rejectStatus), msg);
        } else {
            if (_acceptStatus != AcceptStatus.SUCCESS.getValue()) {
                String msg = String.format("RPC call is ACCEPTED, but the status is not success, acceptStat=%d",
                        _acceptStatus);
                throw new RpcException(AcceptStatus.fromValue(_acceptStatus), msg);
            }
        }
    }

    /**
     * The <code>xid</code> from the request, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    public int getXid() {
        return _xid;
    }

    /**
     * @return The message type, as specified by RFC 1831
     *         (https://tools.ietf.org/html/rfc1831). Should be 1 (REPLY).
     */
    public int getDirection() {
        return _direction;
    }

    /**
     * @return The reply status, as specified by RFC 1831
     *         (https://tools.ietf.org/html/rfc1831).
     */
    public int getReplyStatus() {
        return _replyStatus;
    }

    /**
     * @return The accept status, as specified by RFC 1831
     *         (https://tools.ietf.org/html/rfc1831).
     */
    public int getAcceptStatus() {
        return _acceptStatus;
    }

    /**
     * @return The reject status, as specified by RFC 1831
     *         (https://tools.ietf.org/html/rfc1831).
     */
    public int getRejectStatus() {
        return _rejectStatus;
    }

}
