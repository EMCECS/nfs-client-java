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
package com.emc.ecs.nfsclient.rpc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author seibed
 */
public class Test_Xdr extends Assert {

    @Test
    public void testSkip() {
        Xdr xdr = new Xdr(200);
        assertEquals(0, xdr.getOffset());
        assertNull(xdr.getTerminalPadding());

        xdr.skip(0);
        assertEquals(0, xdr.getOffset());
        assertNull(xdr.getTerminalPadding());
        int expectedOffset = 0;

        for (int skip = 1; skip <= 4; ++skip) {
            xdr.skip(skip);
            expectedOffset += 4;
            assertEquals(expectedOffset, xdr.getOffset());
            assertNull(xdr.getTerminalPadding());
        }

        for (int skip = 5; skip <= 8; ++skip) {
            xdr.skip(skip);
            expectedOffset += 8;
            assertEquals(expectedOffset, xdr.getOffset());
            assertNull(xdr.getTerminalPadding());
        }
    }

    @Test
    public void testPayloadPadding() {
        Xdr xdr = new Xdr(200);
        assertEquals(0, xdr.getOffset());
        assertNull(xdr.getTerminalPadding());

        for (int payloadNumber = 1; payloadNumber < 4; ++payloadNumber) {
            xdr.putPayloads(getPayload(payloadNumber), 1);
            assertEquals(4 - payloadNumber, xdr.getTerminalPadding().limit());
        }

        xdr.putPayloads(getPayload(4), 1);
        assertNull(xdr.getTerminalPadding());

        xdr.putPayloads(getPayload(5), 1);
        assertNotNull(xdr.getTerminalPadding());
        xdr.getPayloads();
        assertNull(xdr.getTerminalPadding());
    }

    /**
     * @param payloadNumber
     * @return a 1-byte payload with the payload number
     */
    private List<ByteBuffer> getPayload(int payloadNumber) {
        List<ByteBuffer> payload = new ArrayList<ByteBuffer>(1);
        byte[] bytes = new byte[1];
        bytes[0] = (byte) payloadNumber;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        payload.add(buffer);
        return payload;
    }

}
