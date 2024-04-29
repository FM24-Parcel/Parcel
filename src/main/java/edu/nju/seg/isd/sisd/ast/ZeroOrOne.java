package edu.nju.seg.isd.sisd.ast;

import edu.nju.seg.isd.sisd.exception.UnexpectedSyntax;
import org.jetbrains.annotations.NotNull;

public enum ZeroOrOne {

    Zero,
    One;

    public static ZeroOrOne parse(@NotNull String s) {
        if (s.equals("0")) {
            return Zero;
        } else if (s.equals("1")) {
            return One;
        } else {
            throw new UnexpectedSyntax(s);
        }
    }

}
