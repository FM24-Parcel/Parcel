package edu.nju.seg.isd.sisd.lab;

import edu.nju.seg.isd.sisd.block.BlockChecker;
import edu.nju.seg.isd.sisd.exception.ExceptionHandler;
import org.sosy_lab.java_smt.api.SolverException;

import java.io.File;

public class Lab {

    public static void main(String[] args) {
        if (args.length ==1) {
            File files = Cases.genFile(args[0]);
            new Lab().run(files, true, true);
        }
        else if (args.length > 1) {
            File files = Cases.genFile(args[0]);
            new Lab().run(files, Boolean.valueOf(args[1]), Boolean.valueOf(args[2]));
        }
        else System.out.println("please provide params about filename or option of tactic.");
    }

    public void run(File file, Boolean subducted, Boolean pruned) {
        if (!subducted)System.out.println("Tactic 1 is closed.");
        if (!pruned)System.out.println("Tactic 2 is closed.");
        var checker = new BlockChecker(file, subducted, pruned);
        try {
            checker.check();
        } catch (InterruptedException | SolverException e) {
            ExceptionHandler.handle(e);
        }
    }

}
