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

package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.wsdl.WSDLException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FirstPanel extends WizardPanel {

    private JLabel lblWSDL;
    private JTextField txtWSDL;
    private JButton btnBrowse;
    private JButton btnHint;
    private JLabel lblHint;
    private boolean flag=false;
    final private String hint="You can select only *.wsdl/*.xml file location.";


    final JFileChooser FileChooser =new JFileChooser();

    private CodegenBean codegenBean;

    public FirstPanel(WizardComponents wizardComponents,CodegenBean codegenBean) {
        super(wizardComponents, "Axis2 Idea Plugin WSDL2Java Wizards");
        this.codegenBean=codegenBean;
        setPanelTopTitle("WSDL selection page");
        setPanelBottomTitle("please Select the WSDl file location");
        init();
    }

    private void init(){

        lblHint =new JLabel("");
        btnHint =new JButton("Hint >>");
        btnHint.setBorder(new EmptyBorder(new Insets(0,0,0,0)));

        this.setLayout(new GridBagLayout());
        lblWSDL =new JLabel("WSDL File Location");
        txtWSDL =new JTextField();
        btnBrowse = new JButton("Browse");

        this.setLayout(new GridBagLayout());
        this.add(lblWSDL
                , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));

        this.add(txtWSDL
                , new GridBagConstraints(1, 0, GridBagConstraints.RELATIVE, 1, 2.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0, 10), 0, 0));

        this.add(btnBrowse
                , new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));
        btnBrowse.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                FileChooser.setFileFilter(new WSDLFileFilter());
                int returnVal = FileChooser.showOpenDialog(btnBrowse);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = FileChooser.getSelectedFile();
                    txtWSDL.setText(file.getAbsolutePath());
                    codegenBean.setWSDLFileName(file.getAbsolutePath());
                    update();
                }
            }
        });

        this.add(btnHint,
                new GridBagConstraints(0, 1, 1, 1, 0.1,0.0
                        , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE
                        , new Insets(5, 10, 0, 10), 0, 0));
        btnHint.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                if(flag){
                    btnHint.setText("Hint >>");
                    lblHint.setText("");
                    flag=false;
                }else{
                    btnHint.setText("Hint <<");
                    lblHint.setText(hint);
                    flag=true;
                }
            }
        });

        this.add(lblHint ,
                new GridBagConstraints(0, 2, GridBagConstraints.REMAINDER, 1, 0.1, 1.0
                        , GridBagConstraints.NORTHWEST , GridBagConstraints.HORIZONTAL
                        , new Insets(5, 10, 0,10), 0, 0));

        this.setPageComplete(false);
    }


    public void next() {
        if(txtWSDL.getText()!=null && isPageComplete()){
            codegenBean.setWSDLFileName(txtWSDL.getText());
            switchPanel(CodegenFrame.PANEL_OPTION_A );
        } else
            switchPanel(CodegenFrame.PANEL_FIRST_A );
    }
    public void back() {
        switchPanel(CodegenFrame.PANEL_CHOOSER );
    }

    public void update(){
        if(!txtWSDL.getText().trim().equals("")){
            try {
                codegenBean.readWSDL();
                setNextButtonEnabled(true);
                setFinishButtonEnabled(false);
                setPageComplete(true);
            } catch (WSDLException e) {
                setNextButtonEnabled(false);
                setFinishButtonEnabled(false);
                setPageComplete(false);
                JOptionPane.showMessageDialog(this, "An error occured while parsing the " +
                        "specified WSDL. Please make sure that the selected file is a valid WSDL.",
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }else{
            setNextButtonEnabled(false);
            setFinishButtonEnabled(false);
            setPageComplete(false);
        }
        setBackButtonEnabled(true);
    }

    public  int getPageType() {
        return  WizardPanel.WSDL_2_JAVA_TYPE;
    }
     public String getWSDLFileName(){
       return txtWSDL.getText().trim();
   }
}

