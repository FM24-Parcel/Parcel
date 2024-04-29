package edu.nju.seg.isd.sisd.topo;

import edu.nju.seg.isd.sisd.ast.Message;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record BasicFragment(
        List<Message> messages
) {

    public Set<PartialNode> events() {
        return messages
                .stream()
                .flatMap(Message::topoEvents)
                .collect(Collectors.toSet());
    }

}
