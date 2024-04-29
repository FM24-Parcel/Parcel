package edu.nju.seg.isd.sisd.exception;

import org.jetbrains.annotations.NotNull;

@NotNull
public class MissingElement extends ParseException {

    private final MissingType type;

    public MissingElement(@NotNull MissingType type) {
        this.type = type;
    }

    @Override
    public void printStackTrace() {
        System.err.println("Missing Element: " + type);
        super.printStackTrace();
    }

}
