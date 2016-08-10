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

/**
 * This interface is the NFS client equivalent of
 * <code>java.io.FileFilter</code>. A filter for abstract pathnames.
 *
 * <p>
 * Instances of this interface may be passed to the <code>listFiles
 * (NfsFileFilter)</code> method of the <code>NfsFile</code> class.
 * </p>
 * 
 * @author seibed
 */
public interface NfsFileFilter {

    /**
     * Tests if a specified file abstract pathname should be included in a
     * pathname list.
     *
     * @param pathName
     *            The abstract pathname to be tested
     * @return <code>true</code> if and only if <code>pathName</code> should be
     *         included
     */
    boolean accept(NfsFile<?, ?> pathName);

}
