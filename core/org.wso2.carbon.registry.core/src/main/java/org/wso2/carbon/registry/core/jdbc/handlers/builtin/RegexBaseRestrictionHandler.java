/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.core.jdbc.handlers.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RegexBaseRestrictionHandler is used to restrict certain operations such as move, rename performing against certain
 * resources. These restrictions are applied on the candidate resources by providing them in the form of regular
 * expressions.
 */
public class RegexBaseRestrictionHandler extends Handler {

    /**
     * This is an adapter class for wrapping a java.util.regex.Pattern to enables comparing based on the pattern string.
     */
    private static class PatternAdapter {
        private Pattern pattern;

        private PatternAdapter(Pattern p) {
            this.pattern = p;
        }

        private Pattern getPattern() {
            return pattern;
        }

        @Override
        public int hashCode() {
            return pattern.pattern().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PatternAdapter) {
                PatternAdapter other = (PatternAdapter) obj;
                return this.pattern.pattern().equals(other.pattern.pattern());
            }

            return false;
        }
    }
    private Set<PatternAdapter> regexPatterns = new HashSet<PatternAdapter>();

    private static final String SYS_COLLECTION_RESTRICTION_REGEX = "^/_system((/config|/local|/governance)?)$";

    private static final String ERROR_MSG = "This operation cannot be performed on resource: %s.";

    private static final Log log = LogFactory.getLog(RegexBaseRestrictionHandler.class);

    public RegexBaseRestrictionHandler() {
        addRegExPattern(SYS_COLLECTION_RESTRICTION_REGEX);
    }

    public RegexBaseRestrictionHandler(Set<String> regexPatterns) {
        this();  //this is to init SYS_COLLECTION_RESTRICTION_REGEX pattern.
        for (String regexPattern : regexPatterns) {
            addRegExPattern(regexPattern);
        }
    }

    public void addRegExPattern(String regExPattern) {
        //Since, a Set is used for storing pattern strings, duplicates will not be contained in the regexPetterns set.
        this.regexPatterns.add(new PatternAdapter(Pattern.compile(regExPattern)));

    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        log.trace("Performing a move operation.");

        // validating for operation restrictions
        validatePath(requestContext);
        return requestContext.getTargetPath();
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        log.trace("Performing a rename operation.");

        // validating for operation restrictions
        validatePath(requestContext);
        return requestContext.getTargetPath();
    }

    /**
     * validates a resource path against a set of given regular expressions.
     *
     * @param requestContext - the resource context.
     * @return - true if all the validations pass.
     * @throws RegistryException - registry exception
     */
    private boolean validatePath(final RequestContext requestContext) throws RegistryException {
        final String resourcePath = requestContext.getSourcePath();
        log.debug("Resource validation: " + resourcePath);

        for (PatternAdapter pa : regexPatterns) {
            // Create a restriction matcher, and match with the path.
            if (pa.getPattern().matcher(resourcePath).find()) {
                requestContext.setProcessingComplete(true);
                final String errMsg = String.format(ERROR_MSG, resourcePath);
                log.error(errMsg);
                throw new RegistryException(errMsg);
            }
        }
        return true;
    }
}
