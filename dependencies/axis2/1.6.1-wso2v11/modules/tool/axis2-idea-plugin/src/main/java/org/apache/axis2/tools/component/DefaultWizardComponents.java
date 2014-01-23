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

package org.apache.axis2.tools.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * this is used for default wizard components
 */
public class DefaultWizardComponents implements WizardComponents {

    private JButton backButton;
    private JButton nextButton;
    private JButton finishButton;
    private JButton cancelButton;

    FinishAction finishAction;
    CancelAction cancelAction;

    List panelList;
    int currentIndex;
    JPanel wizardPanelsContainer;
    PropertyChangeSupport propertyChangeListeners;

    /**
     * This class is the "bread and butter" of this framework.  All of these
     * components can be used visually however you want, as shown in the
     * frame and example packages, but all a developer really needs is this,
     * and they can even instead implement JWizard and choose to do this
     * portion any way they wish.
     */
    public DefaultWizardComponents() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWizardPanel(WizardPanel panel) {
        getWizardPanelList().add(panel);
        wizardPanelsContainer.add(panel,
                getWizardPanelList().size() - 1 + "");
    }

    public void addWizardPanel(int index, WizardPanel panel) {
        getWizardPanelList().add(index, panel);
        wizardPanelsContainer.add(panel, index + "", index);
        if (index < getWizardPanelList().size() - 1) {
            for (int i = index + 1; i < getWizardPanelList().size(); i++) {
                wizardPanelsContainer.add(
                        (WizardPanel)getWizardPanelList().get(i),
                        i + "");
            }
        }
    }

    public void addWizardPanelAfter(
            WizardPanel panelToBePlacedAfter,
            WizardPanel panel) {
        addWizardPanel(
                getWizardPanelList().indexOf(panelToBePlacedAfter) + 1,
                panel);
    }

    public void addWizardPanelBefore(
            WizardPanel panelToBePlacedBefore,
            WizardPanel panel) {
        addWizardPanel(
                getWizardPanelList().indexOf(panelToBePlacedBefore) - 1,
                panel);
    }

    public void addWizardPanelAfterCurrent(WizardPanel panel) {
        addWizardPanel(getCurrentIndex()+1, panel);
    }

    public WizardPanel removeWizardPanel(WizardPanel panel) {
        int index = getWizardPanelList().indexOf(panel);
        getWizardPanelList().remove(panel);
        wizardPanelsContainer.remove(panel);
        for (int i = index; i < getWizardPanelList().size(); i++) {
            wizardPanelsContainer.add(
                    (WizardPanel) getWizardPanelList().get(i),
                    i + "");
        }
        return panel;
    }

    public WizardPanel removeWizardPanel(int index) {
        wizardPanelsContainer.remove(index);
        WizardPanel panel = (WizardPanel) getWizardPanelList().remove(index);
        for (int i = index; i < getWizardPanelList().size(); i++) {
            wizardPanelsContainer.add(
                    (WizardPanel) getWizardPanelList().get(i),
                    i + "");
        }
        return panel;
    }

    public WizardPanel removeWizardPanelAfter(WizardPanel panel) {
        return removeWizardPanel(getWizardPanelList().indexOf(panel) + 1);
    }

    public WizardPanel removeWizardPanelBefore(WizardPanel panel) {
        return removeWizardPanel(getWizardPanelList().indexOf(panel) - 1);
    }

    public WizardPanel getWizardPanel(int index) {
        return (WizardPanel) getWizardPanelList().get(index);
    }

    public int getIndexOfPanel(WizardPanel panel) {
        return getWizardPanelList().indexOf(panel);
    }

    public boolean onLastPanel() {
        return (getCurrentIndex() == getWizardPanelList().size() - 1);
    }

    private void init() throws Exception {
        this.propertyChangeListeners = new PropertyChangeSupport(this);

        backButton = new JButton();
        nextButton = new JButton();
        finishButton = new JButton();
        cancelButton = new JButton();

        panelList = new ArrayList();
        currentIndex = 0;
        wizardPanelsContainer = new JPanel();

        backButton.setText("< Back");
        backButton.setMnemonic("B".charAt(0));
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backButton_actionPerformed(e);
            }
        });

        nextButton.setText("Next >");
        nextButton.setMnemonic("N".charAt(0));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton_actionPerformed(e);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setMnemonic("C".charAt(0));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton_actionPerformed(e);
            }
        });

        finishButton.setText("Finish");
        finishButton.setMnemonic("F".charAt(0));
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                finishButton_actionPerformed(e);
            }
        });

        wizardPanelsContainer.setLayout(new CardLayout());
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        getCancelAction().performAction();
    }

    void finishButton_actionPerformed(ActionEvent e) {
        getFinishAction().performAction();
    }

    void nextButton_actionPerformed(ActionEvent e) {
        try {
            if(getCurrentPanel().isPageComplete()){
                getCurrentPanel().next();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void backButton_actionPerformed(ActionEvent e) {
        try {
            getCurrentPanel().back();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public WizardPanel getCurrentPanel() throws Exception {
        if (getWizardPanelList().get(currentIndex) != null) {
            return (WizardPanel) getWizardPanelList().get(currentIndex);
        } else {
            throw new Exception("No panels in panelList");
        }
    }

    public void updateComponents() {
        try {
            CardLayout cl = (CardLayout) (wizardPanelsContainer.getLayout());
            cl.show(wizardPanelsContainer, currentIndex + "");

            if (currentIndex == 0) {
                backButton.setEnabled(false);
            } else {
                backButton.setEnabled(true);
            }

            if (onLastPanel()) {
                nextButton.setEnabled(false);
                finishButton.setEnabled(true);
            } else {
                finishButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
            // let panel to update itself
            getCurrentPanel().update();

            // inform PropertyChangeListeners
            PropertyChangeEvent event = new PropertyChangeEvent(this, WizardComponents.CURRENT_PANEL_PROPERTY
                    , null,  getCurrentPanel());
            propertyChangeListeners.firePropertyChange(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters and Setters from here on ...

    public List getWizardPanelList() {
        return this.panelList;
    }

    public void setWizardPanelList(ArrayList panelList) {
        this.panelList = panelList;
    }

    public FinishAction getFinishAction() {
        return finishAction;
    }

    public void setFinishAction(FinishAction aFinishAction) {
        finishAction = aFinishAction;
    }

    public CancelAction getCancelAction() {
        return cancelAction;
    }

    public void setCancelAction(CancelAction aCancelAction) {
        cancelAction = aCancelAction;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int aCurrentIndex) {
        currentIndex = aCurrentIndex;
    }

    public JPanel getWizardPanelsContainer() {
        return wizardPanelsContainer;
    }

    public void setWizardPanelsContainer(JPanel aWizardPanelsContainer) {
        wizardPanelsContainer = aWizardPanelsContainer;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void setBackButton(JButton aBackButton) {
        backButton = aBackButton;
    }

    public JButton getNextButton() {
        return nextButton;
    }

    public void setNextButton(JButton aNextButton) {
        nextButton = aNextButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton(JButton aCancelButton) {
        cancelButton = aCancelButton;
    }

    public JButton getFinishButton() {
        return finishButton;
    }

    public void setFinishButton(JButton button) {
        finishButton = button;
    }

    public void setWizardPanelList(List panelList) {
        this.panelList = panelList;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.removePropertyChangeListener(listener);
    }

  
}
