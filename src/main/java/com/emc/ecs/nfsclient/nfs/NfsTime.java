/**
 * Copyright 2016-2018 EMC Corporation. All Rights Reserved.
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
package com.emc.ecs.nfsclient.nfs;

import java.util.Calendar;

import com.emc.ecs.nfsclient.rpc.Xdr;

/**
 * NFS time getter and setter handling, as specified by RFC 1813
 * (https://tools.ietf.org/html/rfc1813).
 * 
 * @author seibed
 */
public class NfsTime implements NfsRequest, NfsResponse {

    /**
     * For setting the time, this indicates a noop - the corresponding attribute
     * should not be changed. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    private static final int DO_NOT_CHANGE_TIME = 0;

    /**
     * For setting the time, this indicates that time should be set to current
     * server time; no data is provided by the client. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    private static final int SET_TO_SERVER_TIME = 1;

    /**
     * This is the default, and indicates that the time attribute is set to the
     * time passed by the client. Specified by RFC 1813
     * (https://tools.ietf.org/html/rfc1813).
     */
    private static final int SET_TO_CLIENT_TIME = 2;

    /**
     * This usually indicates a guard time - attributes should be changed only
     * if the object's ctime matches the associated time, as specified by RFC
     * 1813 (https://tools.ietf.org/html/rfc1813).
     */
    private static final int IS_BARE_TIME = -1;

    /**
     * This constant is the NfsTime passed to setters to indicate that the time
     * should not be changed.
     */
    public static final NfsTime DO_NOT_CHANGE = new NfsTime(0, DO_NOT_CHANGE_TIME);

    /**
     * This constant is the NfsTime passed to setters to indicate that the time
     * should be reset to the current server time.
     */
    public static final NfsTime SET_TO_CURRENT_ON_SERVER = new NfsTime(0, SET_TO_SERVER_TIME);

    private long seconds; // uint32
    private long nanoseconds; // uint32
    private final int _timeSettingType;

    /**
     * Create a new NfsTime for the current time.
     */
    public NfsTime() {
        this(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Create a new NfsTime for the setting time corresponding to milliseconds.
     * 
     * @param milliseconds
     *            same as for java.util.Date.
     */
    public NfsTime(long milliseconds) {
        this(milliseconds, true);
    }

    /**
     * Create a new NfsTime for the setting or guard time corresponding to milliseconds.
     * 
     * @param milliseconds
     *            same as for java.util.Date.
     * @param isSettingTime
     *            true if the time is to be used for setting a value,
     *            false if not (e.g., guard time)
     */
    public NfsTime(long milliseconds, boolean isSettingTime) {
        this(milliseconds, isSettingTime ? SET_TO_CLIENT_TIME : IS_BARE_TIME);
    }

    private NfsTime(long milliseconds, int timeSettingType) {
        _timeSettingType = timeSettingType;
        if ( captureTime() ) {
            seconds = milliseconds / 1000;
            nanoseconds = (milliseconds - seconds * 1000) * 1000000;
        } else {
            seconds = 0;
            nanoseconds = 0;
        }
    }

    /**
     * @return true if this is a bare time (with no time setting type), false
     * otherwise.
     */
    public boolean isBareTime() {
        return IS_BARE_TIME == _timeSettingType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("timeSettingType:").append(_timeSettingType);
        sb.append(", [seconds :").append(seconds);
        sb.append(" nseconds: ").append(nanoseconds);
        sb.append("]");

        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.NfsRequest#marshalling(com.emc.ecs.nfsclient.rpc.Xdr)
     */
    public void marshalling(Xdr xdr) {
        if ( !isBareTime() ) {
            xdr.putInt(_timeSettingType);
        }
        if ( captureTime() ) {
            xdr.putUnsignedInt(seconds);
            xdr.putUnsignedInt(nanoseconds);
        }
    }

    /* (non-Javadoc)
     * @see com.emc.ecs.nfsclient.nfs.NfsResponse#unmarshalling(com.emc.ecs.nfsclient.rpc.Xdr)
     */
    public void unmarshalling(Xdr xdr) {
        seconds = xdr.getUnsignedInt();
        nanoseconds = xdr.getUnsignedInt();
    }

    /**
     * @return time in milliseconds, same as java.util.Calendar.
     */
    public long getTimeInMillis() {
        return seconds * 1000 + nanoseconds / 1000000;
    }

    /**
     * @return true if the time in milliseconds will be used, false otherwise.
     */
    private boolean captureTime() {
        return ( isBareTime() 
              || ( SET_TO_CLIENT_TIME == _timeSettingType ) );
    }

}