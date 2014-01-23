package org.apache.axis2.jaxws.addressbook;

import javax.jws.WebService;

/**
 * JAX-WS service implementation that uses and implicit SEI rather than an explicit SEI.  An
 * implicit SEI means that there is no @WebService.endpointInterface element specified.  This means
 * that all public methods on the service implementation comprise an implicit SEI.
 */

// Simply adding the @WebService annotation makes this a JAX-WS service implementation.
@WebService
public class AddressBookImpl  {

    public String addEntry(String firstName, String lastName, String phone, String street, String city, String state) {
        System.out.println("AddressBookImpl.addEntry");
        AddressBookEntry entry = new AddressBookEntry();
        entry.setFirstName(firstName);
        entry.setLastName(lastName);
        entry.setPhone(phone);
        entry.setStreet(street);
        entry.setCity(city);
        entry.setState(state);
        return "AddEntry Completed!";
    }

    public AddressBookEntry findByLastName(String lastName) {
        System.out.println("AddressBookImpl.findByLastName");
        AddressBookEntry entry = new AddressBookEntry(); 
        entry.setFirstName("firstName");
        entry.setLastName("lastName");
        entry.setPhone("phone");
        entry.setStreet("street");
        entry.setCity("city");
        entry.setState("state");
        System.out.println("AddressBookImpl.findByLastName returning " + entry);
        return entry;
    }

}
