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
import org.eclipse.swt.widgets.Combo;
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

public class WSDLOptionsPage extends AbstractServiceWizardPage {

    private static final String SERVICE_WSDL_DEFAULT_NAME = "service.wsdl";
    private Text classNameTextBox;
    private Text outputFileNameTextBox;
    private Combo styleSelectionCombo;
    private Button searchDeclaredMethodsCheckBox;
    private Table table;
    
//    private boolean dirty = false;
    
    public WSDLOptionsPage(){
        super("page6");
    }
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
       settings.put(PREF_WSDL_FILE_NAME,SERVICE_WSDL_DEFAULT_NAME);
       settings.put(PREF_WSDL_CLASS_NAME,"");
       settings.put(PREF_WSDL_STYLE_INDEX,0);

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;

        // #########################################################

        Label label = new Label(container, SWT.NULL);
        label
                .setText(ServiceArchiver
                        .getResourceString("page6.fileName.label"));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        outputFileNameTextBox = new Text(container, SWT.BORDER);
        outputFileNameTextBox.setLayoutData(gd);
        outputFileNameTextBox.setText(settings.get(PREF_WSDL_FILE_NAME));
        //###########################################################
        outputFileNameTextBox.setEnabled(false);//this text box is disbaled for
                                                // now
        //########################################################
        outputFileNameTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handlFileNameModification();
            }
        });

        //########################################################
        gd = new GridData(GridData.FILL_HORIZONTAL);
        label = new Label(container, SWT.NULL);
        label.setText(ServiceArchiver.getResourceString("page6.class.label"));

        classNameTextBox = new Text(container, SWT.BORDER);
        classNameTextBox.setLayoutData(gd);
        classNameTextBox.setText(settings.get(PREF_WSDL_CLASS_NAME));
        classNameTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleClassNameModification();
            }
        });

        gd = new GridData(GridData.FILL_HORIZONTAL);
        Button loadButton = new Button(container, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setLayoutData(gd);
        loadButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                updateTable();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        
        searchDeclaredMethodsCheckBox = new Button(container,SWT.CHECK);
        searchDeclaredMethodsCheckBox.setLayoutData(gd);
        searchDeclaredMethodsCheckBox.setText("List Declared Methods Only");
        searchDeclaredMethodsCheckBox.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent e){
                updateDirtyStatus(true);//dirty
            }
            public void widgetDefaultSelected(SelectionEvent e){} 
        });
        
        // #####################################################
        label = new Label(container, SWT.NULL);
        label.setText(ServiceArchiver.getResourceString("page6.style.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        styleSelectionCombo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER
                | SWT.READ_ONLY);
        styleSelectionCombo.setLayoutData(gd);
        populateStyleCombo();
        styleSelectionCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                settings.put(PREF_WSDL_STYLE_INDEX, styleSelectionCombo
                        .getSelectionIndex());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 3;
        gd.verticalSpan = 5;
        
        table = new Table(container,SWT.SINGLE|SWT.FULL_SELECTION|SWT.CHECK);
        table.setLinesVisible(true);
        table.setHeaderVisible(true); 
        table.setLayoutData(gd);
        declareColumn(table,20,"");
        declareColumn(table,100,"Method Name");
        declareColumn(table,100,"Return Type");
        declareColumn(table,100,"Parameter Count");
        
        table.setVisible(false);
        
        setControl(container);

    }
    
    private void declareColumn(Table table, int width,String colName){
        TableColumn column = new TableColumn(table,SWT.NONE);
        column.setWidth(width);
        column.setText(colName);
    }
    
    
    private void populateStyleCombo() {
        styleSelectionCombo.add("Document");
        styleSelectionCombo.add("rpc");
        styleSelectionCombo.add("Wrapped");

        styleSelectionCombo.select(settings.getInt(PREF_WSDL_STYLE_INDEX));
        
    }
    
    private void updateTable() {
        //get a URL from the class file location
        try {
            String classFileLocation = getClassFileLocation();
            URL classFileURL = new File(classFileLocation).toURL();
            ClassLoader loader = new URLClassLoader(new URL[] { classFileURL });

            Class clazz = loader.loadClass(classNameTextBox.getText());
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
           updateStatus("Error : invalid location " +e.getMessage());
        } catch (ClassNotFoundException e) {
           updateStatus("Error : Class not found " + e.getMessage());
        }
    }
    private void handleClassNameModification(){
        String className = classNameTextBox.getText();
        settings.put(PREF_WSDL_CLASS_NAME, className);
        
        if (className==null || "".equals(className.trim())){
            updateStatus(ServiceArchiver.getResourceString("page6.error.classname1"));
        }else if (className.endsWith(".")){
            updateStatus(ServiceArchiver.getResourceString("page6.error.classname2"));
        }else{
            updateStatus(null);
        }
        
    }
    
    private void handlFileNameModification(){
        String wsdlFileName = outputFileNameTextBox.getText();
        settings.put(PREF_WSDL_FILE_NAME, wsdlFileName);
        
        if (wsdlFileName==null || "".equals(wsdlFileName.trim())){
            updateStatus(ServiceArchiver.getResourceString("page6.error.fileName1"));
        }else if (wsdlFileName.endsWith(".wsdl")){
            updateStatus(ServiceArchiver.getResourceString("page6.error.fileName2"));
        }else{
            updateStatus(null);
        }
        
    }
    private String getClassFileLocation(){
        ServiceArchiveWizard wizard = (ServiceArchiveWizard)getWizard();
        return wizard.getClassFileLocation();
    }
    
    public WSDLAutoGenerateOptionBean getBean(){
        WSDLAutoGenerateOptionBean optionBean = new WSDLAutoGenerateOptionBean();
        optionBean.setClassFileName(classNameTextBox.getText());
        optionBean.setOutputFileName(outputFileNameTextBox.getText());
        optionBean.setStyle(styleSelectionCombo.getItem(styleSelectionCombo.getSelectionIndex()));
        return optionBean;
    }
    
    private void updateDirtyStatus(boolean status){
//        dirty = status;
        if (table.isVisible()){
            table.setEnabled(!status);
        }
        setPageComplete(!status);
    }
    
	protected boolean getWizardComplete() {
		return false;
	}
}
