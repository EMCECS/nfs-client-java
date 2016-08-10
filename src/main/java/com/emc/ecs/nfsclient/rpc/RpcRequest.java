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

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

/**
 * An RPC request, as specified by RFC 1831
 * (https://tools.ietf.org/html/rfc1831).
 * 
 * @author seibed
 */
public abstract class RpcRequest {

    /**
     * The default Internet charset.
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * The xid generator, so that xid values are unique.
     */
    private static AtomicInteger _nextXid = new AtomicInteger(initializeXid());

    /**
     * RFC1831: the xid field is only used for clients matching reply messages
     * with call messages or for servers detecting retransmissions; the service
     * side cannot treat this id as any type of sequence number.
     */
    private final int _xid;

    /**
     * Requests must be calls (CALL = 0). Specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private final int _direction = 0;

    /**
     * The RPC version, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     */
    private final int _rpcVersion = 2;

    /**
     * The program number of the service.
     */
    private final int _serviceProgram;

    /**
     * The version number of the service.
     */
    private final int _serviceVersion;

    /**
     * The service procedure number to be called.
     */
    private final int _serviceProcedure;

    /**
     * The authentication credential to be used for the call.
     */
    private final Credential _credential;

    /**
     * <ul>
     * <li><code>true</code> if the client should use a privileged local port
     * (below 1024).</li>
     * <li><code>false</code> if the client should use any available local port.
     * </li>
     * </ul>
     * This starts as <code>false</code>, and is changed to <code>true</code> if
     * the rpc call is rejected with an authentication error.
     */
    private boolean _usePrivilegedPort = false;

    /**
     * Create the request, as specified by RFC 1831
     * (https://tools.ietf.org/html/rfc1831).
     * 
     * @param serviceProgram
     *            The program number of the service.
     * @param serviceVersion
     *            The version number of the service.
     * @param serviceProcedure
     *            The service procedure number to be called.
     * @param credential
     *            The authentication credential to be used for the call.
     */
    public RpcRequest(int serviceProgram, int serviceVersion, int serviceProcedure, Credential credential) {
        _xid = nextXid();
        _serviceProgram = serviceProgram;
        _serviceVersion = serviceVersion;
        _serviceProcedure = serviceProcedure;
        _credential = credential;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.emc.ecs.nfsclient.nfs.NfsRequest#marshalling(com.emc.ecs.nfsclient.
     * rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        xdr.setXid(_xid);
        xdr.putInt(_xid);
        xdr.putInt(_direction);
        xdr.putInt(_rpcVersion);
        xdr.putInt(_serviceProgram);
        xdr.putInt(_serviceVersion);
        xdr.putInt(_serviceProcedure);
        _credential.marshalling(xdr);
    }

    /**
     * @return
     *         <ul>
     *         <li><code>true</code> if the client should use a privileged local
     *         port (below 1024).</li>
     *         <li><code>false</code> if the client should use any available
     *         local port.</li>
     *         </ul>
     */
    public boolean isUsePrivilegedPort() {
        return _usePrivilegedPort;
    }

    /**
     * Set the flag.
     * 
     * @param usePrivilegedPort
     *            <ul>
     *            <li><code>true</code> if the client should use a privileged
     *            local port (below 1024).</li>
     *            <li><code>false</code> if the client should use any available
     *            local port.</li>
     *            </ul>
     */
    public void setUsePrivilegedPort(boolean usePrivilegedPort) {
        _usePrivilegedPort = usePrivilegedPort;
    }

    /**
     * @return The next xid, which should be unique for all rpc requests
     */
    private static int nextXid() {
        return _nextXid.getAndIncrement();
    }

    /**
     * @return The first xid, based on the system time in nanoseconds
     *         (OpenSolaris approach).
     */
    private static int initializeXid() {
        long cur = System.nanoTime();
        long seconds = cur / 1000000000;
        long nano = cur % 1000000000;

        int firstXid = (int) ((seconds << 20) | (nano >> 10));

        return firstXid;
    }

    /**
     * Start a StringBuilder for use in toString() (for logging).
     * 
     * @param requestLabel
     * @return the builder
     */
    protected StringBuilder startToString(String requestLabel) {
        StringBuilder stringBuilder = new StringBuilder(requestLabel);
        stringBuilder.append(" serviceVersion:").append(String.valueOf(_serviceVersion));
        stringBuilder.append(" xid:").append(String.valueOf(_xid));
        stringBuilder.append(" usePrivilegedPort:").append(String.valueOf(_usePrivilegedPort));
        return stringBuilder;
    }

    /**
     * @return an appropriate error message
     */
    public abstract String getErrorMessage();

    /**
     * Utility function for cloning file handles. These can never be null.
     * 
     * @param fileHandle
     *            The file handle to clone.
     * @return A clone if <code>fileHandle</code> is not <code>null</code>.
     * @throws FileNotFoundException
     *             If the file handle is null.
     */
    protected static final byte[] cloneFileHandle(byte[] fileHandle) throws FileNotFoundException {
        if (fileHandle == null) {
            throw new FileNotFoundException("The file handle is null, so this file does not exist.");
        }
        return fileHandle.clone();
    }

    /**
     * @param name
     *            The file name to trim.
     * @return The trimmed file name.
     * @throws IllegalArgumentException
     *             If the file name is blank.
     */
    protected static final String trimFileName(String name) throws IllegalArgumentException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The file name cannot be blank.");
        }
        return name.trim();
    }

}
