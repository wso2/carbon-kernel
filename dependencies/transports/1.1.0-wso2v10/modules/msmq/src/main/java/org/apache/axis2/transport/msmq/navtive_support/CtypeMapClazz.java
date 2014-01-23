package org.apache.axis2.transport.msmq.navtive_support;

import java.util.Map;

import org.apache.axis2.transport.msmq.MSMQConstants;

public class CtypeMapClazz {
	private static Map<String, Long> cTypeMap = new java.util.concurrent.ConcurrentHashMap<String, Long>(100);
	static {
		cTypeMap.put("application/xml", (long)1);
		cTypeMap.put("text/html", (long)2);
		cTypeMap.put("application/json", (long)3);
		cTypeMap.put("application/json/badgerfish", (long)4);
		cTypeMap.put("application/x-www-form-urlencoded", (long)5);
		cTypeMap.put("application/soap+xml", (long)6);
		cTypeMap.put("x-application/hessian", (long)7);
		cTypeMap.put("text/javascript", (long)8);
		cTypeMap.put("text/plain", (long)9);
		cTypeMap.put(MSMQConstants.DEFAULT_CONTENT_TYPE,(long)10);
		//TODO:add all supported content types here..
	}
	
	
    public static Long getIdByName(String ctypeName){
    	 Long ctype = cTypeMap.get(ctypeName);
		 return ctype !=null?ctype:cTypeMap.get(MSMQConstants.DEFAULT_CONTENT_TYPE);
	}
    
    public static String getCtypeNameById(Long ctyPe){
    	if(ctyPe == null){
    		return MSMQConstants.DEFAULT_CONTENT_TYPE;
    	}
    	for(Map.Entry<String, Long> entry : cTypeMap.entrySet()){
    		if(entry.getValue().intValue() == ctyPe.intValue()){
    			return entry.getKey();
    		}
    	}
    	
    	return MSMQConstants.DEFAULT_CONTENT_TYPE;
    }
}
