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
package com.emc.ecs.nfsclient.nfs;

import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * Holder for weak cache consistency data, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsWccData {

    /**
     * Attributes before the operation.
     */
    private final NfsPreOpAttributes _preOpAttributes;

    /**
     * Attributes after the operation.
     */
    private final NfsGetAttributes _attributes;

    /**
     * Creates an empty structure.
     */
    public NfsWccData() {
        this(null);
    }

    /**
     * Creates a structure populated from an Xdr response, as specified by RFC
     * 1813 (https://tools.ietf.org/html/rfc1813).
     * 
     * @param xdr
     *            the response
     */
    public NfsWccData(Xdr xdr) {
        _preOpAttributes = makePreOpAttributes(xdr);
        _attributes = makeAttributes(xdr);
    }

    /**
     * @return Attributes before the operation.
     */
    public NfsPreOpAttributes getPreOpAttributes() {
        return _preOpAttributes;
    }

    /**
     * @return Attributes after the operation.
     */
    public NfsGetAttributes getAttributes() {
        return _attributes;
    }

    /**
     * Extracts the pre-operation attributes.
     * @param xdr
     * @return The attributes.
     */
    private static NfsPreOpAttributes makePreOpAttributes(Xdr xdr) {
        NfsPreOpAttributes preOpAttributes = null;
        if ((xdr != null) && xdr.getBoolean()) {
            preOpAttributes = new NfsPreOpAttributes(xdr);
        }
        return preOpAttributes;
    }

    /**
     * Extracts the post-operation attributes.
     * @param xdr
     * @return The attributes.
     */
    private static NfsGetAttributes makeAttributes(Xdr xdr) {
        NfsGetAttributes attributes = null;
        if (xdr != null) {
            attributes = NfsResponseBase.makeNfsGetAttributes(xdr);
        }
        return attributes;
    }

}
