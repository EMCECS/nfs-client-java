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

import java.io.FileNotFoundException;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.emc.ecs.nfsclient.rpc.Credential;
import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * The request, as specified by RFC 1813 (https://tools.ietf.org/html/rfc1813).
 * 
 * <p>
 * Procedure CREATE creates a regular file.
 * </p>
 * 
 * @author seibed
 */
public class NfsCreateRequest extends NfsRequestBase {

    /**
     * size of verifier array
     */
    private static final int NFS3_CREATEVERFSIZE = 8;

    /**
     * The instance describing how the file is to be created. UNCHECKED means
     * that the file should be created without checking for the existence of a
     * duplicate file in the same directory. In this case, how.obj_attributes is
     * a sattr3 describing the initial attributes for the file. GUARDED
     * specifies that the server should check for the presence of a duplicate
     * file before performing the create and should fail the request with
     * NFS3ERR_EXIST if a duplicate file exists. If the file does not exist, the
     * request is performed as described for UNCHECKED. EXCLUSIVE specifies that
     * the server is to follow exclusive creation semantics, using the verifier
     * to ensure exclusive creation of the target. No attributes may be provided
     * in this case, since the server may use the target file metadata to store
     * the createverf3 verifier.
     */
    private NfsCreateMode _createMode;

    /**
     * The name that is to be associated with the created file.
     */
    private String _name;

    /**
     * The initial attributes for the new file.
     */
    NfsSetAttributes _attributes;

    /**
     * A unique identifier to ensure exclusive creation of the new file. If no
     * value is provided for exclusive creation, this is generated.
     */
    private byte[] _verifier;

    /**
     * Creates the request, as specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     * 
     * <p>
     * Procedure CREATE creates a regular file.
     * </p>
     * 
     * @param createMode
     *            The instance describing how the file is to be created.
     *            UNCHECKED means that the file should be created without
     *            checking for the existence of a duplicate file in the same
     *            directory. In this case, how.obj_attributes is a sattr3
     *            describing the initial attributes for the file. GUARDED
     *            specifies that the server should check for the presence of a
     *            duplicate file before performing the create and should fail
     *            the request with NFS3ERR_EXIST if a duplicate file exists. If
     *            the file does not exist, the request is performed as described
     *            for UNCHECKED. EXCLUSIVE specifies that the server is to
     *            follow exclusive creation semantics, using the verifier to
     *            ensure exclusive creation of the target. No attributes may be
     *            provided in this case, since the server may use the target
     *            file metadata to store the createverf3 verifier.
     * @param parentDirectoryFileHandle
     *            The file handle for the directory in which the file is to be
     *            created.
     * @param name
     *            The name that is to be associated with the created file.
     * @param attributes
     *            The initial attributes for the new file.
     * @param verifier
     *            A unique identifier to ensure exclusive creation of the new
     *            file. If no value is provided for exclusive creation, this is
     *            generated.
     * @param credential
     *            The credential used for RPC authentication.
     * @param nfsVersion
     *            The NFS version number
     * @throws FileNotFoundException
     */
    public NfsCreateRequest(NfsCreateMode createMode, byte[] parentDirectoryFileHandle, String name,
            NfsSetAttributes attributes, byte[] verifier, Credential credential, int nfsVersion)
            throws FileNotFoundException {
        super(Nfs.RPC_PROGRAM, nfsVersion, Nfs.NFSPROC3_CREATE, credential, parentDirectoryFileHandle);
        _createMode = createMode;
        _name = trimFileName(name);
        _attributes = attributes;
        if (_createMode == NfsCreateMode.EXCLUSIVE) {
            if (verifier != null) {
                _verifier = verifier;
            } else {
                _verifier = new byte[NFS3_CREATEVERFSIZE];
                new Random().nextBytes(_verifier);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.emc.ecs.nfsclient.nfs.NfsRequestBase#marshalling(com.emc.ecs.
     * nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        super.marshalling(xdr);
        xdr.putString(_name);
        xdr.putInt(_createMode.getValue());
        if (_createMode != NfsCreateMode.EXCLUSIVE) {
            _attributes.marshalling(xdr);
        } else {
            xdr.putByteArray(_verifier);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return startToString("NfsCreateRequest").append(" name:").append(_name).toString();
    }

    /**
     * @return A unique identifier to ensure exclusive creation of the new file.
     *         If no value was provided for exclusive creation, this was
     *         generated.
     */
    public byte[] getVerifier() {
        return _verifier;
    }

}
