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

package sample.spellcheck.editor;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * class sample.google.spellcheck.SyncPanel
 * This Impements its own GUI of the Synchronous Client and it send the SOAP request after the mouse event.
 */
public class SyncPanel extends javax.swing.JPanel implements Observer,
        ActionListener {
    FormModel formModel;
    JTextArea writingTextArea;
    JTextArea displayTextArea;
    JTextField errorMessageField;
    JButton sendButton;

    public SyncPanel() {
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints constraint = new GridBagConstraints();
        this.setLayout(gbLayout);

        formModel = new FormModel(this);
        writingTextArea = new javax.swing.JTextArea();
        writingTextArea.setLineWrap(true);

        displayTextArea = new javax.swing.JTextArea();
        displayTextArea.setLineWrap(true);
        displayTextArea.setEditable(false);

        errorMessageField = new JTextField();
        errorMessageField.setEditable(false);
        errorMessageField.setBackground(Color.LIGHT_GRAY);
        errorMessageField.setForeground(Color.RED);

        sendButton = new javax.swing.JButton("Send");
        JScrollPane scrollPaneget = new JScrollPane(writingTextArea);
        JScrollPane scrollPaneset = new JScrollPane(displayTextArea);
        writingTextArea.setText("Enter a String");

        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 0;
        constraint.weightx = 1;
        constraint.weighty = 8;
        gbLayout.setConstraints(scrollPaneget, constraint);
        this.add(scrollPaneget);
        gbLayout.setConstraints(scrollPaneset, constraint);
        this.add(scrollPaneset);
        constraint.weighty = 1;
        gbLayout.setConstraints(sendButton, constraint);
        this.add(sendButton);
        gbLayout.setConstraints(errorMessageField, constraint);
        this.add(errorMessageField);

        sendButton.addActionListener(this);

    }

    public void update(String suggestion) {
        displayTextArea.setText(suggestion);
    }

    //updates the error message to the error message display area
    public void updateError(String message) {
        errorMessageField.setText(message);
    }

    public void actionPerformed(ActionEvent e) {
        String str = writingTextArea.getText().trim();
        formModel.doSyncSpellingSuggestion(str);
    }

    public void clear() {
        writingTextArea.setText("");
        displayTextArea.setText("");
        errorMessageField.setText("");
    }

}


