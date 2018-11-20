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
package com.emc.ecs.nfsclient.nfs.nfs3;

import java.io.FileNotFoundException;

import com.emc.ecs.nfsclient.nfs.NfsCommitRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * 
 * @author seibed
 */
public class Nfs3CommitRequest extends NfsCommitRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure COMMIT forces or flushes data to stable storage that was
     * previously written with a WRITE procedure call with the stable field set
     * to UNSTABLE.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the file to which data is to be flushed
     *            (committed). This must identify a file system object of type,
     *            NF3REG.
     * @param offsetToCommit
     *            The position within the file at which the flush is to begin.
     *            An offset of 0 means to flush data starting at the beginning
     *            of the file.
     * @param dataSizeToCommit
     *            The number of bytes of data to flush. If count is 0, a flush
     *            from offset to the end of file is done.
     * @param credential
     *            The credential used for RPC authentication.
     * @throws FileNotFoundException 
     */
    public Nfs3CommitRequest(byte[] fileHandle, long offsetToCommit, int dataSizeToCommit, Credential credential) throws FileNotFoundException {
        super(fileHandle, offsetToCommit, dataSizeToCommit, credential, Nfs3.VERSION);
    }

}
