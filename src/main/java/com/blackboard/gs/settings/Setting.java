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

package com.blackboard.gs.settings;

import com.blackboard.gs.autosignon.AutoSignonConfig;
import com.blackboard.gs.settings.validators.*;

/**
 * Modify only this enumeration to define the settings you would like to have.
 * The rest of the framework will automate the display and persistence of these
 * settings.
 *
 * These are safe to use in a clustered environment.
 *
 * @author nbrackett
 *
 */
public enum Setting {

    SSO_ENABLED (
            "AutoSignon Enabled",
            "Disabling will not allow access to the autoSignon url (course forwarding will still function).",
            "bbgs.autosignon.ssoEnabled",
            AutoSignonConfig.SSO_ENABLED,
            true,
            SettingType.RADIO,
            new String[] { AutoSignonConfig.SSO_ENABLED, AutoSignonConfig.SSO_DISABLED },
            new NonEmptyValidator()
            ),

    SHARED_SECRET (  
            "Shared Secret",
            "Shared Secret used for calculating Message Authentication Code.",
            "bbgs.autosignon.sharedSecret",
            "",
            true,
            SettingType.TEXT,
            null,
            new NonEmptyValidator()
            ),

    TIMESTAMP_RANGE (	
            "Timestamp Delta",
            "Allowable difference in Timestamp (in milliseconds).",
            "bbgs.autosignon.timestampRange",
            "60000",
            true,
            SettingType.TEXT,
            null,
            new LongValidator()
            ),
    MAC_ALGORITHM (	
            "MAC Algorithm",
            "Use Secure MAC Algorithm.",
            "bbgs.autosignon.macAlgorithm",
            AutoSignonConfig.SECURE_MAC,
            true,
            SettingType.SELECT,
            new String[] { AutoSignonConfig.SECURE_MAC },
            new NonEmptyValidator()
            ),
    MAC_PARAMETERS (	
            "Request Parameters used for MAC",
            "Defines the set of additional parameters that are used for MAC calculation (UserId and Timestamp are always included).",
            "bbgs.autosignon.macParameters",
            "",
            false,
            SettingType.TEXTAREA,
            null,
            new RequestParameterSettingValidator()
            ),
    PARAM_MAC (	
            "MAC Request Parameter",
            "Request Parameter that Message Authentication Code can be located on.",
            "bbgs.autosignon.paramMac",
            "auth",
            true,
            SettingType.TEXT,
            null,
            new NonEmptyValidator()
            ),
    PARAM_USERID	(	"User Id Request Parameter",
						"Request Parameter that User Id (Batch Uid) can be located on.",
						"bbgs.autosignon.paramUserId",
						"userId",
						true,
						SettingType.TEXT,
						null,
						new NonEmptyValidator()
					),

	PARAM_COURSEID	(	"Course Id Request Parameter",
						"Request Parameter that Course Id (Batch Uid) for forwarding can be located on.",
						"bbgs.autosignon.paramCourseId",
						"courseId",
						true,
						SettingType.TEXT,
						null,
						new NonEmptyValidator()
					),

	PARAM_TIMESTAMP	(	"Timestamp Request Parameter",
						"Request Parameter that Timestamp can be located on.",
						"bbgs.autosignon.paramTimestamp",
						"timestamp",
						true,
						SettingType.TEXT,
						null,
						new NonEmptyValidator()
					),

	PARAM_FORWARD	(	"Forward Request Parameter",
						"Request Parameter that URL for forwarding can be located on.",
						"bbgs.autosignon.paramForward",
						"forward",
						true,
						SettingType.TEXT,
						null,
						new NonEmptyValidator()
					);



	public static SettingsGroup[] getSettingsGroups() {
		final SettingsGroup[] groups = new SettingsGroup[2];

		groups[0] = new SettingsGroup("Security Settings");
		groups[0].addSetting(SSO_ENABLED);
		groups[0].addSetting(MAC_ALGORITHM);
		groups[0].addSetting(MAC_PARAMETERS);
		groups[0].addSetting(SHARED_SECRET);
		groups[0].addSetting(TIMESTAMP_RANGE);

		groups[1] = new SettingsGroup("Request Parameter Settings");
		groups[1].addSetting(PARAM_MAC);
		groups[1].addSetting(PARAM_USERID);
		groups[1].addSetting(PARAM_TIMESTAMP);
		groups[1].addSetting(PARAM_FORWARD);
		groups[1].addSetting(PARAM_COURSEID);

		return groups;
	}


	/*
	 * ###########################
	 * DO NOT CHANGE ANYTHING BELOW UNLESS YOU KNOW WHAT YOU ARE DOING.
	 * ###########################
	 */

	private final String title;
	private final String description;
	private final String key;
	private final String defaultValue;
	private final boolean required;
	private final String[] options;
	private final SettingType type;
	private final SettingValidator validator;
	Setting(final String			title,
			final String 			description,
			final String 			key,
			final String 			defaultValue,
			final boolean			required,
			final SettingType 		type,
			final String[] 			options,
			final SettingValidator 	validator) {
		this.title			= title;
		this.description	= description;
		this.key 			= key;
		this.defaultValue 	= defaultValue;
		this.required		= required;
		this.options		= options;
		this.type 			= type;
		this.validator 		= validator;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public String getKey() {
		return key;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public String[] getOptions() {
		return options;
	}
	public SettingType getType() {
		return type;
	}
	public boolean isRequired() {
		return required;
	}
	public boolean validate(final String settingValue) {
		if(null != validator) {
			return validator.settingIsValid(settingValue);
		}
		return true;
	}
	public String getValidationError(final String settingValue) {
		if(null != validator) {
			return validator.getErrorMessage(title, settingValue);
		}
		return "";
	}

}
