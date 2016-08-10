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
package com.emc.ecs.nfsclient.nfs.nfs3;

import java.io.IOException;

import org.junit.Test;

import com.emc.ecs.nfsclient.NfsTestBase;
import com.emc.ecs.nfsclient.nfs.Nfs;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;

/**
 * @author seibed
 *
 */
public class Test_Nfs3 extends NfsTestBase {

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
