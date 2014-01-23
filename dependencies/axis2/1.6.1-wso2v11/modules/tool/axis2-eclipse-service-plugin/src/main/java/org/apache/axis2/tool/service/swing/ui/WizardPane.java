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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public abstract class WizardPane extends JPanel {
    protected JTextArea descriptionLabel;
    protected JFrame ownerFrame;

    protected int descWidth = 400;
    protected int descHeight = 100;
    protected int width = 400;
    protected int height = 300;
    protected int hgap = 5;
    protected int vgap = 5;

    protected WizardPane() {
    }

    protected WizardPane(JFrame ownerFrame) {
        this.ownerFrame = ownerFrame;
    }

    protected WizardPane(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    protected WizardPane(LayoutManager layout) {
        super(layout);
    }

    protected WizardPane(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    protected void initDescription(String desc) {
        this.descriptionLabel = new JTextArea(desc);
        this.descriptionLabel.setOpaque(false);
        this.descriptionLabel.setEditable(false);
        this.descriptionLabel.setAutoscrolls(true);
        this.descriptionLabel.setBounds(0, 0, descWidth, descHeight);
        this.add(this.descriptionLabel);
    }

    public abstract boolean validateValues();

    protected void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }


    protected String browseForAFile(final String extension) {
        String str = "";
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.getName().endsWith(extension) || f.isDirectory()){
                    return true;
                }else{
                    return false;
                }
            }

            public String getDescription() {
                return extension + " file filter ";
            }
        });

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            str = fc.getSelectedFile().getAbsolutePath().trim();
        }
        return str;
    }

    protected String browseForAFolder() {
        String str = "";
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            str = fc.getSelectedFile().getAbsolutePath().trim();
        }
        return str;
    }
}
