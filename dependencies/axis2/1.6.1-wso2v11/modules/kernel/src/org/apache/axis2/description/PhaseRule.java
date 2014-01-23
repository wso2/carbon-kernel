/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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


package org.apache.axis2.description;

import org.apache.axis2.phaseresolver.PhaseException;

import java.io.Serializable;

/**
 * Class PhaseRule
 */
public class PhaseRule implements Serializable {

    /**
     * Field after
     */
    private String after;

    /**
     * Field before
     */
    private String before;

    /**
     * Field phaseFirst
     */
    private boolean phaseFirst;

    /**
     * Field phaseLast
     */
    private boolean phaseLast;

    /**
     * Field phaseName
     */
    private String phaseName;

    /**
     * Constructor PhaseRule.
     */
    public PhaseRule() {
    }

    public PhaseRule(String phaseName) {
        this.phaseName = phaseName;
    }

    /**
     * Method getAfter.
     *
     * @return Returns String.
     */
    public String getAfter() {
        return after;
    }

    /**
     * Method getBefore.
     *
     * @return Returns String.
     */
    public String getBefore() {
        return before;
    }

    /**
     * Method getPhaseName.
     *
     * @return Returns String.
     */
    public String getPhaseName() {
        return phaseName;
    }

    /**
     * Method isPhaseFirst.
     *
     * @return Returns boolean.
     */
    public boolean isPhaseFirst() {
        return phaseFirst;
    }

    /**
     * Method isPhaseLast.
     *
     * @return Returns boolean.
     */
    public boolean isPhaseLast() {
        return phaseLast;
    }

    /**
     * Set the "after" name for this rule.
     *
     * @param after the name of the "after" handler
     */
    public void setAfter(String after) {
        if ("".equals(after)) after = null;
        this.after = after;
    }

    /**
     * Set the "before" name for this rule.
     *
     * @param before the name of the "before" handler
     */
    public void setBefore(String before) {
        if ("".equals(before)) before = null;
        this.before = before;
    }

    /**
     * Method setPhaseFirst.
     *
     * @param phaseFirst true if this rule defines the first Handler in a Phase
     */
    public void setPhaseFirst(boolean phaseFirst) {
        this.phaseFirst = phaseFirst;
    }

    /**
     * Method setPhaseLast.
     *
     * @param phaseLast true if this rule defines the last Handler in a Phase
     */
    public void setPhaseLast(boolean phaseLast) {
        this.phaseLast = phaseLast;
    }

    /**
     * Method setPhaseName.
     *
     * @param phaseName the name of the Phase
     */
    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    /**
     * Validate "sane" rules - cannot have both phaseFirst/phaseLast and before/after
     *
     * @throws PhaseException if phaseFirst/phaseLast is set along with before/after
     */
    public void validate() throws PhaseException {
        if (before != null || after != null) {
            if (phaseFirst) {
                throw new PhaseException(
                        "Invalid PhaseRule (phaseFirst is set along with before/after)");
            }
            if (phaseLast) {
                throw new PhaseException(
                        "Invalid PhaseRule (phaseLast is set along with before/after)");
            }
        }
    }
}
