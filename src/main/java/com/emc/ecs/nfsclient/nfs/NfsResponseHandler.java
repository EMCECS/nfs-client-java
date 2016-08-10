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
package com.emc.ecs.nfsclient.nfs;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.emc.ecs.nfsclient.rpc.RpcResponseHandler;
import com.emc.ecs.nfsclient.rpc.RejectStatus;
import com.emc.ecs.nfsclient.rpc.RpcRequest;

/**
 * The NFS response handler, used in the wrapper class for post-response
 * actions, including error checking. This base class performs just basic
 * response correctness checking that applies to all NFS responses.
 * 
 * @author seibed
 */
public abstract class NfsResponseHandler<T extends NfsResponseBase> extends RpcResponseHandler<T> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.rpc.RpcResponseHandler#checkResponse(com.emc.ecs.
     * nfsclient.rpc.RpcRequest)
     */
    public void checkResponse(RpcRequest request) throws IOException {
        if (getResponse().getRejectStatus() == RejectStatus.AUTH_ERROR.getValue()) {
            request.setUsePrivilegedPort(true);
        }
        if (getResponse().getState() != NfsStatus.NFS3_OK.getValue()) {
            int responseState = getResponse().getState();
            if (responseState == NfsStatus.NFS3ERR_NOENT.getValue()) {
                throw new FileNotFoundException(request.getErrorMessage());
            }
            NfsStatus status = NfsStatus.fromValue(responseState);
            throw new NfsException(status, request.getErrorMessage() + " error code:" + status.toString());
        }
    }

}
