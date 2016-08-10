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
package com.emc.ecs.nfsclient.mount;

import java.io.IOException;

/**
 * Mount exception class
 * 
 * @author seibed
 */
public class MountException extends IOException {

    /**
     * Default value
     */
    private static final long serialVersionUID = 1L;

    /**
     * The input status instance.
     */
    private final MountStatus _status;

    /**
     * Construct a new Mount exception with the given status, message, and cause.
     * 
     * @param status
     *            The status instance.
     * @param message
     *            The error message.
     * @param cause
     *            The cause
     */
    public MountException(MountStatus status, String message, Exception cause) {
        super(message, cause);
        _status = status;
    }

    /**
     * Construct a new Mount exception with the given status and message.
     * 
     * @param status
     *            The status instance.
     * @param message
     *            The error message
     */
    public MountException(MountStatus status, String message) {
        super(message);
        _status = status;
    }

    /**
     * @return The input status instance.
     */
    public MountStatus getStatus() {
        return _status;
    }

}
