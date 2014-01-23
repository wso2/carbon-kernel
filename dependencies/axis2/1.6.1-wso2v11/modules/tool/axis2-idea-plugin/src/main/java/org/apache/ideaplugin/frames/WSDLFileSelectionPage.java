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
import org.apache.axis2.tools.idea.WSDLFileFilter;
import org.apache.axis2.tools.wizardframe.CodegenFrame;
import org.apache.ideaplugin.bean.ArchiveBean;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: shivantha
 * Date: 16/07/2007
 * Time: 11:20:00
 * To change this template use File | Settings | File Templates.
 */
public class WSDLFileSelectionPage extends WizardPanel {

    private JLabel lblWSDL;
    private JCheckBox chkBoxSkip;
    private JCheckBox chkBoxSelect;
    private JTextField txtWSDL;
    private JButton btnBrowse;
    private JButton btnHint;
    private JTextArea txaHint;
    private boolean flag=false;
    private String hint ="";
    private final JFileChooser fileChooser=new JFileChooser();
    private ArchiveBean archiveBean;     
    /**
     * Constructor
     * @param wizardComponents
     */
    public WSDLFileSelectionPage(WizardComponents wizardComponents, ArchiveBean archiveBean){
        super(wizardComponents,"Axis2 Idea Plugin Service Archiver Creator Wizards");
        setPanelTopTitle("Service Archiver");
        setPanelBottomTitle("Add the WSDL file");
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

        lblWSDL=new JLabel("Select a WSDL file");

        chkBoxSkip =new JCheckBox("Skip WSDL",true);

        chkBoxSelect =new JCheckBox("Select WSDL",false) ;

        ButtonGroup  buttonGroup= new  ButtonGroup();
        buttonGroup.add(chkBoxSkip );
        buttonGroup.add(chkBoxSelect );

        txtWSDL=new JTextField();

        btnBrowse=new JButton("Browse..");

        setBackButtonEnabled(true);
        setNextButtonEnabled(true);
        setFinishButtonEnabled(false);
        this.setLayout(new GridBagLayout());

        this.add(chkBoxSkip
        , new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
        , GridBagConstraints.WEST , GridBagConstraints.NONE
        , new Insets(5, 10, 0,10), 0, 0));
        chkBoxSkip .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });

        this.add(chkBoxSelect
        , new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.NONE
        , new Insets(5, 10, 0,0), 0, 0));
        chkBoxSelect.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.add(lblWSDL
        , new GridBagConstraints(0, 2, 1, 1, 0.1, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.NONE
        , new Insets(5, 10, 0,0), 0, 0));

         this.add(txtWSDL
        , new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
        , new Insets(5, 10, 0, 0), 0, 0));

         this.add(btnBrowse
        , new GridBagConstraints(2, 2, 1, 1, 0.1, 0.0
        , GridBagConstraints.CENTER  , GridBagConstraints.NONE
        , new Insets(5, 10, 0, 10), 0, 0));

        btnBrowse.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                browseWSDLFile();
                checkWSDLFile();
                update();
            }
        });

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

     public void back() {
          switchPanel(CodegenFrame.PANEL_FIRST_C);
    }

    public void next() {
         switchPanel(CodegenFrame.PANEL_THIRD_C );
    }

    public void update(){
        setChangeEnabled();
        fillBean();
        setPageComplete(true);
        setBackButtonEnabled(true);
        setNextButtonEnabled(true);
    }
     public  int getPageType() {
        return  WizardPanel.SERVICE_ARCHIVE_TYPE;
    }

    private void setChangeEnabled(){
        if(chkBoxSkip.isSelected()){
            lblWSDL.setEnabled(false);
            txtWSDL .setEnabled(false);
            btnBrowse.setEnabled(false);
        }else{
            lblWSDL.setEnabled(true);
            txtWSDL.setEnabled(true);
            btnBrowse.setEnabled(true);
        }
    }

    private void fillBean(){
        if(chkBoxSelect.isSelected()){
            if(!txtWSDL.getText().equals(""))
            archiveBean.addWsdls(new File(txtWSDL.getText()));
        }
    }
    private void checkWSDLFile(){
        if (txtWSDL.getText().equals("") ) {
            try{
                WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
                reader.readWSDL(txtWSDL.getText().trim()) ;
            }catch(WSDLException e1) {
                txtWSDL.setText("");
                JOptionPane.showMessageDialog(btnBrowse , "The file selected is not a valid WSDLfile",
                        "Axis2 ServiceArchieve creation", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void browseWSDLFile(){
        fileChooser.setFileFilter(new WSDLFileFilter());
        fileChooser.setCurrentDirectory(archiveBean.getClassLoc());
        int returnVal = fileChooser.showOpenDialog(btnBrowse);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            txtWSDL.setText(file.getAbsolutePath());
        }
    }
}
