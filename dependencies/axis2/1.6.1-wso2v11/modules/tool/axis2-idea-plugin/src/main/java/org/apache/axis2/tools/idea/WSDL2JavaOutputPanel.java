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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.axis2.tools.bean.CodegenBean;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class WSDL2JavaOutputPanel extends WizardPanel {

    private JTextField txtoutput;
    private JTextField txtaxisHome;
    private JTextField txtjarFileName;
    private JButton btnBrowseOutput;
    private JButton btnBrowseAxisHome;
    private JButton btnCheckLib;
    private JRadioButton radCurrentProject;
    private JRadioButton radCustomLocation;
    private JCheckBox chbAddAxisCodegenJar;
    private JCheckBox chbAddAxisLib;
    private JCheckBox chbCreateJar;
    private JLabel lblMessage;
    private JTextArea txtaTilte;
    private JComboBox cmbCurrentProject;
    private JComboBox cmbModuleSrc;
    private JLabel  lblAsixHome;
    private JLabel lblJarFileName;
    private JLabel lbloutput;
    private JLabel lblModule;
    private JLabel lblDirectory;
    final JFileChooser DirChooser=new JFileChooser();
    final private String title="Select one of below to save the codegen output either on idea workspace project or on file " +
            "system and then browse to enter the output path";
    final private String hint="If you have Axis2 binary distribution or Axis source, then you can add those libs also to the" +
            " resulted codegen project by checking the \"Add Axis2 Libraries to the codegen resulted project\"" +
            " check box and specifying the Axis2 home. Another option you have is to compile the codegen " +
            " result project and add it as a jar file in the lib directory of the resulted project, for that you can" +
            " check the \"Create a jar file of codegen result project and add to resulted project lib folder\"" +
            " checkbox and then give the jar file name that you prefer. \n" +
            " If you are adding the codegen result to a eclipse project on current eclipse workspace," +
            " please make sure to refresh that particular eclipse project.\n" +
            " Also if you select the options to add libs to the project, make sure to add those libs to the" +
            " project library path.";

    private CodegenBean codegenBean;
    private boolean flag = true;
    private Project project;
    private JButton btnHint;
    private JTextArea txaHint;
    private boolean jarFileCopyOption = false;
    private String axis2LibsLocation = null;


    public WSDL2JavaOutputPanel(WizardComponents wizardComponents,CodegenBean codegenBean, Project project) {
        super(wizardComponents, "Axis2 Idea Plugin WSDL2Java Wizards");
        this.codegenBean=codegenBean;
        this.project=project;
        setPanelTopTitle("Output");
        setPanelBottomTitle("set the out location for the generated code");
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
        lblMessage = new JLabel();
        lblMessage.setOpaque(true);
        lblMessage.setBackground(Color.white);
        btnBrowseAxisHome =new JButton("Browse..");
        btnBrowseOutput =new JButton("Browse..");
        btnCheckLib =new JButton("Check Libs..");
        cmbCurrentProject =new JComboBox();
        cmbCurrentProject.setEnabled(false);
        cmbModuleSrc=new JComboBox();
        cmbModuleSrc.setEnabled(false);
        txtoutput=new JTextField();
        txtaxisHome =new JTextField("Unpacked Axis2 binary home Or Axis2 source location");
        txtjarFileName =new JTextField();
        txtaTilte =new JTextArea(title);
        txtaTilte.setBorder(null);
        txtaTilte.setFocusable(false);
        txtaTilte.setLineWrap(true);
        txtaTilte.setWrapStyleWord(true);
        txtaTilte.setOpaque(false);
        lblAsixHome=new JLabel("Axis2 Home");
        lblAsixHome.setEnabled(false);
        lblJarFileName = new JLabel("Jar File Name");
        lblJarFileName.setEnabled(false);
        lblDirectory= new JLabel("Select the Directory");
        lblModule = new JLabel("Select the Module");
        lbloutput =new JLabel("Out put path");
        chbAddAxisCodegenJar =new JCheckBox("Add the Axis2 Codegen jars to the codegen resulted project");
        chbAddAxisLib =new JCheckBox(" Add Axis2 libraries to the codegen result project ");
        chbCreateJar=new JCheckBox(" Create a jar file of codegen result project and add to resulted project lib folder(Default :CodegenResults.jar)");
        radCurrentProject =new JRadioButton("Browse and select a project on current idea workspace",false);
        radCustomLocation =new JRadioButton("Browse and select location on local file system",true);
        ButtonGroup  buttonGroup= new  ButtonGroup();
        buttonGroup.add(radCurrentProject );
        buttonGroup.add(radCustomLocation);

        codegenBean.setProject(project);
        setNextButtonEnabled(false);
        this.setLayout(new GridBagLayout() );

        this.add(txtaTilte
                , new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER , 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0,10), 0, 0));
        this.add(radCurrentProject
                , new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));
        radCurrentProject.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                cmbCurrentProject.setEnabled(true);
                cmbModuleSrc.setEnabled(true);
                txtoutput.setEnabled(false);
                btnBrowseOutput.setEnabled(false);
                loadCmbCurrentProject();
                loadcmbModuleSrcProject();
                setFinishButtonEnabled(true);
                update();
            }
        });
        this.add(lblModule
                , new GridBagConstraints(0, 2, 1, 1,  0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 10, 0, 0), 0, 0));

        this.add(cmbCurrentProject
                , new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0,0), 0, 0));
        cmbCurrentProject.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                loadcmbModuleSrcProject();
                update();
            }
        });
        this.add(lblDirectory
                , new GridBagConstraints(0, 3, 1, 1,  0.1, 0.0
                , GridBagConstraints.WEST  , GridBagConstraints.NONE
                , new Insets(5, 10, 0, 0), 0, 0));

        this.add(cmbModuleSrc
                , new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0,0), 0, 0));
        cmbModuleSrc.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.add(radCustomLocation
                , new GridBagConstraints(0, 4, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0,10), 0, 0));
        radCustomLocation.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                cmbCurrentProject.setEnabled(false);
                cmbModuleSrc.setEnabled(false);
                txtoutput.setEnabled(true);
                btnBrowseOutput.setEnabled(true);
                setEnabledForCustomProject();
                update();
            }
        });
        this.add(lbloutput
                , new GridBagConstraints(0, 5, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,0), 0, 0));
        this.add(txtoutput
                , new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0,0), 0, 0));
        this.add(btnBrowseOutput
                , new GridBagConstraints(2, 5, 1, 1, 0.1, 0.0
                , GridBagConstraints.EAST , GridBagConstraints.NONE
                , new Insets(5, 0, 0,10), 0, 0));
        btnBrowseOutput .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                DirChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = DirChooser.showOpenDialog(btnBrowseOutput );
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    DirChooser.setFileSelectionMode(JFileChooser .FILES_ONLY );
                    File newfile = DirChooser.getSelectedFile();
                    txtoutput.setText(newfile.getAbsolutePath() );
                }
                setFinishButtonEnabled(true);
                update();
            }
        });
        this.add(chbAddAxisCodegenJar
                , new GridBagConstraints(0, 6, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0,10), 0, 0));
        chbAddAxisCodegenJar.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.add(new JSeparator()
                , new GridBagConstraints(0, 7, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 0,1), 0, 0));

        this.add(chbAddAxisLib
                , new GridBagConstraints(0, 8, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0,10), 0, 0));
        chbAddAxisLib .addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setEnabledForAddAxisLib();
                handleLoadLibsBrowse();
                update();
            }
        });
        this.add(lblAsixHome
                , new GridBagConstraints(0, 9, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));
        this.add(txtaxisHome
                , new GridBagConstraints(1, 9, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0,0), 0, 0));
        this.add(btnBrowseAxisHome
                , new GridBagConstraints(2, 9, 1, 1, 0.1, 0.0
                , GridBagConstraints.EAST , GridBagConstraints.NONE
                , new Insets(5, 0, 0,10), 0, 0));
        btnBrowseAxisHome .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                DirChooser .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = DirChooser.showOpenDialog(btnBrowseAxisHome );
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    DirChooser.setFileSelectionMode(JFileChooser .FILES_ONLY );
                    File newfile = DirChooser.getSelectedFile();
                    txtaxisHome .setText(newfile.getAbsolutePath() );
                }
                update();
            }
        });
        this.add(btnCheckLib
                , new GridBagConstraints(0, 10, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));
        btnCheckLib .addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                handleLoadLibsBrowse();
                update();
            }
        });
        this.add(lblMessage
                , new GridBagConstraints(1, 10, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0,0), 0, 0));
        this.add(new JSeparator()
                , new GridBagConstraints(0, 11, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 0,1), 0, 0));
        this.add(chbCreateJar
                , new GridBagConstraints(0, 12, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 10, 0,10), 0, 0));
        chbCreateJar.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.add(lblJarFileName
                , new GridBagConstraints(0, 13, 1, 1, 0.1, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.NONE
                , new Insets(5, 10, 0,10), 0, 0));
        this.add(txtjarFileName
                , new GridBagConstraints(1, 13,1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL
                , new Insets(5, 0, 0,0), 0, 0));
        this.add(new JSeparator()
                , new GridBagConstraints(0, 14, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(5, 1, 0,1), 0, 0));

        // hint button
        this.add(btnHint,
                new GridBagConstraints(0,15, 1, 1, 0.1,0.0
                        , GridBagConstraints.WEST , GridBagConstraints.NONE
                        , new Insets(5, 20, 0,0), 0, 0));
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

        // hint lable
        this.add(txaHint,
                new GridBagConstraints(0, 16, GridBagConstraints.REMAINDER, 1, 0.1, 1.0
                        , GridBagConstraints.NORTHWEST , GridBagConstraints.BOTH
                        , new Insets(10, 20, 0,0), 0, 0));
        update();
    }
    public void back() {
        switchPanel(CodegenFrame.PANEL_OPTION_A );
    }
    public void next() {
    }

    public void update(){
        setNextButtonEnabled(false);
        checkPageComlete();
        setEnabledForAddAxisLib();    
    }
    public void loadCmbCurrentProject() {
        Module modules[] = codegenBean .getModules();

        if (modules != null) {
            for(int count = 0; count < modules.length; count++) {
                cmbCurrentProject.addItem(modules[count].getName());
            }
        }

    }
    public void loadcmbModuleSrcProject() {
        String module = null;
        module = (String) cmbCurrentProject.getSelectedItem();
        cmbModuleSrc.removeAllItems();
        int count = 0;
        if (module != null) {
            String src[] = codegenBean.getModuleSrc(module);
            for ( count = 0; count < src.length; count++) {
                cmbModuleSrc.addItem(src[count]);
            }
            count = src.length;
        }
        if (flag)
        {
            if (count == 0) {
                flag =false;
                setEnabledForCustomProject();
            }
            else{
                setEnabledForCurrentProject();
            }
        }
    }

    private void setEnabledForCurrentProject(){
        radCurrentProject.setSelected(true);
        radCurrentProject.setEnabled(true);
        cmbCurrentProject.setEnabled(true);
        cmbModuleSrc.setEnabled(true);
        lblDirectory.setEnabled(true);
        lblModule .setEnabled(true);
        radCurrentProject.setSelected(true);
        txtoutput.setEnabled(false);
        lbloutput.setEnabled(false);
        btnBrowseOutput.setEnabled(false);
    }
    private void setEnabledForCustomProject(){
        if(!flag){
            radCurrentProject.setEnabled(false);
        }else{
            radCurrentProject.setEnabled(true);
            radCurrentProject.setSelected(true);
        }
        cmbCurrentProject.setEnabled(false);
        cmbModuleSrc.setEnabled(false);
        lblDirectory.setEnabled(false);
        lblModule .setEnabled(false);
        radCustomLocation.setSelected(true);
        txtoutput.setEnabled(true);
        lbloutput.setEnabled(true);
        btnBrowseOutput .setEnabled(true);
    }
    private void setEnabledForAddAxisLib(){
        if(chbAddAxisLib.isSelected()){
            lblAsixHome.setEnabled(true);
            lblMessage.setEnabled(true);
            lblJarFileName.setEnabled(true);
            txtaxisHome.setEnabled(true);
            txtjarFileName.setEnabled(true);
            chbCreateJar.setEnabled(true);
            btnBrowseAxisHome.setEnabled(true);
            btnCheckLib .setEnabled(true);
        }else{
            lblAsixHome.setEnabled(false);
            lblMessage.setEnabled(false);
            lblJarFileName.setEnabled(false);
            txtaxisHome.setEnabled(false);
            txtjarFileName.setEnabled(false);
            chbCreateJar.setEnabled(false);
            btnBrowseAxisHome.setEnabled(false);
            btnCheckLib .setEnabled(false);
            txtaxisHome.setText("Unpacked Axis2 binary home Or Axis2 source location");
            lblMessage.setBackground(Color.WHITE);
        }
    }

    private void handleLoadLibsBrowse(){
        if(txtaxisHome.getText().equals("")){
            lblMessage.setText("Axis libs are not available !! ");
            jarFileCopyOption=false;
            //error message
        }else{
            String axis_home = txtaxisHome.getText().trim();
            String axis_target_lib=axis_home+File.separator+"target"+File.separator+"lib";
            String axis_std_lib_directory=axis_home+File.separator+"lib";

            File axis_target_libs_directory = new File(axis_target_lib);
            File axis_libs_directory = new File(axis_std_lib_directory);
            if (axis_libs_directory.isDirectory() || axis_target_libs_directory.isDirectory()) {
                lblMessage.setText("Axis libs loaded successfully!!");
                if(axis_libs_directory.isDirectory()){
                    axis2LibsLocation=axis_std_lib_directory;
                }
                else if(axis_target_libs_directory.isDirectory()){
                    axis2LibsLocation=axis_target_lib;
                }
                jarFileCopyOption =true;
                lblMessage.setBackground(Color.WHITE);
            }else{
                lblMessage.setText("Axis libs are not available !! ");
                lblMessage.setBackground(Color.RED);
                jarFileCopyOption=false;
            }
        }
    }

    /**
     * get the output location
     */

    public String getOutputLocation() {
        if(radCurrentProject.isSelected())
             return cmbModuleSrc.getSelectedItem().toString();
        else
            return txtoutput.getText();

    }

    public String getAxisHomeLocation() {
        return txtaxisHome.getText();
    }

    public String getJarFilename() {
        return txtjarFileName.getText();
    }

    public boolean getAxis2PluginLibCopyCheckBoxSelection(){
        return this.chbAddAxisCodegenJar.isSelected();
    }

    public boolean getAxisLibCopyCheckBoxSelection(){
        return this.chbAddAxisLib.isSelected();
    }

    public boolean getCreateJarCheckBoxSelection(){
        return this.chbCreateJar.isSelected();
    }

    public String getAxisJarsLocation(){
        return this.axis2LibsLocation;
    }

    public boolean oktoLoadLibs(){
        return this.jarFileCopyOption;
    }

    public void setJarFileName(String jarFileName){
        this.txtjarFileName.setText(jarFileName);
    }

    /**
     * this algorithm used for set page complete and Enabled finsh button
     */
    private void checkPageComlete(){
        if(getAxisLibCopyCheckBoxSelection()){
            if(radCurrentProject.isSelected()) {
                if(jarFileCopyOption){
                    setPageComplete(true);
                    setFinishButtonEnabled(true);
                    codegenBean.setOutput(getOutputLocation());
                }else{
                    setPageComplete(false);
                    setFinishButtonEnabled(false);
                }
            }else{
                if(!getOutputLocation().equals("")){
                    if(jarFileCopyOption){
                        setPageComplete(true);
                        setFinishButtonEnabled(true);
                        codegenBean.setOutput(getOutputLocation());
                    }else{
                        setPageComplete(false);
                        setFinishButtonEnabled(false);
                    }
                }else{
                    setPageComplete(false);
                    setFinishButtonEnabled(false);
                }
            }
        }else{
            if(radCurrentProject.isSelected()) {
                setPageComplete(true);
                setFinishButtonEnabled(true);
                codegenBean.setOutput(getOutputLocation());
            }else{
                if(!getOutputLocation().equals("")){
                    setPageComplete(true);
                    setFinishButtonEnabled(true);
                    codegenBean.setOutput(getOutputLocation());
                }else{
                    setPageComplete(false);
                    setFinishButtonEnabled(false);
                }
            }
        }
    }
    /**
     * get page type
     */
    public  int getPageType() {
        return  WizardPanel.WSDL_2_JAVA_TYPE;
    }

    private void handleModifyEvent() {
        String text = this.txtoutput.getText();
        if ((text == null) || (text.trim().equals(""))) {
            // error message "output location needs to be specified"
			return;
		}
		// error message null
	}
	private void handleAxisHomeModifyEvent() {
		String text = this.txtaxisHome.getText();
		if ((text == null) || (text.trim().equals(""))) {
			// error message "output location needs to be specified"
			return;
		}
		// error message null
	}
    private void handleJarNameModifyEvent() {
		String text = this.txtjarFileName.getText();
		if ((text == null) || (text.trim().equals(""))) {
			// error message "output location needs to be specified"
			return;
		}
		// error message null
	}
}







