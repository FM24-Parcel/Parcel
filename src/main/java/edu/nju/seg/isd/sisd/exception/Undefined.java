package edu.nju.seg.isd.sisd.exception;

public class Undefined extends RuntimeException {

    public Undefined() {
        super("undefined");
    }

    public Undefined(String message) {
        super(message);
    }

}
