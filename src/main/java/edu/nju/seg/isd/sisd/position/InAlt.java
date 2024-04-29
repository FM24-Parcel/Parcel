package edu.nju.seg.isd.sisd.position;

import org.jetbrains.annotations.NotNull;

@NotNull
public class InAlt implements Offset {

    private final Alt alt;

    public InAlt(@NotNull Alt alt) {
        this.alt = alt;
    }

    @Override
    @NotNull
    public String encode() {
        return "alt:" + alt.encode();
    }

}
