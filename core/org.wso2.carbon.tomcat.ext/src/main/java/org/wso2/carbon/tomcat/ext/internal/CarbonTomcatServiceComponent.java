package org.wso2.carbon.tomcat.ext.internal;


import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.tomcat.CarbonTomcatException;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.transport.ServletTransportManager;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;

import java.io.File;
import java.nio.file.Paths;

@Component(name = "tomcat.service.comp", immediate = true)
public class CarbonTomcatServiceComponent {
    private static Log log = LogFactory.getLog(CarbonTomcatServiceComponent.class);
    private String webContextRoot;
    private Context carbonContext;

    /**
     * Exposing the {@link org.osgi.service.http.HttpService} by registering the proxy servlet with tomcat.
     *
     * @param componentContext injected by OSGi DS framework
     */
    @Activate
    protected void activate(ComponentContext componentContext) {
        CarbonTomcatService carbonTomcatService = CarbonTomcatServiceHolder.getCarbonTomcatService();
        ServerConfigurationService serverConfigurationService = CarbonTomcatServiceHolder.getServerConfigurationService();
        //Starting transport manager
        ServletTransportManager.init();
        //adding the delegation servlet to the tomcat instance.
        Tomcat tomcat = carbonTomcatService.getTomcat();
        //getting webContext from carbon.xml
        URLMappingHolder.getInstance().setDefaultHost(tomcat.getEngine().getDefaultHost());
        webContextRoot = serverConfigurationService.getFirstProperty("WebContextRoot");
        if (log.isDebugEnabled()) {
            log.debug("webContextRoot : " + webContextRoot);
        }
        String carbonConfigHome = System.getProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        String carbonWebAppDir;
        if (carbonConfigHome == null) {
            String carbonHome = System.getProperty(CarbonBaseConstants.CARBON_HOME);
            carbonWebAppDir = Paths.get(carbonHome, "repository", "conf", "tomcat", "carbon").toString();
        } else {
            carbonWebAppDir = Paths.get(carbonConfigHome, "tomcat", "carbon").toString();
        }
        /*acquiring the thread context classLoader, so that we can swap the default, threadContextClassLoader of
         tomcat transport listeners (web-app classLoader) during the service method invocation in {@link DelegationServlet}
         */
        CarbonTomcatServiceHolder.setTccl(Thread.currentThread().getContextClassLoader());
        try {
            this.carbonContext = carbonTomcatService.addWebApp(webContextRoot,carbonWebAppDir);
        } catch (CarbonTomcatException exception) {
            log.error("Error while adding the carbon web-app", exception);
        }

        // Add a dummy context "/t" to dispatch requests to tenant webapps when the tenant is not
        // loaded yet. This is needed in a situation where the webContextRoot is set other than "/".
        if (!"/".equals(webContextRoot)) {
            File tenantDummyCtxDir = Utils.createDummyTenantContextDir();
            if (tenantDummyCtxDir.exists()) {
                try {
                    carbonTomcatService.addWebApp("/t", tenantDummyCtxDir.getPath());
                } catch (CarbonTomcatException exception) {
                    log.error("Error while adding the dummy tenant context web-app", exception);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        try {
            carbonContext.stop();
            log.info("Stopping the carbon web-app registered under : " + webContextRoot);
            CarbonTomcatServiceHolder.setTccl(null);
        } catch (Exception exception) {
            log.error("Error while stopping carbon web-app", exception);
        }
    }

    @SuppressWarnings("unused")
    @Reference(name = "server.configuration.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
        unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfiguration) {
        CarbonTomcatServiceHolder.setServerConfigurationService(serverConfiguration);
    }

    @SuppressWarnings("unused")
    protected void unsetServerConfigurationService(ServerConfigurationService serverConfiguration) {
        CarbonTomcatServiceHolder.setServerConfigurationService(null);
    }

    @SuppressWarnings("unused")
    @Reference(name = "tomcat.service.provider", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
    unbind = "unsetCarbonTomcatService")
    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        CarbonTomcatServiceHolder.setCarbonTomcatService(carbonTomcatService);
    }

    @SuppressWarnings("unused")
    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        CarbonTomcatServiceHolder.setCarbonTomcatService(null);
    }

    /**
     * taken from the {@link org.apache.catalina.startup.Tomcat} class. It is defined a private in the Tomcat class
     */
    private static final String[] DEFAULT_MIME_MAPPINGS = {
            "abs", "audio/x-mpeg",
            "ai", "application/postscript",
            "aif", "audio/x-aiff",
            "aifc", "audio/x-aiff",
            "aiff", "audio/x-aiff",
            "aim", "application/x-aim",
            "art", "image/x-jg",
            "asf", "video/x-ms-asf",
            "asx", "video/x-ms-asf",
            "au", "audio/basic",
            "avi", "video/x-msvideo",
            "avx", "video/x-rad-screenplay",
            "bcpio", "application/x-bcpio",
            "bin", "application/octet-stream",
            "bmp", "image/bmp",
            "body", "text/html",
            "cdf", "application/x-cdf",
            "cer", "application/x-x509-ca-cert",
            "class", "application/java",
            "cpio", "application/x-cpio",
            "csh", "application/x-csh",
            "css", "text/css",
            "dib", "image/bmp",
            "doc", "application/msword",
            "dtd", "application/xml-dtd",
            "dv", "video/x-dv",
            "dvi", "application/x-dvi",
            "eps", "application/postscript",
            "etx", "text/x-setext",
            "exe", "application/octet-stream",
            "gif", "image/gif",
            "gtar", "application/x-gtar",
            "gz", "application/x-gzip",
            "hdf", "application/x-hdf",
            "hqx", "application/mac-binhex40",
            "htc", "text/x-component",
            "htm", "text/html",
            "html", "text/html",
            "hqx", "application/mac-binhex40",
            "ief", "image/ief",
            "jad", "text/vnd.sun.j2me.app-descriptor",
            "jar", "application/java-archive",
            "java", "text/plain",
            "jnlp", "application/x-java-jnlp-file",
            "jpe", "image/jpeg",
            "jpeg", "image/jpeg",
            "jpg", "image/jpeg",
            "js", "text/javascript",
            "jsf", "text/plain",
            "jspf", "text/plain",
            "kar", "audio/x-midi",
            "latex", "application/x-latex",
            "m3u", "audio/x-mpegurl",
            "mac", "image/x-macpaint",
            "man", "application/x-troff-man",
            "mathml", "application/mathml+xml",
            "me", "application/x-troff-me",
            "mid", "audio/x-midi",
            "midi", "audio/x-midi",
            "mif", "application/x-mif",
            "mov", "video/quicktime",
            "movie", "video/x-sgi-movie",
            "mp1", "audio/x-mpeg",
            "mp2", "audio/x-mpeg",
            "mp3", "audio/x-mpeg",
            "mp4", "video/mp4",
            "mpa", "audio/x-mpeg",
            "mpe", "video/mpeg",
            "mpeg", "video/mpeg",
            "mpega", "audio/x-mpeg",
            "mpg", "video/mpeg",
            "mpv2", "video/mpeg2",
            "ms", "application/x-wais-source",
            "nc", "application/x-netcdf",
            "oda", "application/oda",
            "odb", "application/vnd.oasis.opendocument.database",
            "odc", "application/vnd.oasis.opendocument.chart",
            "odf", "application/vnd.oasis.opendocument.formula",
            "odg", "application/vnd.oasis.opendocument.graphics",
            "odi", "application/vnd.oasis.opendocument.image",
            "odm", "application/vnd.oasis.opendocument.text-master",
            "odp", "application/vnd.oasis.opendocument.presentation",
            "ods", "application/vnd.oasis.opendocument.spreadsheet",
            "odt", "application/vnd.oasis.opendocument.text",
            "otg", "application/vnd.oasis.opendocument.graphics-template",
            "oth", "application/vnd.oasis.opendocument.text-web",
            "otp", "application/vnd.oasis.opendocument.presentation-template",
            "ots", "application/vnd.oasis.opendocument.spreadsheet-template ",
            "ott", "application/vnd.oasis.opendocument.text-template",
            "ogx", "application/ogg",
            "ogv", "video/ogg",
            "oga", "audio/ogg",
            "ogg", "audio/ogg",
            "spx", "audio/ogg",
            "faca", "audio/flac",
            "anx", "application/annodex",
            "axa", "audio/annodex",
            "axv", "video/annodex",
            "xspf", "application/xspf+xml",
            "pbm", "image/x-portable-bitmap",
            "pct", "image/pict",
            "pdf", "application/pdf",
            "pgm", "image/x-portable-graymap",
            "pic", "image/pict",
            "pict", "image/pict",
            "pls", "audio/x-scpls",
            "png", "image/png",
            "pnm", "image/x-portable-anymap",
            "pnt", "image/x-macpaint",
            "ppm", "image/x-portable-pixmap",
            "ppt", "application/vnd.ms-powerpoint",
            "pps", "application/vnd.ms-powerpoint",
            "ps", "application/postscript",
            "psd", "image/x-photoshop",
            "qt", "video/quicktime",
            "qti", "image/x-quicktime",
            "qtif", "image/x-quicktime",
            "ras", "image/x-cmu-raster",
            "rdf", "application/rdf+xml",
            "rgb", "image/x-rgb",
            "rm", "application/vnd.rn-realmedia",
            "roff", "application/x-troff",
            "rtf", "application/rtf",
            "rtx", "text/richtext",
            "sh", "application/x-sh",
            "shar", "application/x-shar",
            /*"shtml", "text/x-server-parsed-html",*/
            "smf", "audio/x-midi",
            "sit", "application/x-stuffit",
            "snd", "audio/basic",
            "src", "application/x-wais-source",
            "sv4cpio", "application/x-sv4cpio",
            "sv4crc", "application/x-sv4crc",
            "svg", "image/svg+xml",
            "svgz", "image/svg+xml",
            "swf", "application/x-shockwave-flash",
            "t", "application/x-troff",
            "tar", "application/x-tar",
            "tcl", "application/x-tcl",
            "tex", "application/x-tex",
            "texi", "application/x-texinfo",
            "texinfo", "application/x-texinfo",
            "tif", "image/tiff",
            "tiff", "image/tiff",
            "tr", "application/x-troff",
            "tsv", "text/tab-separated-values",
            "txt", "text/plain",
            "ulw", "audio/basic",
            "ustar", "application/x-ustar",
            "vxml", "application/voicexml+xml",
            "xbm", "image/x-xbitmap",
            "xht", "application/xhtml+xml",
            "xhtml", "application/xhtml+xml",
            "xls", "application/vnd.ms-excel",
            "xml", "application/xml",
            "xpm", "image/x-xpixmap",
            "xsl", "application/xml",
            "xslt", "application/xslt+xml",
            "xul", "application/vnd.mozilla.xul+xml",
            "xwd", "image/x-xwindowdump",
            "vsd", "application/x-visio",
            "wav", "audio/x-wav",
            "wbmp", "image/vnd.wap.wbmp",
            "wml", "text/vnd.wap.wml",
            "wmlc", "application/vnd.wap.wmlc",
            "wmls", "text/vnd.wap.wmlscript",
            "wmlscriptc", "application/vnd.wap.wmlscriptc",
            "wmv", "video/x-ms-wmv",
            "wrl", "x-world/x-vrml",
            "wspolicy", "application/wspolicy+xml",
            "Z", "application/x-compress",
            "z", "application/x-compress",
            "zip", "application/zip"
    };
}
