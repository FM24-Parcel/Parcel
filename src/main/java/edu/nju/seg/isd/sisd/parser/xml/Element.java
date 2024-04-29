package edu.nju.seg.isd.sisd.parser.xml;

public record Element(
        ElementType type,
        int x,
        int y,
        int w,
        int h,
        String attributes
) {

}
