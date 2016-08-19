package org.wso2.carbon.tools.securevault;

/**
 * Created by jayanga on 8/18/16.
 */
public class Constants {

    public static final String ENCRYPT_TEXT_COMMAND = "-encryptText";
    public static final String DECRYPT_TEXT_COMMAND = "-decryptText";
    public static final String CUSTOM_LIB_PATH_COMMAND = "-customLibPath";

    public static final String INIT_METHOD = "init";
    public static final String ENCRYPT_SECRETS_METHOD = "encryptSecrets";
    public static final String ENCRYPT_TEXT_METHOD = "encryptText";
    public static final String DECRYPT_TEXT_METHOD = "decryptText";


    public static final String CIPHER_TOOL_CLASS = "org.wso2.carbon.kernel.securevault.tool.CipherTool";

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Constants() {
        throw new AssertionError("Trying to a instantiate a constant class");
    }
}
