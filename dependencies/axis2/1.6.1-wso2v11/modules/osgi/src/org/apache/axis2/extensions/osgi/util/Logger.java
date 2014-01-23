package org.apache.axis2.extensions.osgi.util;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Logger extends ServiceTracker {
    public Logger(BundleContext context){
        super(context, LogService.class.getName(), null);
        open();
    }
    
    public LogService getLogService(){
        return (LogService)this.getService();
    }

    public void log(int i, java.lang.String s) {
        LogService service = getLogService();
        if(service != null){
            service.log(i, s);
            //return;
        }
        print(i, s);
    }

    private void print(int i, String s) {
        switch(i){
                case LogService.LOG_ERROR: 
                System.out.print("[ERROR]   ");
                break;
            case LogService.LOG_INFO: 
                System.out.print("[INFO]    ");
                break;
            case LogService.LOG_WARNING: 
                System.out.print("[WARNING]");
                break;
            case LogService.LOG_DEBUG: 
                System.out.print("[DEBUG]   ");
                break;
        }
        System.out.println(" : " + s);
    }

    public void log(int i, java.lang.String s, java.lang.Throwable throwable) {
        LogService service = getLogService();
        if(service != null){
            service.log(i, s, throwable);
            //return;
        }
        print(i, s);
        throwable.printStackTrace(System.out);
    }
}
