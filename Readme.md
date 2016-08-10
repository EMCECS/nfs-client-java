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

How to build the client
---

To build and fully test this code, you will need to set up an NFS export
that can be used for unit testing, and add the parameters for this export
to [test.properties](https://raw.githubusercontent.com/EMCECS/nfs-client-java/master/src/test/resources/test.properties). The
test client will need read/write access to this share.
