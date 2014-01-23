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

/**
 * Created by IntelliJ IDEA.
 * User: shivantha
 * Date: 16/07/2007
 * Time: 10:55:31
 * To change this template use File | Settings | File Templates.
 */
public class ClassFileLocationPage extends WizardPanel {
        /**
         * varialbales
         */
        private JTextField txtClassDir;
        private JButton butSelect;
        private JCheckBox chkBoxIncludeClass;
        private JCheckBox chkBoxArchiveType;
        private JButton btnHint;
        private JTextArea txaHint;
        private boolean flag=false;
        private String hint =":";
        private ArchiveBean archiveBean;
        public final JFileChooser fileChooser = new JFileChooser();
        private File file;
        /**
         * Constructor
         * @param wizardComponents
         */
        public ClassFileLocationPage(WizardComponents wizardComponents, ArchiveBean archiveBean ) {
            super(wizardComponents, "Axis2 Idea Plugin Service Archiver Creator Wizards");
            setPanelTopTitle("Service Archiver");
            setPanelBottomTitle("Welcome to Axis2 Service Archive Wizard.Insert the class files and select the service type. ");
            this.archiveBean=archiveBean;
            init();
        }

        /**
         * initiate panel
         */
        private void init(){

            txaHint =new JTextArea();
            txaHint.setBorder(null);
            txaHint.setFocusable(false);
            txaHint.setLineWrap(true);
            txaHint.setWrapStyleWord(true);
            txaHint.setOpaque(false);
            btnHint =new JButton("Hint >>");
            btnHint.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
            txtClassDir =new JTextField();
            butSelect=new JButton("Browse..");
            chkBoxIncludeClass = new JCheckBox("include .class file only") ;
            chkBoxArchiveType=new JCheckBox("Select for Service Group Archive");

            setBackButtonEnabled(true);
            setFinishButtonEnabled(false);
            setPageComplete(false);

            this.setLayout(new GridBagLayout());

            this.add(new JLabel("Class file location")
                    , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                    , GridBagConstraints.WEST , GridBagConstraints.NONE
                    , new Insets(5, 10, 0,0), 0, 0));

            this.add(txtClassDir
                    , new GridBagConstraints(1, 0, GridBagConstraints.RELATIVE, 1, 1.0, 0.0
                    , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                    , new Insets(5, 1, 1, 1), 0, 0));

            txtClassDir.addActionListener(new ActionListener()  {
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });

            this.add(butSelect
                    , new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
                    , GridBagConstraints.CENTER , GridBagConstraints.NONE
                    , new Insets(5, 1, 1,10), 0, 0));

            butSelect.addActionListener(new ActionListener()  {
                public void actionPerformed(ActionEvent e) {
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = fileChooser.showOpenDialog(butSelect);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                        fileChooser.setCurrentDirectory(file);
                        txtClassDir.setText(file.getAbsolutePath());
                        setPageComplete(true);
                        setNextButtonEnabled(true);
                    } else {
                        txtClassDir.setText("");
                    }

                    update();
                }
            });

            this.add(chkBoxIncludeClass
                    , new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                    , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                    , new Insets(5, 10, 1,1), 0, 0));
            chkBoxIncludeClass.addActionListener(new ActionListener()  {
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });

//             this.add(chkBoxArchiveType
//                    , new GridBagConstraints(0, 2, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
//                    , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
//                    , new Insets(5, 10, 1,1), 0, 0));
//            chkBoxArchiveType .addActionListener(new ActionListener()  {
//                public void actionPerformed(ActionEvent e) {
//                    update();
//                }
//            });
            this.add(btnHint,
                    new GridBagConstraints(0, 2, 1, 1, 0.0,0.0
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
                    , new GridBagConstraints(0, 3, GridBagConstraints.REMAINDER, 1, 0.0,1.0
                    , GridBagConstraints.WEST , GridBagConstraints.BOTH
                    , new Insets(5, 10, 10, 10), 0, 0));

        }

        //next
        public void next() {
            switchPanel(CodegenFrame.PANEL_SECOND_C );
        }
        //back
        public void back() {
            switchPanel(CodegenFrame.PANEL_CHOOSER );
        }
        //update
        public void update() {
            fillBean();
        }
        private void fillBean(){
//          if(!chkBoxArchiveType.isSelected()){
//               archiveBean.setSingleService(true);
//          } else
//              archiveBean.setSingleService(false);
          if(!txtClassDir.getText().trim().equals("")){
              archiveBean.setClassLoc(file);
              archiveBean.addClassLocation(file);
          }
           if(chkBoxIncludeClass.isSelected()){
               archiveBean.setIncludeClass(true);
          } else
              archiveBean.setIncludeClass(false);
        }

        //get page type
        public  int getPageType() {
            return  WizardPanel.SERVICE_ARCHIVE_TYPE;
        }
 }

