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

package com.blackboard.gs.autosignon.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.blackboard.gs.autosignon.AutoSignonConfig;

/**
 * This class provides functionality for validating AutoSignon SSO requests by
 * generating a MAC (message authentication code) based on the request parameters
 * received and then checking this MAC against the one sent with the request. If
 * the generated MAC matches the request MAC and the timestamp is within the allotted
 * range, the SSO request is deemed valid.
 *
 */
public final class SecurityService {

    private static final Logger LOG = LogManager.getLogger(SecurityService.class);

    private static SecurityService instance = null;
    public static synchronized SecurityService getInstance() {
        if(null == instance) {
            instance = new SecurityService();
        }
        return instance;
    }
    private SecurityService() {
    }

    /**
     * Validates that all of the required security parameters are in place in this request
     * and that the generated MAC is valid.
     *
     * This method can use either insecure or secure MAC calculation based on settings.
     *
     * Secure:
     * - sort the request parameters we are going to use by alphabetical parameter name
     * - string together the values of those names with the shared secret at the end
     * - calculate the md5 sum of the resulting string
     *
     * Insecure:
     * - sum the ASCII values of all request values that we are using
     * - string together the sum (number) with the shared secret at the end
     * - calculate the md5 sum of the resulting string
     *
     * @param request
     * @return
     */
    public boolean validateMacInRequest(final HttpServletRequest request) {

        boolean macValid = false;

        if(AutoSignonConfig.allowSecureMac()) {
            macValid = macValid || validateMac(request, true);
        }
        if(AutoSignonConfig.allowInsecureMac()) {
            macValid = macValid || validateMac(request, false);
        }

        if(!macValid) {
          LOG.debug( "MAC from incoming request was invalid" );
        }


        return macValid;

    }

    /**
     * Internal method to validate a MAC based on secure or non-secure algorithm.
     *
     * @param request
     * @param usingSecure
     * @return
     */
    private boolean validateMac(final HttpServletRequest request, final boolean usingSecure) {
        LOG.debug( "Using Secure MAC: " + usingSecure );

        // validate the timestamp
        final long timestamp;
        try {
            timestamp = Long.parseLong(request.getParameter(AutoSignonConfig.getTimestampRequestParameter()));
        } catch(Exception e) {
            LOG.error("Invalid timestamp found on request object", e);
            return false;
        }
        if(timestampExpired(timestamp)) {
            LOG.debug("Timestamp received has expired: " + timestamp);
            return false;
        }

        // use the appropriate algorithm here
        final String algorithmResult;
        if(usingSecure) {
            algorithmResult = getSortedValues(request);
        } else {
            algorithmResult = getAsciiSum(request);
        }

        final String realMac = calculateHash(algorithmResult + AutoSignonConfig.getSharedSecret(), "UTF-8", "MD5");
        final String requestMac = request.getParameter(AutoSignonConfig.getMacRequestParameter());

        LOG.debug("Validating ## realMac = " + realMac + ", requestMac = " + requestMac);
        return (null != realMac && null != requestMac && realMac.equalsIgnoreCase(requestMac));

    }

    /**
     * Returns a string that represents the number of the sum of all required
     * data values.
     *
     * @param request
     * @return
     */
    private String getAsciiSum(final HttpServletRequest request) {

        // select all request values that matter to us
        final StringBuffer buffer = new StringBuffer();

        // required parameters
        buffer.append(request.getParameter(AutoSignonConfig.getTimestampRequestParameter()));
        buffer.append(request.getParameter(AutoSignonConfig.getUserIdRequestParameter()));

        // optional parameters
        final String[] macParameters = AutoSignonConfig.getMacParameters();
        for(String parameter : macParameters) {
            LOG.debug("Optional parameter for MAC: " + parameter);
            if(null != request.getParameter(parameter)) {
                buffer.append(request.getParameter(parameter));
            }
        }

        int asciiSum = 0;
        final String dataString = buffer.toString();
        LOG.debug("Insecure MAC dataString: " + dataString);

        for(int i = 0; i < dataString.length(); i++) {
            asciiSum += dataString.charAt(i);
        }

        return Integer.toString(asciiSum);

    }

    /**
     * Returns a String that is the concatenation of each required/optional value in order of
     * the keys.
     *
     * @param request
     * @return
     */
    private String getSortedValues(final HttpServletRequest request) {

        // sort the input parameters we are looking for
        final TreeSet<String> sortedSet = new TreeSet<String>();

        // required parameters
        sortedSet.add(AutoSignonConfig.getTimestampRequestParameter());
        sortedSet.add(AutoSignonConfig.getUserIdRequestParameter());

        // optional parameters
        final String[] macParameters = AutoSignonConfig.getMacParameters();
        for(String parameter : macParameters) {
            LOG.debug("Optional parameter for MAC: " + parameter);
            sortedSet.add(parameter);
        }

        // go over each parameter in order and build a string of values
        final StringBuffer buffer = new StringBuffer();
        for(String param : sortedSet) {
            if(null != request.getParameter(param)) {
                buffer.append(request.getParameter(param));
            }
        }

        LOG.debug("Secure MAC dataString: " + buffer.toString());
        return buffer.toString();

    }

    /**
     * Internal method to determine if the timestamp we received is too old.
     *
     * @param timestamp
     * @return
     */
    private boolean timestampExpired(long timestamp) {

        final long currentTimestamp = Calendar.getInstance().getTimeInMillis();
        if ((String.valueOf(currentTimestamp).length() - String.valueOf(timestamp).length()) >= 2) {
          timestamp = timestamp * 1000L;
        }
        final long timestampRange = AutoSignonConfig.getTimestampRange();

        // allow for either direction to include some room for subtle clock differences
        final long delta = Math.abs(currentTimestamp - timestamp);

        LOG.debug("Request Timestamp: " + timestamp + ", Current Timestamp: " + currentTimestamp + ", Range: " + timestampRange + ", Delta: " + delta);
        return delta > timestampRange;

    }

    /**
     * Internal method to calculate a valid MD5 for a string.
     *
     * @param encrypt
     * @param charSet
     * @param algorithm
     * @return
     */
    private String calculateHash(final String encrypt, final String charSet, final String algorithm)  {

        byte[] bytes = null;

        try {
            bytes = encrypt.getBytes(charSet);
        } catch (UnsupportedEncodingException e) {
            bytes = encrypt.getBytes();
        }

        return calculateHash(bytes, algorithm);

    }

    /**
     * Internal method to calculate a valid MD5 for a byte array.
     *
     * @param encrypt
     * @param algorithm
     * @return
     */
    private String calculateHash(final byte[] encrypt, final String algorithm) {

        try {
            final MessageDigest md = MessageDigest.getInstance(algorithm);
            final byte[] hash = md.digest(encrypt);
            return new String(Hex.encodeHex(hash));
        } catch (Exception e) {
            LOG.error("Failed to calculate hash sum", e);
            return null;
        }

    }

}