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

package org.apache.axis2.tools.wizardframe;

import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.idea.ChooserPanel;
import org.apache.axis2.tools.idea.FirstPanel;
import org.apache.axis2.tools.idea.SecondPanel;
import org.apache.axis2.tools.idea.WSDL2JavaOutputPanel;
import org.apache.axis2.tools.java2wsdl.MiddlePanel;
import org.apache.axis2.tools.java2wsdl.OptionPanel;
import org.apache.axis2.tools.java2wsdl.OutputPanel;
import org.apache.ideaplugin.frames.ClassFileLocationPage;
import org.apache.ideaplugin.frames.LibraryAddingPage;
import org.apache.ideaplugin.frames.ServiceArchiveOutputLocationPage;
import org.apache.ideaplugin.frames.ServiceXMLEditPage;
import org.apache.ideaplugin.frames.ServiceXMLFileSelectionPage;
import org.apache.ideaplugin.frames.ServiceXMLGenerationPage;
import org.apache.ideaplugin.frames.WSDLFileSelectionPage;

/**
 * codeaFrame class
 */

public class CodegenFrame extends WizardFrame{
    /**
     * variable
     */
    public static final int PANEL_CHOOSER = 0;
    public static final int PANEL_FIRST_A = 1;    
    public static final int PANEL_FIRST_B = 2;
    public static final int PANEL_OPTION_A =3;
    public static final int PANEL_OPTION_B = 4;
    public static final int PANEL_LAST_A = 5;
    public static final int PANEL_LAST_B = 6;
    public static final int PANEL_FIRST_C=7;
    public static final int PANEL_SECOND_C=8;
    public static final int PANEL_THIRD_C=9;
    public static final int PANEL_FOURTH_C=10;
    public static final int PANEL_LOAD_C=11;
    public static final int PANEL_OPTION_C=12;
    public static final int PANEL_LAST_C=13;

    public CodegenFrame() {
        init();
    }

    private void init() {
        this.setTitle("Axis2 Codegen Wizard"); 
        WizardPanel panel = null;

        panel = new ChooserPanel(getWizardComponents());
        getWizardComponents().addWizardPanel(PANEL_CHOOSER , panel);

        panel = new FirstPanel(getWizardComponents(),codegenBean);
        getWizardComponents().addWizardPanel(PANEL_FIRST_A , panel);

        panel = new MiddlePanel(getWizardComponents(),wsdlgenBean );
        getWizardComponents().addWizardPanel(PANEL_FIRST_B, panel);

        panel = new SecondPanel(getWizardComponents(),codegenBean);
        getWizardComponents().addWizardPanel(PANEL_OPTION_A , panel);

        panel = new OptionPanel(getWizardComponents(),wsdlgenBean);
        getWizardComponents().addWizardPanel(PANEL_OPTION_B , panel);

        panel = new WSDL2JavaOutputPanel(getWizardComponents(),codegenBean,project);
        getWizardComponents().addWizardPanel(PANEL_LAST_A , panel);      

        panel = new OutputPanel(getWizardComponents(),wsdlgenBean,project);
        getWizardComponents().addWizardPanel(PANEL_LAST_B , panel);

        panel = new ClassFileLocationPage(getWizardComponents(),archiveBean);
        getWizardComponents().addWizardPanel(PANEL_FIRST_C , panel);

        panel = new WSDLFileSelectionPage(getWizardComponents(),archiveBean);
        getWizardComponents().addWizardPanel(PANEL_SECOND_C , panel);

        panel = new LibraryAddingPage(getWizardComponents(),archiveBean);
        getWizardComponents().addWizardPanel(PANEL_THIRD_C , panel);

        panel = new ServiceXMLFileSelectionPage(getWizardComponents(),archiveBean);
        getWizardComponents().addWizardPanel(PANEL_FOURTH_C , panel);

        panel = new ServiceXMLGenerationPage(getWizardComponents(),archiveBean);
        getWizardComponents().addWizardPanel(PANEL_LOAD_C , panel);

        panel = new ServiceXMLEditPage(getWizardComponents(),archiveBean) ;
        getWizardComponents().addWizardPanel(PANEL_OPTION_C , panel);

        panel = new ServiceArchiveOutputLocationPage(getWizardComponents(),archiveBean);
        getWizardComponents().addWizardPanel(PANEL_LAST_C , panel);

    }
}
