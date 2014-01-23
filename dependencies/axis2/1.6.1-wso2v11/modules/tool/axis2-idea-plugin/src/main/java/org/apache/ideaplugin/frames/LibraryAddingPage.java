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
import org.apache.axis2.tools.java2wsdl.JarFileFilter;
import org.apache.axis2.tools.wizardframe.CodegenFrame;
import org.apache.ideaplugin.bean.ArchiveBean;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;


public class LibraryAddingPage extends WizardPanel {

    private JTextField txtJarLocation;
    private JButton butSelect;
    private JButton butAdd;
    private JButton butRemove;
    private JList listPathDisply;
    private DefaultListModel listModel;
    private JButton btnHint;
    private JTextArea txaHint;
    private boolean flag=false;
    private String hint ="";
    private ArchiveBean archiveBean;
    private final JFileChooser fileChooser=new JFileChooser();


    public LibraryAddingPage(WizardComponents wizardComponents, ArchiveBean archiveBean) {
        super(wizardComponents,  "Axis2 Idea Plugin Service Archiver Creator Wizards");
        setPanelTopTitle("Service Archiver");
        setPanelBottomTitle("Add any external Jar");
        this.archiveBean=archiveBean;
        init();
    }

    private void init(){
        txaHint =new JTextArea();
        txaHint.setBorder(null);
        txaHint.setFocusable(false);
        txaHint.setLineWrap(true);
        txaHint.setWrapStyleWord(true);
        txaHint.setOpaque(false);

        btnHint =new JButton("Hint >>");
        btnHint.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
        txtJarLocation=new JTextField();
        butSelect =new JButton("Browse..");
        butAdd =new JButton("Add ->");
        butRemove=new JButton("Remove <-");
        listModel =new DefaultListModel();
        listPathDisply =new JList(listModel);
        listPathDisply.setAutoscrolls(true);
        listPathDisply.setOpaque(false);
        listPathDisply.setBorder(BorderFactory.createBevelBorder(1) );
        listPathDisply.setFocusable(false);

        setBackButtonEnabled(true);
        setNextButtonEnabled(true);
        setFinishButtonEnabled(false);
        setPageComplete(false);

        this.setLayout(new GridBagLayout());

        this.add(new JLabel("Jar file location")
                , new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));

        this.add(txtJarLocation
                , new GridBagConstraints(1, 0, GridBagConstraints.RELATIVE , 1, 1.0, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 0, 10), 0, 0));

        txtJarLocation.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });

        this.add(butSelect
                , new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.NONE
                , new Insets(5, 10, 1,10), 0, 0));

        butSelect.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                broswseJarFile();
                update();
            }
        });

        this.add(butAdd
                , new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 1,1), 0, 0));
        butAdd.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                listModel.addElement(txtJarLocation.getText() );
                txtJarLocation.setText("");
                update();
            }
        });

        this.add(butRemove
                , new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 1, 1,1), 2, 0));
        butRemove .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                handleRemove();
                update();
            }
        });

        this.add(new JScrollPane(listPathDisply)
                , new GridBagConstraints(0, 2, GridBagConstraints.REMAINDER, 1, 1.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 1,10), 0, 0));

        this.add(btnHint
                , new GridBagConstraints(0, 3, 1, 1, 0.1,0.0
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
                , new GridBagConstraints(0, 4, GridBagConstraints.REMAINDER, 1, 0.1,1.0
                , GridBagConstraints.CENTER , GridBagConstraints.BOTH
                , new Insets(5, 10, 10, 10), 0, 0));

    }
    //next
    public void next() {
        switchPanel(CodegenFrame.PANEL_FOURTH_C );

    }
    //back
    public void back() {
        switchPanel(CodegenFrame.PANEL_SECOND_C );
    }
    //update
    public void update() {
        fillBean();
        setPageComplete(true);
        setNextButtonEnabled(true);
    }

    //get page type
    public  int getPageType() {
        return  WizardPanel.SERVICE_ARCHIVE_TYPE  ;
    }

    private void broswseJarFile(){
        fileChooser.setFileFilter(new JarFileFilter() );
        int returnVal= fileChooser.showOpenDialog(butAdd);
        if(returnVal == JFileChooser .APPROVE_OPTION ){
            File file = fileChooser.getSelectedFile();
            txtJarLocation.setText(file.getAbsolutePath());
        }
    }
    private void handleRemove() {
        int[] selectionIndices = listPathDisply .getSelectedIndices() ;
        for (int i=0;i<selectionIndices.length;i++){
            listModel .remove(selectionIndices[i]);
        }
    }
    private void fillBean(){
        ArrayList tempList=archiveBean.getLibs();
        tempList.addAll(getJarPathlist());
        archiveBean.addLibs(tempList);
        URL urllist[] = new URL[listModel.size() + 1];
        ClassLoader cls = new URLClassLoader(urllist , LibraryAddingPage.class.getClassLoader());
        archiveBean.setClassLoader(cls);
    }
      //get class path list
    public ArrayList getJarPathlist(){
        Object [] listObject = listModel.toArray() ;
        ArrayList listString =new ArrayList();
        for(int i=0 ;i<listObject.length ;i++){
            listString.add(listObject[i].toString()) ;
        }
        return listString ;
    }
}
