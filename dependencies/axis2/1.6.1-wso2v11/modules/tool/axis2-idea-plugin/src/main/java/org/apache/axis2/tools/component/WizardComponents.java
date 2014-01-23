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
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * this interface extends from wizard interface
 */

public interface WizardComponents  extends Wizard {

    public void addWizardPanel(WizardPanel panel);

    public void addWizardPanel(int index, WizardPanel panel);

    public void addWizardPanelAfter(
            WizardPanel panelToBePlacedAfter,
            WizardPanel panel);

    public void addWizardPanelBefore(
            WizardPanel panelToBePlacedBefore,
            WizardPanel panel);

    public void addWizardPanelAfterCurrent(WizardPanel panel);

    public WizardPanel removeWizardPanel(WizardPanel panel);

    public WizardPanel removeWizardPanel(int index);

    public WizardPanel removeWizardPanelAfter(WizardPanel panel);

    public WizardPanel removeWizardPanelBefore(WizardPanel panel);

    public WizardPanel getWizardPanel(int index);

    public int getIndexOfPanel(WizardPanel panel);

    public void updateComponents();

    public WizardPanel getCurrentPanel() throws Exception;

    public FinishAction getFinishAction();

    public void setFinishAction(FinishAction aFinishAction);

    public CancelAction getCancelAction();

    public void setCancelAction(CancelAction aCancelAction);

    public int getCurrentIndex();

    public void setCurrentIndex(int aCurrentIndex);

    public JPanel getWizardPanelsContainer();

    public void setWizardPanelsContainer(JPanel aWizardPanelsContainer);

    public JButton getBackButton();

    public void setBackButton(JButton aBackButton);

    public JButton getNextButton();

    public void setNextButton(JButton aNextButton);

    public JButton getCancelButton();

    public void setCancelButton(JButton aCancelButton);

    public JButton getFinishButton();

    public void setFinishButton(JButton button);

    public List getWizardPanelList();

    public void setWizardPanelList(List panelList);

    public boolean onLastPanel();

    public final static String CURRENT_PANEL_PROPERTY = "currentPanel";

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

}
