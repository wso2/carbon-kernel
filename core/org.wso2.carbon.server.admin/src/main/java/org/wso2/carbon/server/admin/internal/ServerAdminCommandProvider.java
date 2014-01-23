/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.server.admin.internal;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.poi.hssf.record.IterationRecord;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.server.admin.service.ServerAdmin;
import org.wso2.carbon.utils.deployment.Axis2ServiceRegistry;

import javax.management.relation.InvalidRelationTypeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 *Equinox console commands for Server Administration
 */
@SuppressWarnings("unused")
public class ServerAdminCommandProvider implements CommandProvider {

    private ServerAdmin serverAdmin;

    public ServerAdminCommandProvider() {
        this.serverAdmin = new ServerAdmin();
    }

    /**
     * Forcefully restart this Carbon instance
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while restarting
     */
    public void _restartCarbon(CommandInterpreter ci) throws Exception {
        serverAdmin.restart();
    }

    /**
     * Forcefully restart this Carbon instance
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while restarting
     */
    public void _rcbn(CommandInterpreter ci) throws Exception {
        _restartCarbon(ci);
    }

    /**
     * Forcefully shutdown this Carbon instance
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while shutting down
     */
    public void _shutdownCarbon(CommandInterpreter ci) throws Exception {
        serverAdmin.shutdown();
    }

    /**
     * Forcefully shutdown this Carbon instance
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while shutting down
     */
    public void _scbn(CommandInterpreter ci) throws Exception {
        _shutdownCarbon(ci);
    }

    /**
     * Gracefully restart this Carbon instance.
     * All client connections will be served before restarting the server
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while restarting
     */
    public void _restartCarbonGracefully(CommandInterpreter ci) throws Exception {
        serverAdmin.restartGracefully();
    }

    /**
     * Gracefully restart this Carbon instance.
     * All client connections will be served before restarting the server
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while restarting
     */
    public void _rcbng(CommandInterpreter ci) throws Exception {
        _restartCarbonGracefully(ci);
    }

    /**
     * Gracefully shutdown this Carbon instance
     * All client connections will be served before shutting down the server
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while shutting down
     */
    public void _shutdownCarbonGracefully(CommandInterpreter ci) throws Exception {
        serverAdmin.shutdownGracefully();
    }

    /**
     * Gracefully shutdown this Carbon instance
     * All client connections will be served before shutting down the server
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurs while shutting down
     */
    public void _scbng(CommandInterpreter ci) throws Exception {
        _shutdownCarbonGracefully(ci);
    }

    /**
     * Method to switch a node to maintenance mode.
     * <p/>
     * Here is the sequence of events:
     * <p/>
     * <oll>
     * <li>Client calls this method</li>
     * <li>The server stops accepting new requests/connections, but continues to stay alive so
     * that old requests & connections can be served</li>
     * <li>Once all requests have been processed, the method returns</li
     * </ol>
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurred while switching to maintenace mode
     */
    public void _startCarbonMaintenance(CommandInterpreter ci) throws Exception {
         serverAdmin.startMaintenance();
    }

    /**
     * Method to switch a node to maintenance mode.
     * <p/>
     * Here is the sequence of events:
     * <p/>
     * <oll>
     * <li>Client calls this method</li>
     * <li>The server stops accepting new requests/connections, but continues to stay alive so
     * that old requests & connections can be served</li>
     * <li>Once all requests have been processed, the method returns</li
     * </ol>
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurred while switching to maintenace mode
     */
    public void _scbnm(CommandInterpreter ci) throws Exception {
        _startCarbonMaintenance(ci);
    }

    /**
     * Method to change the state of a node from "maintenance" to "normal"
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurred while switching to normal mode
     */
    public void _endCarbonMaintenance(CommandInterpreter ci) throws Exception {
        serverAdmin.endMaintenance();
    }

    /**
     * Method to change the state of a node from "maintenance" to "normal"
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurred while switching to normal mode
     */
    public void _ecbnm(CommandInterpreter ci) throws Exception {
        _endCarbonMaintenance(ci);
    }

    /**
     * List Admin Services deployed on this server
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurred while listing admin services
     */
    public void _listAdminServices(CommandInterpreter ci) throws Exception {
        HashMap<String,AxisService> services =
                ServerAdminDataHolder.getInstance().getConfigContext().
                        getAxisConfiguration().getServices();
        System.out.println("Admin services deployed on this server:");
        int i = 1;
        for (AxisService axisService : services.values()) {
            if(SystemFilter.isAdminService(axisService)) {
                i = printServiceSummary(i, axisService);
            }
        }
    }

    public void _listSystemServicesInfo(CommandInterpreter ci) throws Exception {
        HashMap<String, AxisService> services =
                ServerAdminDataHolder.getInstance().getConfigContext().
                        getAxisConfiguration().getServices();
        System.out.println("\nAll System services deployed on this server.\n");
        AxisService[] axisServicesArray = services.values().toArray(new AxisService[services.size()]);

        for (int i = 0; i < axisServicesArray.length; i++) {
            AxisService axisService = axisServicesArray[i];
            if (SystemFilter.isAdminService(axisService) || SystemFilter.isHiddenService(axisService)) {
                printServiceDetails(i + 1, axisService);

                for (Iterator itr = axisService.getOperations(); itr.hasNext(); ) {
                    AxisOperation axisOperation = (AxisOperation) itr.next();
                    printOperationDetails(axisOperation);
                }
            }
            System.out.println();
        }
    }


    public void _dumpAdminServices(CommandInterpreter ci) throws Exception {
        HashMap<String,AxisService> services =
                ServerAdminDataHolder.getInstance().getConfigContext().
                        getAxisConfiguration().getServices();
        int i = 1;
        String adminServicesDir = System.getProperty("java.io.tmpdir") + File.separator + "adminServices";
        File file = new File(adminServicesDir);
        if(!file.exists() && !file.mkdirs()){
            throw new Exception("Cannot create admin service dump");
        }
        for (AxisService axisService : services.values()) {
            if (SystemFilter.isAdminService(axisService)) {
                OutputStream op = null;
                try {
                    File wsdl = new File(adminServicesDir + File.separator + axisService.getName() + ".wsdl");
                    op = new FileOutputStream(wsdl);
                    axisService.printWSDL(op);
                } finally {
                    if(op != null){
                        op.close();
                    }
                }
            }
        }
        System.out.println("Admin service info dump created at " + file.getAbsolutePath());
    }

    /**
     * List Hidden Services deployed on this server
     *
     * @param ci CommandInterpreter
     * @throws Exception If an error occurred while listing hidden services
     */
    public void _listHiddenServices(CommandInterpreter ci) throws Exception {
        HashMap<String,AxisService> services =
                ServerAdminDataHolder.getInstance().getConfigContext().
                        getAxisConfiguration().getServices();
        System.out.println("Hidden services deployed on this server:");
        int i = 1;
        for (AxisService axisService : services.values()) {
            i = printServiceSummary(i, axisService);
        }
    }

    private int printServiceSummary(int i, AxisService axisService) {
        System.out.print(i + ". " + axisService.getName() + ", " + axisService.getDocumentation() + ", ");
        for(String epr : axisService.getEPRs()){
            System.out.print(epr + " ");
        }
        System.out.println();
        i++;
        return i;
    }

    private void printServiceDetails(int i, AxisService axisService) {
        System.out.print(i + ". " + axisService.getName() + ", ");

        if (SystemFilter.isAdminService(axisService)) {
            System.out.print("AdminService, ");
        }

        if (SystemFilter.isHiddenService(axisService)) {
            System.out.print("HiddenService, ");
        }

        for (String epr : axisService.getEPRs()) {
            System.out.print(epr);
        }

        Parameter param = axisService.getParameter("AuthorizationAction");
        if (param != null) {
            System.out.println(", Permission - " + param.getValue().toString().trim());
        } else {
            System.out.println();
        }
    }

    private void printOperationDetails(AxisOperation axisOperation) {
        System.out.print("\t" + axisOperation.getName() + "()");

        Parameter param = axisOperation.getParameter("AuthorizationAction");
        if (param != null) {
            System.out.println(", Permission - " + param.getValue().toString().trim());
        } else {
            System.out.println();
        }
    }

    public String getHelp() {
        return "---Server Admin (WSO2 Carbon)---\n" +
                "\tlistAdminServices - List admin services deployed on this Carbon instance\n" +
                "\tlistHiddenServices - List hidden services deployed on this Carbon instance\n" +
                "\tlistSystemServicesInfo - List all the systems services deployed on this Carbon instance\n" +
                "\tdumpAdminServices - Dump all the wsdls of admin services on this Carbon instance\n" +
                "\trestartCarbon - Forcefully restart this Carbon instance\n" +
                "\trestartCarbonGracefully - Gracefully restart this Carbon instance." +
                " All client connections will be served before restarting the server\n" +
                "\tshutdownCarbon - Forcefully shutdown this Carbon instance\n" +
                "\tshutdownCarbonGracefully - Gracefully shutdown this Carbon instance." +
                " All client connections will be served before shutting down the server\n" +
                "\tstartCarbonMaintenance - Switch a Carbon instance to maintenance mode.\n" +
                "\tendCarbonMaintenance - Change the state of a Carbon instance from \"maintenance\" to \"normal\"\n";
    }
}
