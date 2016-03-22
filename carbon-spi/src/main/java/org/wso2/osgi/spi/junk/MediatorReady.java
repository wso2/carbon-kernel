package org.wso2.osgi.spi.junk;

public class MediatorReady implements Ready {

    public void mediatorReady() {
        System.out.println("The Mediator Service Used");
    }
}
