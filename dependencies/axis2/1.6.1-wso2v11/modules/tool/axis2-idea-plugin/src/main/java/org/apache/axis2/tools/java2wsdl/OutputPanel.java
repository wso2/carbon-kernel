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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.axis2.tools.bean.WsdlgenBean;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/** this class java 2 wsdl output wizard panel   */
public class OutputPanel extends WizardPanel {

    private JRadioButton rbtnAdd;
    private JRadioButton rbtnSave;
    private JComboBox cmbCurrentProject;
    private JComboBox cmbModuleSrc;
    private JTextField txtLocation;
    private JTextField txtFileName;
    private JLabel lblModule;
    private JLabel lblDirect;
    private JLabel lblOutput;
    private JButton btnBrowes;
    final JFileChooser DirChooser=new JFileChooser();
    private Project project;
    private WsdlgenBean wsdlgenBean;
    private boolean flag = true;

    /**
     * Constructor
     * @param wizardComponents
     * @param wsdlgenBean
     * @param project
     */
    public OutputPanel(WizardComponents wizardComponents,WsdlgenBean wsdlgenBean, Project project){
        super(wizardComponents,  "Axis2 Idea Plugin Java2WSDL Wizards");
        setPanelTopTitle("WSDl file output location");
        setPanelBottomTitle("Select the location for the generated WSDL");
        this.wsdlgenBean=wsdlgenBean;
        this.project=project;
        init();
    }
    private void init(){

        rbtnAdd =new JRadioButton("Browes and Add the WSDL to a project on current Idea workspace",true);

        rbtnSave =new JRadioButton("Browes and Save the WSDL file on local file system",false);

        cmbCurrentProject =new JComboBox();
        cmbCurrentProject.setEnabled(true);
        cmbModuleSrc=new JComboBox();
        cmbModuleSrc.setEnabled(true);
        ButtonGroup  buttonGroup= new  ButtonGroup();
        buttonGroup.add(rbtnAdd );
        buttonGroup.add(rbtnSave );

        txtFileName =new JTextField("service.wsdl");

        txtLocation=new JTextField();

        btnBrowes=new JButton("Browse..");
        lblOutput=new JLabel("OutPut Location");
        lblModule=new JLabel("Select the Module");
        lblModule.setEnabled(false);
        lblDirect=new JLabel("Select the Directory");
        lblDirect.setEnabled(false);

        setBackButtonEnabled(true);
        setNextButtonEnabled(false);
        setFinishButtonEnabled(false);
        this.setLayout(new GridBagLayout());

        this.add(new JLabel("OutPut File Name")
                , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 10, 0, 0), 0, 0));

        this.add(txtFileName
                , new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0, 0), 0, 0));


        this.add(new JLabel("Select the location where to put the output")
                , new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1,  0.0, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.NONE
        , new Insets(5, 10, 0, 10), 0, 0));

        this.add(rbtnAdd
        , new GridBagConstraints(0, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
        , GridBagConstraints.WEST , GridBagConstraints.NONE
        , new Insets(5, 10, 0,10), 0, 0));
        rbtnAdd.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                cmbCurrentProject.setEnabled(true);
                cmbModuleSrc.setEnabled(true);
                lblModule.setEnabled(true);
                txtLocation .setEnabled(false);
                btnBrowes.setEnabled(false);
                loadCmbCurrentProject();
                loadcmbModuleSrcProject();
                setFinishButtonEnabled(true);
                update();
            }
        });

         this.add(lblModule
        , new GridBagConstraints(0, 3, 1, 1,  0.1, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.NONE
        , new Insets(5, 10, 0, 0), 0, 0));

        this.add(cmbCurrentProject
        , new GridBagConstraints(1, 3, GridBagConstraints.RELATIVE, 1, 1.0, 0.0
        , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
        , new Insets(5, 10, 0,0), 0, 0));
        cmbCurrentProject.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                loadcmbModuleSrcProject();
                update();
            }
        });
         this.add(lblDirect
        , new GridBagConstraints(0, 4, 1, 1,  0.1, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.NONE
        , new Insets(5, 10, 0, 0), 0, 0));

        this.add(cmbModuleSrc
        , new GridBagConstraints(1, 4, GridBagConstraints.RELATIVE, 1, 1.0, 0.0
        , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
        , new Insets(5, 10, 0,0), 0, 0));
        cmbModuleSrc.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {                  
                update();
            }
        });

        this.add(rbtnSave
        , new GridBagConstraints(0, 5, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.NONE
        , new Insets(5, 10, 0,0), 0, 0));
        rbtnSave.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                cmbCurrentProject.setEnabled(false);
                cmbModuleSrc.setEnabled(false);
                txtLocation .setEnabled(true);
                btnBrowes.setEnabled(true);
                setEnabledForCustomProject();
                update();
            }
        });
        this.add(lblOutput
        , new GridBagConstraints(0, 6, 1, 1, 0.1, 1.0
        , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
        , new Insets(5, 10, 0,0), 0, 0));

         this.add(txtLocation
        , new GridBagConstraints(1, 6, 1, 1, 1.0, 1.0
        , GridBagConstraints.NORTHWEST , GridBagConstraints.HORIZONTAL
        , new Insets(5, 10, 0, 0), 0, 0));

         this.add(btnBrowes
        , new GridBagConstraints(2, 6, 1, 1, 0.1, 1.0
        , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
        , new Insets(5, 10, 0, 10), 0, 0));

        btnBrowes.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                DirChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = DirChooser.showOpenDialog(btnBrowes );
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    DirChooser.setFileSelectionMode(JFileChooser .FILES_ONLY );
                    File newfile = DirChooser.getSelectedFile();
                    txtLocation.setText(newfile.getAbsolutePath() );
                }
                update();
            }
        });

        wsdlgenBean.setProject(project);
    }

    public void loadCmbCurrentProject() {
        Module modules[] = wsdlgenBean.getModules();

        if (modules != null) {
            for (int count = 0; count < modules.length; count++) {
                cmbCurrentProject.addItem(modules[count].getName());
            }
        }else{
            rbtnSave.setSelected(true);
            rbtnAdd.setEnabled(false);
      }
    }
     public void loadcmbModuleSrcProject() {
        String module = null;
        module = (String) cmbCurrentProject.getSelectedItem();
        cmbModuleSrc.removeAllItems();
        int count = 0;
        if (module != null) {
            String src[] = wsdlgenBean.getModuleSrc(module);
            for ( count = 0; count < src.length; count++) {
                cmbModuleSrc.addItem(src[count]);
            }
            count = src.length;
        }
         if (flag)
        {
            if (count == 0) {
                flag =false;
                setEnabledForCustomProject();
            }
            else{
                setEnabledForCurrentProject();
            }
        }
    }

     public void back() {
          switchPanel(CodegenFrame.PANEL_OPTION_B );
    }

    public void next() {

    }

    public void update(){
        if(rbtnSave.isSelected()){
            if(txtFileName.getText() !=null && txtLocation.getText() !=null) {
                wsdlgenBean.setOutputWSDLName(txtFileName.getText());
                wsdlgenBean.setOutputLocation(txtLocation.getText());
                setFinishButtonEnabled(true);
            }else if(txtLocation.getText() !=null){
                wsdlgenBean.setOutputLocation(txtLocation.getText());
                setFinishButtonEnabled(false);
            }else if(txtFileName.getText() !=null){
                wsdlgenBean.setOutputWSDLName(txtFileName .getText());
                setFinishButtonEnabled(false);
            } else
                setFinishButtonEnabled(false);
        }else if(rbtnAdd.isSelected()){
            if(txtFileName.getText() !=null && cmbModuleSrc.getSelectedItem() !=null){
                wsdlgenBean.setOutputWSDLName(txtFileName.getText());
                wsdlgenBean.setOutputLocation(cmbModuleSrc.getSelectedItem().toString());
                setFinishButtonEnabled(true);
            }else if(txtFileName .getText() !=null){
                wsdlgenBean.setOutputWSDLName(txtFileName .getText());
                setFinishButtonEnabled(false);
            }else
                setFinishButtonEnabled(false);
        }
        setBackButtonEnabled(true);
        setNextButtonEnabled(false);

    }
     public  int getPageType() {
        return  WizardPanel.JAVA_2_WSDL_TYPE;
    }
     private void setEnabledForCurrentProject(){
        rbtnAdd.setSelected(true);
        rbtnAdd.setEnabled(true);
        cmbCurrentProject.setEnabled(true);
        cmbModuleSrc.setEnabled(true);
        lblDirect.setEnabled(true);
        lblModule .setEnabled(true);
        rbtnSave.setSelected(false);
        txtLocation.setEnabled(false);
        lblOutput .setEnabled(false);
        btnBrowes.setEnabled(false);
    }
    private void setEnabledForCustomProject(){
        if(flag){
            rbtnAdd.setEnabled(false);
            rbtnAdd.setSelected(false);
        }else{
            rbtnSave .setEnabled(true);
            rbtnSave .setSelected(true);
        }
        cmbCurrentProject.setEnabled(false);
        cmbModuleSrc.setEnabled(false);
        lblDirect.setEnabled(false);
        lblModule .setEnabled(false);
        txtLocation .setEnabled(true);
        lblOutput .setEnabled(true);
        btnBrowes .setEnabled(true);
    }
}
 
