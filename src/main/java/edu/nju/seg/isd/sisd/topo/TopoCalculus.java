package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.ast.Event;
import edu.nju.seg.isd.sisd.ast.Instance;
import edu.nju.seg.isd.sisd.ast.Message;
import edu.nju.seg.isd.sisd.exception.Impossible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@NotNull
public class TopoCalculus {

    private final Map<Instance, List<PartialNode>> lifelines;

    private final Map<Event, Instance> eventToInstance;

    private final Map<Event, PartialNode> eventToNode;

    /* Causality */
    private final Set<Order> causality = new HashSet<>();

    public TopoCalculus(@NotNull List<Instance> instances) {
        this.lifelines = new HashMap<>();
        this.eventToInstance = new HashMap<>();
        this.eventToNode = new HashMap<>();
        for (Instance instance : instances) {
            lifelines.put(instance, new ArrayList<>());
        }
    }

    @NotNull
    public FlatTopo toTopo() {
        var topo = new FlatTopo();
        causality.forEach(topo::addPartialOrder);
        for (var instance : lifelines.keySet()) {
            var pns = lifelines.get(instance);
            for (var i = 0; i < pns.size() - 1; i++) {
                var up = pns.get(i);
                var down = pns.get(i + 1);
                processUpDown(up, down, topo);
            }
        }
        return topo;
    }

    // Controllability
    // FIFO Order
    // Virtual Events
    private void processUpDown(@NotNull PartialNode up,
                               @NotNull PartialNode down,
                               @NotNull FlatTopo topo) {
        switch (down) {
            case Head h -> topo.addPartialOrder(new Order(up, h));
            case Tail ignored -> {} /* the up must be head, so no partial order */
            case TopoEvent downEvent -> {
                switch (up) {
                    case Head ignored -> throw new Impossible(); /* the down must be tail */
                    case Tail t -> topo.addPartialOrder(new Order(t, downEvent));
                    case TopoEvent upEvent -> {
                        if (isControllable(downEvent)) {
                            topo.addPartialOrder(new Order(upEvent, downEvent));
                        } else if (isSequential(upEvent, downEvent)) {
                            topo.addPartialOrder(new Order(upEvent, downEvent));
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + up);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + down);
        }
    }

    // Controllability
    private boolean isControllable(@NotNull TopoEvent down) {
        return down.isSending();
    }

    // FIFO Order
    private boolean isSequential(@NotNull TopoEvent up,
                                 @NotNull TopoEvent down) {
        var upSource = eventToInstance.get(up.event());
        var downSource = eventToInstance.get(down.event());
        return up.isReceiving()
                && down.isReceiving()
                && upSource.equals(downSource)
                && isSequentialInLifetime(
                    lifelines.get(upSource),
                    eventToNode.get(up.event()),
                    eventToNode.get(down.event()));
    }

    private boolean isSequentialInLifetime(@NotNull List<PartialNode> lifetime,
                                           @Nullable PartialNode up,
                                           @Nullable PartialNode down) {
        int index1 = lifetime.indexOf(up);
        int index2 = lifetime.indexOf(down);
        return index1 < index2;
    }

    public void recordMessage(@NotNull Message m) {
        var sending = TopoEvent.sending(m);
        var receiving = TopoEvent.receiving(m);
        add(m.source(), sending);
        add(m.target(), receiving);
        eventToInstance.put(m.receiving(), m.source());
        eventToNode.put(m.receiving(), sending);
        causality.add(new Order(sending, receiving));
    }

    private void add(@NotNull Instance instance,
                     @NotNull PartialNode node) {
        lifelines.get(instance).add(node);
    }

}