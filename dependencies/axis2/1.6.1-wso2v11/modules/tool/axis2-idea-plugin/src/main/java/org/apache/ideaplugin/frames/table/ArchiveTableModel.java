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

package org.apache.ideaplugin.frames.table;

import org.apache.ideaplugin.bean.OperationObj;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Iterator;

public class ArchiveTableModel extends AbstractTableModel {

    final String[] columnNames = {"Operation Name", "Return Value", "Parameters ", "Select"};
    Object[][] datvalue;
    private HashMap datobjs;

    public ArchiveTableModel(HashMap dataobject) {
        int size = dataobject.size();
        datvalue = new Object[size][4];
        Iterator itr = dataobject.values().iterator();
        int count = 0;
        while (itr.hasNext()) {
            OperationObj operationObj = (OperationObj) itr.next();
            datvalue[count][0] = operationObj.getOpName();
            datvalue[count][1] = operationObj.getReturnValue();
            datvalue[count][2] = operationObj.getParameters();
            datvalue[count][3] = operationObj.getSelect();
            count++;
        }
        this.datobjs = dataobject;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return datvalue.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return datvalue[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return col >= 3;
    }

    public void setValueAt(Object value, int row, int col) {
        OperationObj obj = (OperationObj) datobjs.get(getValueAt(row, 0));
        if (col == 3) {
            obj.setSelect((Boolean) value);
        }

        if (datvalue[0][col] instanceof Integer) {
            try {
                datvalue[row][col] = new Integer((String) value);
            } catch (NumberFormatException e) {
                System.out.println("Error");
            }
        } else {
            datvalue[row][col] = value;
        }
//        obj.printMe();
    }
}



