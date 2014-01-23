/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.filter;

import java.text.ParseException;
import java.util.List;

import org.apache.directory.shared.ldap.filter.AndNode;
import org.apache.directory.shared.ldap.filter.EqualityNode;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.filter.FilterParser;
import org.apache.directory.shared.ldap.filter.NotNode;
import org.apache.directory.shared.ldap.filter.OrNode;
import org.apache.directory.shared.ldap.filter.PresenceNode;

/**
 * Parser for LDAP filter expressions.
 */
public class FilterExpressionParser {
    private FilterExpressionParser() {}
    
    private static FilterExpression[] buildExpressions(List<ExprNode> nodes) {
        FilterExpression[] result = new FilterExpression[nodes.size()];
        int i = 0;
        for (ExprNode node : nodes) {
            result[i++] = buildExpression(node);
        }
        return result;
    }
    
    private static FilterExpression buildExpression(ExprNode node) {
        if (node instanceof AndNode) {
            return new AndExpression(buildExpressions(((AndNode)node).getChildren()));
        } else if (node instanceof OrNode) {
            return new OrExpression(buildExpressions(((OrNode)node).getChildren()));
        } else if (node instanceof NotNode) {
            return new NotExpression(buildExpression(((NotNode)node).getFirstChild()));
        } else if (node instanceof EqualityNode) {
            EqualityNode equalityNode = (EqualityNode)node;
            return new EqualityExpression(equalityNode.getAttribute(), equalityNode.getValue().toString());
        } else if (node instanceof PresenceNode) {
            return new PresenceExpression(((PresenceNode)node).getAttribute());
        } else {
            throw new UnsupportedOperationException("Node type " + node.getClass().getSimpleName() + " not supported");
        }
    }
    
    /**
     * Parse an LDAP filter expression.
     * 
     * @param filter an LDAP filter as defined by <a href="http://www.ietf.org/rfc/rfc2254.txt">RFC2254</a>
     * @return the parsed filter
     * @throws ParseException if the filter is syntactically incorrect
     */
    public static FilterExpression parse(String filter) throws ParseException {
        return buildExpression(FilterParser.parse(filter));
    }
}
