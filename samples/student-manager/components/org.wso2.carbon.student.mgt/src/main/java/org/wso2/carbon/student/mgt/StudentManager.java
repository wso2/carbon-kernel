package org.wso2.carbon.student.mgt;

import java.util.Map;
import java.util.HashMap;

import org.wso2.carbon.student.mgt.data.Student;

public class StudentManager {
	
	private Map<String, Student> studentMap;
	
	public StudentManager(){
		studentMap = new HashMap<String, Student>();
		
		Student std1 = new Student();
        std1.setNICNumber("876123746v");
		std1.setfirstName("John");
		std1.setLastName("Carter");
		std1.setAge(25);

		Student std2 = new Student();
        std2.setNICNumber("846123746v");        
		std2.setfirstName("Tom");
		std2.setLastName("Hanks");
		std2.setAge(28);

		studentMap.put(std1.getNICNumber(), std1);
		studentMap.put(std2.getNICNumber(), std2);
	}

    /**
     * Adds the student information
     * @param student Instance of the Student bean class which contains information
     * @throws Exception if an invalid input is provided
     */
	public void addStudent(Student student) throws Exception{
		if(student == null || student.getNICNumber() == null){
			throw new Exception("Invalid Student");
		}
		studentMap.put(student.getNICNumber(), student);
	}

    /**
     * Delete the student having the give NIC number from the student store
     * @param nicNumber Student NIC number
     * @throws Exception, if an invalid NIC number is given.
     */
    public void deleteStudent(String nicNumber) throws Exception{
        if( nicNumber == null || studentMap.get(nicNumber) == null) {
            throw new Exception("Invalid NIC Number");
        }

        studentMap.remove(nicNumber);
    }

    /**
     * Returns an array of Student instances.
     * @return Student array.
     */
	public Student[] getStudents(){
		Student[] students = new Student[studentMap.size()];
        studentMap.values().toArray(students);
		return students;
	}
}
