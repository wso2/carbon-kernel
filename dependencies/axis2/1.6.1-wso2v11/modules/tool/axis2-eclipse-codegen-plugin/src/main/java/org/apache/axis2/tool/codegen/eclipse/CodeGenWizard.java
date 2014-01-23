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

package org.apache.axis2.tool.codegen.eclipse;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.tool.codegen.WSDL2JavaGenerator;
import org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis2.tool.codegen.eclipse.ui.AbstractWizardPage;
import org.apache.axis2.tool.codegen.eclipse.ui.JavaSourceSelectionPage;
import org.apache.axis2.tool.codegen.eclipse.ui.JavaWSDLOptionsPage;
import org.apache.axis2.tool.codegen.eclipse.ui.JavaWSDLOutputLocationPage;
import org.apache.axis2.tool.codegen.eclipse.ui.OptionsPage;
import org.apache.axis2.tool.codegen.eclipse.ui.OutputPage;
import org.apache.axis2.tool.codegen.eclipse.ui.ToolSelectionPage;
import org.apache.axis2.tool.codegen.eclipse.ui.WSDLFileSelectionPage;
import org.apache.axis2.tool.codegen.eclipse.util.SettingsConstants;
import org.apache.axis2.tool.codegen.eclipse.util.UIConstants;
import org.apache.axis2.tool.codegen.eclipse.util.WSDLPropertyReader;
import org.apache.axis2.tool.core.JarFileWriter;
import org.apache.axis2.tool.core.SrcCompiler;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import javax.wsdl.Definition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The main wizard for the codegen wizard
 */

public class CodeGenWizard extends Wizard implements INewWizard, Java2WSDLConstants {
    private ToolSelectionPage toolSelectionPage;
    
    private WSDLFileSelectionPage wsdlSelectionPage;

    private OptionsPage optionsPage;

    private OutputPage outputPage;

    private JavaWSDLOptionsPage java2wsdlOptionsPage;

    private JavaSourceSelectionPage javaSourceSelectionPage;

    private JavaWSDLOutputLocationPage java2wsdlOutputLocationPage;

    private int selectedWizardType = SettingsConstants.WSDL_2_JAVA_TYPE;//TODO change this

    private int selectedCodegenOptionType = SettingsConstants.CODEGEN_DEFAULT_TYPE;//TODO change this
    
    private static Log logger=LogFactory.getLog(CodeGenWizard.class);

   

    /**
     * Constructor for CodeGenWizard.
     */
    public CodeGenWizard() {
        super();
        setNeedsProgressMonitor(true);
        this
                .setWindowTitle(org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                        .getResourceString("general.name"));
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        toolSelectionPage = new ToolSelectionPage();
        addPage(toolSelectionPage);

        //add the wsdl2java wizard pages
        wsdlSelectionPage = new WSDLFileSelectionPage();
        addPage(wsdlSelectionPage);
        
        optionsPage = new OptionsPage();
        addPage(optionsPage);
        outputPage = new OutputPage();
        addPage(outputPage);

        //add java2wsdl wizard pages
        javaSourceSelectionPage = new JavaSourceSelectionPage();
        addPage(javaSourceSelectionPage);
        java2wsdlOptionsPage = new JavaWSDLOptionsPage();
        addPage(java2wsdlOptionsPage);
        java2wsdlOutputLocationPage = new JavaWSDLOutputLocationPage();
        addPage(java2wsdlOutputLocationPage);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
        IWizardPage[] pages = getPages();
        AbstractWizardPage wizardPage = null;
        for (int i = 0; i < pages.length; i++) {
            wizardPage = (AbstractWizardPage) pages[i];
            if (wizardPage.getPageType() == this.selectedWizardType) {
                if (!(wizardPage.isPageComplete()))
                    return false;
            }
        }
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        AbstractWizardPage currentPage = (AbstractWizardPage) page;
        AbstractWizardPage pageout = (AbstractWizardPage) super
                .getNextPage(page);

        while (pageout != null && selectedWizardType != pageout.getPageType()) {
            AbstractWizardPage temp = pageout;
            pageout = (AbstractWizardPage) super.getNextPage(currentPage);
            currentPage = temp;
        }
        return pageout;
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        try {
            switch (selectedWizardType) {
            case SettingsConstants.WSDL_2_JAVA_TYPE:
                doFinishWSDL2Java();
                break;
            case SettingsConstants.JAVA_2_WSDL_TYPE:
                doFinishJava2WSDL();
                break;
            case SettingsConstants.UNSPECIFIED_TYPE:
                break; //Do nothing
            default:
                throw new RuntimeException(CodegenWizardPlugin.
                		getResourceString("general.invalid.state"));
            }
        } catch (Exception e) {
            MessageDialog.openError(getShell(), 
                    CodegenWizardPlugin.getResourceString("general.Error"), 
                    CodegenWizardPlugin.getResourceString("general.Error.prefix") +
                    e.getMessage());
            return false;
        }
        MessageDialog.openInformation(this.getShell(), 
                 CodegenWizardPlugin
                .getResourceString("general.name"), CodegenWizardPlugin
                .getResourceString("wizard.success"));
        return true;
    }

    /**
     * The worker method, generates the code itself.
     */
    private void doFinishWSDL2Java() {
        WorkspaceModifyOperation op = new WorkspaceModifyOperation()
        {
           protected void execute(IProgressMonitor monitor)
           throws CoreException, InvocationTargetException, InterruptedException{
              if (monitor == null){
                 monitor = new NullProgressMonitor();
              }

              /*
               * "3" is the total amount of steps, see below monitor.worked(amount)
               */
              monitor.beginTask(CodegenWizardPlugin.getResourceString("generator.generating"), 3);

              try
              {
                 /*
                  * TODO: Introduce a progress monitor interface for CodeGenerationEngine.
                  * Since this monitor here doesn't make much sense, we
                  * should either remove the progress monitor from the CodeGenWizard,
                  * or give a (custom) progress monitor to the generate() method, so
                  * we will be informed by Axis2 about the progress of code generation.  
                  */
                 WSDL2JavaGenerator generator = new WSDL2JavaGenerator(); 
                 monitor.subTask(CodegenWizardPlugin.getResourceString("generator.readingWOM"));
                 AxisService service = generator.getAxisService(wsdlSelectionPage.getFileName());
                 monitor.worked(1);
                 
                 //The generate all fix (Axis2-1862)
                 boolean isServerside,isServiceXML,isGenerateServerSideInterface  = false;
                 if (optionsPage.getGenerateAll()){
                         isServerside = true;
                         isServiceXML = true;
                         isGenerateServerSideInterface = true;
                 }else{
                         isServerside = optionsPage.isServerside();
                         isServiceXML =optionsPage.isServerXML();
                         isGenerateServerSideInterface = optionsPage.getGenerateServerSideInterface();
                 }
                 Map optionsMap = generator.fillOptionMap(optionsPage.isAsyncOnlyOn(),
                                                                                                optionsPage.isSyncOnlyOn(),
                                                                                                isServerside,
                                                                                                isServiceXML,
                                                                                                optionsPage.isGenerateTestCase(),
                                                                                                optionsPage.getGenerateAll(),
                                                                                                optionsPage.getServiceName(),
                                                                                                optionsPage.getPortName(),
                                                                                                optionsPage.getDatabinderName(),
                                                                                                wsdlSelectionPage.getFileName(),
                                                                                                optionsPage.getPackageName(),
                                                                                                optionsPage.getSelectedLanguage(),
                                                                                                outputPage.getOutputLocation(),
                                                                                                optionsPage.getNs2PkgMapping(),
                                                                                                isGenerateServerSideInterface,
                                                                                                optionsPage.getAdvanceOptions());

                 //Fix for the CodeGenConfiguration Contructor Change
                 //CodeGenConfiguration codegenConfig = new CodeGenConfiguration(service, optionsMap);
                 CodeGenConfiguration codegenConfig = new CodeGenConfiguration(optionsMap);
                 codegenConfig.addAxisService(service);
                 
                 //set the wsdl definision for codegen config for skeleton generarion.
                 WSDLPropertyReader reader = new WSDLPropertyReader();
                 reader.readWSDL(wsdlSelectionPage.getFileName());
                 Definition wsdlDefinition = reader.getWsdlDefinition();
                 codegenConfig.setWsdlDefinition(wsdlDefinition);
                 
                 //set the baseURI
                 codegenConfig.setBaseURI(generator.getBaseUri(wsdlSelectionPage.getFileName()));
                 monitor.worked(1);
                 
                 monitor.subTask(CodegenWizardPlugin.getResourceString("generator.generating"));
                 
                 new CodeGenerationEngine(codegenConfig).generate();
                 
                 //TODO refresh the eclipse project space to show the generated files
                 
                 //Add the codegen libs that are coming with the plugin to the project lib that has been created
                 if (outputPage.getAxis2PluginLibCopyCheckBoxSelection()){ 
                	 String eclipseHome = System.getProperty("user.dir");
                	 String pluginLibLocation = eclipseHome+File.separator+UIConstants.PLUGINS+
											File.separator+UIConstants.AXIS_CODEGEN_PLUGIN_FOLDER+
											File.separator+UIConstants.LIB;
                	 addLibsToProjectLib(pluginLibLocation, outputPage.getOutputLocation());
                 }
                 
                 //Also another requirement arises 
                 //If the codegen project was newly buided project or else the eclipse
                 //project intended to save this generated code does not have the required libs
                 //to compile the generated code. We need to add the relevent libs to a lib directory 
                 //of the <code>outputPage.getOutputLocation()</code>
                 
                 //Add the libraries on the plugin lib directory to the created project lib
                 if (outputPage.getAxisLibCopyCheckBoxSelection() && outputPage.oktoLoadLibs()){
//                	 String libDirectory = outputPage.getAxisHomeLocation()+File.separator+
//                	 					   UIConstants.TARGET+File.separator+UIConstants.LIB;
                	 String libDirectory = outputPage.getAxisJarsLocation();
                	 addLibsToProjectLib(libDirectory, outputPage.getOutputLocation());
                 }
                 
                 //This will Create a jar file from the codegen results and add to the output 
                 //locations lib directory
                 if (outputPage.getCreateJarCheckBoxSelection()){
                	 IWorkspace workspace = ResourcesPlugin.getWorkspace();
                	 String tempCodegenLocation =  workspace.getRoot().getLocation().toString()+File.separator+"codegen";
                	 String tempProjectSrcLocation = tempCodegenLocation+File.separator+"codegen_temp_src_"+
                	 								 System.currentTimeMillis();
                	 String tempProjectClassLocation = tempCodegenLocation+File.separator+"codegen_temp_class_"+
                	 							       System.currentTimeMillis();
                	 File tempCodegenFile = new File(tempCodegenLocation);
                	 File tempSrcFile = new File(tempProjectSrcLocation);
                	 File tempClassFile = new File(tempProjectClassLocation);
                	 tempCodegenFile.mkdir();
                	 tempSrcFile.mkdir();
                	 tempClassFile.mkdir();
                	 copyDirectory(new File(outputPage.getOutputLocation()), tempSrcFile);
                	 //Compile the source to another directory 
                	 SrcCompiler srcCompileTool = new SrcCompiler();
                	 srcCompileTool.compileSource(tempClassFile, tempProjectSrcLocation);
                	 //create the jar file and add that to the lib directory
                	 String projectLib = outputPage.getOutputLocation()+File.separator+"lib";
                	 JarFileWriter jarFileWriter = new JarFileWriter();
                	 String jarFileName = "CodegenResults.jar";
                	 if (!outputPage.getJarFilename().equals("")){
                		 jarFileName=outputPage.getJarFilename();
                	 }
                	 outputPage.setJarFileName(jarFileName);
                	 jarFileWriter.writeJarFile(new File(projectLib), jarFileName, tempClassFile);
                	 
                	 //Delete the temp folders
                	 deleteDir(tempCodegenFile);

                 }
                 
                 
                 monitor.worked(1);
              }
              catch (Exception e)
              {
                 ///////////////////////////////
            	  e.printStackTrace();
            	 ///////////////////////////// 
                 throw new InterruptedException(e.getMessage());
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
        }
        catch (InvocationTargetException e1)
        {
        	/////////////////////////
        	e1.printStackTrace();
        	////////////////////////
            throw new RuntimeException(e1);
        }
        catch (InterruptedException e1)
        {
           throw new RuntimeException(e1);
        }
        catch (Exception e)
        {
           throw new RuntimeException(e);
        }


    }

    private void doFinishJava2WSDL() throws Exception {

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor) {
                if (monitor == null){
                    monitor = new NullProgressMonitor();
                }

                /*
                 * "2" is the total amount of steps, see below
                 * monitor.worked(amount)
                 */
                monitor.beginTask(CodegenWizardPlugin
                        .getResourceString("generator.generating"), 3);

                try {
                    monitor.worked(1);
                    //fill the option map
                    Map optionsMap = new HashMap();
                    Java2WSDLCommandLineOption option = new Java2WSDLCommandLineOption(
                    		CLASSNAME_OPTION,new String[]{javaSourceSelectionPage.getClassName()});
                    optionsMap.put(CLASSNAME_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		CLASSPATH_OPTION,javaSourceSelectionPage.getClassPathList());
                    optionsMap.put(CLASSPATH_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		TARGET_NAMESPACE_OPTION,
                    		new String[]{java2wsdlOptionsPage.getTargetNamespace()});
                    optionsMap.put(TARGET_NAMESPACE_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		TARGET_NAMESPACE_PREFIX_OPTION,
                    		new String[]{java2wsdlOptionsPage.getTargetNamespacePrefix()});
                    optionsMap.put(TARGET_NAMESPACE_PREFIX_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		SCHEMA_TARGET_NAMESPACE_OPTION,
                    		new String[]{java2wsdlOptionsPage.getSchemaTargetNamespace()});
                    optionsMap.put(SCHEMA_TARGET_NAMESPACE_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		SERVICE_NAME_OPTION,new String[]{java2wsdlOptionsPage.getServiceName()});
                    optionsMap.put(SERVICE_NAME_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                    		new String[]{java2wsdlOptionsPage.getSchemaTargetNamespacePrefix()});
                    optionsMap.put(SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		OUTPUT_LOCATION_OPTION,new String[]{java2wsdlOutputLocationPage.getOutputLocation()});
                    optionsMap.put(OUTPUT_LOCATION_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		OUTPUT_FILENAME_OPTION,new String[]{java2wsdlOutputLocationPage.getOutputWSDLName()});
                    optionsMap.put(OUTPUT_FILENAME_OPTION,option);
                    
                    monitor.worked(1);
                    
                    new Java2WSDLCodegenEngine(optionsMap).generate();
                    
                    monitor.worked(1);
                    
                    
                } catch (Throwable e) {
                		e.printStackTrace();
                	    throw new RuntimeException(e);
                }

                monitor.done();
            }
        };

        try {
            getContainer().run(false, true, op);
        } catch (InvocationTargetException e1) {
            throw new RuntimeException(e1);
        } catch (InterruptedException e1) {
            throw new RuntimeException(CodegenWizardPlugin.
            		getResourceString("general.useraborted.state"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * We will accept the selection in the workbench to see if we can initialize
     * from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        //do nothing
    }

    /**
     * @return Returns the selectedWizardType.
     */
    public int getSelectedWizardType() {
        return selectedWizardType;
    }    

    /**
     * @param selectedWizardType
     *            The selectedWizardType to set.
     */
    public void setSelectedWizardType(int selectedWizardType) {
        this.selectedWizardType = selectedWizardType;
    }
    
    /**
     * @return Returns the codegenOptionType.
     */
    public int getSelectedCodegenOptionType() {
        return selectedCodegenOptionType;
    }
    
    /**
     * @param selectedCodegenOptionType
     *            The selectedCodegenOptionType to set.
     */
    public void setSelectedCodegenOptionType(int selectedCodegenOptionType) {
        this.selectedCodegenOptionType = selectedCodegenOptionType;
    }

    /**
     * Get the selected WSDL from the WSDLselectionpage
     * @return
     */
    public String getWSDLname(){
        return wsdlSelectionPage.getFileName();	
    }
    
    /**
     * populate the options page. Usually done after reloading the WSDL
     *
     */
    public void populateOptions(){
    	optionsPage.populateParamsFromWSDL();
    }
    
    public void setDefaultNamespaces(String fullyQualifiedClassName){
    	java2wsdlOptionsPage.setNamespaceDefaults(fullyQualifiedClassName);
    }
    
    
    private void addLibsToProjectLib(String libDirectory, String outputLocation){
    	String newOutputLocation = outputLocation+File.separator+UIConstants.LIB;
    	//Create a lib directory; all ancestor directories must exist
    	new File(newOutputLocation).mkdir();
        try {
			copyDirectory(new File(libDirectory),new File(newOutputLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // Copies all files under srcDir to dstDir.
    // If dstDir does not exist, it will be created.
    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
    
            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                                     new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }
        
    // Copies src file to dst file.
    // If the dst file does not exist, it is created
    private void copyFile(File src, File dst) throws IOException {
    	InputStream in = null;
    	OutputStream out = null;
    	try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
   
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
			    out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			logger.error("Error while copying the files",e);
			throw new IOException();
		}finally{
			try {
				in.close();
				out.close();
			} catch (Exception e) {
				logger.debug("Failed to close the streams",e);
			}
		}
    }
    
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
    
}
