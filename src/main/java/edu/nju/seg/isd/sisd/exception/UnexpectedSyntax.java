package edu.nju.seg.isd.sisd.exception;

import org.jetbrains.annotations.NotNull;

@NotNull
public class UnexpectedSyntax extends ParseException {

    private final String s;

    public UnexpectedSyntax(@NotNull String s) {
        this.s = s;
    }

    @Override
    public void printStackTrace() {
        System.err.println("Unexpected Syntax: " + s);
        super.printStackTrace();
    }

}
