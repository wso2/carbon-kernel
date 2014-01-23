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

package org.apache.axis2.tools.java2wsdl;

import org.apache.axis2.tools.bean.ClassLoadingTestBean;
import org.apache.axis2.tools.bean.NamespaceFinder;
import org.apache.axis2.tools.bean.WsdlgenBean;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * this is the first panel of java2wsdl wizard
 */
public class MiddlePanel  extends WizardPanel {
    /**
     * varibale
     */
    private JTextField txtClass;
    private JButton btnFolder;
    private JButton btnJar;
    private JButton btnRemove;
    private JButton btnTest;
    private JButton btnHint;
    private JTextArea txaHint;
    private boolean flag=false;
    private JList listPathDisply;
    private DefaultListModel listModel;
    private JLabel lblTest;
    private String hint ="Please give the fully qualified class name, example :com.foo.BarService" +
            " Then add the folder or the jar file which contains that class file." +
            " Finally check whether the class file can be loaded from the plugin." +
            "  If the class that you are going to load contains any dependencies" +
            "  on other axis2 libraries ( for example like axiom*.jar), please add those" +
            " libraries as well and try to load the class.";

    final JFileChooser FileChooser =new JFileChooser();
    final JFileChooser DirChooser=new JFileChooser();
    private WsdlgenBean wsdlgenBean;

    /**
     * Constructor
     * @param wizardComponents
     * @param wsdlgenBean
     */
    public MiddlePanel(WizardComponents wizardComponents ,WsdlgenBean wsdlgenBean) {
        super(wizardComponents, "Axis2 Idea Plugin Java2WSDL Wizards");
        setPanelTopTitle("Java source / classpath selection");
        setPanelBottomTitle("Select the classes and the libraries");
        this.wsdlgenBean=wsdlgenBean;
        init();
    }

    /**
     * initiate panel
     */
    private void init(){

        txaHint =new JTextArea();
        txaHint.setBorder(null);
        txaHint.setFocusable(false);
        txaHint.setLineWrap(true);
        txaHint.setWrapStyleWord(true);
        txaHint.setOpaque(false);

        btnHint =new JButton("Hint >>");
        btnHint.setBorder(new EmptyBorder(new Insets(0,0,0,0)));

        btnFolder=new JButton("Add Folder");
        btnJar=new JButton("Add Jar");
        btnRemove=new JButton("Remove");
        btnTest=new JButton("Test Class Loading");

        txtClass =new JTextField();
        lblTest= new JLabel();

        listModel =new DefaultListModel();
        listPathDisply =new JList(listModel);
        listPathDisply.setAutoscrolls(true);
        listPathDisply.setOpaque(false);
        listPathDisply.setBorder(BorderFactory.createBevelBorder(1) );
        listPathDisply.setFocusable(false);

        setBackButtonEnabled(true);
        setNextButtonEnabled(false);
        setFinishButtonEnabled(false);
        setPageComplete(false);

        this.setLayout(new GridBagLayout());

        this.add(new JLabel("Fully Qualified Class Name :")
                , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));

        this.add(txtClass
                , new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 0, 10), 0, 0));

        this.add(new JLabel("java class path Entries.select either folders or jar files ")
                , new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1,0.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0,10), 0, 0));

        this.add(btnFolder
                , new GridBagConstraints(0, 2, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL
                , new Insets(5,10, 1,1), 0, 0));

        btnFolder.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                DirChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = DirChooser.showOpenDialog(btnFolder );
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    DirChooser.setFileSelectionMode(JFileChooser .FILES_ONLY );
                    File newfile = DirChooser.getSelectedFile();
                    listModel.addElement(newfile.getAbsolutePath() );
                    setDefaultPathAndName(newfile );
                    updateStatusTextField( false,"");
                }
                update();
            }
        });

        this.add(btnJar
                , new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 1,1), 0, 0));
        btnJar.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {

                FileChooser.setFileFilter(new JarFileFilter() );
                int returnVal= FileChooser.showOpenDialog(btnJar);
                if(returnVal == JFileChooser .APPROVE_OPTION ){
                    File file = FileChooser.getSelectedFile();
                    listModel.addElement(file.getAbsolutePath() );
                    setDefaultPathAndName(file );
                    updateStatusTextField( false,"");
                }
                update();
            }
        });

        this.add(btnRemove
                , new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER   , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 1,10), 0, 0));
        btnRemove.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                handleRemove();
                update();
            }
        });

        this.add(new JScrollPane(listPathDisply)
                , new GridBagConstraints(0, 3, GridBagConstraints.REMAINDER, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 1,10), 0, 0));

        this.add(btnTest
                , new GridBagConstraints(0, 4, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 1,1), 0, 0));
        btnTest.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                if(!testLoading()){
                    setBackButtonEnabled(true);
                    setNextButtonEnabled(false);
                    setFinishButtonEnabled(false);
                }else  {
                    setBackButtonEnabled(true);
                    setNextButtonEnabled(true);
                    setFinishButtonEnabled(false);
                    wsdlgenBean.setClassPathList(getClassPathlist());
                    wsdlgenBean.setClassName(txtClass.getText().trim() );
                    setPageComplete(true);
                }
                update();
            }
        });

        this.add(lblTest
                , new GridBagConstraints(1, 4, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 1,10), 0, 0));

        this.add(new JSeparator()
                , new GridBagConstraints(0, 5, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 1,1), 0, 0));

        this.add(btnHint,
                new GridBagConstraints(0, 6, 1, 1, 0.1,0.0
                        , GridBagConstraints.WEST , GridBagConstraints.NONE
                        , new Insets(5, 10, 0, 10), 0, 0));
        btnHint.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                if(flag){
                    btnHint.setText("Hint >>");
                    txaHint.setText("");
                    flag=false;
                }else{
                    btnHint.setText("Hint <<");
                    txaHint.setText(hint);
                    flag=true;
                }
                update();
            }
        });

        this.add(txaHint
                , new GridBagConstraints(0, 7, GridBagConstraints.REMAINDER, 1, 0.1,1.0
                , GridBagConstraints.CENTER , GridBagConstraints.BOTH
                , new Insets(5, 10, 10, 10), 0, 0));

    }

    //next
    public void next() {
        if(txtClass.getText()!=null && isPageComplete()){
            switchPanel(CodegenFrame.PANEL_OPTION_B );
        } else{
            switchPanel(CodegenFrame.PANEL_FIRST_B );
            setNextButtonEnabled(false);
        }
    }
    //back
    public void back() {
        switchPanel(CodegenFrame.PANEL_CHOOSER );
    }
    //update
    public void update() {
    }
    //set default path and name
    private void setDefaultPathAndName(File file)  {
        if(file.getParent()!=null){
            wsdlgenBean.setOutputLocation(file.getParent());
            wsdlgenBean.setOutputWSDLName("Services.wsdl");
        }
    }
    // update next page
    public void updateStatusTextField(boolean success,String text){
        if (success){
            wsdlgenBean.setServiceName(NamespaceFinder.getServiceNameText(txtClass.getText()) );
            wsdlgenBean.setTargetNamespace(NamespaceFinder.getTargetNamespaceFromClass(txtClass.getText()));
            wsdlgenBean.setTargetNamespacePrefix(NamespaceFinder.getDefaultNamespacePrefix());
            wsdlgenBean.setSchemaTargetNamespace(NamespaceFinder.getSchemaTargetNamespaceFromClass(txtClass.getText()));
            wsdlgenBean.setSchemaTargetNamespacePrefix(NamespaceFinder.getDefaultSchemaNamespacePrefix());
        }
        lblTest.setText(text);
    }

    //  Pops up the file browse dialog box

    private void handleRemove() {
        int[] selectionIndices = listPathDisply .getSelectedIndices() ;
        for (int i=0;i<selectionIndices.length;i++){
            listModel .remove(selectionIndices[i]);
        }
        updateStatusTextField(false,"");
        update();
    }

    //get class path list
    public String[] getClassPathlist(){
        Object [] listObject = listModel.toArray() ;
        String [] listString =new String[listObject.length];
        for(int i=0 ;i<listObject.length ;i++){
            listString[i]=listObject[i].toString() ;
        }
        return listString ;
    }
    // test loading
    public boolean testLoading(){
        java.util.List errorListener = new ArrayList();
        String [] listString =getClassPathlist() ;
        if (!ClassLoadingTestBean.tryLoadingClass(txtClass.getText(),listString,errorListener)){
            Iterator it = errorListener.iterator();
            while(it.hasNext()){
                Object nextObject = it.next();
                String errorMessage = nextObject==null? "Unknown error!" :nextObject.toString();
                lblTest .setText(errorMessage );
                updateStatusTextField(false,errorMessage);
                update();
            }
            return false;
        }else{
            updateStatusTextField(true,"Class file loaded successfully");
            return true;
        }

    }
    //get page type
    public  int getPageType() {
        return  WizardPanel.JAVA_2_WSDL_TYPE;
    }
}
