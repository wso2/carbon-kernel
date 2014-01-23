package org.apache.axis2.tool.codegen.eclipse.ui;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.axis2.util.CommandLineOptionConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class WSDLJavaAdvanceDialog extends Dialog {

	private Button tmpCheckBox; 
//	private Text tmpTextBox;
	private HashMap advanceOptions;
	private boolean isEditAdvanceOptions=false;
	private Combo wsdlVersionCombo;
	private Button packageRemoveButton;
	private Table packageNameList;
	private Button packageAddButton;
	private Text addNewExcludePackageName;

	protected WSDLJavaAdvanceDialog(Shell shell, HashMap advanceOptions) {
		super(shell);
		isEditAdvanceOptions=(advanceOptions!=null);
		if (isEditAdvanceOptions){
			this.advanceOptions=(HashMap) advanceOptions.clone();
		}else{
			this.advanceOptions=new HashMap();
		}
	}

	private Button addCheckBox(Composite container,
			final Button tmpCheckBox,String caption,final String parameterType){
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		//tmpCheckBox = new Button(container, SWT.CHECK);
		tmpCheckBox.setLayoutData(gd);
		tmpCheckBox.setText(caption);
		if (isEditAdvanceOptions){
			tmpCheckBox.setSelection(advanceOptions.containsKey(parameterType));
		}
		tmpCheckBox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				setCheckBoxState(parameterType, tmpCheckBox.getSelection());
			}
		});
		return tmpCheckBox;
	}
	
	private Text addTextBox(Composite container,Label lblCaption, 
			final Text tmpTextBox, String caption,final String parameterType,boolean isBrowseFolder){
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint=200;
		lblCaption.setLayoutData(gd);
		lblCaption.setText(caption);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (isBrowseFolder){
			gd.horizontalSpan = 1;
		}else{
			gd.horizontalSpan = 2;
		}
		tmpTextBox.setLayoutData(gd);
		if (isEditAdvanceOptions){
			if (advanceOptions.containsKey(parameterType)){
				tmpTextBox.setText(((String[])advanceOptions.get(parameterType))[0]);
			}
		}
		tmpTextBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setTextBoxValue(parameterType, tmpTextBox.getText());
			}
		});
		if (isBrowseFolder){
			Button browse=new Button(container,SWT.NULL);
			browse.setText("Browse");
			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog dialog = new DirectoryDialog(getShell());
					String returnString = dialog.open();
					if (returnString != null) {
						tmpTextBox.setText(returnString);
					}
				}
			});
		}
		return tmpTextBox;
	}
	
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		//FontUtil fontUtil = FontUtil.getInstance(container.getDisplay());

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 10;

		GridData gd;
		
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Specify a repository against which code is generated.", 
				CommandLineOptionConstants.WSDL2JavaConstants.REPOSITORY_PATH_OPTION,true);
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Specify a directory path for generated source", 
				CommandLineOptionConstants.WSDL2JavaConstants.SOURCE_FOLDER_NAME_OPTION,true);
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Specify a directory path for generated resources", 
				CommandLineOptionConstants.WSDL2JavaConstants.RESOURCE_FOLDER_OPTION,true);
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Proxy host address if you are behind a firewall", 
				CommandLineOptionConstants.WSDL2JavaConstants.HTTP_PROXY_HOST_OPTION_LONG,false);
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Proxy port address if you are behind a firewall", 
				CommandLineOptionConstants.WSDL2JavaConstants.HTTP_PROXY_PORT_OPTION_LONG,false);
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Skelton interface name", 
				CommandLineOptionConstants.WSDL2JavaConstants.SKELTON_INTERFACE_NAME_OPTION,false);
		addTextBox(container,new Label(container,SWT.NULL), new Text(container,SWT.BORDER), 
				"Skelton class name", 
				CommandLineOptionConstants.WSDL2JavaConstants.SKELTON_CLASS_NAME_OPTION,false);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		Label fillLabel = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		fillLabel.setLayoutData(gd);
		
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Unpacks the databinding classes", 
				CommandLineOptionConstants.WSDL2JavaConstants.UNPACK_CLASSES_OPTION);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Flattens the generated files", 
				CommandLineOptionConstants.WSDL2JavaConstants.FLATTEN_FILES_OPTION);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Switch on un-wrapping", 
				CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Generate code for all ports", 
				CommandLineOptionConstants.WSDL2JavaConstants.All_PORTS_OPTION);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Overwrite the existing classes", 
				CommandLineOptionConstants.WSDL2JavaConstants.OVERRIDE_OPTION);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Generate Axis 1.x backword compatible code", 
				CommandLineOptionConstants.WSDL2JavaConstants.BACKWORD_COMPATIBILITY_OPTION);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Suppress namespace prefixes (Optimzation that reduces size of soap request/response)", 
				CommandLineOptionConstants.WSDL2JavaConstants.SUPPRESS_PREFIXES_OPTION);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Dont generate the build.xml in the output directory", 
				CommandLineOptionConstants.WSDL2JavaConstants.NO_BUILD_XML_OPTION_LONG);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Dont generate WSDLs in the resources directory", 
				CommandLineOptionConstants.WSDL2JavaConstants.NO_WSDLS_OPTION_LONG);
		tmpCheckBox=new Button(container, SWT.CHECK);addCheckBox(container,tmpCheckBox, 
				"Dont generate a MessageReceiver in the generated sources", 
				CommandLineOptionConstants.WSDL2JavaConstants.NO_MESSAGE_RECEIVER_OPTION_LONG);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fillLabel = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		fillLabel.setLayoutData(gd);
	
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint=400;
		Label fillLabel1=new Label(container,SWT.NULL);
		fillLabel1.setLayoutData(gd);
		fillLabel1.setText("WSDL version");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		wsdlVersionCombo=new Combo(container,SWT.DROP_DOWN | SWT.BORDER| SWT.READ_ONLY);
		wsdlVersionCombo.setLayoutData(gd);
		fillWSDLVersionCombo();
		String key=CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_OPTION;
		if ((isEditAdvanceOptions) && (advanceOptions.containsKey(key))){
			wsdlVersionCombo.select(wsdlVersionCombo.indexOf(((String[])advanceOptions.get(key))[0]));
		}
		
		wsdlVersionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handleWsdlVersionComboSelected();

			};

			public void widgetDefaultSelected(SelectionEvent e) {
			};
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fillLabel = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		fillLabel.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		Label labelPackageAddCaption = new Label(container, SWT.NULL);
		labelPackageAddCaption.setLayoutData(gd);
		labelPackageAddCaption.setText("Excludes Packages");

		addNewExcludePackageName=new Text(container,SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		addNewExcludePackageName.setLayoutData(gd);
		addNewExcludePackageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleNewPackageNameChange();
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		packageAddButton= new Button(container, SWT.NULL);
		packageAddButton.setText("Add");
		packageAddButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				addPackageName();
			}
		});
		packageAddButton.setLayoutData(gd);

		packageNameList = new Table(container,
				SWT.SINGLE|SWT.FULL_SELECTION|SWT.V_SCROLL|SWT.H_SCROLL);
		packageNameList.setLinesVisible(true);
		packageNameList.setHeaderVisible(true); 
		packageNameList.setLayoutData(gd);
		TableColumn column = new TableColumn(packageNameList,SWT.NONE);
		column.setWidth(500);
		column.setText("Excluded packages");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.heightHint = 80;
		packageNameList.setLayoutData(gd);
		packageNameList.setVisible(false);
		if (isEditAdvanceOptions){
			updateExcludePackageList();
		}
		packageNameList.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				handlePackageNameListClick();
			}
		});
//		tableOutputMaps.addMouseListener(new MouseAdapter(){
//			public void mouseDoubleClick(MouseEvent e){
//				//handleOutputMapEditQuery();
//			}
//		});
		packageNameList.redraw();
		packageNameList.setVisible(true);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		Label fillLabel4 = new Label(container, SWT.NULL);
		fillLabel4.setLayoutData(gd);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		packageRemoveButton= new Button(container, SWT.NULL);
		packageRemoveButton.setText("Remove");
		packageRemoveButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				removePackageName();
			}
		});
		packageRemoveButton.setLayoutData(gd);
		
		handlePackageNameListClick();
		handleNewPackageNameChange();
		
		return super.createDialogArea(parent);
	}
	
	private void handlePackageNameListClick(){
		packageRemoveButton.setEnabled(packageNameList.getSelectionCount()>0);
	}
	
	private void removePackageName(){
		if (packageNameList.getSelectionIndex()!=-1){
			String type=CommandLineOptionConstants.WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION;
			String[] commaSeperatedPackageList=(String[]) advanceOptions.get(type);
			String[] packageList=commaSeperatedPackageList[0].split(",");
			String newList="";
			int selectedIndex=packageNameList.getSelectionIndex();
			String toRemove=packageNameList.getItem(selectedIndex).getText();
			for (String s : packageList) {
				if (!s.equalsIgnoreCase(toRemove)){
					if (newList.equalsIgnoreCase("")){
						newList=s;
					}else{
						newList=newList+","+s;
					}
				}
			}
			if (newList.equalsIgnoreCase("")){
				advanceOptions.remove(type);
			}else{
				advanceOptions.put(type, new String[]{newList});
			}
			packageNameList.remove(selectedIndex);
			if (selectedIndex>=packageNameList.getItemCount()){
				selectedIndex--;
			}
			packageNameList.select(selectedIndex);
		}
		packageNameList.redraw();
		handlePackageNameListClick();
	}
	
	private void addPackageName(){
		String newList;
		String type=CommandLineOptionConstants.WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION;
		if (packageNameList.getItemCount()>0){
			String[] commaSeperatedPackageList=(String[]) advanceOptions.get(type);
			newList=commaSeperatedPackageList[0]+","+addNewExcludePackageName.getText();
		}else
			newList=addNewExcludePackageName.getText();
		advanceOptions.put(type,new String[]{newList});
		addTableItem(addNewExcludePackageName.getText());
		addNewExcludePackageName.setText("");
		handleNewPackageNameChange();
	}
	
	private void updateExcludePackageList(){
		packageNameList.clearAll();
		String type=CommandLineOptionConstants.WSDL2JavaConstants.EXCLUDE_PAKAGES_OPTION;
		if (advanceOptions.containsKey(type)){
			String[] commaSeperatedPackageList=(String[]) advanceOptions.get(type);
			String[] packageList=commaSeperatedPackageList[0].split(",");
			for (String string : packageList) {
				addTableItem(string);
			}
		}
	}
	
	private void addTableItem(String itemText){
		TableItem item=new TableItem(packageNameList,SWT.None);
		item.setText(itemText);
	}
	
	private void handleNewPackageNameChange(){
		Pattern p = Pattern.compile("^[a-zA-Z_\\$][\\w\\$]*(?:\\.[a-zA-Z_\\$][\\w\\$]*)*$");
        packageAddButton.setEnabled(p.matcher(addNewExcludePackageName.getText()).matches());
	}
	
	private void handleWsdlVersionComboSelected(){
		advanceOptions.put(CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_OPTION, 
				new String[]{wsdlVersionCombo.getItem(wsdlVersionCombo.getSelectionIndex())});
	}
	
	private void fillWSDLVersionCombo(){
		wsdlVersionCombo.removeAll();
		wsdlVersionCombo.add("1.1");
		wsdlVersionCombo.add("2.0");
	}
	private void setCheckBoxState(String type,boolean state){
		if (state){
			advanceOptions.put(type, null);
		}else{
			advanceOptions.remove(type);
		}
	}
	
	private void setTextBoxValue(String type,String value){
		if (value.equalsIgnoreCase("")){
			advanceOptions.remove(type);
		}else{
			advanceOptions.put(type, new String[]{value});
		}
	}

	public HashMap getAdvanceOptions(){
		return advanceOptions;
	}
}
