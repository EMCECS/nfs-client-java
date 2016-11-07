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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emc.ecs.nfsclient.nfs.Nfs;

/**
 * @author seibed
 *
 */
public class LinkTracker<F extends NfsFile<? extends Nfs<F>, F>> {

    /**
     * Maximum size of a symlink chain to follow.
     */
    private static final int MAXSYMLINKS = 40;

    private Set<String> _unresolvedPaths = new HashSet<String>();

    private Map<String, F> _resolvedPaths = new HashMap<String, F>();

    private int linksTraversed = 0;

    public LinkTracker() {}

    final F addLink(String path) throws IOException {
        if (++linksTraversed > MAXSYMLINKS) {
            throw new IllegalArgumentException("Too many links to follow (> " + MAXSYMLINKS + ").");
        }

        F resolvedPath = _resolvedPaths.get(path);

        if (resolvedPath == null) {
            for (String unresolvedPath : _unresolvedPaths) {
                if (path.equals(unresolvedPath) || path.startsWith(unresolvedPath + "/")) {
                    throw new IOException("Links form a loop: " + path);
                }
            }
            _unresolvedPaths.add(path);
        }

        return resolvedPath;
    }

    /**
     * @param path
     * @param file
     */
    public void addResolvedPath(String path, F file) {
        _resolvedPaths.put(path, file);
    }

}
