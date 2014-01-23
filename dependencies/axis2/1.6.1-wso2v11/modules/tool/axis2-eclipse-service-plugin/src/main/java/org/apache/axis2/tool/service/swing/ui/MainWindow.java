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

import org.apache.axis2.tool.service.bean.WizardBean;
import org.apache.axis2.tool.service.control.Controller;
import org.apache.axis2.tool.service.control.ProcessException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {

    private JPanel wizardPaneContainer;
    private JButton nextButton;
    private JButton previousButton;
    private JButton cancelButton;
    private JButton finishButton;
    private int currentPage;
    private WizardPane currentWizardPane;

    private static final int PAGE_1 = 1;
    private static final int PAGE_2 = 2;
    private static final int PAGE_3 = 3;
    //private static final int PAGE_4=4;


    private WizardBean wizardBean = new WizardBean();

    public MainWindow() throws HeadlessException {
        super("Axis 2 - Service Jar Builder");
        init();

    }

    private void init() {
        this.getContentPane().setLayout(null);

        this.setBounds(
                (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() /
                2 -
                400 / 2,
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() /
                2 -
                360 / 2,
                400, 360);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        int hgap = 5;
        int vgap = 5;
        int bWidth = 80;
        int bHeight = 20;

        this.wizardPaneContainer = new JPanel(null);
        this.getContentPane().add(this.wizardPaneContainer);
        this.wizardPaneContainer.setBounds(0, 0, 400, 300);

        this.previousButton = new JButton("Previous");
        this.getContentPane().add(this.previousButton);
        this.previousButton.setBounds(hgap, 300 + vgap, bWidth, bHeight);
        this.previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveBackWard();
            }

        });

        this.nextButton = new JButton("Next");
        this.getContentPane().add(this.nextButton);
        this.nextButton.setBounds(hgap + bWidth + hgap,
                300 + vgap,
                bWidth,
                bHeight);
        this.nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveForward();
            }
        });

        this.cancelButton = new JButton("Close");
        this.getContentPane().add(this.cancelButton);
        this.cancelButton.setBounds(hgap + (bWidth + hgap) * 2,
                300 + vgap,
                bWidth,
                bHeight);
        this.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (confirmExit()){
                    System.exit(0);
                }
            }
        });

        this.finishButton = new JButton("Finish");
        this.getContentPane().add(this.finishButton);
        this.finishButton.setBounds(hgap + (bWidth + hgap) * 3,
                300 + vgap,
                bWidth,
                bHeight);
        this.finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processFinish();
            }
        });


        this.currentPage = PAGE_1;
        moveToPage(currentPage); //add the first page as default
    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "Required Value Not set!!!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean confirmExit() {
        int returnType = JOptionPane.showOptionDialog(this,
                "Are you sure you want to exit?",
                "Exit service builder",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, null, null);
        return (returnType == JOptionPane.YES_OPTION);
    }

    private void moveForward() {
        if (currentWizardPane.validateValues()) {
            this.currentPage++;
            moveToPage(this.currentPage);
        } else {
            showErrorMessage();
        }
    }

    private void moveBackWard() {
        this.currentPage--;
        moveToPage(this.currentPage);

    }

    private void moveToPage(int page) {
        switch (page) {
            case PAGE_1:
                processPage(new WizardPane1(this.wizardBean, this),
                        false,
                        true,
                        false);
                break;
            case PAGE_2:
                processPage(new WizardPane2(this.wizardBean, this),
                        true,
                        true,
                        false);
                break;
            case PAGE_3:
                processPage(new WizardPane3(this.wizardBean, this),
                        true,
                        false,
                        true);
                break;
            default:
                return;
        }
    }

    private void processFinish() {
        if (currentWizardPane.validateValues()) {
            try {
                new Controller().process(wizardBean);
                showSuccessMessage(" jar file creation successful! ");
            } catch (ProcessException e) {
                showErrorMessage(e.getMessage());
            } catch (Exception e) {
                showErrorMessage("Unknown Error! " + e.getMessage());
            }
        } else {
            showErrorMessage();
        }
    }

    private void processPage(WizardPane pane,
                             boolean prevButtonState,
                             boolean nextButtonState,
                             boolean finishButtonState) {
        this.wizardPaneContainer.removeAll();
        currentWizardPane = pane;
        this.wizardPaneContainer.add(pane);
        this.previousButton.setEnabled(prevButtonState);
        this.nextButton.setEnabled(nextButtonState);
        this.finishButton.setEnabled(finishButtonState);
        this.repaint();
    }


    public static void main(String[] args) {
        new MainWindow().show();
    }


}
