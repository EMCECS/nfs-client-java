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
package com.emc.ecs.nfsclient.nfs.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emc.ecs.nfsclient.nfs.Nfs;

/**
 * This class tracks the process of resolving a symbolic link all the way to a
 * concrete file. It ensures that exceptions are thrown if the links form a
 * loop, or if there are too many links in the chain. Link trackers must be
 * passed so that monitoring is continued until the link resolves to a file that
 * is not a symbolic link.
 * 
 * @author seibed
 *
 */
public class LinkTracker<N extends Nfs<?>, F extends NfsFile<N, F>> {

    /**
     * Maximum size of a symlink chain to follow.
     */
    private static final int MAXSYMLINKS = 40;

    private Set<String> _unresolvedPaths = new HashSet<String>();

    private Map<String, F> _resolvedPaths = new HashMap<String, F>();

    private int linksTraversed = 0;

    /**
     * The only constructor.
     */
    public LinkTracker() {
    }

    /**
     * Checks for problems. If the link has already been resolved, it returns
     * the final file. If not, it adds the link path to the list of unresolved
     * paths that have been seen while evaluating this chain.
     * 
     * @param path
     *            The path to the link that is currently being resolved.
     * @return The file, if that link has already been resolved.
     * @throws IOException
     *             If there are too many links in the chain (more than
     *             MAXSYMLINKS)
     */
    synchronized final F addLink(String path) throws IOException {
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
     * After each link is completely resolved, the linkTracker caller should
     * call this method to store that resolved path so that it can be resolved
     * directly the next time is is seen.
     * 
     * @param path
     *            The path to the original symbolic link.
     * @param file
     *            The file to which that link was finally resolved.
     */
    synchronized void addResolvedPath(String path, F file) {
        _resolvedPaths.put(path, file);
        _unresolvedPaths.remove(path);
    }

}
