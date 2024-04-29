package edu.nju.seg.isd.sisd.trace;

import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

public class TraceBuilder {
    List<String> curTrace;
    Map<String, EventInfo> eventInfoMap;
    Map<String, Integer> priorMap;
    Map<String, Boolean> isMatched;

    public TraceBuilder(List<String> curTrace,Map<String, Integer> priorMap) {
        this.curTrace = curTrace;
        this.priorMap = priorMap;
        this.isMatched = new HashMap<>();
        this.eventInfoMap = checkCurTrace();
    }

    public List<Tuple2<Integer, Integer>> calculateIndexRange(String name1, String name2) {
        List<Integer> frontIndexList = this.eventInfoMap.get(name1).getIndexList();
        List<Integer> backIndexList = this.eventInfoMap.get(name2).getIndexList();
        List<Tuple2<Integer, Integer>> indexRangeList = new ArrayList<>();
        for (int backIndex : backIndexList) {
            for (int index = backIndex; index >= 0; index--) {
                if (frontIndexList.contains(index)) {
                    // index,backIndex
                    indexRangeList.add(Tuple.of(index, backIndex));
                    break;
                }
            }
        }
        return indexRangeList;
    }

    public HashMap<Tuple2<Integer, Integer>, List<Tuple2<Integer, Integer>>> calculateHighPriorIndexRange(
            List<Tuple2<Integer, Integer>> indexRangeList, int curPriority) {
        List<EventInfo> highPriorEvents = new ArrayList<>();
        HashMap<Tuple2<Integer, Integer>, List<Tuple2<Integer, Integer>>> map = new HashMap<>();
        for (Tuple2<Integer, Integer> indexRange : indexRangeList) {
            List<Integer> highPriorIndex = new ArrayList<>();
            for (int index = indexRange._1(); index < indexRange._2(); index++) {
                String name = this.curTrace.get(index);
                EventInfo eventInfo = this.eventInfoMap.get(name);
                if (eventInfo.priority() > curPriority) {
                    highPriorIndex.add(index);
                    highPriorEvents.add(eventInfo);
                }
            }
            List<Tuple2<Integer, Integer>> contiguousSegments = findContiguousSegments(highPriorIndex);
            map.put(indexRange, contiguousSegments);
        }
        return map;
    }

    public List<Tuple2<Integer, Integer>> findContiguousSegments(List<Integer> list) {
        List<Tuple2<Integer, Integer>> firstAndLast = new ArrayList<>();
        int start = 0;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) != list.get(i - 1) + 1) {
                List<Integer> segment = new ArrayList<>();
                segment.add(list.get(start));
                segment.add(list.get(i - 1));
                firstAndLast.add(Tuple.of(list.get(start), list.get(i - 1)));
                start = i;
            }
        }
        return firstAndLast;
    }

    public List<Tuple2<String, String>> calculateEventPair(String front, String back) {
        List<Tuple2<String, String>> eventPairs = new ArrayList<>();
        //先要获得前后两事件的indexList,这会对其匹配
        List<Integer> frontIndexList = this.eventInfoMap.get(front).getIndexList().stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        List<Integer> backIndexList = this.eventInfoMap.get(back).getIndexList();
        //这里的front是发生在较后的事件
        for (int frontIndex : frontIndexList) {
            for (int index = frontIndex; index >= 0; index--) {
                if (backIndexList.contains(index)) {
                    String frontName = this.eventInfoMap.get(back).toString(index);
                    String backName = this.eventInfoMap.get(front).toString(frontIndex);
                    if(isMatched.getOrDefault(frontName, false)&&isMatched.getOrDefault(backName, false)) {
                        continue;
                    }
                    eventPairs.add(Tuple.of(
                            this.eventInfoMap.get(back).toString(index),
                            this.eventInfoMap.get(front).toString(frontIndex)
                    ));
                    break;
                }
            }
        }
        return eventPairs;
    }

    public Map<String, EventInfo> checkCurTrace() {
        Map<String, EventInfo> eventInfoMap = new HashMap<>();
        Set<String> allEventInTrace = new HashSet<>(this.curTrace);

        for (String name : allEventInTrace) {
            eventInfoMap.put(name, seekEvent(name));
        }
        return eventInfoMap;
    }

    public EventInfo seekEvent(String eventName) {
        //返回一个list，包含所有符合条件的event的index
        //这个队列也应该解决事件名重复的问题
        Map<Integer, Integer> indexToVersion = new HashMap<>();
        Map<Integer, Integer> versionToIndex = new HashMap<>();
        int count = 0;
        for (int i = 0; i < this.curTrace.size(); i++) {
            if (this.curTrace.get(i).equals(eventName)) {
                indexToVersion.put(i, count);
                versionToIndex.put(count, i);
                count++;
            }
        }
        return new EventInfo(eventName, indexToVersion, versionToIndex, this.priorMap.get(eventName));
    }

    public String getEventInfoName(String name, int index) {
        return this.eventInfoMap.get(name).toString(index);
    }

    public int getIndex(String nameWithVersion) {
        List<String> temp = List.of(nameWithVersion.split("@"));
        String name = temp.get(0);
        int version = Integer.parseInt(temp.get(1));
        return this.eventInfoMap.get(name).versionToIndex().get(version);
    }
}
