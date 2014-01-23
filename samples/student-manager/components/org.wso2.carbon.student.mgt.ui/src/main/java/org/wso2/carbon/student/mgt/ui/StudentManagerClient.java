package org.wso2.carbon.student.mgt.ui;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.student.mgt.data.xsd.Student;
import java.lang.Exception;


public class StudentManagerClient {
	
	private StudentManagerStub stub;
	
	public StudentManagerClient(ConfigurationContext configCtx, String backendServerURL, String cookie) throws Exception{
		String serviceURL = backendServerURL + "StudentManager";
        stub = new StudentManagerStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}
	
	public void addStudent(Student student) throws Exception{
		try{
			stub.addStudent(student);
		}catch (RemoteException e) {
            String msg = "Cannot add the student"
                + " . Backend service may be unvailable";
            throw new Exception(msg, e);
		}
	}
	
	public Student[] getStudents() throws Exception{
		try{
			return stub.getStudents();
		}catch (RemoteException e) {
            String msg = "Cannot get the list of students"
                + " . Backend service may be unvailable";
            throw new Exception(msg, e);
		}
	}	
}
