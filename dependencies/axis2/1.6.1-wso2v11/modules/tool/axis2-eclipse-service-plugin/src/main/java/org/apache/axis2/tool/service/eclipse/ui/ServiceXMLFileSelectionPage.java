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

package org.apache.axis2.tool.service.eclipse.ui;

import java.io.File;

import org.apache.axis2.tool.service.bean.Page2Bean;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.apache.axis2.tool.util.ServicePluginUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ServiceXMLFileSelectionPage extends AbstractServiceWizardPage {
   
    private static final String SERVICES_XML_NAME = "*.xml";
    private Text serviceXMLText;
    private Label manualSelectionLabel;
    private Label recommendationTextLable;
    private Button browseButton;
    private Button selectAutoFileGenerationCheckBox;
    
    
    private boolean skipNextPage=true;
    
    public ServiceXMLFileSelectionPage(){
        super("page2");
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
       settings.put(PREF_SERVICE_XML_FILE,"");
       settings.put(PREF_CHECK_AUTO_GEN_SERVICE_XML,false);

    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns=3;
        container.setLayout(layout);
               
        manualSelectionLabel = new Label(container,SWT.NULL);
        manualSelectionLabel.setText(ServiceArchiver.getResourceString("page2.selectservicexml.caption"));
		
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		serviceXMLText = new Text(container,SWT.BORDER);
		serviceXMLText.setLayoutData(gd);
		serviceXMLText.setText(settings.get(PREF_SERVICE_XML_FILE));
		serviceXMLText.addModifyListener(new ModifyListener(){
		    public void modifyText(ModifyEvent e){
		    handleModify();
		    }
		});
		
		browseButton = new Button(container,SWT.PUSH);
		browseButton.setText(ServiceArchiver.getResourceString("general.browse"));
		browseButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleBrowse();
		    }
		});
		
		gd = new GridData();
		gd.horizontalSpan = 2;
		selectAutoFileGenerationCheckBox = new Button(container,SWT.CHECK);
		selectAutoFileGenerationCheckBox.setLayoutData(gd);
		selectAutoFileGenerationCheckBox.setText(ServiceArchiver.getResourceString("page2.generateauto.caption"));
		selectAutoFileGenerationCheckBox.setSelection(settings.getBoolean(PREF_CHECK_AUTO_GEN_SERVICE_XML));
		selectAutoFileGenerationCheckBox.addSelectionListener(new SelectionListener(){
		    public void widgetSelected(SelectionEvent e){
		        handleSelection();
		    }
		    public void widgetDefaultSelected(SelectionEvent e){}
		});
		/////////////////////////////////////////
		//enable the selection combo for now
		//selectAutoFileGenerationCheckBox.setEnabled(false);
		selectAutoFileGenerationCheckBox.setToolTipText(ServiceArchiver.getResourceString("page2.autogen.tooltip"));
		////////////////////////////////////////////
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.verticalSpan  =2;
		recommendationTextLable = new Label(container,SWT.NULL);
		recommendationTextLable.setLayoutData(gd);
		//recommendationTextLable.setForeground()));
		
		setControl(container);
		
		if (restoredFromPreviousSettings){
		    handleModify();
		    handleSelection();
		}else{
		    setPageComplete(false);
		    updateGenerationPage(false);
		}
		
		
    }
    
    private void handleBrowse(){
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[]{SERVICES_XML_NAME});
        String returnFileName = fileDialog.open() ;
        if (returnFileName!=null){
            this.serviceXMLText.setText(returnFileName);
        }
    }
    
    private void handleSelection(){
        boolean selection = this.selectAutoFileGenerationCheckBox.getSelection();
        settings.put(PREF_CHECK_AUTO_GEN_SERVICE_XML,selection);
        if (selection){
            changeManualSelectionStatus(false); 
            this.skipNextPage = false;
            updateStatus(null);
            updateGenerationPage(false);
        }else{
            changeManualSelectionStatus(true);
            this.skipNextPage = true;
            handleModify();
            updateGenerationPage(true);
        }
    }
    
    private void updateGenerationPage(boolean status){
        ServiceArchiveWizard wizard = (ServiceArchiveWizard)this.getWizard();
        wizard.updateServiceXMLGeneration(status);
       
    }
    
    private void changeManualSelectionStatus(boolean state){
        this.serviceXMLText.setEnabled(state);
        this.browseButton.setEnabled(state);
        this.manualSelectionLabel.setEnabled(state);
    }
    
    private void handleModify(){
        String serviceXMLString =serviceXMLText.getText().trim(); 
        settings.put(PREF_SERVICE_XML_FILE,serviceXMLString);
        if (serviceXMLString.equals("")){
           this.updateStatus(ServiceArchiver.getResourceString("page2.error.servicenameempty"));
        }else if (!(new File(serviceXMLString)).exists()){
        	this.updateStatus(ServiceArchiver.getResourceString("page2.error.servicenotexist"));
        }else if(!ServicePluginUtils.isServicesXMLValid(serviceXMLString)){
            this.updateStatus(ServiceArchiver.getResourceString("page2.error.serviceselectedinvalid"));  
        }else{
            this.updateStatus(null);
        }
    }
    
 
    public void updateRecommendation(String message){
		if (recommendationTextLable != null) {
			recommendationTextLable.setText(message);
		}
    }
    
   
    
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#isSkipNext()
     */
    public boolean isSkipNext() {
       return this.skipNextPage;
    }
    public Page2Bean getBean(){
        Page2Bean pageBean = new Page2Bean();
        pageBean.setManual(!this.selectAutoFileGenerationCheckBox.getSelection());
        pageBean.setManualFileName(this.serviceXMLText.getText());
        return pageBean;
    }
    
	protected boolean getWizardComplete() {
		return false;
	}
}
