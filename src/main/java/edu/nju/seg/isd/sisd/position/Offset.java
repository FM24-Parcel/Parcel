package edu.nju.seg.isd.sisd.position;

import io.vavr.collection.List;
import org.petitparser.parser.Parser;

import static edu.nju.seg.isd.sisd.parser.isd.ExpressionParser.integer;
import static org.petitparser.parser.primitive.CharacterParser.letter;
import static org.petitparser.parser.primitive.StringParser.of;

public interface Offset {

    String encode();

    static Offset decode(String position) {
        return offsetParser().parse(position).get();
    }

    private static Parser offsetParser() {
        return of("container").map(ignored -> new InContainer())
                .or(of("loop:")
                        .seq(integer())
                        .seq(of("("))
                        .seq(integer())
                        .seq(of(")"))
                        .map((List<Integer> os) -> new InLoop(os.get(1), os.get(3))))
                .or(of("alt:")
                        .seq(letter().plus().flatten().map(Alt::parse))
                        .map((List<Alt> os) -> new InAlt(os.get(1))))
                .or(of("int:")
                        .seq(integer())
                        .seq(of("("))
                        .seq(integer())
                        .seq(of(")"))
                        .map((List<Integer> os) -> new InInt(os.get(1), os.get(3))));
    }

}
