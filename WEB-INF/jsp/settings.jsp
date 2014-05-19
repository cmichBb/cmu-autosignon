<%--
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
--%>
<%@page import= "com.blackboard.gs.settings.Setting"%>
<%@page import= "com.blackboard.gs.settings.SettingsGroup"%>
<%@page import= "com.blackboard.gs.settings.SettingType"%>
<%@page import= "java.util.HashMap"%>

<%@ taglib uri="/bbNG"    prefix="bbNG"%>

<%
//string extraction

final String pageTitle  = "AutoSignon Settings";
final String pageDescr  = "This Building Block allows for AutoSignon functionality similar to that provided by CE/Vista. MAC authentication is used for security.";

final HashMap<Setting, String> currentValues = (HashMap<Setting, String>) request.getAttribute("currentValues");
%>

<bbNG:genericPage title="<%=pageTitle%>" entitlement="system.admin.VIEW">

  <bbNG:pageHeader instructions="<%=pageDescr%>">
    <bbNG:breadcrumbBar environment="sys_admin" navItem="admin_plugin_manage">
        <bbNG:breadcrumb><%=pageTitle%></bbNG:breadcrumb>
    </bbNG:breadcrumbBar>
    <bbNG:pageTitleBar><%=pageTitle%></bbNG:pageTitleBar>
  </bbNG:pageHeader>

  <bbNG:form method="POST" action="storeSettings.do">
    <bbNG:dataCollection>
      <% for(SettingsGroup group : Setting.getSettingsGroups()) { %>

        <bbNG:step title="<%=group.getTitle()%>">

          <% for(Setting setting : group.getSettings()) { %>
            <bbNG:dataElement isRequired="<%=setting.isRequired()%>" label="<%=setting.getTitle()%>">
              <%=setting.getType().renderInput(setting, currentValues.get(setting))%>
              <bbNG:elementInstructions text="<%=setting.getDescription()%>"/>
            </bbNG:dataElement>
          <% } %>

        </bbNG:step>

      <% } %>

      <bbNG:stepSubmit title="Save Settings" cancelUrl="/webapps/blackboard/admin/manage_plugins.jsp"/>
    </bbNG:dataCollection>
  </bbNG:form>

</bbNG:genericPage>