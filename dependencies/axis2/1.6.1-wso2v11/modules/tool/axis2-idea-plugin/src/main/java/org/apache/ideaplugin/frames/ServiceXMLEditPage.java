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

package org.apache.ideaplugin.frames;

import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;
import org.apache.ideaplugin.bean.ArchiveBean;
import org.apache.ideaplugin.bean.ParameterObj;
import org.apache.ideaplugin.bean.ValidateXMLFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ServiceXMLEditPage extends WizardPanel {
    protected JTextArea desArea;
    protected JButton addpara;
    protected JButton addModuleRef;
    protected JButton validateXML;
    private JButton reGenerate;
    protected JScrollPane sp;
    private JLabel lblerror;
     private ArchiveBean archiveBean;

     public ServiceXMLEditPage(WizardComponents wizardComponents, ArchiveBean archiveBean){
        super(wizardComponents, "Axis2 Idea Plugin Service Archiver Creator Wizards");
        setPanelTopTitle("Service Archiver");
        setPanelBottomTitle("Edit the generated service.xml");
          this.archiveBean=archiveBean;
        init();
    }
    public void init(){
        ParameterDialog.initialize(addpara, "Parameter Dialog");
        addpara = new JButton("+Parameter ");
        addpara.setEnabled(false);
        addModuleRef = new JButton("+ModuleRef ");
        addModuleRef.setEnabled(false);
        validateXML =new JButton("Validate XML");
        reGenerate=new JButton("ReGenerate XML");
        lblerror=new JLabel();
        desArea = new JTextArea("");
        sp = new JScrollPane(desArea);
        sp.setAutoscrolls(true);
        this.setLayout(new GridBagLayout());
        setDefaultEnabled();

         this.add(addpara
                , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.NONE
                , new Insets(5, 1, 0, 10), 0, 0));

       addpara.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                ParameterObj selectedName=ParameterDialog.showDialog("Parameter Dialog");
                setParameter(selectedName);
                setEnabledToNotValidate();
                update();
            }
        });

        this.add(addModuleRef
                , new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.NONE
                , new Insets(5, 10, 1,10), 0, 0));

        addModuleRef.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                String moduleName = (String)JOptionPane.showInputDialog(
                        addModuleRef,
                        "Module Name","Module Dialog",
                        JOptionPane.PLAIN_MESSAGE);
                setModule(moduleName);
                setEnabledToNotValidate();
                update();
            }
        });
         this.add(validateXML
                , new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.NONE
                , new Insets(5, 10, 1,10), 0, 0));

        validateXML.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                if(new ValidateXMLFile().Validate("<?xml version=\"1.0\"?>\n"+desArea.getText()))  {
                    setEnabledToValidate();
                    lblerror.setText("Service XML file validation successfully");
                } else{
                    setEnabledToNotValidate();
                    lblerror.setText("Error! Service XML file validation Error");
            }
                update();
            }
        });
         this.add(reGenerate
                , new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.NONE
                , new Insets(5, 10, 1,10), 0, 0));

        reGenerate.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                desArea.setText("");
                desArea.setText(archiveBean.getServiceXML());
                update();
            }
        });
        this.add(sp
                , new GridBagConstraints(0, 1,GridBagConstraints.REMAINDER, 1, 1.0, 1.0
                , GridBagConstraints.WEST , GridBagConstraints.BOTH
                , new Insets(5, 10, 10,10), 0, 0));
        desArea.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e){
                addpara.setEnabled(true);
                addModuleRef.setEnabled(true);
            }
            public void mousePressed(MouseEvent e){}
            public void mouseReleased(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        });
        this.add(lblerror
                , new GridBagConstraints(0,2,GridBagConstraints.REMAINDER, 1, 1.0,0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 20, 10,10), 0, 0));

    }

    //next
    public void next() {
        if(!archiveBean.getServiceXML().equals("") ){
           archiveBean.setServiceXML(desArea.getText());
         }
            switchPanel(CodegenFrame.PANEL_LAST_C );

    }
    //back
    public void back() {
        switchPanel(CodegenFrame.PANEL_LOAD_C );
    }
    //update
    public void update() {

    }

    //get page type
    public  int getPageType() {
        return  WizardPanel.SERVICE_ARCHIVE_TYPE;
    }

    public void setDescription(String descrip){
        this.desArea.setText(descrip);
        update();
    }

    private void setEnabledToValidate(){
        setNextButtonEnabled(true);
        reGenerate.setEnabled(false);
        addpara.setEnabled(false);
        addModuleRef.setEnabled(false);
        setPageComplete(true);
    }
    private void setEnabledToNotValidate(){
        reGenerate.setEnabled(true);
        setNextButtonEnabled(false);
        setPageComplete(false);
        addpara.setEnabled(false);
        addModuleRef.setEnabled(false);
    }
    public void setDefaultEnabled(){
        lblerror.setText("");
        addpara.setEnabled(false);
        addModuleRef.setEnabled(false);
        validateXML.setEnabled(true);
        reGenerate.setEnabled(false);
        setNextButtonEnabled(false);
    }
    private void setParameter(ParameterObj obj){
       int position = desArea.getCaretPosition();
        System.out.println(desArea.getLineCount());
        System.out.println(desArea.getCaretPosition());
        String str = "      <parameter name=\"" + obj.getName() + "\" locked=\"false\">"
                            + obj.getValue() +
                            "</parameter>\n";
        desArea.insert(str, position + 1);
    }
    private void setModule(String module){
        int position = desArea.getCaretPosition();
        String str = "      <module ref=\"" + module + "\" />\n";
        desArea .insert(str, position + 1);
    }
}

