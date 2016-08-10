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
package com.emc.ecs.nfsclient.nfs.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.emc.ecs.nfsclient.NfsTestBase;
import com.emc.ecs.nfsclient.nfs.NfsDirectoryEntry;
import com.emc.ecs.nfsclient.nfs.NfsDirectoryPlusEntry;
import com.emc.ecs.nfsclient.nfs.NfsSetAttributes;
import com.emc.ecs.nfsclient.nfs.NfsType;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;

/**
 * @author seibed
 *
 */
public class Test_Nfs3File extends NfsTestBase {

    /**
     * @throws IOException
     */
    public Test_Nfs3File() throws IOException {
        super();
    }

    @Test
    public void testFileAndDirectoryCreationAndDeletion() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/test2");
        assertFalse(test.exists());
        test.mkdir();
        test = new Nfs3File(nfs3, "/test2/dummy2");
        assertFalse(test.exists());
        assertTrue(test.createNewFile());
        assertTrue(test.exists());

        Nfs3File test2 = new Nfs3File(nfs3, "/test2/dummy2");
        assertEquals("/test2/", test.getParent());
        assertEquals("dummy2", test.getName());
        assertTrue(test2.exists());
        assertEquals(test.lastModified(), test2.lastModified());

        Nfs3File test3 = new Nfs3File(nfs3, "/test2/dummy3_b");
        assertFalse(test3.exists());
        test3.createNewFile();
        assertTrue(test3.exists());

        test = new Nfs3File(nfs3, "/test2");
        List<String> children = test.list();
        assertEquals(2, children.size());
        assertEquals("dummy2", children.get(0));
        assertEquals("dummy3_b", children.get(1));
        List<Nfs3File> childFiles = test.listFiles();
        assertEquals(2, childFiles.size());
        List<NfsDirectoryPlusEntry> list = new ArrayList<NfsDirectoryPlusEntry>();
        assertEquals(0, list.size());
        nfs3.wrapped_getReaddirplus(test.makeReaddirplusRequest(0, 0, 10000, 10000), list);
        assertEquals(4, list.size());
        assertEquals(".", list.get(0).getFileName());
        assertEquals(test.getFileHandle().length, list.get(0).getFileHandle().length);
        for (int i = 0; i < test.getFileHandle().length; ++i) {
            assertEquals(test.getFileHandle()[i], list.get(0).getFileHandle()[i]);
        }
        assertEquals("dummy2", list.get(2).getFileName());
        assertEquals(test2.getFileHandle().length, list.get(2).getFileHandle().length);
        for (int i = 0; i < test2.getFileHandle().length; ++i) {
            assertEquals(test2.getFileHandle()[i], list.get(2).getFileHandle()[i]);
        }

        test3.delete();
        assertFalse(test3.exists());

        test2.delete();
        assertFalse(test2.exists());

        test = new Nfs3File(nfs3, "/test2");
        test.delete();
        test2 = new Nfs3File(nfs3, "/test2");
        assertFalse(test2.exists());

        test = new Nfs3File(nfs3, "/test2/dummy2");
        test.mkdirs();
        assertTrue(test2.exists());
        assertTrue(test2.isDirectory());
        assertFalse(test2.isFile());
        assertTrue(test.exists());
        assertTrue(test.isDirectory());
        assertFalse(test.isFile());
        test.delete();
        test = test.getParentFile();
        test.delete();
        assertFalse(test2.exists());
    }

    @Test
    public void testDirectoryReading() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/");
        int initialRootChildren = test.list().size();

        test = new Nfs3File(nfs3, "/test2");
        assertFalse(test.exists());
        test.mkdir();

        final Nfs3File test2 = new Nfs3File(nfs3, "/test2/dummy2");
        assertFalse(test2.exists());
        assertTrue(test2.createNewFile());
        assertTrue(test2.exists());
        assertEquals("/test2/", test2.getParent());
        assertEquals("dummy2", test2.getName());
        assertTrue(test2.exists());
        Nfs3File test3 = new Nfs3File(nfs3, "/test2/dummy2");
        assertEquals(test3.lastModified(), test2.lastModified());

        test3 = new Nfs3File(nfs3, "/test2/dummy3_b");
        test3.createNewFile();

        test = new Nfs3File(nfs3, "/test2");
        List<String> children = test.list();
        assertEquals(2, children.size());
        assertEquals("dummy2", children.get(0));
        assertEquals("dummy3_b", children.get(1));
        List<Nfs3File> childFiles = test.listFiles();
        assertEquals(2, childFiles.size());

        List<NfsDirectoryPlusEntry> list = new ArrayList<NfsDirectoryPlusEntry>();
        assertEquals(0, list.size());
        nfs3.wrapped_getReaddirplus(test.makeReaddirplusRequest(0, 0, 10000, 10000), list);
        assertEquals(4, list.size());
        assertEquals(".", list.get(0).getFileName());
        assertEquals(test.getFileHandle().length, list.get(0).getFileHandle().length);
        for (int i = 0; i < test.getFileHandle().length; ++i) {
            assertEquals(test.getFileHandle()[i], list.get(0).getFileHandle()[i]);
        }
        assertEquals("dummy2", list.get(2).getFileName());
        assertEquals(test2.getFileHandle().length, list.get(2).getFileHandle().length);
        for (int i = 0; i < test2.getFileHandle().length; ++i) {
            assertEquals(test2.getFileHandle()[i], list.get(2).getFileHandle()[i]);
        }

        List<NfsDirectoryEntry> list2 = new ArrayList<NfsDirectoryEntry>();
        assertEquals(0, list2.size());
        nfs3.wrapped_getReaddir(test.makeReaddirRequest(0, 0, 1000), list2);
        assertEquals(4, list2.size());
        assertEquals(".", list2.get(0).getFileName());
        assertEquals("dummy2", list2.get(2).getFileName());

        assertEquals(0, test.list(new NfsFilenameFilter() {

            public boolean accept(NfsFile<?, ?> dir, String name) {
                return ".".equals(name);
            }

        }).size());

        assertEquals(1, test.list(new NfsFilenameFilter() {

            public boolean accept(NfsFile<?, ?> dir, String name) {
                return "dummy2".equals(name);
            }

        }).size());

        assertEquals(2, test.list(new NfsFilenameFilter() {

            public boolean accept(NfsFile<?, ?> dir, String name) {
                return !".".equals(name);
            }

        }).size());

        assertEquals(0, test.listFiles(new NfsFilenameFilter() {

            public boolean accept(NfsFile<?, ?> dir, String name) {
                return ".".equals(name);
            }

        }).size());

        assertEquals(1, test.listFiles(new NfsFilenameFilter() {

            public boolean accept(NfsFile<?, ?> dir, String name) {
                return "dummy2".equals(name);
            }

        }).size());

        assertEquals(2, test.listFiles(new NfsFilenameFilter() {

            public boolean accept(NfsFile<?, ?> dir, String name) {
                return !".".equals(name);
            }

        }).size());

        assertEquals(0, test.listFiles(new NfsFileFilter() {

            public boolean accept(NfsFile<?, ?> pathName) {
                return ".".equals(pathName.getName());
            }

        }).size());

        assertEquals(1, test.listFiles(new NfsFileFilter() {

            public boolean accept(NfsFile<?, ?> pathName) {
                return "dummy2".equals(pathName.getName());
            }

        }).size());

        assertEquals(1, test.listFiles(new NfsFileFilter() {

            public boolean accept(NfsFile<?, ?> pathName) {
                return test2.equals(pathName);
            }

        }).size());

        assertEquals(2, test.listFiles(new NfsFileFilter() {

            public boolean accept(NfsFile<?, ?> pathName) {
                return !".".equals(pathName.getName());
            }

        }).size());

        test3.delete();
        assertFalse(test3.exists());

        test = new Nfs3File(nfs3, "/");
        children = test.list();
        assertEquals(initialRootChildren + 1, children.size());
        assertTrue(children.contains("test2"));

        test = new Nfs3File(nfs3, "/test2/dummy2");
        assertFalse(test.isDirectory());
        assertTrue(test.isFile());
        test2.delete();
        assertFalse(test.exists());

        test = new Nfs3File(nfs3, "/test2");
        test.delete();
        Nfs3File test4 = new Nfs3File(nfs3, "/test2");
        assertFalse(test4.exists());

        test = new Nfs3File(nfs3, "/test2/dummy2");
        test.mkdirs();
        assertTrue(test4.exists());
        assertTrue(test4.isDirectory());
        assertFalse(test4.isFile());
        assertTrue(test.exists());
        assertTrue(test.isDirectory());
        assertFalse(test.isFile());
        test.delete();
        test = test.getParentFile();
        assertTrue(test4.exists());
        test.delete();
        assertFalse(test4.exists());
    }

    @Test
    public void testAttributeSettingAndGetting() throws Exception {
        Nfs3 nfs3 = new Nfs3(getAbsolutePath(), new CredentialUnix(0, 0, null), 3);
        Nfs3File test = new Nfs3File(nfs3, "/test3");
        test.mkdir();
        long millis = new Date().getTime() - 20000;
        boolean timeInSeconds = (0 == test.fsinfo().getFsInfo().time_delta.getTimeInMillis());
        if (timeInSeconds) {
            millis = 1000 * (millis / 1000);
        }

        assertNotEquals(millis, test.getAttributes().getMtime().getTimeInMillis());
        test.setLastModified(millis);

        Nfs3File test2 = new Nfs3File(nfs3, "/test3");
        System.out.println(test2.fsinfo().getFsInfo().time_delta.getTimeInMillis());
        assertEquals(millis, test2.getAttributes().getMtime().getTimeInMillis());

        assertTrue(test2.exists());
        test.delete();
        assertFalse(test2.exists());

        test = new Nfs3File(nfs3, "/test/dummy");
        assertNotNull(test.getParent());
        assertNotNull(test.getName());
        try {
            assertNotNull(new Date(test.lastModified()));
            fail("This should have thrown a FileNotFoundException.");
        } catch (FileNotFoundException e) {
            // Ignore, this is expected.
        }

        test = new Nfs3File(nfs3, "/test3");
        test.mkdir();
        millis = new Date().getTime() - 20000;
        if (timeInSeconds) {
            millis = 1000 * (millis / 1000);
        }
        assertNotEquals(millis, test.getAttributes().getMtime().getTimeInMillis());
        test.setLastModified(millis);
        test.setMode(7);
        assertEquals(7, test.getMode());

        test2 = new Nfs3File(nfs3, "/test3");
        assertEquals(millis, test2.getAttributes().getMtime().getTimeInMillis());

        assertTrue(test2.exists());
        test.delete();
        assertFalse(test2.exists());

        test.createNewFile();
        millis = new Date().getTime() - 20000;
        if (timeInSeconds) {
            millis = 1000 * (millis / 1000);
        }
        assertNotEquals(millis, test.getAttributes().getMtime().getTimeInMillis());
        test.setLastModified(millis);
        test.setMode(7);
        assertEquals(7, test.getMode());

        assertEquals(millis, test2.getAttributes().getMtime().getTimeInMillis());
        assertTrue(test2.exists());
        test.delete();
        assertFalse(test2.exists());

        test.symlink("dummy_data", new NfsSetAttributes());
        assertTrue(test2.exists());
        assertEquals("dummy_data", test.readlink().getData());
        assertEquals(NfsType.NFS_LNK, test2.getAttributes().getType());
        assertNotEquals(millis, test2.getAttributes().getMtime().getTimeInMillis());
        test.setLastModified(millis);
        assertEquals(millis, test2.getAttributes().getMtime().getTimeInMillis());
        test.delete();
        assertFalse(test2.exists());
    }

}
