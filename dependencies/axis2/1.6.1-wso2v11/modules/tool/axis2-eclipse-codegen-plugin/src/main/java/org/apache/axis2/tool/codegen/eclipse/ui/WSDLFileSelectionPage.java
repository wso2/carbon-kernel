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

package org.apache.axis2.tool.codegen.eclipse.ui;

import org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The first page of the code generator wizrad. Asks for the WSDL file Name
 */

public class WSDLFileSelectionPage extends AbstractWizardPage {

    private Text fileText; 

   
    /**
     * 
     * @param pageName
     */
    public WSDLFileSelectionPage() {
        super("page1");
       

    }

    /**
     * Creates a default value for the settings on this page. For
     * WSDLFileSelection, this is not very much.
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_WSDL_LOCATION, "");
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        //layout.verticalSpacing = 9;
     
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
       
        gd = new GridData(GridData.FILL_HORIZONTAL);
        Label labelFile = new Label(container, SWT.NULL);
        labelFile.setText(CodegenWizardPlugin
                .getResourceString("page1.fileselection.label"));
        
        fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        fileText.setLayoutData(gd);
	    fileText.setText(settings.get(PREF_WSDL_LOCATION));
        fileText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_WSDL_LOCATION, fileText.getText());
                dialogChanged();
            }
        });

        Button button = new Button(container, SWT.PUSH);
        button.setText(CodegenWizardPlugin
                .getResourceString("page1.fileselection.browse"));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan=3;
        Label fillLabel = new Label(container, SWT.NULL);
        fillLabel.setLayoutData(gd);
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan=3;
        Label hintLabel = new Label(container, SWT.NULL);
        hintLabel.setText(CodegenWizardPlugin.getResourceString("page1.hint.desc"));
        hintLabel.setLayoutData(gd);
//        hintLabel.setFont(new Font(new Device() {
//			public int internal_new_GC(GCData data) {return 0;}
//			public void internal_dispose_GC(int handle, GCData data) {}
//											},"hintFont",6,SWT.NORMAL));
        
        setPageComplete(false);
        setControl(container);

        /*
         * Validate this dialog, because we could have got valid values from the
         * settings already.
         */
        if (restoredFromPreviousSettings){
            dialogChanged();
        }
    }

    /**
     * Handle the dialog change event. Basically evaluates the file name and
     * sets the error message accordingly
     * 
     * TODO - we might need to call this in a different event!!!
     */
    private void dialogChanged() {
        String fileName = getFileName();

        if (fileName.length() == 0) {
            updateStatus(CodegenWizardPlugin
                    .getResourceString("page1.error.filemissingerror"));
            return;
        }
        
        //try populate the options
        getCodegenWizard().populateOptions();
        // update the status
        updateStatus(null);

    }

    /**
     * Pops up the file browse dialog box
     *  
     */
    private void handleBrowse() {
	    fileText.setText("enter a valid *.wsdl/*.xml service description file");
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[] { "*.wsdl" ,"*.xml"});
        String fileName = fileDialog.open();
        if (fileName != null) {
            fileText.setText(fileName);
        }
    }

    
    /**
     * Get the file name
     * 
     * @return
     */
    public String getFileName() {
        return fileText.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.tool.codegen.eclipse.ui.CodegenPage#getPageType()
     */
    public int getPageType() {
        return WSDL_2_JAVA_TYPE;
    }
    
    
}
