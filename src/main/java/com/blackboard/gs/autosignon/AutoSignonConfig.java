/* ****************************************************************************
 * Copyright (c) 2010, Blackboard Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   -- Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *
 *   -- Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *
 *   -- Neither the name of Blackboard nor the names of its contributors may be
 *        used to endorse or promote products derived from this software
 *        without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * ***************************************************************************/

package com.blackboard.gs.autosignon;

import com.blackboard.gs.settings.Setting;
import com.blackboard.gs.settings.SettingsManager;

/**
 * This class provides to abstract out the values of settings to something more logical.
 *
 * @author nbrackett
 *
 */
public abstract class AutoSignonConfig {

    // default forward location after user is authenticated via SSO
    public static final String BB_FORWARD_HOME          = "/webapps/portal/execute/tabs/tabAction?tab_tab_group_id=_1_1";

    // root url for a Blackboard Course
    public static final String BB_COURSE_HOME           = "webapps/blackboard/execute/announcement?method=search&context=course&course_id=";

    public static final String SSO_ENABLED              = "Enabled";
    public static final String SSO_DISABLED             = "Disabled";

    public static final String TIMESTAMP_RANGE_DEF      = "60000";

    public static final String SECURE_MAC               = "Secure";
    public static final String INSECURE_MAC             = "Insecure";
    public static final String ANY_MAC                  = "Any";


    /**
     * Returns whether or not AutoSignon is enabled.
     *
     * @return <code>true</code> is AutoSignon is enabled, <code>false</code> otherwise
     */
    public static boolean isSsoEnabled() {
        return SSO_ENABLED.equals(SettingsManager.getInstance().getSetting(Setting.SSO_ENABLED));
    }

    /**
     * Returns the shared-secret used for MAC calculation.
     *
     * @return the shared secret for MAC calculation
     */
    public static String getSharedSecret() {
        return SettingsManager.getInstance().getSetting(Setting.SHARED_SECRET);
    }

    /**
     * Returns the allowable difference (in milliseconds) between request timestamp and local timestamp.
     *
     * @return allowable difference (in milliseconds) between request timestamp and local timestamp
     */
    public static long getTimestampRange() {
        try {
            return Long.parseLong(SettingsManager.getInstance().getSetting(Setting.TIMESTAMP_RANGE));
        } catch(Exception e) {
            return Long.parseLong(TIMESTAMP_RANGE_DEF);
        }
    }

    /**
     * Returns whether or not the Secure MAC algorithm is allowed.
     *
     * @return <code>true</code> if Secure MAC algorithm is allowed, <code>false</code> otherwise
     */
    public static boolean allowSecureMac() {
        final String macAlgorithm = SettingsManager.getInstance().getSetting(Setting.MAC_ALGORITHM);
        return SECURE_MAC.equals(macAlgorithm);
    }

    /**
     * Returns whether or not the Insecure MAC algorithm is allowed.
     *
     * @return <code>true</code> if Insecure MAC algorithm is allowed, <code>false</code> otherwise
     */
    public static boolean allowInsecureMac() {
        final String macAlgorithm = SettingsManager.getInstance().getSetting(Setting.MAC_ALGORITHM);
        return INSECURE_MAC.equals(macAlgorithm) || ANY_MAC.equals(macAlgorithm);
    }

    /**
     * Returns array of optional parameters to use in MAC calculation.
     *
     * @return set of optional parameters to use in MAC calculation
     */
    public static String[] getMacParameters() {
        try {
            return SettingsManager.getInstance().getSetting(Setting.MAC_PARAMETERS).split("\n");
        } catch(Exception e) {
            return new String[0];
        }
    }

    /**
     * Returns the name of the request parameter that the remote MAC can be found on.
     *
     * @return request parameter that remote MAC can be found on
     */
    public static String getMacRequestParameter() {
        return SettingsManager.getInstance().getSetting(Setting.PARAM_MAC);
    }

    /**
     * Returns the name of the request parameter that the user <code>batch uid</code> can be found on.
     *
     * @return request parameter that user <code>batch uid</code> can be found on
     */
    public static String getUserIdRequestParameter() {
        return SettingsManager.getInstance().getSetting(Setting.PARAM_USERID);
    }

    /**
     * Returns the name of the request parameter that the timestamp can be found on.
     *
     * @return request parameter that timestamp can be found on
     */
    public static String getTimestampRequestParameter() {
        return SettingsManager.getInstance().getSetting(Setting.PARAM_TIMESTAMP);
    }

    /**
     * Returns the name of the request parameter that the forwarding url can be found on.
     *
     * @return request parameter that forwarding url can be found on
     */
    public static String getForwardRequestParameter() {
        return SettingsManager.getInstance().getSetting(Setting.PARAM_FORWARD);
    }

    /**
     * Returns the name of the request parameter that the course <code>batch uid</code> can be found on.
     *
     * @return request parameter that course <code>batch uid</code> can be found on
     */
    public static String getCourseIdRequestParameter() {
        return SettingsManager.getInstance().getSetting(Setting.PARAM_COURSEID);
    }

}