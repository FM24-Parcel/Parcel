package edu.nju.seg.isd.sisd.trace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record EventInfo(
        String name,
        Map<Integer, Integer> indexToVersion, //index -> version
        Map<Integer, Integer> versionToIndex, //version -> index
        int priority
) {

    public List<Integer> getIndexList() {
        return List.copyOf(indexToVersion.keySet());
    }

    public List<Integer> getVersionList() {
        return List.copyOf(indexToVersion.values());
    }

    public String toString(int index) {
        return name + "@" + indexToVersion.get(index);
    }
}
