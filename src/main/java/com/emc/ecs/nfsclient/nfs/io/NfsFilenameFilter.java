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
 * <code>java.io.FilenameFilter</code>. Instances of classes that implement this
 * interface are used to filter filenames. These instances are used to filter
 * directory listings in the <code>list</code> method of class
 * <code>NfsFile</code>.
 * 
 * @author seibed
 */
public interface NfsFilenameFilter {

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir
     *            the directory in which the file was found.
     * @param name
     *            the name of the file.
     * @return <code>true</code> if and only if the name should be included in
     *         the file list; <code>false</code> otherwise.
     */
    boolean accept(NfsFile<?, ?> dir, String name);

}
