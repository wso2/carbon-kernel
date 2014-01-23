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

import org.apache.axis2.tool.service.bean.Page3Bean;
import org.apache.axis2.tool.service.bean.WizardBean;
import org.apache.axis2.tool.util.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class WizardPane3 extends WizardPane {

    private Page3Bean myBean;

    private JLabel outputFileLocationLabel;
    private JTextField outputFileLocationTextBox;
    private JButton browseButton;

    private JLabel outputFileNameLabel;
    private JTextField outputFileNameTextBox;

    public WizardPane3(WizardBean bean, JFrame ownerFrame) {
        super(ownerFrame);
        init();
        if (bean.getPage3bean() != null) {
            this.myBean = bean.getPage3bean();
            setBeanValues();
        } else {
            this.myBean = new Page3Bean();
            bean.setPage3bean(myBean);
        }
    }

    public boolean validateValues() {
        String text1 = myBean.getOutputFileName();
        String text2 = myBean.getOutputFolderName();
        boolean text1Validity = (text1 != null && text1.trim().length() > 0);
        boolean text2Validity = (text2 != null && text2.trim().length() > 0);

        return text1Validity && text2Validity;
    }

    private void setBeanValues() {
        this.outputFileLocationTextBox.setText(myBean.getOutputFolderName());
        this.outputFileNameTextBox.setText(myBean.getOutputFileName());
    }

    private void init() {
        this.setLayout(null);
        this.setSize(width, height);

        initDescription("\nInput the location for the output file and the name for \n" +
                "the compiled jar file ");


        this.outputFileLocationLabel = new JLabel("Output Folder");
        this.add(this.outputFileLocationLabel);
        this.outputFileLocationLabel.setBounds(hgap,
                descHeight,
                Constants.UIConstants.LABEL_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);

        this.outputFileLocationTextBox = new JTextField();
        this.add(this.outputFileLocationTextBox);
        this.outputFileLocationTextBox.setBounds(
                Constants.UIConstants.LABEL_WIDTH + 2 * hgap,
                descHeight,
                Constants.UIConstants.TEXT_BOX_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.outputFileLocationTextBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLocationChange();
            }
        });
        this.outputFileLocationTextBox.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                handleLocationChange();
            }

            public void keyReleased(KeyEvent e) {
            }
        });


        this.browseButton = new JButton(".");
        this.add(this.browseButton);
        this.browseButton.setBounds(
                Constants.UIConstants.LABEL_WIDTH + 2 * hgap +
                Constants.UIConstants.TEXT_BOX_WIDTH,
                descHeight,
                Constants.UIConstants.BROWSE_BUTTON_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputFileLocationTextBox.setText(browseForAFolder());
                handleLocationChange();

            }
        });


        this.outputFileNameLabel = new JLabel("Out File Name");
        this.add(this.outputFileNameLabel);
        this.outputFileNameLabel.setBounds(hgap,
                descHeight + Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap,
                Constants.UIConstants.LABEL_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);

        this.outputFileNameTextBox = new JTextField();
        this.add(this.outputFileNameTextBox);
        this.outputFileNameTextBox.setBounds(
                Constants.UIConstants.LABEL_WIDTH + 2 * hgap,
                descHeight + Constants.UIConstants.GENERAL_COMP_HEIGHT + vgap,
                Constants.UIConstants.TEXT_BOX_WIDTH,
                Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.outputFileNameTextBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleFileNameChange();
            }
        });
        this.outputFileNameTextBox.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                handleFileNameChange();
            }
        });

    }

    private void handleLocationChange() {
        myBean.setOutputFolderName(outputFileLocationTextBox.getText());
    }

    private void handleFileNameChange() {
        myBean.setOutputFileName(outputFileNameTextBox.getText());
    }

}
