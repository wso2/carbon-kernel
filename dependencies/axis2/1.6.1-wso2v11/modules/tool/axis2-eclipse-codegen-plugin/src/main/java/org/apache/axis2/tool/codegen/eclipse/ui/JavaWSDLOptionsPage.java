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

package org.apache.axis2.tool.codegen.eclipse.ui;

import org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis2.tool.codegen.eclipse.util.NamespaceFinder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/*
 * Usage java2wsdl -cn <fully qualified class name> : class file name -o <output
 * Location> : output file location -cp <class path uri> : list of classpath
 * entries - (urls) -tn <target namespace> : target namespace -tp <target
 * namespace prefix> : target namespace prefix -stn <schema target namespace> :
 * target namespace for schema -stp <schema target namespace prefix> : target
 * namespace prefix for schema -sn <service name> : service name -of <output
 * file name> : output file name for the WSDL
 */
public class JavaWSDLOptionsPage extends AbstractWizardPage {

	private Text targetNamespaceText;

	private Text targetNamespacePrefixText;

	private Text schemaTargetNamepaceText;

	private Text schemaTargetNamespacePrefixText;

	private Text serviceNameText;

	// TODO need more here

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.tool.codegen.eclipse.ui.AbstractWizardPage#initializeDefaultSettings()
	 */
	protected void initializeDefaultSettings() {
		settings.put(PREF_JAVA_TARGET_NS,"" );
		settings.put(PREF_JAVA_TARGET_NS_PREF, "");
		settings.put(PREF_JAVA_SCHEMA_TARGET_NS, "");
		settings.put(PREF_JAVA_SCHEMA_TARGET_NS_PREF, "");
		settings.put(PREF_JAVA_SERVICE_NAME, "");

	}

	
	public void setNamespaceDefaults(String fullyQualifiedClassName){
		
		targetNamespaceText.setText(NamespaceFinder.getTargetNamespaceFromClass(fullyQualifiedClassName));
		schemaTargetNamepaceText.setText(NamespaceFinder.getSchemaTargetNamespaceFromClass(fullyQualifiedClassName));
		
		targetNamespacePrefixText.setText(NamespaceFinder.getDefaultNamespacePrefix());
		schemaTargetNamespacePrefixText.setText(NamespaceFinder.getDefaultSchemaNamespacePrefix());
		
//		serviceNameText.setText(fullyQualifiedClassName.replaceAll("\\.","_"));
		serviceNameText.setText(NamespaceFinder.getServiceNameText(fullyQualifiedClassName));
	}
	/**
	 * @param pageName
	 */
	public JavaWSDLOptionsPage() {
		super("page5");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.tool.codegen.eclipse.ui.CodegenPage#getPageType()
	 */
	public int getPageType() {
		return JAVA_2_WSDL_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Label label = new Label(container, SWT.NULL);
		label.setText(CodegenWizardPlugin
				.getResourceString("page5.targetNamespace.label"));

		targetNamespaceText = new Text(container, SWT.BORDER | SWT.SINGLE);
		targetNamespaceText.setLayoutData(gd);
		targetNamespaceText.setText(settings.get(PREF_JAVA_TARGET_NS));
		targetNamespaceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				settings
						.put(PREF_JAVA_TARGET_NS, targetNamespaceText.getText());
				// dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(CodegenWizardPlugin
				.getResourceString("page5.targetNamespacePrefix.label"));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		targetNamespacePrefixText = new Text(container, SWT.BORDER);
		targetNamespacePrefixText.setLayoutData(gd);
		targetNamespacePrefixText.setText(settings
				.get(PREF_JAVA_TARGET_NS_PREF));
		targetNamespacePrefixText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				settings.put(PREF_JAVA_TARGET_NS_PREF,
						targetNamespacePrefixText.getText());
				// dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(CodegenWizardPlugin
				.getResourceString("page5.schemaTargetNs.label"));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		schemaTargetNamepaceText = new Text(container, SWT.BORDER);
		schemaTargetNamepaceText.setLayoutData(gd);
		schemaTargetNamepaceText.setText(settings
				.get(PREF_JAVA_SCHEMA_TARGET_NS_PREF));
		schemaTargetNamepaceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				settings.put(PREF_JAVA_SCHEMA_TARGET_NS_PREF,
						schemaTargetNamepaceText.getText());
				// dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(CodegenWizardPlugin
				.getResourceString("page5.schemaTargetNsPrefix.label"));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		schemaTargetNamespacePrefixText = new Text(container, SWT.BORDER);
		schemaTargetNamespacePrefixText.setLayoutData(gd);
		schemaTargetNamespacePrefixText.setText(settings
				.get(PREF_JAVA_SCHEMA_TARGET_NS));
		schemaTargetNamespacePrefixText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				settings.put(PREF_JAVA_SCHEMA_TARGET_NS,
						schemaTargetNamespacePrefixText.getText());
				// dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(CodegenWizardPlugin
				.getResourceString("page5.serviceName.label"));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		serviceNameText = new Text(container, SWT.BORDER);
		serviceNameText.setLayoutData(gd);
		serviceNameText.setText(settings
				.get(PREF_JAVA_SCHEMA_TARGET_NS));
		serviceNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				settings.put(PREF_JAVA_SERVICE_NAME,
						serviceNameText.getText());
				dialogChanged();
			}
		});

		setControl(container);
		

	}

	public String getTargetNamespace() {
		return this.targetNamespaceText.getText();
	}

	public String getTargetNamespacePrefix() {
		return this.targetNamespacePrefixText.getText();
	}

	public String getSchemaTargetNamespace() {
		return this.schemaTargetNamepaceText.getText();
	}

	public String getSchemaTargetNamespacePrefix() {
		return this.schemaTargetNamespacePrefixText.getText();
	}

	public String getServiceName() {
		return this.serviceNameText.getText();
	}

    /**
     * Handle the dialog change event. Basically evaluates the file name and
     * sets the error message accordingly
     */
    private void dialogChanged() {
        String fileName = getServiceName();

        if (fileName.length() == 0) {
            updateStatus(CodegenWizardPlugin
                    .getResourceString("page5.error.filemissingerror"));
            return;
	}
        // update the status
        updateStatus(null);
    }

}
