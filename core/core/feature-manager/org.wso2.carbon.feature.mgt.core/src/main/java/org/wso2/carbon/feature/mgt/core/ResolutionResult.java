/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.wso2.carbon.feature.mgt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.wso2.carbon.feature.mgt.core.util.IUPropertyUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ResolutionResult implements Serializable {

    private static final long serialVersionUID = -5597451836018051452L;
    private static final String NESTING_INDENT = "  "; //$NON-NLS-1$

    private HashMap iuToStatusMap = new HashMap();
    private MultiStatus summaryStatus;
    private IProvisioningPlan provisioningPlan;
    private String installationSize;

    private ArrayList<IInstallableUnit> reviewedInstallableUnits = new ArrayList<IInstallableUnit>();
    private ArrayList<IInstallableUnit> reviewedUninstallableUnits = new ArrayList<IInstallableUnit>();
    private ArrayList<IInstallableUnit> failedinstallableUnits = new ArrayList<IInstallableUnit>();
    private ArrayList<IInstallableUnit> failedUninstallableUnits = new ArrayList<IInstallableUnit>();

    public void addFailedUninstallableUnit(IInstallableUnit iu) {
        failedUninstallableUnits.add(iu);
    }

    public void addFailedInstallableUnit(IInstallableUnit iu) {
        failedinstallableUnits.add(iu);
    }

    public void addReviewedUninstallableUnit(IInstallableUnit iu) {
        reviewedUninstallableUnits.add(iu);
    }

    public void addReviewedInstallableUnit(IInstallableUnit iu) {
        reviewedInstallableUnits.add(iu);
    }

    public IInstallableUnit[] getFailedInstallableUnits() {
        IInstallableUnit[] ius = new IInstallableUnit[failedinstallableUnits.size()];
        return failedinstallableUnits.toArray(ius);
    }

    public IInstallableUnit[] getFailedUninstallableUnits() {
        IInstallableUnit[] ius = new IInstallableUnit[failedUninstallableUnits.size()];
        return failedUninstallableUnits.toArray(ius);
    }

    public IInstallableUnit[] getReviewedInstallableUnits() {
        IInstallableUnit[] ius = new IInstallableUnit[reviewedInstallableUnits.size()];
        return reviewedInstallableUnits.toArray(ius);
    }

    public IInstallableUnit[] getReviewedUninstallableUnits() {
        IInstallableUnit[] ius = new IInstallableUnit[reviewedUninstallableUnits.size()];
        return reviewedUninstallableUnits.toArray(ius);
    }

    public String getInstallationSize() {
        return installationSize;
    }

    public void setInstallationSize(String installationSize) {
        this.installationSize = installationSize;
    }

    public IProvisioningPlan getProvisioningPlan() {
        return provisioningPlan;
    }

    public void setProvisioningPlan(IProvisioningPlan provisioningPlan) {
        this.provisioningPlan = provisioningPlan;
    }

    public IStatus getSummaryStatus() {
        if (summaryStatus != null) {
            return summaryStatus;
        }
        return Status.OK_STATUS;
    }

    public void addSummaryStatus(IStatus status) {
        if (summaryStatus == null) {
            summaryStatus = new MultiStatus("temp", 0, "Operation details", null);
        }
        summaryStatus.add(status);
    }

    public IStatus statusOf(IInstallableUnit iu) {
        return (IStatus) iuToStatusMap.get(iu);
    }

    public void addStatus(IInstallableUnit iu, IStatus status) {
        MultiStatus iuSummaryStatus = (MultiStatus) iuToStatusMap.get(iu);
        if (iuSummaryStatus != null) {
            iuSummaryStatus.add(status);
        }
    }

    private String getIUString(IInstallableUnit iu) {
        if (iu == null) {
            return "Items";
        }
        // Get the iu name in the default locale
        String name = IUPropertyUtils.getIUProperty(iu, IInstallableUnit.PROP_NAME);
        if (name != null) {
            return name;
        }
        return iu.getId();
    }

    public String getSummaryReport() {
        if (summaryStatus != null) {
            StringBuffer buffer = new StringBuffer();
            appendDetailText(summaryStatus, buffer, -1, false);
            return buffer.toString();
        }
        return ""; //$NON-NLS-1$
    }

    // Answers null if there is nothing to say about the ius
    public String getDetailedReport(IInstallableUnit[] ius) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < ius.length; i++) {
            MultiStatus iuStatus = (MultiStatus) iuToStatusMap.get(ius[i]);
            if (iuStatus != null) {
                appendDetailText(iuStatus, buffer, 0, true);
            }
        }
        String report = buffer.toString();
        if (report.length() == 0) {
            return null;
        }
        return report;
    }

    void appendDetailText(IStatus status, StringBuffer buffer, int indent, boolean includeTopLevelMessage) {
        if (includeTopLevelMessage) {
            for (int i = 0; i < indent; i++) {
                buffer.append(NESTING_INDENT);
            }
            if (status.getMessage() != null) {
                buffer.append(status.getMessage());
            }
        }
        Throwable t = status.getException();
        if (t != null) {
            // A provision (or core) exception occurred.  Get its status message or if none, its top level message.
            // Indent by one more level (note the <=)
            buffer.append('\n');
            for (int i = 0; i <= indent; i++) {
                buffer.append(NESTING_INDENT);
            }
            if (t instanceof CoreException) {
                IStatus exceptionStatus = ((CoreException) t).getStatus();
                if (exceptionStatus != null && exceptionStatus.getMessage() != null) {
                    buffer.append(exceptionStatus.getMessage());
                } else {
                    String details = t.getLocalizedMessage();
                    if (details != null) {
                        buffer.append(details);
                    }
                }
            } else {
                String details = t.getLocalizedMessage();
                if (details != null) {
                    buffer.append(details);
                }
            }
        }
        // Now print the children status info (if there are children)
        IStatus[] children = status.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            appendDetailText(children[i], buffer, indent + 1, true);
        }
    }
}

