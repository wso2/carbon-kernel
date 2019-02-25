/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.ui.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.valves.Constants;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import javax.servlet.ServletException;

/**
 * Implementation of a Valve that outputs pluggable HTML error pages
 */
public class ErrorHandleValve extends ErrorReportValve {

    private boolean showReport = true;

    private boolean showServerInfo = true;

    private static String exceptionResponseHtml = null;

    private static final Log log = LogFactory.getLog(ErrorHandleValve.class);

    public ErrorHandleValve() {

        super();
    }

    /**
     * Prints out an error report.
     *
     * @param request   The request being processed
     * @param response  The response being generated
     * @param throwable The exception that occurred (which possibly wraps
     *                  a root cause exception
     */
    protected void report(Request request, Response response, Throwable throwable) {

        int statusCode = response.getStatus();

        // Do nothing on a 1xx, 2xx and 3xx status
        // Do nothing if anything has been written already
        // Do nothing if the response hasn't been explicitly marked as in error
        //    and that error has not been reported.
        if (statusCode < 400 || response.getContentWritten() > 0 || !response.setErrorReported()) {
            return;
        }
        String message = RequestUtil.filter(response.getMessage());
        if (message == null) {
            if (throwable != null) {
                String exceptionMessage = throwable.getMessage();
                if (exceptionMessage != null && exceptionMessage.length() > 0) {
                    message = RequestUtil.filter((new Scanner(exceptionMessage)).nextLine());
                }
            }
            if (message == null) {
                message = "";
            }
        }

        // Do nothing if there is no report for the specified status code and
        // no error message provided
        String report = null;
        StringManager smClient = StringManager.getManager(
                Constants.Package, request.getLocales());
        response.setLocale(smClient.getLocale());
        try {
            report = smClient.getString("http." + statusCode);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
        }
        if (report == null) {
            if (message.length() == 0) {
                return;
            } else {
                report = smClient.getString("errorReportValve.noDescription");
            }
        }

        try {
            try {
                response.setContentType("text/html");
                response.setCharacterEncoding("utf-8");
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
            }
            Writer writer = response.getReporter();
            if (writer != null) {
                // If writer is null, it's an indication that the response has
                // been hard committed already, which should never happen
                writer.write(getExceptionResponseHtml(throwable, statusCode, message, report, smClient));
                response.finishResponse();
            }
        } catch (IOException | IllegalStateException e) {
            // Ignore
            if (log.isDebugEnabled()) {
                log.debug("Error while writing the HTML response", e);
            }
        }

    }

    private String buildDefaultErrorResponse(Throwable throwable, int statusCode, String message, String report,
                                             StringManager smClient) {

        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>");
        if (showServerInfo || showReport) {
            sb.append("<title>");
            if (showServerInfo) {
                sb.append(ServerInfo.getServerInfo()).append(" - ");
            }
            sb.append(smClient.getString("errorReportValve.errorReport"));
            sb.append("</title>");
            sb.append("<style><!--");
            sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
            sb.append("--></style> ");
        } else {
            sb.append("<title>");
            sb.append(smClient.getString("errorReportValve.errorReport"));
            sb.append("</title>");
        }
        sb.append("</head><body>");
        sb.append("<h1>");
        sb.append(smClient.getString("errorReportValve.statusHeader",
                String.valueOf(statusCode), message)).append("</h1>");
        if (showReport) {
            sb.append("<HR size=\"1\" noshade=\"noshade\">");
            sb.append("<p><b>type</b> ");
            if (throwable != null) {
                sb.append(smClient.getString("errorReportValve.exceptionReport"));
            } else {
                sb.append(smClient.getString("errorReportValve.statusReport"));
            }
            sb.append("</p>");
            sb.append("<p><b>");
            sb.append(smClient.getString("errorReportValve.message"));
            sb.append("</b> <u>");
            sb.append(message).append("</u></p>");
            sb.append("<p><b>");
            sb.append(smClient.getString("errorReportValve.description"));
            sb.append("</b> <u>");
            sb.append(report);
            sb.append("</u></p>");
            if (throwable != null) {

                String stackTrace = getPartialServletStackTrace(throwable);
                sb.append("<p><b>");
                sb.append(smClient.getString("errorReportValve.exception"));
                sb.append("</b> <pre>");
                sb.append(RequestUtil.filter(stackTrace));
                sb.append("</pre></p>");

                int loops = 0;
                Throwable rootCause = throwable.getCause();
                while (rootCause != null && (loops < 10)) {
                    stackTrace = getPartialServletStackTrace(rootCause);
                    sb.append("<p><b>");
                    sb.append(smClient.getString("errorReportValve.rootCause"));
                    sb.append("</b> <pre>");
                    sb.append(RequestUtil.filter(stackTrace));
                    sb.append("</pre></p>");
                    // In case root cause is somehow heavily nested
                    rootCause = rootCause.getCause();
                    loops++;
                }

                sb.append("<p><b>");
                sb.append(smClient.getString("errorReportValve.note"));
                sb.append("</b> <u>");
                sb.append(smClient.getString("errorReportValve.rootCauseInLogs",
                        showServerInfo ? ServerInfo.getServerInfo() : ""));
                sb.append("</u></p>");

            }
            sb.append("<HR size=\"1\" noshade=\"noshade\">");
        }
        if (showServerInfo) {
            sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * Enables/Disables full error reports
     *
     * @param showReport
     */
    public void setShowReport(boolean showReport) {

        this.showReport = showReport;
    }

    public boolean isShowReport() {

        return showReport;
    }

    /**
     * Enables/Disables server info on error pages
     *
     * @param showServerInfo
     */
    public void setShowServerInfo(boolean showServerInfo) {

        this.showServerInfo = showServerInfo;
    }

    public boolean isShowServerInfo() {

        return showServerInfo;
    }

    private String getExceptionResponseHtml(Throwable throwable, int statusCode, String message, String report,
                                            StringManager smClient) {

        if (exceptionResponseHtml != null) {
            return exceptionResponseHtml;
        }
        String redirectHtmlPath = null;
        FileInputStream fis;
        try {
            redirectHtmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository"
                    + File.separator + "resources" + File.separator + "identity" + File.separator + "pages" +
                    File.separator + "server_error_response.html";
            fis = new FileInputStream(new File(redirectHtmlPath));
            exceptionResponseHtml = new Scanner(fis, "UTF-8").useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Showing default error page, " + "\'" + redirectHtmlPath + "\' not found");
            }
            exceptionResponseHtml = buildDefaultErrorResponse(throwable, statusCode, message, report, smClient);
        }
        return exceptionResponseHtml;
    }
}
