package org.wso2.carbon.tools.securevault;

import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.securevault.tool.CipherTool;
import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.securevault.utils.CommandLineParser;
import org.wso2.carbon.tools.securevault.utils.Utils;

import java.net.URLClassLoader;
import java.util.Optional;

/**
 * This class defines the unit test cases for Cipher Tool Initializer.
 *
 * @since 5.2.0
 */
@PrepareForTest(Utils.class)
public class CipherToolInitializerTest {

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testExecuteTestEncryptSecrets() throws CarbonToolException, SecureVaultException {
        String[] toolArgs = new String[]{};

        PowerMock.mockStatic(Utils.class);
        CommandLineParser commandLineParser = new CommandLineParser(toolArgs);
        URLClassLoader urlClassLoader = EasyMock.mock(URLClassLoader.class);
        CipherTool cipherTool = EasyMock.mock(CipherTool.class);
        EasyMock.expect(Utils.createCommandLineParser(toolArgs)).andReturn(commandLineParser);
        EasyMock.expect(Utils.getCustomClassLoader(Optional.empty())).andReturn(urlClassLoader);
        EasyMock.expect(Utils.createCipherTool(urlClassLoader)).andReturn(cipherTool);

        cipherTool.encryptSecrets();
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();
        EasyMock.replay(urlClassLoader);
        EasyMock.replay(cipherTool);

        CipherToolInitializer cipherToolInitializer = new CipherToolInitializer();
        cipherToolInitializer.execute(toolArgs);
    }

    @Test
    public void testExecuteTestEncryptText() throws CarbonToolException, SecureVaultException {
        String[] toolArgs = new String[]{"-encryptText", "ABC@123"};

        PowerMock.mockStatic(Utils.class);
        CommandLineParser commandLineParser = new CommandLineParser(toolArgs);
        URLClassLoader urlClassLoader = EasyMock.mock(URLClassLoader.class);
        CipherTool cipherTool = EasyMock.mock(CipherTool.class);
        EasyMock.expect(Utils.createCommandLineParser(toolArgs)).andReturn(commandLineParser);
        EasyMock.expect(Utils.getCustomClassLoader(Optional.empty())).andReturn(urlClassLoader);
        EasyMock.expect(Utils.createCipherTool(urlClassLoader)).andReturn(cipherTool);

        EasyMock.expect(cipherTool.encryptText(EasyMock.anyObject())).andReturn("dummy");

        PowerMock.replayAll();
        EasyMock.replay(urlClassLoader);
        EasyMock.replay(cipherTool);

        CipherToolInitializer cipherToolInitializer = new CipherToolInitializer();
        cipherToolInitializer.execute(toolArgs);
    }

    @Test
    public void testExecuteTestDecryptText() throws CarbonToolException, SecureVaultException {
        String[] toolArgs = new String[]{"-decryptText", "ABC@123"};

        PowerMock.mockStatic(Utils.class);
        CommandLineParser commandLineParser = new CommandLineParser(toolArgs);
        URLClassLoader urlClassLoader = EasyMock.mock(URLClassLoader.class);
        CipherTool cipherTool = EasyMock.mock(CipherTool.class);
        EasyMock.expect(Utils.createCommandLineParser(toolArgs)).andReturn(commandLineParser);
        EasyMock.expect(Utils.getCustomClassLoader(Optional.empty())).andReturn(urlClassLoader);
        EasyMock.expect(Utils.createCipherTool(urlClassLoader)).andReturn(cipherTool);

        EasyMock.expect(cipherTool.decryptText(EasyMock.anyObject())).andReturn("dummy");

        PowerMock.replayAll();
        EasyMock.replay(urlClassLoader);
        EasyMock.replay(cipherTool);

        CipherToolInitializer cipherToolInitializer = new CipherToolInitializer();
        cipherToolInitializer.execute(toolArgs);
    }

    @Test
    public void testExecuteTestEncryptSecretsWithCustomLibPath() throws CarbonToolException, SecureVaultException {
        String[] toolArgs = new String[]{"-customLibPath", "/tmp"};

        PowerMock.mockStatic(Utils.class);
        CommandLineParser commandLineParser = new CommandLineParser(toolArgs);
        URLClassLoader urlClassLoader = EasyMock.mock(URLClassLoader.class);
        CipherTool cipherTool = EasyMock.mock(CipherTool.class);
        EasyMock.expect(Utils.createCommandLineParser(toolArgs)).andReturn(commandLineParser);
        EasyMock.expect(Utils.getCustomClassLoader(EasyMock.anyObject())).andReturn(urlClassLoader);
        EasyMock.expect(Utils.createCipherTool(urlClassLoader)).andReturn(cipherTool);

        cipherTool.encryptSecrets();
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();
        EasyMock.replay(urlClassLoader);
        EasyMock.replay(cipherTool);

        CipherToolInitializer cipherToolInitializer = new CipherToolInitializer();
        cipherToolInitializer.execute(toolArgs);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteTestEncryptSecretsWithOddParameters() {
        String[] toolArgs = new String[]{"-customLibPath", "/tmp", "xyz"};
        CipherToolInitializer cipherToolInitializer = new CipherToolInitializer();
        cipherToolInitializer.execute(toolArgs);
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteTestEncryptSecretsWithWrongCommand() {
        String[] toolArgs = new String[]{"-ENCRYPTTEXT", "ABC@123"};
        CipherToolInitializer cipherToolInitializer = new CipherToolInitializer();
        cipherToolInitializer.execute(toolArgs);
    }
}
