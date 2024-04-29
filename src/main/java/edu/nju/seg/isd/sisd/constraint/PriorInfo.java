package edu.nju.seg.isd.sisd.constraint;

import edu.nju.seg.isd.sisd.ast.*;
import edu.nju.seg.isd.sisd.exception.Impossible;

import java.util.*;

public class PriorInfo {
    ISD isd;
    int priority;
    Map<String, Integer> priorMap;
    SortedMap<Integer, List<Event>> eventMap;

    public PriorInfo(ISD isd) {
        this.isd = isd;
        this.priority = 0;
        this.priorMap = new HashMap<>();
        this.eventMap = new TreeMap<>();
    }

    public Map<String, Integer> transform() {
        var tuple = isd.partition();
        handleContainer(tuple._1());
        SortedMap<Integer, List<IntFragment>> intMap = tuple._2;
        for (Integer integer : intMap.keySet()) {
            List<IntFragment> intFragments = intMap.get(integer);
            for (IntFragment intFragment : intFragments) {
                this.priority = integer;
                handleItems(intFragment.getItems());
            }
        }
        return priorMap;
    }

    public void handleContainer(Container container) {
        handleItems(container.getItems());
    }

    public void handleItems(List<Item> items) {
        for (Item item : items) {
            switch (item) {
                case Message message -> {
                    this.priorMap.put(message.sending().name(), this.priority);
                    this.priorMap.put(message.receiving().name(), this.priority);
                    this.eventMap.put(this.priority, List.of(message.sending(), message.receiving()));
                }
                case AltFragment altFragment -> {
                    handleItems(altFragment.getItems());
                    handleItems(altFragment.getElseItems());
                }
                case LoopFragment loopFragment -> {
                    handleItems(loopFragment.getItems());
                }
                case Container ignored -> throw new Impossible();
                case IntFragment ignored -> throw new Impossible();
            }
        }
    }
}
