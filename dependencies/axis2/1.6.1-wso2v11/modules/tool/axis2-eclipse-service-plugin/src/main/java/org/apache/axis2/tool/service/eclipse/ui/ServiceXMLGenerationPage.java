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

import org.apache.axis2.tool.service.bean.Page2Bean;
import org.apache.axis2.tool.service.bean.WSDLAutoGenerateOptionBean;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;


public class ServiceXMLGenerationPage extends AbstractServiceWizardPage{
    
    private Text classNameTextBox;
    private Text serviceNameTextBox;
    private Button searchDeclaredMethodsCheckBox;
    Button loadButton;
    private Table table;
    
    private boolean dirty = false;
    
    public ServiceXMLGenerationPage(){
        super("page3");
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
      settings.put(PREF_SERVICE_GEN_SERVICE_NAME,"MyService");
      settings.put(PREF_SERVICE_GEN_CLASSNAME,"");
      settings.put(PREF_SERVICE_GEN_LOAD_ALL,false);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns=3;
        container.setLayout(layout);
        
        Label label = new Label(container,SWT.NULL);
        label.setText(ServiceArchiver.getResourceString("page3.servicename.lable"));
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        serviceNameTextBox = new Text(container,SWT.BORDER);
        serviceNameTextBox.setLayoutData(gd);
        serviceNameTextBox.setToolTipText(ServiceArchiver.getResourceString("page3.servicename.tooltip"));
        serviceNameTextBox.setText(settings.get(PREF_SERVICE_GEN_SERVICE_NAME));
        serviceNameTextBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
                updateDirtyStatus(true);
                settings.put(PREF_SERVICE_GEN_SERVICE_NAME,serviceNameTextBox.getText());
                //validate
            }
        });
       
        
        label = new Label(container,SWT.NULL);
        label.setText(ServiceArchiver.getResourceString("page3.classname.lable"));
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        classNameTextBox = new Text(container,SWT.BORDER);
        classNameTextBox.setLayoutData(gd);
        classNameTextBox.setToolTipText(ServiceArchiver.getResourceString("page3.classname.tooltip"));
        classNameTextBox.setText(settings.get(PREF_SERVICE_GEN_CLASSNAME));
        classNameTextBox.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e){
                updateDirtyStatus(true);
                settings.put(PREF_SERVICE_GEN_CLASSNAME,classNameTextBox.getText());
            }
        });
       
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        loadButton = new Button(container,SWT.PUSH);
        loadButton.setText(ServiceArchiver.getResourceString("page3.loadbutton.lable"));
        loadButton.setLayoutData(gd);
        loadButton.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent e){
                updateTable();
            }
            public void widgetDefaultSelected(SelectionEvent e){}
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        
        searchDeclaredMethodsCheckBox = new Button(container,SWT.CHECK);
        searchDeclaredMethodsCheckBox.setLayoutData(gd);
        searchDeclaredMethodsCheckBox.setText(ServiceArchiver.getResourceString("page3.declared.lable"));
        searchDeclaredMethodsCheckBox.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent e){
                updateDirtyStatus(true);//dirty
                settings.put(PREF_SERVICE_GEN_LOAD_ALL,searchDeclaredMethodsCheckBox.getSelection());
                if(table.isVisible()){
                    updateTable();
                }
                
            }
            public void widgetDefaultSelected(SelectionEvent e){} 
        });
        searchDeclaredMethodsCheckBox.setSelection(settings.getBoolean(PREF_SERVICE_GEN_LOAD_ALL));
        
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.verticalSpan = 5;
        
        table = new Table(container,SWT.SINGLE|SWT.FULL_SELECTION|SWT.CHECK);
        table.setLinesVisible(true);
        table.setHeaderVisible(true); 
        table.setLayoutData(gd);
        declareColumn(table,20,"");
        declareColumn(table,100,ServiceArchiver.getResourceString("page3.table.col1"));
        declareColumn(table,100,ServiceArchiver.getResourceString("page3.table.col2"));
        declareColumn(table,100,ServiceArchiver.getResourceString("page3.table.col3"));
        
        table.setVisible(false);
		
        if (restoredFromPreviousSettings){
            //try running the update
            updateTable();
        }else{
            setPageComplete(false);  
        }
        
        
		setControl(container);

    }
     
    /**
     * if the user has already filled the data, this page needs to be 
     * unchangeable
     *
     */
    public void fillDatafromPrevious(WSDLAutoGenerateOptionBean bean) {
        //set class name
        String automaticClassName = bean.getClassFileName();
        this.classNameTextBox.setText(automaticClassName);
        this.classNameTextBox.setEnabled(false);
        //set service name
        String[] classnameParts = automaticClassName.split("\\.");
        if (classnameParts.length == 0) {
            this.serviceNameTextBox.setText(automaticClassName);
        } else {
            this.serviceNameTextBox
                    .setText(classnameParts[classnameParts.length - 1]);
        }
        //load the classes
        updateTable();
        //check the correct ones

        //disbale the table
        table.setEnabled(false);
        loadButton.setEnabled(false);
    }
    
    public void clearPreviousData(){
        this.classNameTextBox.setText("");
        this.classNameTextBox.setEnabled(true);
        //set service name
        this.serviceNameTextBox.setText("");
        if (table.isVisible()){
            table.setVisible(false);
        }
        loadButton.setEnabled(true);
    }
    private void updateDirtyStatus(boolean status){
        dirty = status;
        if (table.isVisible()){
            table.setEnabled(!status);
        }
        setPageComplete(!status);
    }
    
    private void declareColumn(Table table, int width,String colName){
        TableColumn column = new TableColumn(table,SWT.NONE);
        column.setWidth(width);
        column.setText(colName);
    }
    
    private void updateTable() {
        //get a URL from the class file location
        try {
            String classFileLocation = getClassFileLocation();
            URL classFileURL = new File(classFileLocation).toURL();
            
           ArrayList listofURLs = new ArrayList();
           listofURLs.add(classFileURL);
           
            //get the libraries from the lib page and load it 
            String[] libFileList = ((ServiceArchiveWizard)this.getWizard()).getLibFileList();
            if (libFileList!=null){
            	int count = libFileList.length;
				for (int i=0;i<count;i++){
					listofURLs.add(new File(libFileList[i]).toURL());
            	}
            }
           
            
            ClassLoader loader = new URLClassLoader((URL[])listofURLs.toArray(new URL[listofURLs.size()]));
            Class clazz = Class.forName(classNameTextBox.getText(),true,loader);
            Method[] methods = null;
            
            
            if (searchDeclaredMethodsCheckBox.getSelection()){
                methods = clazz.getDeclaredMethods();
            }else{
                methods = clazz.getMethods();
            }

            int methodCount = methods.length;
            if (methodCount > 0) {
                table.removeAll();
                TableItem[] items = new TableItem[methodCount]; // An item for each field
                for (int i = 0 ; i < methodCount; i++){
                   items[i] = new TableItem(table, SWT.NONE);
                   items[i].setText(1,methods[i].getName());
                   items[i].setText(2,methods[i].getReturnType().getName());
                   items[i].setText(3,methods[i].getParameterTypes().length+"");
                   items[i].setChecked(true);//check them all by default
                }
                table.setVisible(true);
                
                //update the dirty variable
               updateDirtyStatus(false);
               updateStatus(null);
            }

        } catch (MalformedURLException e) {
           updateStatus(ServiceArchiver.getResourceString("page3.error.url") +e.getMessage());
        } catch (ClassNotFoundException e) {
           updateStatus(ServiceArchiver.getResourceString("page3.error.class")+ e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            updateStatus(ServiceArchiver.getResourceString("page3.error.unknown") + e.getMessage()); 
        }
    }
    
    public boolean isDirty(){
        return dirty;
    }
    private String getClassFileLocation(){
        ServiceArchiveWizard wizard = (ServiceArchiveWizard)getWizard();
        return wizard.getClassFileLocation();
    }
    
    public Page2Bean getBean(Page2Bean previousBean){
        if (!previousBean.isManual()){
	        previousBean.setAutomatic(true);
	        previousBean.setAutomaticClassName(classNameTextBox.getText());
	        ArrayList list = new ArrayList();
	        TableItem[] items = table.getItems();
	        int itemLength = items.length;
	        for(int i=0;i<itemLength;i++){
	           if(items[i].getChecked()){
	               list.add(items[i].getText(1));//get the selected method name only
	           }
	        }
	        previousBean.setSelectedMethodNames(list);
	        previousBean.setServiceName(this.serviceNameTextBox.getText());
        }
        return previousBean;
    }
    
	protected boolean getWizardComplete() {
		return false;
	}
}
