/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ideaplugin.plugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import javax.xml.stream.XMLInputFactory;

public class Axis2IdeaPlugin implements ApplicationComponent, Configurable {
    private CodegenFrame form;
    private ImageIcon myIcon;

    /**
     * Method is called after plugin is already created and configured. Plugin can start to communicate with
     * other plugins only in this method.
     */
    public void initComponent() {
        try {
            XMLInputFactory.newInstance();
        } catch (Exception e) {
            //Fixing class loading issue
        } catch (Throwable e) {
            ////Fixing class loading issue
        }

        if (form == null) {
            form = new CodegenFrame();
            form.setResizable(true);
            form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        if (myIcon == null) {
            java.net.URL resource = Axis2IdeaPlugin.class.getResource("/icons/icon.png");
            myIcon = new ImageIcon(resource);
        }
    }

    /**
     * This method is called on plugin disposal.
     */
    public void disposeComponent() {
    }

    /**
     * Returns the name of component
     *
     * @return String representing component name. Use PluginName.ComponentName notation
     *         to avoid conflicts.
     */
    public String getComponentName() {
        return "ActionsSample.ActionsPlugin";
    }

    public String getDisplayName() {
        return "Axis2 Plug-ins";
    }

    public Icon getIcon() {
        return myIcon;
    }

    public String getHelpTopic() {
        return "No help available";
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new CodegenFrame();
        }
        return form.getRootComponent();
    }

    public boolean isModified() {
        return false;
    }

    public void apply() throws ConfigurationException {

    }

    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disposeUIResources() {
        form = null;
    }

    public void showTool(Project project) {
        form.setProject(project);
        form.showUI();

    }
}

