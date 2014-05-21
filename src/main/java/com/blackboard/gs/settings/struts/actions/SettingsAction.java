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

package com.blackboard.gs.settings.struts.actions;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;

import com.blackboard.gs.settings.Setting;
import com.blackboard.gs.settings.SettingsManager;

public class SettingsAction extends MappingDispatchAction {

	// internal services
	private final SettingsManager settingsManager;

	public SettingsAction() {
		settingsManager = SettingsManager.getInstance();
	}

	/**
	 * Action to load the most recent configuration options and display them.
	 * If the options do not exist, the defaults will be used.
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward loadSettings(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		// if we aren't passing through, load up all settings from the manager
		if(null == request.getAttribute("currentValues")) {

			final HashMap<Setting, String> currentValues = new HashMap<Setting, String>();
			for(Setting setting : Setting.values()) {
				currentValues.put(setting, settingsManager.getSetting(setting));
			}
			request.setAttribute("currentValues", currentValues);

		}

		return mapping.findForward("view");

	}

	/**
	 * Action to store any configuration options and report on errors. This will forward to the configure
	 * method to load the latest versions of all configuration options.
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward storeSettings(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		final HashMap<Setting, String> currentValues = new HashMap<Setting, String>();

		for(Setting setting : Setting.values()) {

			final String settingValue = request.getParameter(setting.getKey());
			if(setting.validate(settingValue)) {
				settingsManager.setSetting(setting, settingValue);
			}

			// keep the current value to report on errors correctly
			currentValues.put(setting, settingValue);

		}

		// store these in the request
		request.setAttribute("currentValues", currentValues);

		return mapping.findForward("view");

	}

}
