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

package com.blackboard.gs.settings.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.blackboard.gs.settings.SettingsManager;

public class InitSettingsListener implements ServletContextListener {

    private static final Logger LOG = LogManager.getLogger(InitSettingsListener.class);

    public void contextInitialized(final ServletContextEvent event) {

        try { // application initialization

            final File webroot = new File(event.getServletContext().getRealPath("/"));

            try { // initialize the logging

                // load in the configuration
                final File log4jConfig = new File(webroot, "WEB-INF/config/log4j.properties");
                final Properties log4jProps = new Properties();
                log4jProps.load(new FileInputStream(log4jConfig));

                // create any directories as needed
                for(Object obj : log4jProps.keySet()) {
                    String key = (String) obj;
                    if(key == null) {
                        continue;
                    }

                    key = key.toLowerCase().trim();
                    if (key.startsWith("log4j.appender.") && key.endsWith(".file")) {
                        final String value = log4jProps.getProperty((String) obj);

                        final File dir = (new File(value)).getParentFile();
                        if(!dir.exists()) {
                            dir.mkdirs();
                        }
                    }
                }

                // configure log4j with a 2 second refresh
                PropertyConfigurator.configureAndWatch(log4jConfig.getCanonicalPath(), 2000);

            } catch(Exception e) {
                // log failed to initialize
                e.printStackTrace();
                throw e;
            }

            LOG.info("Initializing Settings Manager-based module.");

        } catch(Exception e) {
            // log failed to initialize
            LOG.fatal("Failed to initialize Settings Manager-based module", e);
            throw new RuntimeException("Failed to initialize application", e);
        }

        // boot up the Settings Manager
        SettingsManager.getInstance();

    }

    public void contextDestroyed(final ServletContextEvent arg0) {

        // shutdown the Settings Manager
        SettingsManager.getInstance().shutdown();

        try {
            LogManager.shutdown();
        } catch(Exception e) {
          // ignore exception
        }

    }

}