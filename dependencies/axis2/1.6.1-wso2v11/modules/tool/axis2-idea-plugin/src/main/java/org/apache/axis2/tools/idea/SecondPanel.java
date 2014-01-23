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

package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;
import org.apache.axis2.util.URLProcessor;

import javax.swing.*;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SecondPanel  extends  WizardPanel {

    private JComboBox cmbCodeGenOption;
    private JComboBox cmbLan;
    private JComboBox cmbServiceName;
    private JComboBox cmbPortName;
    private JTextField txtPacakgeName;
    private JComboBox cmbdbtype;
    private JCheckBox chkTestCase;
    private JRadioButton clientSide;
    private JRadioButton rdBoth;
    private JRadioButton rdsyn;
    private JRadioButton rdasync;
    private JRadioButton serverSide;
    private JCheckBox serviceXML;
    private JCheckBox serverSideInterface;
    private JRadioButton all;
    private JTable table;
    private JScrollPane spTable;
    private List serviceNameList;
    private PackageNameTableModel model;
    private CodegenBean codegenBean;

    public SecondPanel(WizardComponents wizardComponents,CodegenBean codegenBean) {
        super(wizardComponents, "Axis2 Idea Plugin WSDL2Java Wizards");
        this.codegenBean=codegenBean;
        setPanelTopTitle("Options");
        setPanelBottomTitle("Set the options for the code generator. If you wish to edit the codegen options, Select custom");
        init();
    }

    private void init(){

        cmbLan = new JComboBox();
        cmbLan.addItem("java");
        cmbLan.setToolTipText("Select the language of the generated code");

        cmbServiceName = new JComboBox();
        add(cmbServiceName);
        cmbServiceName.setToolTipText("Select the name of the service that the code should be generated for");

        cmbPortName = new JComboBox();
        cmbPortName.setToolTipText("Select the port name that the code should be generated for");

        txtPacakgeName = new JTextField("org.axis2");
        txtPacakgeName.setToolTipText("Set the package name of the generated code");

        cmbdbtype = new JComboBox();
        cmbdbtype.addItem("adb");
        cmbdbtype.addItem("xmlbeans");
        cmbdbtype.addItem("none");
        cmbdbtype.setToolTipText("Select the databinding framework to be used in the generation process");

        chkTestCase = new JCheckBox("Generate Test Case", false);
        chkTestCase.setVisible(true);
        chkTestCase.setToolTipText("A test case will be generated if this is checked");

        ButtonGroup generationType = new ButtonGroup();

        clientSide = new JRadioButton("Generate Client Side",true);
        generationType.add(clientSide);

        ButtonGroup buttonGroup = new ButtonGroup();

        rdBoth = new JRadioButton("Generate both sync and async", true);
        buttonGroup.add(rdBoth);

        rdsyn = new JRadioButton("Generate sync only", false);
        buttonGroup.add(rdsyn);

        rdasync = new JRadioButton("Generate async only", false);
        buttonGroup.add(rdasync);

        serverSide = new JRadioButton("Generate Server Side");
        generationType.add(serverSide);

        serviceXML = new JCheckBox("Generate default service.xml", false);

        serverSideInterface = new JCheckBox("Generate an interface for skeleton", false);

        all = new JRadioButton("Generate All");
        generationType.add(all);

        model = new PackageNameTableModel(new Object [1][2]);
        table = new JTable(model);        
        spTable=new JScrollPane(table);

        cmbCodeGenOption =new JComboBox();
        cmbCodeGenOption .addItem("default");
        cmbCodeGenOption .addItem("custom");
        cmbCodeGenOption .setToolTipText("Select the Codegen option");


        this.setLayout(new GridBagLayout());

        this.add(new JLabel("Cogen Option")
                , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));

        this.add(cmbCodeGenOption
                , new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));
        cmbCodeGenOption.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setEnabledForCodegenType();
                    update();
                }
            }
        });

        this.add(new JLabel("Out put lang")
                , new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1,10, 0,10), 0, 0));

        this.add(cmbLan
                , new GridBagConstraints(1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));

        this.add(new JLabel("Service name")
                , new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));

        this.add(cmbServiceName
                , new GridBagConstraints(1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));

        this.add(new JLabel("port Name")
                , new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));
        this.add(cmbPortName
                , new GridBagConstraints(1, 3, GridBagConstraints.REMAINDER, 1,1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));
        this.add(new JLabel("DataBinding")
                , new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));
        this.add(cmbdbtype
                , new GridBagConstraints(1, 4, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));
        this.add(new JLabel("Custom Packege")
                , new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));
        this.add(txtPacakgeName
                , new GridBagConstraints(1, 5, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));
        this.add(chkTestCase
                , new GridBagConstraints(0, 6, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 10, 0,10), 0, 0));
        this.add(new JSeparator()
                , new GridBagConstraints(0, 7, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.BOTH
                , new Insets(1, 10, 1,1), 0, 0));


        this.add(clientSide
                , new GridBagConstraints(0, 8, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));
        clientSide.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setEnabledForCustom();
                    update();
                }
            }
        });
        this.add(rdBoth
                , new GridBagConstraints(0, 9, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 10, 0,10), 0, 0));
        this.add(rdsyn
                , new GridBagConstraints(1, 9, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 5, 0,10), 0, 0));
        this.add(rdasync
                , new GridBagConstraints(2, 9, 1, 1, 1.0,0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(1, 5, 0,10), 0, 0));


        this.add(new JSeparator()
                , new GridBagConstraints(0, 10, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.BOTH
                , new Insets(1, 10, 1,1), 0, 0));

        this.add(serverSide
                , new GridBagConstraints(0, 11, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 10, 0,10), 0, 0));
        serverSide.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setEnabledForCustom();
                    update();
                }
            }
        });
        this.add(serviceXML
                , new GridBagConstraints(0, 12, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 10, 0,10), 0, 0));
        this.add(serverSideInterface
                , new GridBagConstraints(1, 12, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1, 5, 0,10), 0, 0));


        this.add(new JSeparator()
                , new GridBagConstraints(0, 13, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.BOTH
                , new Insets(1, 10, 1,1), 0, 0));


        this.add(all
                , new GridBagConstraints(0, 14, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1,10, 0,10), 0, 0));
        all.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setEnabledForCustom();
                    update();
                }
            }
        });
        this.add(new JSeparator()
                , new GridBagConstraints(0, 15, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.BOTH
                , new Insets(1, 10, 1,1), 0, 0));

        this.add(new JLabel("Namespace to Package Mapping")
                , new GridBagConstraints(0, 16, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(1,10, 0,10), 0, 0));

        this.add(spTable
                , new GridBagConstraints(0, 17, GridBagConstraints.REMAINDER, 1, 1.0, 1.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.BOTH
                , new Insets(1,10, 20,10), 0, 0));

        setNextButtonEnabled(false);
        setEnabledForCodegenType();

    }
    public void next() {
        switchPanel(CodegenFrame.PANEL_LAST_A );
    }
    public void back() {
        switchPanel(CodegenFrame.PANEL_FIRST_A );
    }

    public void update(){
        checkPageComlete();
        populateParamsFromWSDL();

    }

    private void loadNamespaces(Collection namespaceMap){

        Iterator namespaces = namespaceMap.iterator();
        Object tableData [][] = new Object[namespaceMap.size()][2];
        int i = 0;
        while(namespaces.hasNext()){

            String namespace = (String)namespaces.next();
            tableData[i][0] = namespace;
            tableData[i][1] = getPackageFromNamespace(namespace);
            i++;
        }

        model.setTableData(tableData);


    }
    private void setEnabledForCodegenType(){
        if(cmbCodeGenOption.getSelectedItem().equals("default") ){
            cmbdbtype.setEnabled(false);
            cmbLan.setEnabled(false);
            cmbPortName .setEnabled(false);
            cmbServiceName.setEnabled(false);
            txtPacakgeName.setEnabled(false);
            cmbdbtype.setEnabled(false);
            if(chkTestCase.isSelected()) {
                chkTestCase.setEnabled(false);
                chkTestCase.setSelected(false);
            } else
                chkTestCase.setEnabled(false);
            clientSide.setEnabled(false);
            rdBoth .setEnabled(false);
            rdsyn.setEnabled(false);
            rdasync.setEnabled(false);
            serverSide.setEnabled(false);
            table.setEnabled(false);
            spTable.setEnabled(false);
            serviceXML.setEnabled(false);
            serverSideInterface.setEnabled(false);
            all.setEnabled(false);
        }else{
            cmbdbtype.setEnabled(true);
            cmbLan.setEnabled(true);
            cmbPortName .setEnabled(true);
            cmbServiceName.setEnabled(true);
            txtPacakgeName.setEnabled(true);
            cmbdbtype.setEnabled(true);
             if(!clientSide.isSelected()) {
                clientSide.setEnabled(true);
                clientSide.setSelected(true);
            } else
                clientSide.setEnabled(true);
            serverSide.setEnabled(true);
            table.setEnabled(true);
            spTable.setEnabled(true);
            all.setEnabled(true);
            setEnabledForCustom();
        }
    }
    private void setEnabledForCustom(){
        if(clientSide.isSelected()){
            if(serviceXML.isSelected()||serverSideInterface.isSelected()){
                serverSide.setSelected(false);
                serverSideInterface.setSelected(false);
                serviceXML.setEnabled(false);
                serverSideInterface.setEnabled(false);
            }else{
                serviceXML.setEnabled(false);
                serverSideInterface.setEnabled(false);
            }
            if(rdasync.isSelected()){
                rdasync.setEnabled(true);
                rdasync.setSelected(false);
            }else{
                rdasync.setEnabled(true);
            }
            if(!rdBoth.isSelected()){
                rdBoth.setEnabled(true);
                rdBoth.setSelected(true);
            }else{
                rdBoth.setEnabled(true);
            }
             if(rdsyn.isSelected()){
                rdsyn.setEnabled(true);
                rdsyn.setSelected(false);
            }else{
                rdsyn.setEnabled(true);
            }
            chkTestCase.setEnabled(true);
        }else if(serverSide.isSelected()){
             if(serviceXML.isSelected()||serverSideInterface.isSelected()){
                serverSide.setSelected(false);
                serverSideInterface.setSelected(false);
                serviceXML.setEnabled(true);
                serverSideInterface.setEnabled(true);
            }else{
                serviceXML.setEnabled(true);
                serverSideInterface.setEnabled(true);
            }
            if( rdasync.isSelected()||rdBoth.isSelected()||rdsyn.isSelected()) {
                rdasync.setSelected(false);
                rdBoth.setSelected(false);
                rdsyn.setSelected(false);
                rdasync.setEnabled(false);
                rdBoth.setEnabled(false);
                rdsyn .setEnabled(false);
            } else{
                rdasync.setEnabled(false);
                rdBoth.setEnabled(false);
                rdsyn .setEnabled(false);
            }
            if(chkTestCase.isSelected()) {
                chkTestCase.setEnabled(false);
                chkTestCase.setSelected(false);
            } else
                chkTestCase.setEnabled(false);
        }else if(all.isSelected()){
            chkTestCase.setEnabled(true);
            if(serviceXML.isSelected()||serverSideInterface.isSelected()){
                serverSide.setSelected(false);
                serverSideInterface.setSelected(false);
                serviceXML.setEnabled(false);
                serverSideInterface.setEnabled(false);
            }else{
                serviceXML.setEnabled(false);
                serverSideInterface.setEnabled(false);
            }
            if( rdasync.isSelected()||rdBoth.isSelected()||rdsyn.isSelected()) {
                rdasync.setSelected(false);
                rdBoth.setSelected(false);
                rdsyn.setSelected(false);
                rdasync.setEnabled(false);
                rdBoth.setEnabled(false);
                rdsyn .setEnabled(false);
            } else{
                rdasync.setEnabled(false);
                rdBoth.setEnabled(false);
                rdsyn .setEnabled(false);
            }
        }
    }
    public String getPackageFromNamespace(String namespace){
        return  URLProcessor.makePackageName(namespace);
    }
    public  int getPageType() {
        return  WizardPanel.WSDL_2_JAVA_TYPE;
    }
    private void checkPageComlete(){
        if(cmbCodeGenOption.getSelectedItem().equals("default")){
            setPageComplete(true);
            setNextButtonEnabled(true);
        } else{
            if(txtPacakgeName.getText().equals("")){
                setPageComplete(false);
                setNextButtonEnabled(false);
            } else{
                setPageComplete(true);
                setNextButtonEnabled(true);
            }
        }
    }
    /**
     * populate the service and the port from the WSDL this needs to be public
     * since the WSDLselection page may call this
     */
    public void populateParamsFromWSDL() {
        try {
            String lname = codegenBean.getWSDLFileName();
            if (!"".equals(lname.trim())) {

                codegenBean.readWSDL();

                // enable the combo's
               // setComboBoxEnable(true);

                this.serviceNameList = codegenBean.getServiceList();
                if (!serviceNameList.isEmpty()) {
                    cmbServiceName.removeAll();
                    for (int i = 0; i < serviceNameList.size(); i++) {
                        // add the local part of the
                        cmbServiceName.addItem(((QName) serviceNameList.get(i))
                                .getLocalPart());
                    };
                    // select the first one as the default
                    cmbServiceName.setSelectedIndex(0);
                    //setComboBoxEnable(false);
                    // load the ports
                    loadPortNames();
                } else {
                    // service name list being empty means we are switching to
                    // the interface mode
                    if (cmbServiceName!=null) cmbServiceName .removeAll();
                    if (cmbPortName!=null) cmbPortName.removeAll();
                    // disable the combo's
                   // setComboBoxEnable(false);
                    //this is not an error

                }

                populatePackageName();

                //populate the namespacess
                loadNamespaces(codegenBean.getDefinitionNamespaceMap());
            }
        } catch (WSDLException e) {
            // disable the combo's
            setComboBoxEnable(false);
        }
        catch (Exception e) {
            // disable the combo's
            setComboBoxEnable(false);
        }

    }
    private void populatePackageName() {
        this.txtPacakgeName.setText(codegenBean.packageFromTargetNamespace());
    }
    private void setComboBoxEnable(boolean b) {
        if (cmbServiceName != null) {
            cmbServiceName .setEnabled(b);
            cmbPortName .setEnabled(b);
        }
    }
    private void loadPortNames() {
        int selectionIndex = cmbServiceName.getSelectedIndex();
        if (selectionIndex != -1) {
            java.util.List ports = codegenBean.getPortNameList((QName) serviceNameList
                    .get(selectionIndex));
            if (!ports.isEmpty()) {
                cmbPortName.removeAll();
                for (int i = 0; i < ports.size(); i++) {
                    // add the local part of the
                    cmbPortName.addItem(ports.get(i).toString());
                }
                cmbPortName.setSelectedIndex(0);
            } else {
                //Todo error message null
            }
        }
    }
    public void populateOptions(){
        populateParamsFromWSDL();
    }
    private void dialogChanged() {
        String fileName = getPackageName();

        if (fileName.length() == 0) {
            //Todo error message "File name should be specified"
            setError("File name should be specified",true);
            setPageComplete(false);
            return;
        }

        //try populate the options
        //   getWizardComponents().getWizardPanel(3).populateOptions();
        setPageComplete(true);
        // update the status
        //Todo error message null

    }
    public String getSelectedLanguage() {
        return cmbLan.getSelectedItem().toString();
    }
    /**
     * the async only status
     *
     * @return true if "Generate asynchronous code only" is checked
     */
    public boolean isAsyncOnlyOn() {
        return rdasync.isSelected();
    }
    /**
     * the sync only status
     *
     * @return true if "Generate synchronous code only" is checked
     */
    public boolean isSyncOnlyOn() {
        return rdsyn.isSelected();
    }
    /**
     * return the package name
     *
     * @return a string containing the package name to use for code generation
     */
    public String getPackageName() {
        if ("".equals(txtPacakgeName.getText().trim())){
            //we do not allow the packaging to be empty
            //if the user sets it to empty we set it to
            //the default
            return URLProcessor.DEFAULT_PACKAGE;
        }
        return this.txtPacakgeName.getText();
    }
    /**
     * The serverside status
     *
     * @return true if "Generate Server-Side" is checked
     */
    public boolean isServerside() {
        return this.serverSide.isSelected();
    }
    /**
     *
     * @return true if "Generate XML configuration file" is checked
     */
    public boolean isServerXML() {
        if (this.serviceXML.isEnabled())
            return this.serviceXML.isSelected();
        else
            return false;
    }
    /**
     *
     * @return true if "Generate test case" is checked
     */
    public boolean isGenerateTestCase() {
        return this.chkTestCase.isSelected();
    }
    /**
     *
     * @return null if portname is empty
     */
    public String getPortName() {
        int selectionIndex = cmbPortName.getSelectedIndex();
        if (selectionIndex != -1) {
            String text = this.cmbPortName.getSelectedItem().toString();
            if (text == null || text.trim().equals("")) {
                return null;
            }
            return text;
        } else {
            return null;
        }
    }
    /**
     * @return null if the text is empty
     *
     */
    public String getServiceName() {
        int selectionIndex = cmbServiceName.getSelectedIndex();
        // cater for the scenario where the combo's can be empty
        if (selectionIndex != -1) {
            String text = this.cmbServiceName.getSelectedItem().toString();

            if (text == null || text.trim().equals("")) {
                return null;
            }
            return text;
        } else {
            return null;
        }
    }

    public String getDatabinderName() {
        return this.cmbdbtype.getSelectedItem().toString();

    }

    public boolean getGenerateServerSideInterface() {
        return this.serverSideInterface.isSelected();
    }

    public boolean getGenerateAll() {
        return this.all.isSelected();
    }

    public String getNs2PkgMapping(){
        String returnList="";
        String packageValue;
        for (int i=0;i<table.getRowCount() ;i++){
            packageValue = table.getValueAt(i,1).toString();
            if (packageValue!=null && !"".equals(packageValue)){
                returnList = returnList +
                        ("".equals(returnList)?"":",") +
                        table.getValueAt(i,0).toString()+ "=" + packageValue;
            }
        }
        return "".equals(returnList)?null:returnList;
    }

    private void handleCustomPackageNameModifyEvent() {
        // This method is add as a tempory fix for the Axis2-1368
        // TODO fix this permanantly.
        String text = this.txtPacakgeName.getText();
        if ((text == null) || (text.trim().equals(""))|| (text.endsWith(".")) || (text.startsWith("."))) {
            //Todo error message "Invalid package name. Please enter a valid package name."
            return;
        }
        //Todo error message null
    }

}

