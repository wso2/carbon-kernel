<html>
<%@ page language="java" import="org.apache.ws.axis.oasis.ping.*, javax.xml.rpc.holders.StringHolder" %>

<% 
	String url = request.getParameter ("url");
	String scenario = request.getParameter ("scenario").trim();
%>
<head><title>WSS4J Interop Scenario <%= scenario %> Results</title></head>
<body>
<big><strong>WSS4J Interop Scenario <%= scenario %> Results</strong></big><p>
<strong>Server URL:</strong> <%= url %><p>

<%
        PingServiceLocator service = new PingServiceLocator();
        java.net.URL endpoint = new java.net.URL(url);
        PingPort port = null;
        if(scenario.equals("1"))
             port = (PingPort) service.getPing1(endpoint);
        else if(scenario.equals("2"))
             port = (PingPort) service.getPing2(endpoint);
        else if(scenario.equals("3"))
             port = (PingPort) service.getPing3(endpoint);
        else if(scenario.equals("4"))
             port = (PingPort) service.getPing4(endpoint);
        else if(scenario.equals("5"))
             port = (PingPort) service.getPing5(endpoint);
        else if(scenario.equals("6"))
             port = (PingPort) service.getPing6(endpoint);
        else if(scenario.equals("7"))
             port = (PingPort) service.getPing7(endpoint);

        StringHolder text =
                new StringHolder("WSS4J - Scenario " + scenario + " @ [" + new java.util.Date(System.currentTimeMillis()) + "]");
%>
<strong>Request:</strong> <%= text.value %><p>

<%

        port.ping(new org.apache.ws.axis.oasis.ping.TicketType("WSS4J" + scenario), text);
        System.out.println(text.value);

%>
<strong>Response:</strong> <%= text.value %><p>

<p/>
<font size="-1">
<A HREF="wss4j.html">Go Back</A>
</font>
<p/>        <font size="-1">

         <em>Copyright &#169; 1999-2004, The Apache Software Foundation</em>
        </font>


</body>
</html>