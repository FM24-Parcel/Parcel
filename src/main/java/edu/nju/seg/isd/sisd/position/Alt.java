package edu.nju.seg.isd.sisd.position;

import edu.nju.seg.isd.sisd.exception.Impossible;
import org.jetbrains.annotations.NotNull;

public enum Alt {
    If, Else;

    @NotNull
    public String encode() {
        return switch (this) {
            case If -> "if";
            case Else -> "else";
        };
    }

    @NotNull
    public static Alt parse(@NotNull String alt) {
        return switch (alt) {
            case "if" -> If;
            case "else" -> Else;
            default -> throw new Impossible();
        };
    }

}
