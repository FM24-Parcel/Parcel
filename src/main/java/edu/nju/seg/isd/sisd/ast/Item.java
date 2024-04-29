package edu.nju.seg.isd.sisd.ast;

public sealed interface Item permits Fragment, Message {

    boolean notNested();

}
