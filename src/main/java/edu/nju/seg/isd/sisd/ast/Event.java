package edu.nju.seg.isd.sisd.ast;

import java.util.List;

public record Event(
        String name,
        List<Assignment> assignments
) {

}
