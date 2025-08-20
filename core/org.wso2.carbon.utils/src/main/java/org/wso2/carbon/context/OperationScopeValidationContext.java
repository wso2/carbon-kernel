package org.wso2.carbon.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds the context for validating operation scopes.
 * It contains information about whether validation is required,
 * the list of validated scopes, the normalized resource URI,
 * and a map of operation scopes.
 */
public class OperationScopeValidationContext {

    private boolean validationRequired;
    private List<String> validatedScopes;
    private Map<String, String> operationScopeMap = new ConcurrentHashMap<>();

    public boolean isValidationRequired() {

        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {

        this.validationRequired = validationRequired;
    }

    public List<String> getValidatedScopes() {

        return validatedScopes;
    }

    public void setValidatedScopes(List<String> validatedScopes) {

        this.validatedScopes = validatedScopes;
    }

    public Map<String, String> getOperationScopeMap() {

        return operationScopeMap;
    }

    public void setOperationScopeMap(Map<String, String> operationScopeMap) {

        this.operationScopeMap = operationScopeMap;
    }
}
