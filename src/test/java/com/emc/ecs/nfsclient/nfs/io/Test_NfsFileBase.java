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

import org.junit.Assert;
import org.junit.Test;

import com.emc.ecs.nfsclient.nfs.io.NfsFileBase;

/**
 * @author seibed
 *
 */
public class Test_NfsFileBase extends Assert {

    @Test
    public void testMakeName() {
        assertEquals("", NfsFileBase.makeName("/"));
        assertEquals("junk", NfsFileBase.makeName("/junk"));
        assertEquals("junk", NfsFileBase.makeName("//junk"));
        assertEquals("junk", NfsFileBase.makeName("//junk//"));
        assertEquals("junk", NfsFileBase.makeName("//dummy///junk"));
        assertEquals("junk", NfsFileBase.makeName("//dummy////junk//"));
        assertEquals("junk", NfsFileBase.makeName("//dummy/junk//"));
    }

    @Test
    public void testMakeParentPath() {
        assertEquals("/", NfsFileBase.makeParentPath("/"));
        assertEquals("/", NfsFileBase.makeParentPath("/junk"));
        assertEquals("/", NfsFileBase.makeParentPath("//junk"));
        assertEquals("/", NfsFileBase.makeParentPath("//junk//"));
        assertEquals("//dummy/", NfsFileBase.makeParentPath("//dummy///junk"));
        assertEquals("//dummy/", NfsFileBase.makeParentPath("//dummy////junk//"));
        assertEquals("//dummy/", NfsFileBase.makeParentPath("//dummy/junk//"));
    }

    @Test
    public void testMakeChildPath() {
        assertEquals("/test", NfsFileBase.makeChildPath("/", "test"));
        assertEquals("/test", NfsFileBase.makeChildPath("///", "test"));
        assertEquals("/test", NfsFileBase.makeChildPath("", "test"));
        assertEquals("/dummy/test", NfsFileBase.makeChildPath("/dummy/", "test"));
        assertEquals("/dummy/test", NfsFileBase.makeChildPath("//dummy//", "test"));
        assertEquals("/dummy/test", NfsFileBase.makeChildPath("/dummy", "test"));
    }

}
