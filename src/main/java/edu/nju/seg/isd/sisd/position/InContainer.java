package edu.nju.seg.isd.sisd.position;

import org.jetbrains.annotations.NotNull;

@NotNull
public class InContainer implements Offset {

    @Override
    @NotNull
    public String encode() {
        return "container";
    }

}
