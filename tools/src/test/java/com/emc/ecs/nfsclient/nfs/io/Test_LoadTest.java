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

import java.io.IOException;

import org.junit.Test;

import com.emc.ecs.nfsclient.NfsTestBase;

/**
 * @author seibed
 *
 */
public class Test_LoadTest extends NfsTestBase {

    /**
     * @throws IOException
     */
    public Test_LoadTest() throws IOException {
        super();
    }

    @Test
    public void testThreadedReadingPerformance100MB() throws Exception {
        LoadTest.testThreadedReadingPerformance(getAbsolutePath(), "/TestIo", 100000, 1, 100);
    }

    @Test
    public void testThreadedReadingPerformance10MB() throws Exception {
        LoadTest.testThreadedReadingPerformance(getAbsolutePath(), "/TestIo", 10000, 8, 100);
    }

    @Test
    public void testThreadedReadingPerformance1MB() throws Exception {
        LoadTest.testThreadedReadingPerformance(getAbsolutePath(), "/TestIo", 1000, 64, 100);
    }

    @Test
    public void testThreadedReadingPerformance500kB() throws Exception {
        LoadTest.testThreadedReadingPerformance(getAbsolutePath(), "/TestIo", 500, 64, 100);
    }

}
