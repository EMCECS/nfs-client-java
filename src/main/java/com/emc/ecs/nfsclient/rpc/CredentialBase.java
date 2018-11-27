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
package com.emc.ecs.nfsclient.rpc;

/**
 * @author seibed
 *
 */
public class CredentialBase implements Credential {

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.rpc.Credential#marshalling(com.emc.ecs.nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        xdr.putInt(getCredentialFlavor()); // credential flavor
        xdr.putByteArray(getCredential()); // bytes of credentials
        xdr.putInt(getVerifierFlavor()); // verifier flavor
        xdr.putByteArray(getVerifier()); // bytes of verifier
    }

    /**
     * The default value is AUTH_NONE.
     * 
     * @return The flavor.
     */
    protected int getCredentialFlavor() {
        return AUTH_NONE;
    }

    /**
     * The default value is a 0-byte array.
     * 
     * @return An Xdr structure with the bytes.
     */
    protected Xdr getCredential() {
        return new Xdr(0);
    }

    /**
     * The default value is AUTH_NONE.
     * 
     * @return The flavor.
     */
    protected int getVerifierFlavor() {
        return AUTH_NONE;
    }

    /**
     * The default value is a 0-byte array.
     * 
     * @return An Xdr structure with the bytes.
     */
    protected Xdr getVerifier() {
        return new Xdr(0);
    }

}
