/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.bootup.validator;

import com.jezhumble.javasysmon.JavaSysMon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.bootup.validator.util.UnknownParameterException;
import org.wso2.carbon.core.bootup.validator.util.ValidationResult;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class SystemValidator extends ConfigurationValidator {

    private static final Log log = LogFactory.getLog(SystemValidator.class);
    //to call MBean objects indirectly in JVM-independent manner
    private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private JavaSysMon sys = new JavaSysMon();

    public static final String CPU_PARAM = "CPU";
    public static final String RAM_PARAM = "RAM";
    public static final String SWAP_PARAM = "swap";
    public static final String MAX_FREE_DISK_PARAM = "freeDisk";
    public static final String OPEN_FILES_PARAM = "ulimit";
    public static final String CERT_FINGERPRINT_PARAM = "certFingerprint";
    public static final int MB_BASE = 1024 * 1024;
    public static final int MHz_BASE = 1000 * 1000;
    private static final String IBM_J9_VM = "IBM J9 VM";

    @Override
    public Map<String, ValidationResult> validate() {
        Map<String, String> recommendedConfigs = getRecommendedConfigurations();
        Map<String, ValidationResult> validationResults = new HashMap<String, ValidationResult>();
        for (String paramName : recommendedConfigs.keySet()) {
            try {
                ValidationResult result = validateConfiguration(paramName);
                validationResults.put(paramName, result);
            } catch (Exception e) {
            	log.warn("Could not validate the system for configuration parameter : " + paramName);
            	log.debug("Error occured while trying to validate configuration parameter : " +
            	 				          paramName, e);
            }
        }
        return validationResults;
    }

    protected ValidationResult validateConfiguration(String parameterName) throws
                                                                           MalformedObjectNameException,
                                                                           AttributeNotFoundException,
                                                                           InstanceNotFoundException,
                                                                           MBeanException,
                                                                           ReflectionException,
                                                                           CertificateException,
                                                                           NoSuchAlgorithmException,
                                                                           KeyStoreException,
                                                                           IOException {
        ValidationResult result;
        if (CPU_PARAM.equals(parameterName)) {
            String recommendedCpu = getRecommendedConfigurations().get(parameterName);
            long systemCpu = getCpuMhz();
            result = validateCPU(systemCpu, Long.parseLong(recommendedCpu));
        } else if (RAM_PARAM.equals(parameterName)) {
            String recommendedRAM = getRecommendedConfigurations().get(parameterName);
            long systemRAM = getRAM();
            result = validateRAM(systemRAM, Long.parseLong(recommendedRAM));
        } else if (SWAP_PARAM.equals(parameterName)) {
            String recommendedTotalSwap = getRecommendedConfigurations().get(parameterName);
            long systemTotalSwap = getSwap();
            result = validateSwap(systemTotalSwap, Long.parseLong(recommendedTotalSwap));
        } else if (OPEN_FILES_PARAM.equals(parameterName)) {
            //do not validate the ulimit if the running OS is Windows.
            if (isWindows()) {
                String windowsUlimitMsg = "Windows OS doesn't support ulimit query";
                result = new ValidationResult(true, windowsUlimitMsg);
            } else {
                String recommendedFDCount = getRecommendedConfigurations().get(parameterName);
                long systemFDCount = getOpenFilesLimit();
                result = validateOpenFilesLimit(systemFDCount, Long.parseLong(recommendedFDCount));
            }
        } else if (MAX_FREE_DISK_PARAM.equals(parameterName)) {
            String recommendedDiskSpace = getRecommendedConfigurations().get(parameterName);
            long systemDiskSpace = getMaxFreeDiskSpace();
            result = validateDiskSpace(systemDiskSpace, Long.parseLong(recommendedDiskSpace));
        } else if (CERT_FINGERPRINT_PARAM.equals(parameterName)) {
            String certFingerprint = getRecommendedConfigurations().get(parameterName);
            result = validateKeystoreFingerprint(certFingerprint);
        } else {
            throw new UnknownParameterException(" Unknown paramater :" + parameterName);
        }
        return result;
    }

    private ValidationResult validateRAM(long systemRam, long minReq) {
        ValidationResult result = new ValidationResult();
        String msg = null;
        boolean isValid;
        if (systemRam >= minReq) {
            isValid = true;
        } else {
            msg =
                    "RAM size (MB): " + systemRam +
                    " of the system is below the recommended minimum size :" + minReq;
            isValid = false;
        }
        result.setValidationMessage(msg);
        result.setValid(isValid);
        return result;
    }

    private ValidationResult validateSwap(long systemSwap, long minReq) {
        ValidationResult result = new ValidationResult();
        String msg = null;
        boolean isValid;
        if (systemSwap >= minReq) {
            isValid = true;
        } else {
            msg =
                    "Swap Memory size (MB): " + systemSwap +
                    " of the system is below the recommended minimum size :" + minReq;
            isValid = false;
        }
        result.setValidationMessage(msg);
        result.setValid(isValid);
        return result;
    }

    private ValidationResult validateCPU(long systemCPU, long minReq) {
        ValidationResult result = new ValidationResult();
        String msg = null;
        boolean isValid;
        if (systemCPU >= minReq) {
            isValid = true;
        } else {
            msg =
                    "CPU speed (MHz): " + systemCPU +
                    " of the system is below the recommended minimum speed :" + minReq;
            isValid = false;
        }
        result.setValidationMessage(msg);
        result.setValid(isValid);
        return result;
    }

    private ValidationResult validateOpenFilesLimit(long openFileLimit, long minLimit) {
        ValidationResult result = new ValidationResult();
        String msg = null;
        boolean isValid;
        if (openFileLimit >= minLimit) {
            isValid = true;
        } else {
            msg =
                    "Open files limit :" + openFileLimit +
                    " of the system is below the recommended minimum count :" + minLimit;
            isValid = false;
        }
        result.setValidationMessage(msg);
        result.setValid(isValid);
        return result;
    }

    private ValidationResult validateDiskSpace(long systemDisk, long minReq) {
        ValidationResult result = new ValidationResult();
        String msg = null;
        boolean isValid;
        if (systemDisk >= minReq) {
            isValid = true;
        } else {
            msg =
                    "Maximum free Disk Space (MB): " + systemDisk +
                    " of the system is below the recommended minimum size :" + minReq;
            isValid = false;
        }
        result.setValidationMessage(msg);
        result.setValid(isValid);
        return result;
    }

    /**
     * validate primary keystore with default keystore in the application
     * here we assume that if the primary keystore did not contain certificate with wso2carbon alias, customer has
     * modified the default wso2carbon keystore. (means customer using his own keystore)
     *
     * @return validated ValidationResult object
     * @throws CertificateException
     */
    private ValidationResult validateKeystoreFingerprint(String certFingerprint) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        ValidationResult result = new ValidationResult();
        String msg = null;
        boolean isValid;
        KeyStore primaryKeyStore = getPrimaryKeyStore();
        X509Certificate wso2CarbonCert = null;
        if (primaryKeyStore != null) {
            wso2CarbonCert = (X509Certificate) primaryKeyStore.getCertificate(RegistryResources.SecurityManagement.DEFAULT_SECURITY_CERTIFICATE_ALIAS);
        } else {
            log.error("Error loading primary keystore, cannot validate keystore");
        }

        if ((wso2CarbonCert != null) && getCertFingerprint(wso2CarbonCert).equalsIgnoreCase(certFingerprint)) {
            // this is the fault stage where the client use default wso2carbon keystore
            msg = "Carbon is configured to use the default keystore (wso2carbon.jks). To maximize security when deploying to a production environment, configure a new keystore with a unique password in the production server profile.";
            isValid = false;
        } else {
            // wso2carbon keystore not present (client has modified the keystore)
            isValid = true;
        }
        result.setValidationMessage(msg);
        result.setValid(isValid);
        return result;
    }

    /**
     * Load the primary key store
     *
     * @return primary key store object
     * @throws IOException
     * @throws CertificateException Permission denied for accessing security certificate
     */
    private KeyStore getPrimaryKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore primaryKeyStore;
        ServerConfigurationService config = ServerConfiguration.getInstance();
        String file =
                new File(config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE))
                        .getAbsolutePath();
        KeyStore store = KeyStore
                .getInstance(config
                        .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE));
        String password = config
                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            store.load(in, password.toCharArray());
            primaryKeyStore = store;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return primaryKeyStore;
    }

    /**
     * Generate the MD5 thumbprint of the certificate
     *
     * @param certificate that we need the thumbprint
     * @return MD5 thumbprint value
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    private String getCertFingerprint(X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
        MessageDigest digestValue = MessageDigest.getInstance("MD5");
        byte[] der = certificate.getEncoded();
        digestValue.update(der);
        byte[] digestInBytes = digestValue.digest();
        return hexify(digestInBytes);
    }

    /**
     * Helper method to hexify a byte array.
     * @param bytes
     * @return hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer(bytes.length * 2);

        // appending : marks to make fingerprint more readable
        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f] + ":");
        }
        // removing the last : value from the buffer string
        buf.deleteCharAt(buf.length()-1);
        return buf.toString();
    }

    /**
     * calculate the cpu as (cpu frequency in Mhz) * no.of cpu cores
     *
     * @return the aggregated cpu frequency
     */
    private long getCpuMhz() {
        return (sys.cpuFrequencyInHz() / MHz_BASE) * sys.numCpus();
    }

    /**
     * @return RAM size (MB)
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     */
    private long getRAM() throws MalformedObjectNameException, AttributeNotFoundException,
                                 InstanceNotFoundException, MBeanException, ReflectionException {
        Long ramValue = null;
        ObjectName osBean = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        if(IBM_J9_VM.equals(System.getProperty("java.vm.name"))){
            ramValue = (Long) mBeanServer.getAttribute(osBean, "TotalPhysicalMemory");
        } else{ // if not IBM JVM we handling as if it is the Oracle JVM : Other JVMs are not supported
            ramValue = (Long) mBeanServer.getAttribute(osBean, "TotalPhysicalMemorySize");
        }
        return ramValue / MB_BASE;
    }

    /**
     * @return swap space (MB) in the system
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     */
    private long getSwap() throws MalformedObjectNameException, AttributeNotFoundException,
                                  InstanceNotFoundException, MBeanException, ReflectionException {
        ObjectName osBean = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        Long swapValue = (Long) mBeanServer.getAttribute(osBean, "TotalSwapSpaceSize");
        return swapValue / MB_BASE;
    }

    /**
     * Unix OS Specific method to retrieve open files limit of the system
     *
     * @return open files limit of the system
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     */
    private long getOpenFilesLimit() throws MalformedObjectNameException,
                                            AttributeNotFoundException, InstanceNotFoundException,
                                            MBeanException, ReflectionException {
        ObjectName osBean = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        Long maxFDValue = null;
        if(IBM_J9_VM.equals(System.getProperty("java.vm.name"))){
            BufferedReader output;
            try {
                Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", "ulimit -n" });
                InputStream in = p.getInputStream();
                output = new BufferedReader(new InputStreamReader(in));
                try{
                    String maxFileDesCount;
                    if ((maxFileDesCount = output.readLine()) != null) {
                        maxFDValue = Long.parseLong(maxFileDesCount);
                    }
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }

            }catch (IOException exception){
                log.warn("Could not get the maximum file descriptor count ",exception);
            }
        } else {
            maxFDValue = (Long) mBeanServer.getAttribute(osBean, "MaxFileDescriptorCount");
        }
        return maxFDValue;
    }

    /**
     * @return maximum free disk space in the filesystem
     */
    private long getMaxFreeDiskSpace() {
        // list of all filesystem roots of this system
        long maxFreeSpace = 0;
        File[] roots = File.listRoots();
        for (File disk : roots) {
            long diskFreeSpace = disk.getFreeSpace() / MB_BASE;
            if (disk.isDirectory() && (diskFreeSpace > maxFreeSpace)) {
                maxFreeSpace = diskFreeSpace;
            }
        }
        return maxFreeSpace;
    }

    private boolean isWindows() {
        boolean isWindows = false;
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            isWindows = true;
        }
        return isWindows;
    }

}
