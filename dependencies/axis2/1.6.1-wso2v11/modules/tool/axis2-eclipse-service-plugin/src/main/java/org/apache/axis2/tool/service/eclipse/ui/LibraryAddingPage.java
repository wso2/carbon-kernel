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

import org.apache.axis2.tool.service.bean.LibrarySelectionBean;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class LibraryAddingPage extends AbstractServiceWizardPage {
    
    private Text libraryNameText;
    private Button browseButton;
    private Button addButton;
    private Button removeButton;
    private List jarFileList;
    private Label jarFilecountLabel;
    
    /**
     * 
     */
    public LibraryAddingPage() {
        super("page6");
       
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
       settings.put(PREF_LIB_LIBNAMES,new String[]{});//put an empty array

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns=3;
        container.setLayout(layout);
     
		
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan =3;
        libraryNameText = new Text(container,SWT.BORDER);
        libraryNameText.setLayoutData(gd);
       
        libraryNameText.addModifyListener(new ModifyListener(){
		    public void modifyText(ModifyEvent e){
		    //handleModify();
		    }
		});
		
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
		browseButton = new Button(container,SWT.PUSH);
		browseButton.setLayoutData(gd);
		browseButton.setText(ServiceArchiver.getResourceString("general.browse"));
		browseButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleBrowse();
		    }
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addButton = new Button(container,SWT.PUSH);
		addButton.setLayoutData(gd);
		addButton.setText(ServiceArchiver.getResourceString("page6.add"));
		addButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleAdd();
		    }

           
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeButton = new Button(container,SWT.PUSH);
		removeButton.setLayoutData(gd);
		removeButton.setText(ServiceArchiver.getResourceString("page6.remove"));
		removeButton.addMouseListener(new MouseAdapter(){
		    public void mouseUp(MouseEvent e) {
		        handleRemove();
		    }

           
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		Label dummyLabel = new Label(container,SWT.NONE);
		dummyLabel.setText(ServiceArchiver.getResourceString("page6.liblist.caption"));
		dummyLabel.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 5;
		
		jarFileList = new List(container,SWT.BORDER|SWT.V_SCROLL);
		jarFileList.setItems(settings.getArray(PREF_LIB_LIBNAMES));
		jarFileList.setLayoutData(gd);
		
		
		// Label for the count
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		jarFilecountLabel = new Label(container,SWT.NONE);
		jarFilecountLabel.setLayoutData(gd);
		
		setControl(container);
		setPageComplete(true);
    }

    private void handleBrowse(){
        FileDialog fileDialog = new FileDialog(this.getShell());
        fileDialog.setFilterExtensions(new String[]{"*.jar"});
        String returnFileName = fileDialog.open() ;
        if (returnFileName!=null){
            this.libraryNameText.setText(returnFileName);
        }
    }
    
    private void handleAdd() {
        String libName = libraryNameText.getText().trim();
        if (!libName.equals("")){
            //add the libs to the list
            jarFileList.add(libName);
            updateList();
            libraryNameText.setText("");
        }
    }
    
    private void handleRemove() {
        int selectedIndex = jarFileList.getSelectionIndex();
        //-1 is returned when nothing is selected
        if (selectedIndex!=-1){
           jarFileList.remove(selectedIndex);
           updateList();
        }
    }
    
    private void updateList(){
        jarFilecountLabel.setText(jarFileList.getItemCount() + " " + ServiceArchiver.getResourceString("page6.liblist.count.caption"));
        settings.put(PREF_LIB_LIBNAMES,jarFileList.getItems());
    }
    
    public LibrarySelectionBean getBean(){
        LibrarySelectionBean bean = new LibrarySelectionBean();
        bean.setFileList(jarFileList.getItems());
        return bean;
    }
    
	protected boolean getWizardComplete() {
		return false;
	}
    
}
