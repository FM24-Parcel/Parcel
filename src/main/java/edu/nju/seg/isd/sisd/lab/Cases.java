package edu.nju.seg.isd.sisd.lab;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

@NotNull
public class Cases {

    private static final String base = "test-cases";

    public static final File case_bug = genCaseFile("Case-Bug");

    public static final File case_fix = genCaseFile("Case-Fix");

    public static final File case_test = genCaseFile("Case-Test");

    public static final File case_test2 = genCaseFile("Case-Test2");

    public static final File case_topology = genCaseFile("Case-Topology");

    public static final File int_alt = genCaseFile("Int-Alt");

    public static final File int_bug = genCaseFile("Int-Bug");

    public static final File mask_int = genCaseFile("Mask-Int");

    public static final File simple_alt = genCaseFile("Simple-Alt");

    public static final File simple_alt_loop = genCaseFile("Simple-Alt-Loop");

    public static final File simple_loop = genCaseFile("Simple-Loop");

    public static final File car_controller = genCaseFile("Car-Controller");

    public static final File spin_comp = genCaseFile("Spin-Comp");

    public static final File spin_comp_plus = genCaseFile("Spin-Comp-Plus");

    public static final File task_rotate = genCaseFile("Task-Rotate");

    public static final File triple_int = genCaseFile("Triple-Int");

    public static final File test = genCaseFile("Test");

    public static final File ADC_Bug = genCaseFile("ADC-Bug");
    public static final File altitude_display = genCaseFile("Altitude-Display");
    public static final File backup_computing = genCaseFile("Backup-Computing");
    public static final File fridge_controller = genCaseFile("Fridge-Controller");
    public static final File medical_monitor = genCaseFile("Medical-Monitor");
    public static final File orbit_upload= genCaseFile("Orbit-Upload");
    public static final File system_tick= genCaseFile("System-Tick");
    public static final File time_sync= genCaseFile("Time-Sync");

    public static List<File> cases = List.of(
            case_bug,
            case_fix,
            case_test,
            case_test2,
            case_topology,
            int_alt,
            int_bug,
            mask_int,
            simple_alt,
            simple_alt_loop,
            simple_loop,
            spin_comp,
            spin_comp_plus,
            triple_int,
            car_controller,
            task_rotate);

    private static String genCasePath(String name) {
        return base + "/" + name + "/" + name + ".uxf";
    }

    static File genFile(String name) {
        return new File(name + ".uxf");
//        if (name.startsWith("llm-case")) {
//            return new File( name + ".uxf");
//        }
//        else return new File(base + "/" + name + "/" + name + ".uxf");
    }

    private static File genCaseFile(String name) {
        return new File(genCasePath(name));
    }
}
