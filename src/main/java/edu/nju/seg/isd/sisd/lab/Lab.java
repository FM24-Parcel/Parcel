package edu.nju.seg.isd.sisd.lab;

import edu.nju.seg.isd.sisd.block.BlockChecker;
import edu.nju.seg.isd.sisd.exception.ExceptionHandler;
import org.sosy_lab.java_smt.api.SolverException;

import static edu.nju.seg.isd.sisd.lab.Cases.*;

public class Lab {

    public static void main(String[] args) {
        new Lab().run();
    }

    public void run() {
        var checker = new BlockChecker(ADC_Bug,true,true);
        try {
        checker.check();
        } catch (InterruptedException | SolverException e) {
            ExceptionHandler.handle(e);
        }
    }

}
