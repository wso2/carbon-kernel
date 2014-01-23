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

package org.apache.ideaplugin.frames;

import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;
import org.apache.ideaplugin.bean.ArchiveBean;
import org.apache.ideaplugin.bean.ClassFileFilter;
import org.apache.ideaplugin.bean.OperationObj;
import org.apache.ideaplugin.bean.ServiceObj;
import org.apache.ideaplugin.frames.table.ArchiveTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class ServiceXMLGenerationPage extends WizardPanel {

    private JTextField txtServiceName;
    private JTextField txtClassName;
    private JCheckBox chkBoxSearchMethod;
    private JButton btnLoad;
    private JButton btnBrowse;
    private JTable table;
    private JLabel lblTable;
    private JScrollPane scrollPane;
    private HashMap operations;
    private String fileName;
    private int count = 1;
    private ArrayList servicelsit;
    private String sgXMl;
    private final JFileChooser fileChooser=new JFileChooser();
    private ArchiveBean archiveBean;
    /**
     * Constructor
     * @param wizardComponents
     */
    public ServiceXMLGenerationPage(WizardComponents wizardComponents, ArchiveBean archiveBean){
        super(wizardComponents, "Axis2 Idea Plugin Service Archiver Creator Wizards");
        setPanelTopTitle("Service Archiver");
        setPanelBottomTitle("Service XML Generation");
        this.archiveBean=archiveBean;
        init();
    }
    private void init(){
       
        txtServiceName =new JTextField();
        txtClassName =new JTextField();

        btnBrowse=new JButton("Browse");
        btnLoad =new JButton("Load");

        chkBoxSearchMethod=new JCheckBox("Search declared method only",true);

        lblTable=new JLabel("Mark operation you do not want to publish ");
        operations = new HashMap();
        ArchiveTableModel myModel=new ArchiveTableModel(operations);
        table=new JTable(myModel);
        table.setOpaque(true);
        table.setBackground(getBackground());
        table.setShowGrid(true);
        table.setSize(getPreferredSize());

        scrollPane =new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0,0,0,0));
        scrollPane.setSize(table.getSize());
        scrollPane.setOpaque(true);
        scrollPane.setBackground(getBackground());
        scrollPane.getViewport().setBackground(getBackground());
        scrollPane.setViewportBorder(new EmptyBorder(0,0,0,0));

        setBackButtonEnabled(true);
        setNextButtonEnabled(false);
        setFinishButtonEnabled(false);
        setPageComplete(false);
        this.setLayout(new GridBagLayout());

        this.add(new JLabel("Class Name")
                , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.NORTHWEST   , GridBagConstraints.NONE
                , new Insets(5, 10, 0, 0), 0, 0));

        this.add(txtClassName
                , new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTHWEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0, 0), 0, 0));
        this.add(btnBrowse
                , new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.NORTH  , GridBagConstraints.NONE
                , new Insets(5, 1, 1, 1), 0, 0));
        btnBrowse .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                browseClassFile();
                update();
            }
        });
        this.add(btnLoad
                , new GridBagConstraints(3, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.NORTH  , GridBagConstraints.NONE
                , new Insets(5, 1, 1, 10), 0, 0));

        btnLoad .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                updateTable();
                update();
            }
        });
         this.add(new JLabel("Service Name")
                , new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.NORTHWEST  , GridBagConstraints.NONE
                , new Insets(5, 10, 0,0), 0, 0));
         this.add(txtServiceName
                , new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTHWEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0, 0), 0, 0));

        this.add(chkBoxSearchMethod
                , new GridBagConstraints(0, 2, GridBagConstraints.RELATIVE, 1, 0.0, 0.0
                , GridBagConstraints.NORTHWEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0, 10), 0, 0));
         chkBoxSearchMethod .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                updateTable();
                update();
            }
        });
        this.add(lblTable
                , new GridBagConstraints(0, 3, GridBagConstraints.RELATIVE, 1, 0.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0, 10), 0, 0));

       this.add(scrollPane
                        , new GridBagConstraints(0, 4, GridBagConstraints.REMAINDER , 1, 1.0, 1.0
                        , GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH
                        , new Insets(5, 10, 10, 10), 0, 0));

        setPageComplete(true);
    }


    public void back() {
        switchPanel(CodegenFrame.PANEL_FOURTH_C );
    }

    public void next() {
        setNextButtonEnabled(false);        
        checkautoGeneration();
        switchPanel(CodegenFrame.PANEL_OPTION_C );
        ((ServiceXMLEditPage)getWizardComponents().getWizardPanel(CodegenFrame.PANEL_OPTION_C )).setDescription(archiveBean.getServiceXML());
        ((ServiceXMLEditPage)getWizardComponents().getWizardPanel(CodegenFrame.PANEL_OPTION_C )).setDefaultEnabled();
    }

    public void update(){

    }
    public  int getPageType() {
        return  WizardPanel.SERVICE_ARCHIVE_TYPE;
    }


    private void updateTable() {
        //get a URL from the class file location
        try {
            String classFileLocation = archiveBean.getClassLoc().getPath();
            URL classFileURL = new File(classFileLocation).toURL();

           ArrayList listofURLs = new ArrayList();
           listofURLs.add(classFileURL);

            //get the libraries from the lib page and load it
            ArrayList libList=archiveBean.getLibs();
            String[] libFileList=new String[libList.size()];
            for (int i = 0; i < libList.size(); i++) {
                libFileList[i] =  (String )libList.get(i);
            }

            if (libFileList!=null){
                int count = libFileList.length;
                for (int i=0;i<count;i++){
                    listofURLs.add(new File(libFileList[i]).toURL());
                }
            }
            txtServiceName.setText("MyService" + count);
            ClassLoader loader = new URLClassLoader((URL[])listofURLs.toArray(new URL[listofURLs.size()]));
            Class clazz = Class.forName(fileName ,true,loader);
            Method[] methods = null;
            if (chkBoxSearchMethod.isSelected()){
                methods = clazz.getDeclaredMethods();
            }else{
                methods = clazz.getMethods();
            }

            int methodCount = methods.length;
            if (methodCount > 0) {
                try {
                    table.removeAll();
                    table.setVisible(true);
                    operations.clear();
                } catch (Exception e1) {
                }
                for (int i = 0 ; i < methodCount; i++){
                    Method method = methods[i];
                    OperationObj operationobj = new OperationObj(method.getName(),
                            method.getReturnType().toString(),
                            new Integer(method.getParameterTypes().length), new Boolean(true));
                    operations.put(method.getName(), operationobj);
                }

                ArchiveTableModel myModel=new ArchiveTableModel(operations);
                table.setModel(myModel);
                scrollPane.repaint();
                this.repaint();
                setNextButtonEnabled(true);
            }

        } catch (MalformedURLException e) {
            setNextButtonEnabled(false);
           JOptionPane.showMessageDialog(btnLoad, "The specified file is not a valid java class",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            setNextButtonEnabled(false);
           JOptionPane.showMessageDialog(btnLoad, "The specified file is not a valid java class",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void browseClassFile(){
        fileChooser.setFileFilter(new ClassFileFilter() );
        fileChooser.setCurrentDirectory(archiveBean.getClassLoc());
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File tempfile = fileChooser .getSelectedFile();
            String newFile = tempfile.getPath();
            int index = newFile.indexOf(archiveBean.getClassLoc().getAbsolutePath().trim());
            if (index >= 0) {
                int lastindex = archiveBean.getClassLoc().getAbsolutePath().trim().length();
                newFile = newFile.substring(lastindex + 1);
                char ch = File.separatorChar;
                char newch = '.';
                int cindex = newFile.indexOf(ch);
                while (cindex >= 0) {
                    newFile = newFile.replace(ch, newch);
                    cindex = newFile.indexOf(ch);
                }
                fileName = newFile;
                int classIndex = fileName.lastIndexOf(".");
                fileName = fileName.substring(0, classIndex);
                txtClassName .setText(fileName);
            }
        }
    }
    private void serviceGroupProcess(){

        ArrayList ops = new ArrayList();
            Iterator opitr = operations.values().iterator();
            while (opitr.hasNext()) {
                OperationObj operationObj = (OperationObj) opitr.next();
                if (operationObj.getSelect().booleanValue()) {
                    ops.add(operationObj.getOpName());
                }
            }

            ServiceObj service = new ServiceObj(txtServiceName.getText(), fileName, ops);
            archiveBean.addToServicelsit(service);
            if (!archiveBean.isSingleService()) {
                int valu = JOptionPane.showConfirmDialog(this, "Do you want to add an another service to group", "Service Archive",
                        JOptionPane.YES_NO_OPTION);
                if (valu == 0) {
                    fileName = "";
                    txtClassName.setText("");
                    txtServiceName.setText("");
                    operations.clear();
                    setNextButtonEnabled(false);
                    switchPanel(CodegenFrame.PANEL_FIRST_C);
                    count++;
                    this.repaint();
                } else {
                    servicelsit =archiveBean.getServicelsit();
                    System.out.println(servicelsit.size());
                    sgXMl = "<serviceGroup>\n";
                    for (int i = 0; i < servicelsit.size(); i++) {
                        ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
                        sgXMl = sgXMl + serviceObj.toString();
                    }
                    sgXMl = sgXMl + "</serviceGroup>";
                    archiveBean.setServiceXML(sgXMl);
                    sgXMl="";
                    switchPanel(CodegenFrame.PANEL_OPTION_C );
                }
            } else {
                servicelsit = archiveBean.getServicelsit();
                sgXMl = "<serviceGroup>\n";
                for (int i = 0; i < servicelsit.size(); i++) {
                    ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
                    sgXMl = sgXMl + serviceObj.toString();
                }
                sgXMl = sgXMl + "</serviceGroup>";
                archiveBean.setServiceXML(sgXMl);
                sgXMl="";
                switchPanel(CodegenFrame.PANEL_OPTION_C );
            }
    }

    private void checkautoGeneration(){
        ArrayList ops = new ArrayList();
        Iterator opitr = operations.values().iterator();
        while (opitr.hasNext()) {
            OperationObj operationObj = (OperationObj) opitr.next();
            if (operationObj.getSelect().booleanValue()) {
                ops.add(operationObj.getOpName());
            }
        }
        txtServiceName.setText(txtServiceName.getText());
        ServiceObj service = new ServiceObj(txtServiceName.getText(), fileName, ops);
        archiveBean.addToServicelsit(service);
        servicelsit = archiveBean.getServicelsit();
        sgXMl="<serviceGroup>";
        for (int i = 0; i < servicelsit.size(); i++) {
            ServiceObj serviceObj = (ServiceObj) servicelsit.get(i);
            sgXMl = sgXMl + serviceObj.toString();
        }
        sgXMl = sgXMl + "</serviceGroup>";
        archiveBean.setServiceXML(sgXMl);
        sgXMl="";
    }
}

