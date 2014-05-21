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

public enum SettingType {

    TEXT,
    TEXTAREA,
    SELECT,
    RADIO;

    public String renderInput(final Setting setting, final String value) {
        switch (this) {
            case TEXT:
                return renderTextInput(setting, value);
            case TEXTAREA:
                return renderTextareaInput(setting, value);
            case SELECT:
                return renderSelectInput(setting, value);
            case RADIO:
                return renderRadioInput(setting, value);
            default:
                return "";
        }
    }

    /**
     * Renders HTML for a text input box.
     *
     * @param setting
     * @param value
     * @return
     */
    private String renderTextInput(final Setting setting, final String value) {
        final StringBuffer buffer = new StringBuffer();
        appendError(setting, value, buffer);

        buffer.append("<input type=\"text\" ");
        buffer.append("name=\"").append(setting.getKey()).append("\" ");
        buffer.append("id=\"").append(setting.getKey()).append("\" ");
        buffer.append("value=\"").append(value).append("\"/>");

        return buffer.toString();
    }

    /**
     * Renders HTML for a text input box.
     *
     * @param setting
     * @param value
     * @return
     */
    private String renderTextareaInput(final Setting setting, final String value) {
        final StringBuffer buffer = new StringBuffer();
        appendError(setting, value, buffer);

        buffer.append("<textarea ");
        buffer.append("name=\"").append(setting.getKey()).append("\" ");
        buffer.append("id=\"").append(setting.getKey()).append("\">");
        buffer.append(value);
        buffer.append("</textarea>");

        return buffer.toString();
    }

    /**
     * Renders HTML for a select pulldown.
     *
     * @param setting
     * @param value
     * @return
     */
    private String renderSelectInput(final Setting setting, final String value) {
        final StringBuffer buffer = new StringBuffer();
        appendError(setting, value, buffer);

        buffer.append("<select ");
        buffer.append("name=\"").append(setting.getKey()).append("\" ");
        buffer.append("id=\"").append(setting.getKey()).append("\">\n");
        if (null != setting.getOptions()) {
            for (String option : setting.getOptions()) {
                buffer.append("<option value=\"").append(option).append("\" ");
                if (option.equalsIgnoreCase(value)) {
                    buffer.append("selected");
                }
                buffer.append(">").append(option).append("</option>\n");
            }
        }
        buffer.append("</select>");

        return buffer.toString();
    }

    /**
     * Renders HTML for a radio button input.
     *
     * @param setting
     * @param value
     * @return
     */
    private String renderRadioInput(final Setting setting, final String value) {
        final StringBuffer buffer = new StringBuffer();
        appendError(setting, value, buffer);

        if (null != setting.getOptions()) {
            for (String option : setting.getOptions()) {
                buffer.append("<input type=\"radio\"");
                buffer.append(" name=\"").append(setting.getKey()).append("\"");
                buffer.append(" id=\"").append(setting.getKey()).append("\"");
                buffer.append(" value=\"").append(option).append("\"");
                if (option.equalsIgnoreCase(value)) {
                    buffer.append(" checked");
                }
                buffer.append("/> ").append(option).append(" &nbsp;&nbsp;&nbsp;\n");
            }
        }

        return buffer.toString();
    }

    /**
     * Appends error html if applicable to the StringBuffer.
     *
     * @param setting
     * @param value
     * @param buffer
     */
    private void appendError(final Setting setting, final String value, final StringBuffer buffer) {
        if (!setting.validate(value)) {
            buffer.append("<font color=\"red\"><b>* ");
            buffer.append(setting.getValidationError(value));
            buffer.append("</b></font><br/>");
        }
    }
}
