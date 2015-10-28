package org.wso2.carbon.kernel.internal.transports;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

import java.util.Dictionary;

public class DummyInterpreter implements CommandInterpreter {
    private String[] transportIdList;
    private int counter;

    public void setTransportIdList(String[] list) {
        transportIdList = list;
        counter = 0;
    }

    public void resetCounter() {
        counter = 0;
    }

    public void setTransportIdListValuesToNull() {
        transportIdList = new String[2];
        transportIdList[0] = null;
        transportIdList[1] = null;
    }

    public void setTransportIdListValuesToEmptyString() {
        transportIdList = new String[2];
        transportIdList[0] = "";
        transportIdList[1] = "";
    }


    @Override
    public String nextArgument() {
        String id = transportIdList[counter];
        counter++;
        return id;
    }

    @Override
    public Object execute(String s) {
        return null;
    }

    @Override
    public void print(Object o) {

    }

    @Override
    public void println() {

    }

    @Override
    public void println(Object o) {

    }

    @Override
    public void printStackTrace(Throwable throwable) {

    }

    @Override
    public void printDictionary(Dictionary<?, ?> dictionary, String s) {

    }

    @Override
    public void printBundleResource(Bundle bundle, String s) {

    }
}
