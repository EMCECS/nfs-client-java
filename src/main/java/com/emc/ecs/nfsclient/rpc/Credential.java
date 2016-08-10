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
 * Used to handle RPC Credentials, as specified by RFC 1831
 * (https://tools.ietf.org/html/rfc1831).
 * 
 * @author seibed
 */
public interface Credential {

    /**
     * Discriminant for calls where the client does not know its identity or the
     * server does not care who the client is. Specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    static final int AUTH_NONE = 0;

    /**
     * Put the credential into an XDR buffer.
     * 
     * @param xdr
     */
    void marshalling(Xdr xdr);

}
