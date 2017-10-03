package org.wso2.carbon.tomcat.ext.valves;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jayanga on 7/16/14.
 */
public class TomcatValveContainerTest extends TestCase {
    static final Logger log = Logger.getLogger("TomcatValveContainerTest");

    private TomcatValveContainer instance;
    ArrayList<CarbonTomcatValve> tomcatContainerValves;

    public TomcatValveContainerTest() {
        instance = new TomcatValveContainer();
        Class c = instance.getClass();
        try {
            Field filed = c.getDeclaredField("valves");
            filed.setAccessible(true);
            tomcatContainerValves = ((ArrayList<CarbonTomcatValve>)(filed.get(instance)));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<CarbonTomcatValve> getTomcatValveContainerValves() {
        return tomcatContainerValves;
    }

    private void cleanUpTomcatValveContainerValves() {
        tomcatContainerValves.clear();
    }

    private int getNextIndexFromTomcatValveContainerValves() {
        return tomcatContainerValves.size();
    }

    private class CustomTestValve extends CarbonTomcatValve {
        String code = " ";
        CustomTestValve(String code){
            this.code = code;
        }

        @Override
        public void invoke(Request request, Response response, CompositeValve compositeValve) {

            Object obj = request.getNote("TestValue");
            if (obj != null) {
                String temp = (String)obj;
                request.setNote("TestValue", temp += code);
            } else {
                request.setNote("TestValue", code);
            }
        }
    }

    private boolean checkLinksInTomcatValveContainerValves(int expectedCount){
        CarbonTomcatValve valve = tomcatContainerValves.get(0);

        int count = 0;
        while (valve != null) {
            count++;
            if (valve.getNext() instanceof CustomTestValve) {
                valve = valve.getNext();
            } else {
                valve = null;
            }
        }

        return (count == expectedCount);
    }

    private boolean checkLinksInTomcatValveContainerValves(String expectedString){
        CarbonTomcatValve valve = tomcatContainerValves.get(0);
        Request request = new Request();

        while (valve != null) {
            valve.invoke(request, null, null);
            if (valve.getNext() instanceof CustomTestValve) {
                valve = valve.getNext();
            } else {
                valve = null;
            }
        }

        String temp = (String)request.getNote("TestValue");
        log.info("[Actual=" + temp + ", Expected=" + expectedString + "]");

        return temp.equals(expectedString);
    }

    @Test
    public void testAddValues_WithNullValveList() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, null);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(true);
    }

    @Test
    public void testAddValues_WithEmptyValveList() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, valves);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(true);
    }

    @Test
    public void testAddValues_WithOneInTheValveListToIndexMinusOne() {
        cleanUpTomcatValveContainerValves();

        int index = -1;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());

        try {
            TomcatValveContainer.addValves(index, valves);
        } catch (IllegalArgumentException exception) {
            // This exception is expected when addValves is invoked with minus values.
        }

        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(true);
    }

    @Test
    public void testAddValues_WithOneInTheValveListToIndexOne() {
        cleanUpTomcatValveContainerValves();

        int index = 1;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());

        try {
            TomcatValveContainer.addValves(index, valves);
        } catch (IllegalArgumentException exception) {
            // This exception is expected when addValves is invoked with a positive int,
            // when there are no element in the list.
        }

        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(true);
    }

    @Test
    public void testAddValues_WithOneInTheValveListToIndexZero() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, valves);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(checkLinksInTomcatValveContainerValves(1));
        Assert.assertTrue(checkLinksInTomcatValveContainerValves("A"));
    }

    @Test
    public void testAddValues_WithTwoInTheValveListToIndexZero() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));
        valves.add(new CustomTestValve("B"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, valves);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(checkLinksInTomcatValveContainerValves(2));
        Assert.assertTrue(checkLinksInTomcatValveContainerValves("AB"));
    }

    @Test
    public void testAddValues_WithFewInTheValveListToIndexZero() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));
        valves.add(new CustomTestValve("B"));
        valves.add(new CustomTestValve("C"));
        valves.add(new CustomTestValve("D"));
        valves.add(new CustomTestValve("E"));
        valves.add(new CustomTestValve("F"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, valves);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(checkLinksInTomcatValveContainerValves(6));
        Assert.assertTrue(checkLinksInTomcatValveContainerValves("ABCDEF"));
    }

    @Test
    public void testAddValues_WithFewInTheValveListToAnExistingListToIndexZero() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));
        valves.add(new CustomTestValve("B"));
        valves.add(new CustomTestValve("C"));
        valves.add(new CustomTestValve("D"));

        TomcatValveContainer.addValves(index, valves);

        index = 0;
        List<CarbonTomcatValve> valves1 = new ArrayList<CarbonTomcatValve>();
        valves1.add(new CustomTestValve("E"));
        valves1.add(new CustomTestValve("F"));
        valves1.add(new CustomTestValve("G"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, valves1);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(checkLinksInTomcatValveContainerValves(7));
        Assert.assertTrue(checkLinksInTomcatValveContainerValves("EFGABCD"));
    }

    @Test
    public void testAddValues_WithFewInTheValveListToAnExistingListToIndex2() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));
        valves.add(new CustomTestValve("B"));
        valves.add(new CustomTestValve("C"));
        valves.add(new CustomTestValve("D"));

        TomcatValveContainer.addValves(index, valves);

        index = 2;
        List<CarbonTomcatValve> valves1 = new ArrayList<CarbonTomcatValve>();
        valves1.add(new CustomTestValve("E"));
        valves1.add(new CustomTestValve("F"));
        valves1.add(new CustomTestValve("G"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(index, valves1);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(checkLinksInTomcatValveContainerValves(7));
        Assert.assertTrue(checkLinksInTomcatValveContainerValves("ABEFGCD"));
    }

    @Test
    public void testAddValues_WithFewInTheValveListToAnExistingListToTheNextIndex() {
        cleanUpTomcatValveContainerValves();

        int index = 0;
        List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
        valves.add(new CustomTestValve("A"));
        valves.add(new CustomTestValve("B"));
        valves.add(new CustomTestValve("C"));
        valves.add(new CustomTestValve("D"));

        TomcatValveContainer.addValves(index, valves);

        List<CarbonTomcatValve> valves1 = new ArrayList<CarbonTomcatValve>();
        valves1.add(new CustomTestValve("E"));
        valves1.add(new CustomTestValve("F"));
        valves1.add(new CustomTestValve("G"));

        log.info("TomcatValveContainerValves size (before) = " + getTomcatValveContainerValves().size());
        TomcatValveContainer.addValves(getNextIndexFromTomcatValveContainerValves(), valves1);
        log.info("TomcatValveContainerValves size (after) = " + getTomcatValveContainerValves().size());

        Assert.assertTrue(checkLinksInTomcatValveContainerValves(getNextIndexFromTomcatValveContainerValves()));
        Assert.assertTrue(checkLinksInTomcatValveContainerValves("ABCDEFG"));
    }
}
