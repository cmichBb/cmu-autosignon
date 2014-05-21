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

import blackboard.platform.plugin.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SettingsManager {

    private static final Logger LOG = LogManager.getLogger(SettingsManager.class);
    // building block vendor id and handle for identifying plugin
    public static final String _B2_VENDOR = "bbgs";
    public static final String _B2_HANDLE = "autosignon";
    // config file properties
    private static final String _B2_CONFIGDIR = "/config";
    private static final String _PROP_FILENAME = "config.properties";
    private static final String _PROP_HEADER = "AutoSignon Settings";
    // hashmap of Settings values (for persistence)
    private final Map<Setting, String> entries;
    // set of change listeners
    private final List<SettingsChangeListener> listeners;
    // flag to indicate that settings are dirty (user modified)
    private boolean settingsAreDirty;
    // properties file (for persistence)
    private final Properties props;
    // internal thread for updating settings
    private final SettingsUpdateThread updateThread;
    private static SettingsManager instance = null;

    public static synchronized SettingsManager getInstance() {
        if (null == instance) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private SettingsManager() {

        // list of listeners
        listeners = new ArrayList<SettingsChangeListener>();

        // map of settings
        entries = new HashMap<Setting, String>();

        // no settings are dirty
        settingsAreDirty = false;

        // properties file
        props = new Properties();

        // kick off the sync thread
        updateThread = new SettingsUpdateThread();
        updateThread.start();
    }

    /**
     * Returns the value of a setting as it exists in the properties file (since
     * last Sync) or the default value defined by the Setting enumeration.
     *
     * @param setting
     * @return
     */
    public String getSetting(final Setting setting) {
        synchronized (entries) {
            if (entries.containsKey(setting)) {
                return entries.get(setting);
            }
        }
        return setting.getDefaultValue();
    }

    /**
     * Persists a new setting in-memory. This will be picked up on the next
     * iteration of the Sync thread.
     *
     * @param setting
     * @param value
     */
    public void setSetting(final Setting setting, final String value) {
        synchronized (entries) {
            entries.put(setting, value);
            settingsAreDirty = true;
        }
    }

    /**
     * Registers a Settings Change Listener for notification.
     *
     * @param listener
     */
    public void registerListener(final SettingsChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a Settings Change Listener for notification.
     *
     * @param listener
     */
    public void removeListener(final SettingsChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Cleanup method to halt listening thread. Take care when using this call.
     */
    public void shutdown() {
        updateThread.shutdown();
        try {
            updateThread.interrupt();
        } catch (Exception e) {
            LOG.warn("Failed to interrupt thread: ", e);
        }
    }

    /**
     * Internal method to signal that settings have been changed.
     */
    private void signalSettingsChanged(final List<Setting> changedSettings) {
        synchronized (listeners) {
            for (SettingsChangeListener listener : listeners) {
                listener.settingsWereUpdated(changedSettings);
            }
        }
    }

    /**
     * Internal method to force a re-sync of properties from the properties
     * file. This method will store properties if the local ones are "dirty".
     */
    private void syncProperties() {

        synchronized (entries) {
            // store based on dirty state
            if (settingsAreDirty) {
                storeProperties();
            }
            loadProperties();
        }
    }

    /**
     * Persists the in-memory properties in the properties file. This method is
     * only called from synchronized blocks and only when settings are dirty, so
     * we know we have something to persist.
     *
     * This needs to be called in a block synchronized on entries!
     */
    private void storeProperties() {
        // update the properties file
        for (Map.Entry<Setting, String> pair : entries.entrySet()) {
            props.setProperty(pair.getKey().getKey(), pair.getValue());
        }

        // save the properties file
        FileOutputStream fos = null;
        try {
            fos = FileUtils.openOutputStream(getPropertiesFile());
            props.store(fos, _PROP_HEADER);
        } catch (IOException ioe) {
            LOG.error("Failed to save properties file", ioe);
        } finally {
            IOUtils.closeQuietly(fos);
        }

        // mark settings as clean
        settingsAreDirty = false;
    }

    /**
     * Loads all properties from the properties file.
     *
     * This needs to be called in a block synchronized on entries!
     */
    private void loadProperties() {

        // load the properties file
        final File file = getPropertiesFile();
        FileInputStream fis = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fis = FileUtils.openInputStream(file);
            props.load(fis);
        } catch (IOException ioe) {
            LOG.error("Failed to load properties file", ioe);
        } finally {
            IOUtils.closeQuietly(fis);
        }

        final ArrayList<Setting> changedSettings = new ArrayList<Setting>();

        for (Setting setting : Setting.values()) {
            final String value = props.getProperty(setting.getKey());
            if (value == null) {
                try {
                    props.setProperty(setting.getKey(), entries.containsKey(setting) ? entries.get(setting) : setting.getDefaultValue());
                    settingsAreDirty = true;
                } catch (Exception e) {
                    LOG.error("Failed to load property " + setting.getKey() + " into cache", e);
                    continue;
                }
            } else {
                entries.put(setting, value);
            }

            // detect any changes here for signaling
            final String origValue = entries.get(setting);
            if (null != origValue && !value.equals(origValue)) {
                changedSettings.add(setting);
            }

        }

        if (!changedSettings.isEmpty()) {
            signalSettingsChanged(changedSettings);
        }
    }

    /**
     * Internal listening thread that periodically syncs properties with
     * persisted properties.
     *
     * @author nbrackett
     */
    private class SettingsUpdateThread extends Thread {

        private boolean running;

        private SettingsUpdateThread() {
            running = false;
        }

        public void run() {
            running = true;
            while (running) {
                try {
                    syncProperties();
                } catch (Exception e) {
                    LOG.error("Failed while syncing properties", e);
                }

                try {
                    Thread.sleep(30 * 1000);		// 30 second refresh time
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }

        private void shutdown() {
            running = false;
        }
    }

    /**
     * Internal method to fetch the properties file.
     *
     */
    private File getPropertiesFile() {

        // get the plugin config directory
        final PlugInManager pluginMgr = PlugInManagerFactory.getInstance();
        final PlugIn plugin = pluginMgr.getPlugIn(_B2_VENDOR, _B2_HANDLE);
        final File configDir = new File(pluginMgr.getPlugInDir(plugin) + _B2_CONFIGDIR);

        // create directory if it doesn't already exist
        if (!configDir.exists()) {
            try {
                FileUtils.forceMkdir(configDir);
            } catch (IOException ioe) {
                LOG.error("Failed to create config directory: " + ioe);
            }
        }

        return new File(configDir, _PROP_FILENAME);

    }
}
