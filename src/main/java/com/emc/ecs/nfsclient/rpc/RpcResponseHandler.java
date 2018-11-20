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

import java.io.IOException;

/**
 * This class allows users to specify how new response instances are created
 * during repeated RPC calls, and how responses are validated before their data
 * is used.
 * 
 * @author seibed
 */
public abstract class RpcResponseHandler<T> {

    /**
     * The current response.
     */
    private T _response;

    /**
     * Get the current response.
     * 
     * @return the current response
     */
    public T getResponse() {
        return _response;
    }

    /**
     * Create a new response and return it.
     * 
     * @return the new response
     */
    public T getNewResponse() {
        _response = makeNewResponse();
        return _response;
    }

    /**
     * This is implemented in all concrete subclasses, so that the new response
     * can be created using any available parameters.
     * 
     * @return the new response.
     */
    protected abstract T makeNewResponse();

    /**
     * This is implemented in all concrete subclasses to check responses for
     * validity, and throws an IOException if the response indicates that the
     * request failed.
     * 
     * @param request
     *            This is used for error messages, to aid debugging, since the
     *            response will likely not have enough data to determine the
     *            request that caused it.
     * @throws IOException
     */
    public abstract void checkResponse(RpcRequest request) throws IOException;

}
