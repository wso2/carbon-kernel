package org.apache.axis2.jaxws.addressbook;

/**
 * The JAX-WS Service Endpoint Interface (SEI).  
 * 
 * NOTE: The SEI is NOT USED in this example.  The service implementation publishing all public
 *       methods as an implicit SEI. 
 */

public interface AddressBook {
    
    public void addEntry(String firstName, String lastName, String phone, String street, String city, String state);
    
    public AddressBookEntry findByLastName(String lastName);
}
