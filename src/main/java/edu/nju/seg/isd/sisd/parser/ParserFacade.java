package edu.nju.seg.isd.sisd.parser;

import edu.nju.seg.isd.sisd.ast.ISD;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static edu.nju.seg.isd.sisd.parser.isd.ISDParser.parseInterruptSequenceDiagram;
import static edu.nju.seg.isd.sisd.parser.xml.XMLParser.parseXML;

public class ParserFacade {

    // compose XML parser and ISD parser
    @NotNull
    public static ISD parse(File f) {
        return parseInterruptSequenceDiagram(parseXML(f));
    }

}
