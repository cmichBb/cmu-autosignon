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

package com.blackboard.gs.settings.validators;

import com.blackboard.gs.autosignon.AutoSignonConfig;

/**
 * This Validator ensures that the userId and timestamp parameters have not
 * been included in the list of parameters.
 *
 * @author nbrackett
 *
 */
public class RequestParameterSettingValidator implements SettingValidator {

	public boolean settingIsValid(final String settingValue) {

		if(null == settingValue) {
			return true;
		}

		final String paramUser = AutoSignonConfig.getUserIdRequestParameter();
		final String paramTime = AutoSignonConfig.getTimestampRequestParameter();

		// split the options and look for parameters
		final String[] parameters = settingValue.split("\\n");
		if(null == parameters) {
			return true;
		}
		for(String parameter : parameters) {
			parameter = parameter.trim();
			if(paramUser.equals(parameter) || paramTime.equals(parameter)) {
				return false;
			}
		}

		return true;

	}

	public String getErrorMessage(final String settingName, final String settingValue) {
		return settingName + " must not include the parameters for userId or timestamp, these are included always.";
	}

}
