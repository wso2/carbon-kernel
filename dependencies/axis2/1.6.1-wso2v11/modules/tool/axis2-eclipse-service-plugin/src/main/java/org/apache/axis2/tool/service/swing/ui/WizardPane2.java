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

package org.apache.axis2.tool.service.swing.ui;

import org.apache.axis2.tool.service.bean.Page2Bean;
import org.apache.axis2.tool.service.bean.WizardBean;
import org.apache.axis2.tool.service.control.Controller;
import org.apache.axis2.tool.service.control.ProcessException;
import org.apache.axis2.tool.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class WizardPane2 extends WizardPane {

    private WizardBean parentBean;
    private Page2Bean myBean;

    private JRadioButton selectManualFileRadioButton;
    private JRadioButton createAutomaticFileRadioButton;
    private JPanel selectionPanel;


    public WizardPane2(WizardBean wizardBean, JFrame ownerFrame) {
        super(ownerFrame);

        init();

        parentBean = wizardBean;

        if (wizardBean.getPage2bean() != null) {
            myBean = wizardBean.getPage2bean();
            //set the initial settings from the bean
            setBeanValues();

        } else {
            myBean = new Page2Bean();
            wizardBean.setPage2bean(myBean);
            setDefaultValues();
        }

    }

    public void setBeanValues() {
        if (myBean.isManual()) {
            this.selectManualFileRadioButton.setSelected(true);
            loadScreen(new ManualSelectionPanel(true));
        } else {
            this.createAutomaticFileRadioButton.setSelected(true);
            loadScreen(new AutomaticSelectionPanel(true));
        }
    }


    public boolean validateValues() {
        String text = "";
        String text2 = "";
        boolean returnValue = false;
        if (myBean.isManual()) {
            text = myBean.getManualFileName();
            returnValue = (text != null && text.trim().length() > 0);
        } else {
            text = myBean.getAutomaticClassName();
            text2 = myBean.getProviderClassName();
            returnValue = (text != null && text.trim().length() > 0) &&
                    (text2 != null && text2.trim().length() > 0);
        }

        return returnValue;
    }

    private void init() {
        this.setLayout(null);
        this.setSize(width, height);

        initDescription("\n Select either the service xml file or the class that you want to \n " +
                " expose as the service to auto generate a service.xml. \n " +
                " Only the class files that are in the previously selected location can\n" +
                " be laded from here");

        ButtonGroup group = new ButtonGroup();

        this.selectManualFileRadioButton =
                new JRadioButton("Select a file manually");
        this.selectManualFileRadioButton.setBounds(hgap,
                descHeight,
                Constants.UIConstants.RADIO_BUTTON_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.add(this.selectManualFileRadioButton);
        group.add(selectManualFileRadioButton);
        this.selectManualFileRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSelectionScreen();
            }
        });
        this.createAutomaticFileRadioButton =
                new JRadioButton("Create a file automatically");
        this.createAutomaticFileRadioButton.setBounds(hgap,
                descHeight + vgap + Constants.UIConstants.GENERAL_COMP_HEIGHT,
                Constants.UIConstants.RADIO_BUTTON_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.add(this.createAutomaticFileRadioButton);
        group.add(createAutomaticFileRadioButton);
        this.createAutomaticFileRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSelectionScreen();
            }
        });

        this.selectionPanel = new JPanel();
        this.selectionPanel.setLayout(null);
        this.selectionPanel.setBounds(0,
                descHeight + 2 * Constants.UIConstants.GENERAL_COMP_HEIGHT +
                2 * vgap,
                width,
                100);
        this.add(this.selectionPanel);

        //select manual option by default


    }

    private void setDefaultValues() {
        this.selectManualFileRadioButton.setSelected(true);
        loadScreen(new ManualSelectionPanel());
        updateBeanFlags(true);
    }

    private void changeSelectionScreen() {
        if (selectManualFileRadioButton.isSelected()) {
            loadScreen(new ManualSelectionPanel(true));
            updateBeanFlags(true);
        } else {
            loadScreen(new AutomaticSelectionPanel(true));
            updateBeanFlags(false);
        }
    }

    private void updateBeanFlags(boolean flag) {
        myBean.setManual(flag);
        myBean.setAutomatic(!flag);
    }

    private void loadScreen(JPanel panel) {
        this.selectionPanel.removeAll();
        this.selectionPanel.add(panel);
        this.repaint();
    }


    private class ManualSelectionPanel extends JPanel {

        private JLabel serverXMLFileLocationLabel;
        private JTextField serverXMLFileLocationTextBox;
        private JButton browseButton;

        public ManualSelectionPanel() {
            init();
        }

        public ManualSelectionPanel(boolean loadVals) {
            init();
            if (loadVals) {
                this.serverXMLFileLocationTextBox.setText(
                        myBean.getManualFileName());
            }
        }

        private void init() {
            this.setLayout(null);
            this.setSize(width, 100);

            this.serverXMLFileLocationLabel = new JLabel("Service File");
            this.add(this.serverXMLFileLocationLabel);
            this.serverXMLFileLocationLabel.setBounds(hgap,
                    vgap,
                    Constants.UIConstants.LABEL_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);

            this.serverXMLFileLocationTextBox = new JTextField();
            this.add(this.serverXMLFileLocationTextBox);
            this.serverXMLFileLocationTextBox.setBounds(
                    Constants.UIConstants.LABEL_WIDTH + 2 * hgap,
                    vgap,
                    Constants.UIConstants.TEXT_BOX_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.serverXMLFileLocationTextBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setOutFileName();
                }
            });
            this.serverXMLFileLocationTextBox.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                    setOutFileName();
                }
            });

            this.browseButton = new JButton(".");
            this.add(this.browseButton);
            this.browseButton.setBounds(
                    Constants.UIConstants.LABEL_WIDTH + 2 * hgap +
                    Constants.UIConstants.TEXT_BOX_WIDTH,
                    vgap,
                    Constants.UIConstants.BROWSE_BUTTON_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    serverXMLFileLocationTextBox.setText(browseForAFile("xml"));
                    setOutFileName();
                }
            });

        }

        private void setOutFileName() {
            myBean.setManualFileName(serverXMLFileLocationTextBox.getText());
        }
    }

    private class AutomaticSelectionPanel extends JPanel {

        private JLabel classFileListLable;
        private JLabel providerClassLable;
        private JTextField classFileNameTextBox;
        private JTextField providerClassNameTextBox;
        private JButton loadButton;
        private JButton advancedButton;

        public AutomaticSelectionPanel() {
            init();
        }

        public AutomaticSelectionPanel(boolean loadVals) {
            init();
            if (loadVals) {
                this.classFileNameTextBox.setText(
                        myBean.getAutomaticClassName());
                this.providerClassNameTextBox.setText(
                        myBean.getProviderClassName());
            }
        }

        private void init() {
            this.setLayout(null);
            this.setSize(width, 100);

            this.classFileListLable = new JLabel("Class Name");
            this.add(this.classFileListLable);
            this.classFileListLable.setBounds(hgap,
                    vgap,
                    Constants.UIConstants.LABEL_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);

            this.classFileNameTextBox = new JTextField();
            this.add(this.classFileNameTextBox);
            this.classFileNameTextBox.setBounds(
                    Constants.UIConstants.LABEL_WIDTH + 2 * hgap,
                    vgap,
                    Constants.UIConstants.TEXT_BOX_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.classFileNameTextBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setClassName();
                }
            });
            this.classFileNameTextBox.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                    setClassName();
                }
            });

            this.providerClassLable = new JLabel("Provider Class Name");
            this.add(this.providerClassLable);
            this.providerClassLable.setBounds(hgap,
                    (Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap),
                    Constants.UIConstants.LABEL_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);

            this.providerClassNameTextBox = new JTextField();
            this.add(this.providerClassNameTextBox);
            this.providerClassNameTextBox.setBounds(
                    Constants.UIConstants.LABEL_WIDTH + 2 * hgap,
                    (Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap * 2),
                    Constants.UIConstants.TEXT_BOX_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.providerClassNameTextBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setProviderClassName();
                }
            });
            this.providerClassNameTextBox.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                    setProviderClassName();
                }
            });

            this.loadButton = new JButton("Load");
            this.add(this.loadButton);
            this.loadButton.setBounds(hgap, (Constants.UIConstants.GENERAL_COMP_HEIGHT +
                    vgap) *
                    2 +
                    vgap,
                    Constants.UIConstants.GENERAL_BUTTON_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadAllMethods();
                }
            });
            loadButton.setEnabled(false);

            this.advancedButton = new JButton("Advanced");
            this.add(this.advancedButton);
            this.advancedButton.setBounds(
                    2 * hgap + Constants.UIConstants.GENERAL_BUTTON_WIDTH
                    , (Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap) * 2 +
                    vgap,
                    Constants.UIConstants.GENERAL_BUTTON_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.advancedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openDialog();
                }
            });
            this.advancedButton.setEnabled(false);
        }

        private void loadAllMethods() {
            try {
                ArrayList methodList = new Controller().getMethodList(
                        parentBean);
                myBean.setSelectedMethodNames(methodList);
                loadButton.setEnabled(false);
                advancedButton.setEnabled(true);
            } catch (ProcessException e) {
                showErrorMessage(e.getMessage());
            }
        }

        private void openDialog() {
            try {
                new AdvancedSelectionDialog().show();
            } catch (ProcessException e) {
                showErrorMessage(e.getMessage());
            }
        }

        private void setClassName() {
            loadButton.setEnabled(true);
            advancedButton.setEnabled(false);
            myBean.setAutomaticClassName(classFileNameTextBox.getText());
        }

        private void setProviderClassName() {
            //loadButton.setEnabled(true);
            //advancedButton.setEnabled(false);
            myBean.setProviderClassName(providerClassNameTextBox.getText());
        }


    }


    private class AdvancedSelectionDialog extends JDialog {

        private JPanel lablePanel;
        private JButton okButton;
        private JButton cancelButton;
        private boolean[] selectedValues;
        private ArrayList completeMethodList;


        public AdvancedSelectionDialog() throws HeadlessException,
                ProcessException {
            super();
            super.setModal(true);
            super.setTitle("Select Methods");
            this.getContentPane().setLayout(null);
            init();
        }

        private void init() throws ProcessException {
            //load the class file list
            this.completeMethodList =
                    new Controller().getMethodList(parentBean);
            int methodCount = this.completeMethodList.size();
            int panelHeight = methodCount *
                    (Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap);

            this.lablePanel = new JPanel();
            this.lablePanel.setLayout(null);
            this.lablePanel.setBounds(0, 0, width, panelHeight);
            this.getContentPane().add(this.lablePanel);

            ArrayList currentSelectedList = myBean.getSelectedMethodNames();
            //create check boxes for all the methods and add them to the panel
            JCheckBox tempCheckBox;
            boolean currentSelection;
            this.selectedValues = new boolean[methodCount];

            for (int i = 0; i < methodCount; i++) {
                tempCheckBox =
                        new JCheckBox(
                                this.completeMethodList.get(i).toString());
                currentSelection =
                        currentSelectedList.contains(
                                this.completeMethodList.get(i));
                tempCheckBox.setSelected(currentSelection);
                selectedValues[i] = currentSelection;
                tempCheckBox.setBounds(hgap, vgap +
                        (Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap) * i,
                        Constants.UIConstants.LABEL_WIDTH * 3,
                        Constants.UIConstants.GENERAL_COMP_HEIGHT);
                tempCheckBox.addActionListener(
                        new CheckBoxActionListner(tempCheckBox, i));
                this.lablePanel.add(tempCheckBox);

            }

            okButton = new JButton("OK");
            this.getContentPane().add(this.okButton);
            this.okButton.setBounds(hgap, panelHeight + vgap,
                    Constants.UIConstants.GENERAL_BUTTON_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadValuesToBean();
                    closeMe();
                }
            });

            cancelButton = new JButton("Cancel");
            this.getContentPane().add(this.cancelButton);
            this.cancelButton.setBounds(
                    hgap * 2 + Constants.UIConstants.GENERAL_BUTTON_WIDTH, panelHeight +
                    vgap,
                    Constants.UIConstants.GENERAL_BUTTON_WIDTH,
                    Constants.UIConstants.GENERAL_COMP_HEIGHT);
            this.cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    closeMe();
                }
            });

            this.setSize(width,
                    panelHeight +
                    2 * Constants.UIConstants.GENERAL_COMP_HEIGHT +
                    30);
            this.setResizable(false);
        }

        private void updateSelection(JCheckBox checkBox, int index) {
            if (checkBox.isSelected()) {
                selectedValues[index] = true;
            } else {
                selectedValues[index] = false;
            }

        }

        private void loadValuesToBean() {
            ArrayList modifiedMethodList = new ArrayList();
            for (int i = 0; i < selectedValues.length; i++) {
                if (selectedValues[i])
                    modifiedMethodList.add(completeMethodList.get(i));
            }

            myBean.setSelectedMethodNames(modifiedMethodList);
        }

        private void closeMe() {
            this.dispose();
        }

        private class CheckBoxActionListner implements ActionListener {
            private JCheckBox checkBox;
            private int index;

            public CheckBoxActionListner(JCheckBox checkBox, int index) {
                this.index = index;
                this.checkBox = checkBox;
            }

            public void actionPerformed(ActionEvent e) {
                updateSelection(checkBox, index);
            }

        }
    }


}
