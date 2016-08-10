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
package com.emc.ecs.nfsclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Assume;

/**
 * @author seibed
 *
 */
public class NfsTestBase extends Assert {

    /**
     * Name of the properties file holding unit test parameters. This is
     * searched for in the classpath and the home directory, e.g., =>
     * $HOME/test.properties.
     */
    private static final String DEFAULT_FILE_NAME = "test";

    /**
     * Properties key for the NFS server.
     */
    private static final String NFS_SERVER = "nfs.server";

    /**
     * Property key for the path exported by the NFS server.
     */
    private static final String NFS_EXPORT = "nfs.export";

    /**
     * Properties for use in testing.
     */
    private final Properties _properties;

    /**
     * The server to use for testing.
     */
    private final String _server;

    /**
     * The exported path to use for testing.
     */
    private final String _exportedPath;

    /**
     * Protected to force subclassing. This will throw an exception if test
     * properties have not been properly configured.
     * 
     * @throws IOException
     */
    protected NfsTestBase() throws IOException {
        _properties = loadProperties();
        _server = getProperty(NFS_SERVER);
        _exportedPath = getProperty(NFS_EXPORT);
    }

    /**
     * @param propertyKey The key for the property.
     * @return The property value.
     */
    protected final String getProperty(String propertyKey) {
        return _properties.getProperty(propertyKey);
    }

    /**
     * @return The server to use for testing.
     */
    protected final String getServer() {
        return _server;
    }

    /**
     * @return The exported path to use for testing.
     */
    protected final String getExportedPath() {
        return _exportedPath;
    }

    /**
     * @return The absolute path to use for testing.
     */
    protected final String getAbsolutePath() {
        return _server + ":" + _exportedPath;
    }

    /**
     * Loads the default properties, throws an exception if they can't be
     * loaded.
     * 
     * @return The properties.
     * @throws IOException
     */
    protected static Properties loadProperties() throws IOException {
        return loadProperties(DEFAULT_FILE_NAME);
    }

    /**
     * Locates and loads the properties file for the test configuration. This
     * file can reside in one of two places: somewhere in the CLASSPATH or in
     * the user's home directory.
     *
     * @param fileName The file name.
     * @return the contents of the properties file as a
     *         {@link java.util.Properties} object.
     * @throws IOException
     */
    protected static Properties loadProperties(String fileName) throws IOException {
        String fullFileName = fileName + ".properties";
        InputStream inputStream = NfsTestBase.class.getClassLoader().getResourceAsStream(fullFileName);
        if (inputStream == null) {
            // Check in home directory
            File homeProperties = new File(System.getProperty("user.home") + File.separator + fullFileName);
            if (homeProperties.exists()) {
                inputStream = new FileInputStream(homeProperties);
            }
        }

        Assume.assumeNotNull(inputStream);

        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }

}
