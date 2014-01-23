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


import org.apache.axis2.tool.service.bean.WizardBean;
import org.apache.axis2.tool.service.control.Controller;
import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import java.lang.reflect.InvocationTargetException;


public class ServiceArchiveWizard extends Wizard implements INewWizard {


	private ClassFileLocationPage classFileLocationPage;
    private WSDLFileSelectionPage wsdlFileSelectionPage;
    private ServiceXMLFileSelectionPage serviceXMLFileSelectionPage;
    private ServiceXMLGenerationPage serviceXMLGenerationPage;
    private ServiceArchiveOutputLocationPage serviceArchiveOutputLocationPage;
    private LibraryAddingPage libPage;

    private String classFileLocation;
    private String wsdlFileGenerationStatus;
    
  
    
    /**
     * @return Returns the wsdlFileGenerationStatus.
     */
    public String getWsdlFileGenerationStatus() {
        return wsdlFileGenerationStatus;
    }
    /**
     * @param message The wsdlFileGenerationStatus to set.
     */
    public void updateWsdlFileGenerationStatus(String message) {
        this.wsdlFileGenerationStatus = message;
    }
    public  String getClassFileLocation(){
        return classFileLocation;
    }
    
    //get the lib file list
    public String[] getLibFileList(){
        return libPage.getBean().getFileList();
    }
    
    public  void setClassFileLocation(String location){
        this.classFileLocation = location;
    }
    
    public void updateServiceXMLGeneration(boolean status){
        serviceXMLGenerationPage.setPageComplete(status);
    }
    /**
     * 
     */
    public ServiceArchiveWizard() {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle(ServiceArchiver.getResourceString("main.title"));
    }

    public boolean canFinish() {
        IWizardPage[] pages = getPages();
        AbstractServiceWizardPage wizardPage = null;
        for (int i = 0; i < pages.length; i++) {
            wizardPage = (AbstractServiceWizardPage) pages[i];
            if (wizardPage.getName().equals("page4.name")){
	            if (!wizardPage.getWizardComplete() ) {
	                    return false;
	            }
            }
        }
        return true;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage page) {
        AbstractServiceWizardPage thisPage = (AbstractServiceWizardPage)page;
        AbstractServiceWizardPage nextPage = (AbstractServiceWizardPage)super.getNextPage(page);
        while (thisPage!=null && thisPage.isSkipNext()) {
            if (nextPage!=null) {
                thisPage = nextPage;
                nextPage = (AbstractServiceWizardPage)super.getNextPage(nextPage);
            }else{
                break;
            }
        }
        return nextPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        classFileLocationPage = new ClassFileLocationPage();
        this.addPage(classFileLocationPage);
        wsdlFileSelectionPage = new WSDLFileSelectionPage();
        this.addPage(wsdlFileSelectionPage);
        libPage = new LibraryAddingPage();
        this.addPage(libPage);
        serviceXMLFileSelectionPage = new ServiceXMLFileSelectionPage();
        this.addPage(serviceXMLFileSelectionPage);
        serviceXMLGenerationPage = new ServiceXMLGenerationPage();
        this.addPage(serviceXMLGenerationPage);
        serviceArchiveOutputLocationPage = new ServiceArchiveOutputLocationPage();
        this.addPage(serviceArchiveOutputLocationPage);
    }

    /* (non-Javadobc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
                
        WorkspaceModifyOperation op = new WorkspaceModifyOperation()
        {
           protected void execute(IProgressMonitor monitor)
           throws CoreException , InvocationTargetException, InterruptedException{
              if (monitor == null){
                 monitor = new NullProgressMonitor();
              }

              /*
               * "7" is the total amount of steps, see below monitor.worked(amount)
               */
              monitor.beginTask(ServiceArchiver.getResourceString("wizard.codegen.startmsg"), 8);

              try
              {
                  monitor.worked(1);
                  // create a wizard bean
                  WizardBean wizBean = new WizardBean();
                  monitor.worked(1);
                  wizBean.setPage1bean(classFileLocationPage.getBean());
                  monitor.worked(1);
                  wizBean.setWsdlBean(wsdlFileSelectionPage.getBean());
                  monitor.worked(1);
                  wizBean.setPage2bean(serviceXMLGenerationPage.getBean(serviceXMLFileSelectionPage.getBean()));
                  monitor.worked(1);
                  wizBean.setLibraryBean(libPage.getBean());
                  monitor.worked(1);
                  wizBean.setPage3bean(serviceArchiveOutputLocationPage.getBean());
                  monitor.worked(1);
                  new Controller().process(wizBean);
                  monitor.worked(1);
              }
              catch (Throwable e)
              {
                 throw new InterruptedException(e.getMessage() );
              }

              monitor.done();
           }
        };
        
        /*
         * Start the generation as new Workbench Operation, so the user
         * can see the progress and, if needed, can stop the operation.
         */
        try
        {
           getContainer().run(false, true, op);
           showSuccessMessage(ServiceArchiver.getResourceString("wizard.codegen.success"));
           return true;
        }
        catch (InvocationTargetException e1)
        {
            showErrorMessage(e1.getTargetException().getMessage());
            return false;
        }
        catch (InterruptedException e1)
        {
            showErrorMessage(e1.getMessage());
            return false;
        }
        catch (Exception e)
        {
            showErrorMessage(ServiceArchiver.getResourceString("wizard.codegen.unknown.error") + "  "+  e.getMessage());
            return false;
        }

    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    private void showErrorMessage(String message) {
        MessageDialog.openError(this.getShell(), ServiceArchiver.getResourceString("wizard.codegen.error.msg.heading"), message);
    }

    private void showSuccessMessage(String message) {
        MessageDialog.openInformation(this.getShell(), ServiceArchiver.getResourceString("wizard.codegen.success.msg.heading"), message);
    }
}
