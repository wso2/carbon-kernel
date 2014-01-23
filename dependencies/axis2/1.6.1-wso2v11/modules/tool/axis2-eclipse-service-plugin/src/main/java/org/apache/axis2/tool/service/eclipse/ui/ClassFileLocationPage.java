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


import org.apache.axis2.tool.service.bean.ClassFileSelectionBean;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class ClassFileLocationPage extends AbstractServiceWizardPage{

    private Text classFileLocationText;
    private Button filterByClassFilesCheckBox;
    
   
    public ClassFileLocationPage(){
        super("page1");
    }
   
    

    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
      settings.put(PREF_CLASS_FILE_LOCATION,System.getProperty("user.dir"));
      settings.put(PREF_FILTER_BY_CLASSES,true);

    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns=3;
        layout.verticalSpacing = 9;
        
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Label lable = new Label(container,SWT.NULL);
		lable.setText(ServiceArchiver.getResourceString("page1.fileLocationLabel"));
		
		classFileLocationText = new Text(container,SWT.BORDER);
		classFileLocationText.setLayoutData(gd);
		classFileLocationText.setText(settings.get(PREF_CLASS_FILE_LOCATION));
		classFileLocationText.addModifyListener(new ModifyListener(){
		    public void modifyText(ModifyEvent e){
		        handleModify();
		    }
		});
		
		Button browseButton = new Button(container,SWT.PUSH);
		browseButton.setText(ServiceArchiver.getResourceString("general.browse"));
		browseButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleBrowse();
		    } 
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		filterByClassFilesCheckBox = new Button(container,SWT.CHECK);
		filterByClassFilesCheckBox.setLayoutData(gd);
		filterByClassFilesCheckBox.setText(ServiceArchiver.getResourceString("page1.filter.caption"));
		filterByClassFilesCheckBox.setSelection(settings.getBoolean(PREF_FILTER_BY_CLASSES));
		filterByClassFilesCheckBox.addSelectionListener(new SelectionListener(){
		    public void widgetSelected(SelectionEvent e){
		        settings.put(PREF_FILTER_BY_CLASSES,filterByClassFilesCheckBox.getSelection());
		    }
		    public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		
		setControl(container);
		//call the handle modify method if the setttings are restored
		
		if (restoredFromPreviousSettings){
		    handleModify();
		}
    }
    
    
    private void handleBrowse(){
       DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
       dirDialog.setMessage(ServiceArchiver.getResourceString("page1.filedialogTitle"));
       String returnText = dirDialog.open();
       if (returnText!=null){
           this.classFileLocationText.setText(returnText);
       }
    }
    
    private void handleModify(){
        String classLocationText = this.classFileLocationText.getText().trim();
        settings.put(PREF_CLASS_FILE_LOCATION,classLocationText);
        //update the wizard
        ((ServiceArchiveWizard)this.getWizard()).setClassFileLocation(classLocationText);
        
        if ("".equals(classLocationText)){
            updateStatus(ServiceArchiver.getResourceString("page1.error.filemissing"));
        }else{    
            updateStatus(null);
        }
    }
    
    public String getClassFileLocation(){
        return classFileLocationText.getText();
    }
    
    public ClassFileSelectionBean getBean(){
        ClassFileSelectionBean pageBean = new ClassFileSelectionBean();
        pageBean.setFileLocation(this.classFileLocationText.getText());
        if (filterByClassFilesCheckBox.getSelection()){
            pageBean.setFilter(".class");
        }
        return pageBean;
    }



	protected boolean getWizardComplete() {
		return false;
	}
}
