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
 */  package org.apache.axis2.tool.service.eclipse.ui;

 import org.apache.axis2.tool.service.bean.Page3Bean;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

 public class ServiceArchiveOutputLocationPage extends AbstractServiceWizardPage {

     private static final String DEFAULT_JAR_NAME = "my_service";
     private Text outputFileLocationTextBox;
     private Button browseButton;
     private Text outputFileNameTextbox;
     private boolean isWizardComplete = false;
     
     public ServiceArchiveOutputLocationPage(){
         super("page4");
     }
     
     
    /* (non-Javadoc)
     * @see org.apache.axis2.tool.service.eclipse.ui.AbstractServiceWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_OUTPUT_LOCATION,System.getProperty("user.dir"));
        settings.put(PREF_OUTPUT_NAME,DEFAULT_JAR_NAME);

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
        gd.grabExcessHorizontalSpace = true;
         
 		Label lable = new Label(container,SWT.NULL);
 		lable.setText(ServiceArchiver.getResourceString("page4.outputlocation.label"));
 		
 		outputFileLocationTextBox = new Text(container,SWT.BORDER);
 		outputFileLocationTextBox.setLayoutData(gd);
 		outputFileLocationTextBox.setText("");
 		outputFileLocationTextBox.addModifyListener(new ModifyListener(){
 		    public void modifyText(ModifyEvent e){
 		        handleLocationModification();
 		    }
 		});
 		
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
 				
 		browseButton = new Button(container,SWT.PUSH);
 		browseButton.setText(ServiceArchiver.getResourceString("general.browse"));
 		browseButton.setLayoutData(gd);
 		browseButton.addMouseListener(new MouseAdapter(){
 		    public void mouseUp(MouseEvent e) {
 		        handleBrowse();
 		    } 
 		});
 		
 		lable = new Label(container,SWT.NULL);
 		lable.setText(ServiceArchiver.getResourceString("page4.outputname.label"));
 		
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		
 		outputFileNameTextbox = new Text(container,SWT.BORDER);
 		outputFileNameTextbox.setLayoutData(gd);
 		outputFileNameTextbox.setText(settings.get(PREF_OUTPUT_NAME));
 		outputFileNameTextbox.addModifyListener(new ModifyListener(){
 		    public void modifyText(ModifyEvent e){
 		        handleFileNameModification();
 		    }
        });
 		
 		//Add some fill lables 
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
 		Label fillLable = new Label(container,SWT.NULL);
 		fillLable.setText("");
 		fillLable.setLayoutData(gd);
 		Label fillLable1 = new Label(container,SWT.NULL);
 		fillLable1.setText("");
 		fillLable1.setLayoutData(gd);
 		
        //Hint Lable
 		Label hintLable = new Label(container,SWT.NULL);
 		hintLable.setText(ServiceArchiver.getResourceString("page4.hint.caption"));
 		hintLable.setLayoutData(gd);
 		
        if (restoredFromPreviousSettings) {
            handleFileNameModification();
            handleLocationModification();
        } else {
            //nothing yet
        }

        setControl(container);

    }
     
     private void handleBrowse(){
         DirectoryDialog dirDialog = new DirectoryDialog(this.getShell());
         dirDialog.setMessage(ServiceArchiver.getResourceString("page4.dirdialog.caption"));
         String returnText = dirDialog.open();
         if (returnText!=null){
             this.outputFileLocationTextBox.setText(returnText);
             this.outputFileLocationTextBox.setToolTipText(returnText);
         }
      }
     
     private void handleLocationModification(){
         String outputLocationText = outputFileLocationTextBox.getText();
         settings.put(PREF_OUTPUT_LOCATION,outputLocationText);
         if (outputLocationText==null ||"".equals(outputLocationText.trim())){
             this.updateStatus(ServiceArchiver.getResourceString("page4.error.location"));
             isWizardComplete=false;
             updateStatus(null);
         }else{
             String outputFilenameText = outputFileNameTextbox.getText();
             settings.put(PREF_OUTPUT_NAME,outputFilenameText);
        	 if (outputFilenameText==null || "".equals(outputFilenameText.trim())){
                 this.updateStatus(ServiceArchiver.getResourceString("page4.error.filename"));
                 isWizardComplete=false;
                 updateStatus(null);
        	 }else{
        		 isWizardComplete = true;
        		 updateStatus(null);
        	 }
         }
     }
     private void handleFileNameModification(){
         String outputFilenameText = outputFileNameTextbox.getText();
         settings.put(PREF_OUTPUT_NAME,outputFilenameText);
         if (outputFilenameText==null || "".equals(outputFilenameText.trim())){
             this.updateStatus(ServiceArchiver.getResourceString("page4.error.filename"));
             isWizardComplete=false;
             updateStatus(null);
         }else{
             String outputLocationText = outputFileLocationTextBox.getText();
             settings.put(PREF_OUTPUT_LOCATION,outputLocationText);
             if (outputLocationText==null ||"".equals(outputLocationText.trim())){
                 this.updateStatus(ServiceArchiver.getResourceString("page4.error.location"));
                 isWizardComplete=false;
                 updateStatus(null);
             }else{
            	 isWizardComplete = true;
            	 updateStatus(null);
             }
         }
     }
     
     
     
     public Page3Bean getBean(){
         Page3Bean pageBean = new Page3Bean();
         pageBean.setOutputFolderName(this.outputFileLocationTextBox.getText().trim());
         pageBean.setOutputFileName(this.outputFileNameTextbox.getText().trim());
         return pageBean;
     }
     
 	protected boolean getWizardComplete() {
		return isWizardComplete;
	}
 }
