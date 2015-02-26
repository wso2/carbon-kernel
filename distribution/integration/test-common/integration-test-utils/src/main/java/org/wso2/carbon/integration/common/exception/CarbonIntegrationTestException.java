package org.wso2.carbon.integration.common.exception;

public class CarbonIntegrationTestException extends Exception
{

    private static final long serialVersionUID = 1997753363232807009L;

    public CarbonIntegrationTestException()
    {
        super();
    }

    public CarbonIntegrationTestException(String message)
    {
        super(message);
    }

    public CarbonIntegrationTestException(Throwable cause)
    {
        super(cause);
    }

    public CarbonIntegrationTestException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
