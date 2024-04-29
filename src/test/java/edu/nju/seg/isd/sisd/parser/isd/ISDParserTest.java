package edu.nju.seg.isd.sisd.parser.isd;

import edu.nju.seg.isd.sisd.ast.Instance;
import org.junit.jupiter.api.Test;
import org.petitparser.parser.Parser;

import java.util.Map;

public class ISDParserTest {

    @Test
    public void parse_item_test_1() {
        Map<String, Instance> context = Map.of(
                "a", new Instance("T1", "a"),
                "b", new Instance("T2", "b")
        );
        ISDParser.ItemDefinition definition = new ISDParser.ItemDefinition(context);
        Parser p = definition.build();
        p.parse("""
                a->>>b : (e0,e1,m1);


                combinedFragment=int (p=1) [TM1] ~
                a->>>b : (e2,e3,m2);
                --


                combinedFragment=int (p=2) [TM2] ~
                a->>>b : (e4,e5,m3);
                --""").get();
    }

    @Test
    public void parse_item_test_2() {
        Map<String, Instance> context = Map.of(
                "a", new Instance("T1", "a"),
                "b", new Instance("T2", "b")
        );
        ISDParser.ItemDefinition definition = new ISDParser.ItemDefinition(context);
        Parser p = definition.build();
        p.parse("""
                combinedFragment=int (p=1) [TM1] ~
                a->>>b : (e2,e3,m2);
                --


                combinedFragment=int (p=2) [TM2] TASK2~
                a->>>b : (e4,e5,m3);
                --""").get();
    }

    @Test
    public void parse_item_test_3() {
        Map<String, Instance> context = Map.of(
                "a", new Instance("T1", "a"),
                "b", new Instance("T2", "b")
        );
        ISDParser.ItemDefinition definition = new ISDParser.ItemDefinition(context);
        Parser p = definition.build();
        p.parse("""
                combinedFragment=loop(2,3)~
                a->>>b : (e0,e1,waiting1);
                combinedFragment=loop(2,2)~
                a->>>b : (e2,e3,writing);
                --
                --""").get();
    }

    @Test
    public void parse_item_test_4() {
        Map<String, Instance> context = Map.of(
                "a", new Instance("T1", "a"),
                "b", new Instance("T2", "b")
        );
        ISDParser.ItemDefinition definition = new ISDParser.ItemDefinition(context);
        Parser p = definition.build();
        p.parse("""
                combinedFragment=int (p=1) (2, 3, 1.2) [TM1] ~
                a->>>b : (e2,e3,m2);
                --


                combinedFragment=int (p=2) [TM2] TASK2~
                a->>>b : (e4,e5,m3);
                --""").get();
    }

}
