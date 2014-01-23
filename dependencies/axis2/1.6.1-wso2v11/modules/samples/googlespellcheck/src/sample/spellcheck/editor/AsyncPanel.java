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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * class sample.google.spellcheck.AsyncPanel
 * This Impements its own GUI of the Asynchronous Client and it updates the string after getting the response to textarea
 */
public class AsyncPanel extends javax.swing.JPanel implements Observer,
        KeyListener {
    FormModel formModel;
    JTextArea writingTextArea;
    JTextArea displayTextArea;
    JTextField errorMessageField;

    public AsyncPanel() {
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints constraint = new GridBagConstraints();
        this.setLayout(gbLayout);

        formModel = new FormModel(this);

        writingTextArea = new JTextArea();
        writingTextArea.setLineWrap(true);

        displayTextArea = new JTextArea();
        displayTextArea.setEditable(false);
        displayTextArea.setLineWrap(true);

        errorMessageField = new JTextField();
        errorMessageField.setEditable(false);
        errorMessageField.setBackground(Color.LIGHT_GRAY);
        errorMessageField.setForeground(Color.RED);

        JScrollPane scrollPaneGet = new JScrollPane(writingTextArea);
        JScrollPane scrollPaneSet = new JScrollPane(displayTextArea);

        writingTextArea.setText("Enter a String");
        writingTextArea.addKeyListener(this);

        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 0;
        constraint.weightx = 1;
        constraint.weighty = 8;
        gbLayout.setConstraints(scrollPaneGet, constraint);
        this.add(scrollPaneGet);
        gbLayout.setConstraints(scrollPaneSet, constraint);
        this.add(scrollPaneSet);
        constraint.weighty = 1;
        gbLayout.setConstraints(errorMessageField, constraint);
        this.add(errorMessageField);


    }

    public void update(String message) {
        displayTextArea.setText(displayTextArea.getText() + " " + message);
    }

    //updates the error message to the error message display area
    public void updateError(String message) {
        errorMessageField.setText(message);
    }


    public void keyPressed(KeyEvent e) {
        int key = e.getKeyChar();
        if ((key == KeyEvent.VK_SPACE) || (key == KeyEvent.VK_ENTER)) {
            String[] words = writingTextArea.getText().split("\\s");
            if (words.length > 0)
                formModel.doAsyncSpellingSuggestion(words[words.length - 1]);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void clear() {
        displayTextArea.setText("");
        writingTextArea.setText("");
        errorMessageField.setText("");
    }
}
