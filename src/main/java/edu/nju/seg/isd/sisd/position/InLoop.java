package edu.nju.seg.isd.sisd.position;

import org.jetbrains.annotations.NotNull;

@NotNull
public class InLoop implements Offset {

    private final int total;

    private final int index;

    public InLoop(int total, int index) {
        this.total = total;
        this.index = index;
    }

    public InLoop() {
        this.total = 0;
        this.index = 0;
    }

    @Override
    @NotNull
    public String encode() {
        return String.format("loop:%d(%d)", total, index);
    }

}
