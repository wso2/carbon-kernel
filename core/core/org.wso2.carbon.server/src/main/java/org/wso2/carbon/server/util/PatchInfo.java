package org.wso2.carbon.server.util;

import java.util.ArrayList;
import java.util.List;

public class PatchInfo {

    List<String> newPatches = new ArrayList<String>();
    List<String> removedPatches = new ArrayList<String>();

    public int getNewPatchesCount() {
        return newPatches.size();
    }

    public int getRemovedPatchesCount() {
        return removedPatches.size();
    }

    public void addNewPatches(String newPatchDirName) {
        if (newPatchDirName != null && !newPatchDirName.equals("")) {
            newPatches.add(newPatchDirName);
        }
    }

    public void addRemovedPatches(String removedPatchDirName) {
        if (removedPatchDirName != null && !removedPatchDirName.equals("")) {
            removedPatches.add(removedPatchDirName);
        }
    }

    public boolean isPatchesChanged() {
        return ((getNewPatchesCount() > 0) || (getRemovedPatchesCount() > 0));
    }

}