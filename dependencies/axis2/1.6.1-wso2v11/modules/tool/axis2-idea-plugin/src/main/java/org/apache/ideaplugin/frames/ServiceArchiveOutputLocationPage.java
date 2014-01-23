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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class ServiceArchiveOutputLocationPage  extends WizardPanel {

    private JTextField txtLocation;
    private JTextField txtFileName;
    private JButton btnBrowes;
    private JButton btnHint;
    private JTextArea txaHint;
    private boolean flag=false;
    final JFileChooser DirChooser=new JFileChooser();
    public static final String hint="";
    private ArchiveBean archiveBean;
    /**
     * Constructor
     * @param wizardComponents
     */
    public ServiceArchiveOutputLocationPage(WizardComponents wizardComponents, ArchiveBean archiveBean){
        super(wizardComponents, "Axis2 Idea Plugin Service Archiver Creator Wizards");
        setPanelTopTitle("Service Archiver");
        setPanelBottomTitle("Select the location for the generated Archiver");
        this.archiveBean=archiveBean;
        init();
    }
    private void init(){
        txaHint =new JTextArea();
        txaHint.setBorder(null);
        txaHint.setFocusable(false);
        txaHint.setLineWrap(true);
        txaHint.setWrapStyleWord(true);
        txaHint.setOpaque(false);

        btnHint =new JButton("Hint >>");
        btnHint.setBorder(new EmptyBorder(new Insets(0,0,0,0)));

        txtFileName =new JTextField("Service_Archiver");

        txtLocation=new JTextField();

        btnBrowes=new JButton("Browse..");

        setBackButtonEnabled(true);
        setNextButtonEnabled(false);
        setFinishButtonEnabled(false);
        this.setLayout(new GridBagLayout());

        this.add(new JLabel("OutPut File Name")
                       , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                       , GridBagConstraints.NORTHWEST  , GridBagConstraints.NONE
                       , new Insets(5, 10, 0, 0), 0, 0));

               this.add(txtFileName
                       , new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                       , GridBagConstraints.NORTHWEST  , GridBagConstraints.HORIZONTAL
                       , new Insets(5, 10, 0, 0), 0, 0));

        this.add(new JLabel("OutPut Location")
                , new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 10, 0,0), 0, 0));

        this.add(txtLocation
                , new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0, 0), 0, 0));

        this.add(btnBrowes
                , new GridBagConstraints(2, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.NONE
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
        this.add(btnHint,
                new GridBagConstraints(0, 2, 1, 1, 0.1,0.0
                        , GridBagConstraints.WEST , GridBagConstraints.NONE
                        , new Insets(5, 10, 0, 10), 0, 0));
        btnHint.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                if(flag){
                    btnHint.setText("Hint >>");
                    txaHint.setText("");
                    flag=false;
                }else{
                    btnHint.setText("Hint <<");
                    txaHint.setText(hint);
                    flag=true;
                }
                update();
            }
        });

        this.add(txaHint
                , new GridBagConstraints(0, 3, GridBagConstraints.REMAINDER, 1, 0.1,1.0
                , GridBagConstraints.CENTER , GridBagConstraints.BOTH
                , new Insets(5, 10, 10, 10), 0, 0));
    }


    public void back() {
        if(!((ServiceXMLFileSelectionPage)getWizardComponents().getWizardPanel(CodegenFrame.PANEL_FOURTH_C)).isIncludeXml()){
             switchPanel(CodegenFrame.PANEL_FOURTH_C );
        }else{
            switchPanel(CodegenFrame.PANEL_OPTION_C );
        }
    }

    public void next() {

    }

    public void update(){
        if(!txtFileName.getText() .equals("")){
            archiveBean.setArchiveName(txtFileName.getText());
        }
        if(!txtLocation .getText() .equals("")){
           if(new File(txtLocation.getText()).isDirectory())
                archiveBean.setOutPath(txtLocation.getText());
        }
        setBackButtonEnabled(true);
        setNextButtonEnabled(false);

    }
    public  int getPageType() {
        return  WizardPanel.SERVICE_ARCHIVE_TYPE;
    }
}
 
