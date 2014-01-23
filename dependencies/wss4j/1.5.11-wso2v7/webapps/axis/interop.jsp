<html>
<%@ page language="java" import="org.apache.ws.axis.oasis.ping.*, javax.xml.rpc.holders.StringHolder" %>

<% 
	String context = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/services" ;
%>
<head><title>WSS4J Interop Client</title></head>
<body>
<big><strong>WSS4J Interop Client</strong></big><p>
If you have problems using this self-help page. Please send an email to <A href="mailto:dims@yahoo.com">Davanum Srinivas</a>
<p/>
<dl>
<dt><strong>Interop Scenario 1</strong></dt>
<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="1">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping1' /></td></tr>

<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 1'></td></tr>
</form></table></dd>
<dt><strong>Interop Scenario 2</strong></dt>
<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="2">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping2' /></td></tr>
<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 2'></td></tr>
</form></table></dd>
<dt><strong>Interop Scenario 3</strong></dt>
<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="3">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping3' /></td></tr>

<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 3'></td></tr>
</form></table></dd>
<dt><strong>Interop Scenario 4</strong></dt>
<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="4">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping4' /></td></tr>
<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 4'></td></tr>
</form></table></dd>
<dt><strong>Interop Scenario 5</strong></dt>
<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="5">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping5' /></td></tr>
<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 5'></td></tr>
</form></table></dd>
<dt><strong>Interop Scenario 6</strong></dt>

<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="6">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping6' /></td></tr>
<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 6'></td></tr>
</form></table></dd>
<dt><strong>Interop Scenario 7</strong></dt>
<dd><table border='0'><form method='POST' action='wss4j2.jsp'><INPUT NAME="scenario" TYPE=HIDDEN VALUE="7">
<tr><td colspan='2'>URL: <input type='text' size='64' name='url' value='<%= context %>/Ping7' /></td></tr>
<tr><td>&nbsp;</td><td align='right'><input type='submit' value='Interop 7'></td></tr>
</form></table></dd>
</dl>
<p/>        <font size="-1">

         <em>Copyright &#169; 1999-2004, The Apache Software Foundation</em>
        </font>

</body>
</html>