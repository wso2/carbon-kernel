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

package org.apache.axis2.json.adb;

import org.apache.axis2.json.adb.xsd.Address;
import org.apache.axis2.json.adb.xsd.Book;
import org.apache.axis2.json.adb.xsd.Library;
import org.apache.axis2.json.adb.xsd.Person;

/**
 * LibraryServiceSkeleton java skeleton for the axisService
 */
public class LibraryServiceSkeleton {


    /**
     * @param echoLibrary
     * @return echoLibraryResponse
     */
    public EchoLibraryResponse echoLibrary(EchoLibrary echoLibrary) {

        EchoLibraryResponse response = new EchoLibraryResponse();
        response.set_return(echoLibrary.getArgs0());
        return response;
    }

    /**
     * @param getLibrary
     * @return getLibraryResponse
     */
    public GetLibraryResponse getLibrary(GetLibrary getLibrary) {
        String name = getLibrary.getArgs0();

        Library library = new Library();
        library.setAdmin(getPerson(name));
        library.setBooks(getBooks("Jhon", 5));
        library.setStaff(50);

        GetLibraryResponse response = new GetLibraryResponse();
        response.set_return(library);
        return response;
    }

    private Book[] getBooks(String name, int size) {
        Book[] books = new Book[size];
        Book book;
        for (int i = 0; i < size; i++) {
            book = new Book();
            book.setAuthor(name + "_" + i);
            book.setNumOfPages(175);
            book.setPublisher("Foxier");
            book.setReviewers(new String[]{"rev1", "rev2", "rev3"});
            books[i] = book;
        }
        return books;
    }

    private Person getPerson(String name) {
        Person person = new Person();
        person.setName(name);
        person.setAge(24);
        person.setAddress(getAddress());
        person.setPhone(12345);
        return person;
    }

    private Address getAddress() {
        Address address = new Address();
        address.setCountry("My Country");
        address.setCity("My City");
        address.setStreet("My Street");
        address.setZipCode("00000");
        return address;
    }
}
