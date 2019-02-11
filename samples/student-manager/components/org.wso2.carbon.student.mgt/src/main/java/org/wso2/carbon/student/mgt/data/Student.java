package org.wso2.carbon.student.mgt.data;

public class Student {

    //National Identity Card Number
    //http://en.wikipedia.org/wiki/National_identification_number#Sri_Lanka
    private String nicNumber;
	private String fName;
	private String lName;
	private int age;

    public String getNICNumber(){
        return nicNumber;
    }

    public void setNICNumber(String nicNumber){
        this.nicNumber = nicNumber;
    }
	
	public String getFirstName(){
		return fName;
	}
	
	public void setfirstName(String fName){
		this.fName = fName;
	}
	
	public String getLastName(){
		return lName;
	}
	
	public void setLastName(String lName){
		this.lName = lName;
	}                   
	
	public int getAge(){
		return age;
	}
	
	public void setAge(int age){
		this.age = age;
	}
}
