EMC NFS Java Client
===

Description
---

This project is an NFS Java client, with some extra abstraction
to allow extensions to handle other NFS versions (it currently handles
only NFS v3). The package
com.emc.ecs.nfsclient.nfs has generic classes and interfaces,
while com.emc.ecs.nfsclient.nfs.nfs3 has the NFS Version 3 implementation
based on RFC 1813 (https://tools.ietf.org/html/rfc1813). For full details
on the semantics of these calls and the allowed parameter values, please
consult the RFC.

The project also includes NFS equivalents to java.io.File and related
functionality. The classes for this, both the generic and the
NFS3-specific versions, are in the package com.emc.ecs.nfsclient.nfs.io.

Installation
---

You can download the library as a Maven dependency from Maven Central Repository:

    <dependency>
        <groupId>com.emc.ecs</groupId>
        <artifactId>nfs-client</artifactId>
        <version>1.0.3</version>
    </dependency>

You can also use it as a Gradle dependency:

    compile group: 'com.emc.ecs', name: 'nfs-client', version: '1.0.3'

Docs
---

Javadocs are [here](https://emcecs.github.io/nfs-client-java/latest/javadoc/).

Building
---

    ./gradlew distZip

This project uses the Gradle wrapper.  As long as you are not doing any publishing activities, you should not need any specific configuration.

The minimum Java version required for building master is Java 8.

Testing
---
  
To run the unit tests, you will need to set up an NFS export
that can be used for testing, and add the parameters for this export
to `~/test.properties` (see the [template](https://raw.githubusercontent.com/EMCECS/nfs-client-java/master/src/test/resources/test.properties.template)).
The test client will need read/write access to this share. If you do not
do this, you can still run the tests, but most of them will be skipped.

How to use the bare client
---

The Nfs client instances provide both direct and lightly wrapped access to all NFS functionality.
Working examples for how to instantiate an Nfs3 client and how to exercise most 
NFS functionality are available in the JUnit tests.
[Test_Nfs3.java](https://raw.githubusercontent.com/EMCECS/nfs-client-java/master/src/test/java/com/emc/ecs/nfsclient/nfs/nfs3/Test_Nfs3.java)

How to use the NfsFile instances
---

The NfsFile instances provide file-based access to NFS functionality, including caching of repeatedly used
data to minimize the number of remote calls. Working examples for how to instantiate Nfs3File instances and
how to exercise most NfsFile functionality are available in the JUnit tests.
[Test_Nfs3File.java](https://raw.githubusercontent.com/EMCECS/nfs-client-java/master/src/test/java/com/emc/ecs/nfsclient/nfs/io/Test_Nfs3File.java).

How to use the NfsFileInputStream and NfsFileOutputStream instances
---

The NfsFileInputStream and NfsFileOutputStream instances provide easy access to NFS IO functionality, and 
include optimizations of buffer sizes.
Working examples for how to instantiate NfsFileInputStream and NfsFileOutputStream instances and
how to exercise most NFS IO functionality are available in the JUnit tests.
[Test_Streams.java](https://raw.githubusercontent.com/EMCECS/nfs-client-java/master/src/test/java/com/emc/ecs/nfsclient/nfs/io/Test_Streams.java).

How to use the tools jar
---

The tools jar currently contains only functionality for load testing NFS client IO functionality, as this is the
most likely  bottleneck. Working examples of how to exercise this code are available in the tools jar JUnit tests.
[Test_LoadTest.java](https://raw.githubusercontent.com/EMCECS/nfs-client-java/master/tools/src/test/java/com/emc/ecs/nfsclient/nfs/io/Test_LoadTest.java).
