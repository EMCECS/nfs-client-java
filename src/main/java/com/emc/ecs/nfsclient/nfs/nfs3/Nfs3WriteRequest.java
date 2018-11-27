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

import com.emc.ecs.nfsclient.nfs.NfsWriteRequest;
import com.emc.ecs.nfsclient.rpc.Credential;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure WRITE writes data to a file.
 * </p>
 * 
 * @author seibed
 */
public class Nfs3WriteRequest extends NfsWriteRequest {

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure WRITE writes data to a file.
     * </p>
     * 
     * @param fileHandle
     *            The file handle for the file to which data is to be written.
     *            This must identify a file system object of type, NF3REG.
     * @param offset
     *            The position within the file at which the write is to begin.
     *            An offset of 0 means to write data starting at the beginning
     *            of the file.
     * @param payload
     *            The data to be written to the file.
     * @param syncType
     *            One of the values below.
     *            <ul>
     *            <li>UNSTABLE = 0 - Best effort, no promises.</li>
     *            <li>DATA_SYNC = 1 - Commit all data to stable storage, plus
     *            enough metadata for retrieval, before returning.</li>
     *            <li>FILE_SYNC = 2 - Commit all data and metadata to stable
     *            storage before returning.</li>
     *            </ul>
     * @param credential
     *            The credential used for RPC authentication.
     * @throws FileNotFoundException
     */
    public Nfs3WriteRequest(byte[] fileHandle, long offset, List<ByteBuffer> payload, int syncType,
            Credential credential) throws FileNotFoundException {
        super(fileHandle, offset, payload, syncType, credential, Nfs3.VERSION);
    }

}
