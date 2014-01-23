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
import org.apache.ideaplugin.bean.XmlFileFilter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: shivantha
 * Date: 17/07/2007
 * Time: 09:45:03
 * To change this template use File | Settings | File Templates.
 */
public class ServiceXMLFileSelectionPage extends WizardPanel {
    /**
     * varialbales
     */
    private JLabel lblXmlLocation;
    private JTextField txtServiceXml;
    private JButton butSelect;
    private JCheckBox chkBoxIncludeXml;
    private JButton btnHint;
    private JTextArea txaHint;
    private boolean flag=false;
    private String hint ="";
    private final JFileChooser fileChooser=new JFileChooser();
    private ArchiveBean archiveBean;
    private String value;
    /**
     * Constructor
     * @param wizardComponents
     */
    public ServiceXMLFileSelectionPage(WizardComponents wizardComponents,ArchiveBean archiveBean ) {
        super(wizardComponents, "Axis2 Idea Plugin Service Archiver Creator Wizards");
        setPanelTopTitle("Service Archiver");
        setPanelBottomTitle("Select the service.xml file");
        this.archiveBean=archiveBean;
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
        lblXmlLocation=new JLabel("set the Service XML file");
        lblXmlLocation.setEnabled(false);
        txtServiceXml =new JTextField();
        txtServiceXml.setEnabled(false);
        butSelect=new JButton("Browse..");
        butSelect.setEnabled(false);
        chkBoxIncludeXml = new JCheckBox("Generate service xml automatically", true) ;

        setBackButtonEnabled(true);
        setNextButtonEnabled(false);
        setFinishButtonEnabled(false);
        setPageComplete(false);

        this.setLayout(new GridBagLayout());


         this.add(chkBoxIncludeXml
                , new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1,0.0, 0.0
                , GridBagConstraints.CENTER  , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 1,1), 0, 0));
        chkBoxIncludeXml.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                setChangeEnabled();
                update();
            }
        });
        this.add(lblXmlLocation
                , new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,0), 0, 0));

        this.add(txtServiceXml
                , new GridBagConstraints(1, 1, GridBagConstraints.RELATIVE , 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 0, 0), 0, 0));

        txtServiceXml.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });

        this.add(butSelect
                , new GridBagConstraints(2, 1, 1, 1, 0.1, 0.0
                , GridBagConstraints.CENTER , GridBagConstraints.NONE
                , new Insets(5, 0, 1,10), 0, 0));

        butSelect.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                browseXmlFile();
                setNextButtonEnabled(true);
                setPageComplete(true);
                update();
            }
        });
        this.add(btnHint,
                new GridBagConstraints(0, 2, GridBagConstraints.REMAINDER, 1, 0.1,0.0
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
                , new GridBagConstraints(0, 3, GridBagConstraints.REMAINDER, 1, 0.1,1.0
                , GridBagConstraints.CENTER , GridBagConstraints.BOTH
                , new Insets(5, 10, 10, 10), 0, 0));

    }

    //next
    public void next() {
        if(!chkBoxIncludeXml.isSelected()){
            switchPanel(CodegenFrame.PANEL_LAST_C);
        } else{
            switchPanel(CodegenFrame.PANEL_LOAD_C);
        }
        setNextButtonEnabled(false);

    }
    //back
    public void back() {
        switchPanel(CodegenFrame.PANEL_THIRD_C );
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

    private void browseXmlFile(){
        fileChooser.setFileFilter(new XmlFileFilter() );
        fileChooser.setCurrentDirectory(archiveBean.getClassLoc());
        int returnVal= fileChooser.showOpenDialog(butSelect);
        if(returnVal == JFileChooser .APPROVE_OPTION ){
            File xmlfile = fileChooser.getSelectedFile();
            txtServiceXml.setText(xmlfile.getAbsolutePath());
            byte[] buf = new byte[1024];
            int read;
            ByteArrayOutputStream out;
            try {
                FileInputStream in = new FileInputStream(xmlfile);

                out = new ByteArrayOutputStream();
                while ((read = in.read(buf)) > 0) {
                    out.write(buf, 0, read);
                }
                in.close();
                value = new String(out.toByteArray());
            } catch (IOException e1) {
            }

        } else {
            txtServiceXml.setText("");
        }
    }
    private void setChangeEnabled(){
        if(chkBoxIncludeXml.isSelected()){
            lblXmlLocation.setEnabled(false);
            txtServiceXml .setEnabled(false);
            butSelect.setEnabled(false);
        }else{
            lblXmlLocation.setEnabled(true);
            txtServiceXml.setEnabled(true);
            butSelect.setEnabled(true);
        }
    }
    private void fillBean(){
        if(!chkBoxIncludeXml.isSelected()){
            if(value!=null)
                archiveBean.setServiceXML(value);
        }

    }
    public boolean isIncludeXml(){
        return this.chkBoxIncludeXml.isSelected();
    }
}
