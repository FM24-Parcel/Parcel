package edu.nju.seg.isd.sisd.parser.xml;

import com.jcabi.xml.XMLDocument;
import edu.nju.seg.isd.sisd.util.$;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    @NotNull
    public static List<Element> parseXML(File f) {
        return $.readContent(f)
                .map(XMLDocument::new)
                .map(XMLParser::convertElementNode)
                .orElse(new ArrayList<>(0));
    }

    @NotNull
    private static List<Element> convertElementNode(XMLDocument xml) {
        int count = xml.nodes("//element").size();
        List<Element> r = new ArrayList<>();
        String prefix = "//diagram/element/";
        for (int i = 0; i < count; i++) {
            ElementType type = ElementType.valueOf(xml.xpath(prefix + "id/text()").get(i));
            int x = Integer.parseInt(xml.xpath(prefix + "coordinates/x/text()").get(i));
            int y = Integer.parseInt(xml.xpath(prefix + "coordinates/y/text()").get(i));
            int w = Integer.parseInt(xml.xpath(prefix + "coordinates/w/text()").get(i));
            int h = Integer.parseInt(xml.xpath(prefix + "coordinates/h/text()").get(i));
            String attributes = xml.xpath(prefix + "panel_attributes/text()").get(i);
            r.add(new Element(type, x, y, w, h, attributes));
        }
        return r;
    }

}
