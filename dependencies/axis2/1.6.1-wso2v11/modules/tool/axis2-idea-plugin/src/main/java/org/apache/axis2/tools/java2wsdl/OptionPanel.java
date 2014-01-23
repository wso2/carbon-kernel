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

package org.apache.axis2.tools.java2wsdl;


import org.apache.axis2.tools.bean.WsdlgenBean;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import java.awt.*;

/**
 * this is the first panel of java2wsdl wizard
 */
public class OptionPanel extends WizardPanel{
    /**
     * variable
     */
    private JTextField txtNsp;
    private JTextField txtNspPrefix;
    private JTextField txtSchemaTargetNsp;
    private JTextField txtSchemaTargetNspPrefix;
    private JTextField txtService;
    private WsdlgenBean wsdlgenBean;

    /**
     * Construct method
     * @param wizardComponents
     * @param wsdlgenBean
     */
    public OptionPanel(WizardComponents wizardComponents,WsdlgenBean wsdlgenBean){
        super(wizardComponents,  "Axis2 Idea Plugin Java2WSDL Wizards");
        setPanelTopTitle("Java to WSDL Options");
        setPanelBottomTitle("Set the Option for the generation");
        this.wsdlgenBean=wsdlgenBean;
        init();
    }
    /** initaite method*/
    public void init(){

        txtNsp =new JTextField();

        txtNspPrefix =new JTextField();

        txtSchemaTargetNsp =new JTextField();

        txtSchemaTargetNspPrefix =new JTextField();

        txtService =new JTextField();

        setBackButtonEnabled(true);
        setNextButtonEnabled(true);
        setFinishButtonEnabled(false);
        this.setLayout(new GridBagLayout());

        this.add(new JLabel("Target Namespace")
                , new GridBagConstraints(0, 0, 1, 1,  0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 20, 0,0), 0, 0));

        this.add(txtNsp
                , new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 5, 0,20), 0, 0));

        this.add(new JLabel("Target Namespace Prefix")
                , new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 20, 0, 0), 0, 0));

        this.add(txtNspPrefix
                , new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 5, 0,20), 0, 0));

        this.add(new JLabel("Schema Target Namespace")
                , new GridBagConstraints(0, 2, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 20, 0,0), 0, 0));

        this.add(txtSchemaTargetNsp
                , new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 5, 0,20), 0, 0));

        this.add(new JLabel("Schema Target Namespace Prefix")
                , new GridBagConstraints(0, 3, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 20, 0, 0), 0, 0));

        this.add(txtSchemaTargetNspPrefix
                , new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 5, 0,20), 0, 0));

        this.add(new JLabel("Service Name")
                , new GridBagConstraints(0, 4, 1, 1, 0.1, 1.0
                , GridBagConstraints.NORTHWEST  , GridBagConstraints.NONE
                , new Insets(5, 20, 0, 0), 0, 0));

        this.add(txtService
                , new GridBagConstraints(1, 4, 2, 1, 1.0, 1.0
                , GridBagConstraints.NORTHWEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 5, 0,20), 0, 0));

    }
    //next
    public void next() {
        switchPanel(CodegenFrame.PANEL_LAST_B );
    }
    //back
    public void back() {
        switchPanel(CodegenFrame.PANEL_FIRST_B );
    }
    //update
    public void update() {
        if(wsdlgenBean.getTargetNamespace()!=null )
            txtNsp.setText(wsdlgenBean.getTargetNamespace());
        if(wsdlgenBean.getTargetNamespacePrefix()!=null )
            txtNspPrefix .setText(wsdlgenBean.getTargetNamespacePrefix());
        if(wsdlgenBean.getSchemaTargetNamespace()!=null )
            txtSchemaTargetNsp .setText(wsdlgenBean.getSchemaTargetNamespace());
        if(wsdlgenBean.getSchemaTargetNamespacePrefix()!=null )
            txtSchemaTargetNspPrefix.setText(wsdlgenBean.getSchemaTargetNamespacePrefix());
        if(wsdlgenBean.getServiceName()!=null )
            txtService.setText(wsdlgenBean.getServiceName());
        ((OutputPanel)getWizardComponents().getWizardPanel(CodegenFrame.PANEL_LAST_B)).loadCmbCurrentProject();
        ((OutputPanel)getWizardComponents().getWizardPanel(CodegenFrame.PANEL_LAST_B)).loadcmbModuleSrcProject();        
        setBackButtonEnabled(true);
        setNextButtonEnabled(true);
        setFinishButtonEnabled(false);
        setPageComplete(isComplete());
    }
    // get page type
    public  int getPageType() {
        return  WizardPanel.JAVA_2_WSDL_TYPE;
    }

    private boolean isComplete(){
        if(txtNsp.getText()!=null
                && txtNspPrefix.getText()!=null
                && txtSchemaTargetNsp.getText()!=null
                && txtSchemaTargetNspPrefix.getText()!=null
                && txtService.getText()!=null){
            return true;
        }else{
            return  false;
        }
    }
}
