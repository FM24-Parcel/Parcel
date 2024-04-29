package edu.nju.seg.isd.sisd.exception;

import org.jetbrains.annotations.NotNull;

@NotNull
public class WrongElementNumber extends ParseException {

    private final int number;

    public WrongElementNumber(int number) {
        this.number = number;
    }

    @Override
    public void printStackTrace() {
        System.err.println("Wrong Element Number: \n" +
                "the expected number of elements is 3, \n" +
                String.format("the real number is %d.", number));
        super.printStackTrace();
    }

}
