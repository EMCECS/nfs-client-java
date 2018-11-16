/**
 * Copyright 2016-2018 EMC Corporation. All Rights Reserved.
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

import java.io.IOException;

import org.junit.Test;

import com.emc.ecs.nfsclient.NfsTestBase;
import com.emc.ecs.nfsclient.nfs.NfsCreateMode;
import com.emc.ecs.nfsclient.nfs.NfsCreateRequest;
import com.emc.ecs.nfsclient.nfs.NfsRemoveRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttrRequest;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsStatus;
import com.emc.ecs.nfsclient.nfs.NfsTime;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;

/**
 * @author seibed
 *
 */
public class Test_Nfs3 extends NfsTestBase {

    /**
     * Name of a test file to use in the export
     */
    private static final String TEST_FILE_NAME = "test";

    /**
     * @throws IOException
     */
    public Test_Nfs3() throws IOException {
        super();
    }

    @Test
    public void testGetRootHandle() throws Exception {
        Nfs3 nfs3 = new Nfs3(getServer(), getExportedPath(), new CredentialUnix(), 3);

        byte[] output = nfs3.lookupRootHandle();
        assertNotNull(output);
        printFileHandle(output);
    }

    @Test
    public void testSetAttrGuarded() throws Exception {
        Nfs3 nfs3 = new Nfs3(getServer(), getExportedPath(), new CredentialUnix(), 3);

        byte[] rootHandle = nfs3.lookupRootHandle();

        Nfs3CreateResponse createRes = nfs3.sendCreate(
                new NfsCreateRequest(NfsCreateMode.UNCHECKED,
                        rootHandle,
                        TEST_FILE_NAME,
                        new NfsSetAttributes(),
                        null,
                        new CredentialUnix(),
                        3));

        assertEquals(NfsStatus.NFS3_OK.getValue(), createRes.getState());

        try {
            NfsTime guardTime = new NfsTime( createRes.getAttributes().getCtime().getTimeInMillis(), false );
            Nfs3SetAttrResponse setAttrResponse = nfs3.setAttr(new NfsSetAttrRequest(createRes.getFileHandle(),
                    new NfsSetAttributes(null, null, null, new Long(1024), null, null),
                    guardTime,
                    new CredentialUnix(), 3));

            assertEquals(NfsStatus.NFS3_OK.getValue(), setAttrResponse.getState());

            guardTime = new NfsTime( 0, false );
            setAttrResponse = nfs3.setAttr(new NfsSetAttrRequest(createRes.getFileHandle(),
                    new NfsSetAttributes(null, null, null, new Long(1024), null, null),
                    guardTime,
                    new CredentialUnix(), 3));

            assertEquals(NfsStatus.NFS3ERR_NOT_SYNC.getValue(), setAttrResponse.getState());
        } finally {
            // Clean up file
            nfs3.sendRemove(new NfsRemoveRequest(rootHandle, TEST_FILE_NAME, new CredentialUnix(), 3));
        }
    }

    /**
     * @param fileHandle
     */
    static void printFileHandle(byte[] fileHandle) {
        int len = fileHandle.length;

        StringBuffer buf = new StringBuffer();
        buf.append("file handle: [" + String.valueOf(len) + "] ");
        for (byte e : fileHandle) {
            buf.append((int) e);
            buf.append(" ");
        }

        System.out.println(buf.toString());
    }

//    @Test
//    public void testNfs3() throws Exception {
//        Nfs3 nfs3 = new Nfs3("128.222.169.15:/var/nfsshare", 0, 0, 3);
//        String[] listing = nfs3.readdir("/");
//        for (String file : listing) {
//            System.out.println(file);
//        }
//        assertEquals(1, listing.length);
//        assertEquals("test", listing[0]);
//        listing = nfs3.readdir("/test");
//        for (String file : listing) {
//            System.out.println(file);
//        }
//        assertEquals(1, listing.length);
//        assertEquals("dummy", listing[0]);
//
//        nfs3 = new Nfs3("128.222.169.15:/exports", 0, 0, 3);
//        listing = nfs3.readdir("/");
//        for (String file : listing) {
//            System.out.println(file);
//        }
//        assertEquals(1, listing.length);
//        assertEquals("test2", listing[0]);
//        listing = nfs3.readdir("/test2");
//        for (String file : listing) {
//            System.out.println(file);
//        }
//        assertEquals(1, listing.length);
//        assertEquals("dummy2", listing[0]);
//
//    }

}
