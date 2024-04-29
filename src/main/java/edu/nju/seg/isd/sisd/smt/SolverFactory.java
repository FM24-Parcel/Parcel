package edu.nju.seg.isd.sisd.smt;

import org.jetbrains.annotations.NotNull;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;

@NotNull
public class SolverFactory {

    private static final SolverFactory instance = new SolverFactory();

    private final SolverContext context;

    private final FormulaManager formulaManager;
    private final BooleanFormulaManager boolManager;

    private final IntegerFormulaManager intManager;

    private final RationalFormulaManager rationalManager;

    private final StringFormulaManager stringManager;

    public SolverFactory() {
        this.context = createContext();
        this.formulaManager = context.getFormulaManager();
        this.boolManager = formulaManager.getBooleanFormulaManager();
        this.intManager = formulaManager.getIntegerFormulaManager();
        this.rationalManager = formulaManager.getRationalFormulaManager();
        this.stringManager = formulaManager.getStringFormulaManager();
    }

    @NotNull
    public ProverEnvironment getSmtEnvironment() {
        return context.newProverEnvironment(
                SolverContext.ProverOptions.GENERATE_MODELS
        );
    }

    @NotNull
    public ProverEnvironment getUnSatEnvironment() {
        return context.newProverEnvironment(
                SolverContext.ProverOptions.GENERATE_UNSAT_CORE
        );
    }

    @NotNull
    private static SolverContext createContext() {
        Configuration configuration = Configuration.defaultConfiguration();
        try {
            LogManager logger = LogManager.createNullLogManager();
            ShutdownManager shutdown = ShutdownManager.create();
            ShutdownNotifier notifier = shutdown.getNotifier();
            return SolverContextFactory.createSolverContext(
                    configuration,
                    logger,
                    notifier,
                    SolverContextFactory.Solvers.Z3
            );
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean solve(@NotNull BooleanFormula constraint) {
        return instance.satisfiable(constraint);
    }

    private boolean satisfiable(@NotNull BooleanFormula constraint) {
        try (ProverEnvironment prover = context.newProverEnvironment()) {
            prover.addConstraint(constraint);
            if (prover.isUnsat()) {
                prover.getUnsatCore().forEach(System.out::println);
            }
            return !prover.isUnsat();
        } catch (SolverException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @NotNull
    public FormulaManager getFormulaManager() {
        return formulaManager;
    }

    @NotNull
    public BooleanFormulaManager getBoolManager() {
        return boolManager;
    }

    @NotNull
    public IntegerFormulaManager getIntManager() {
        return intManager;
    }

    @NotNull
    public RationalFormulaManager getRationalManager() {
        return rationalManager;
    }

    @NotNull
    public StringFormulaManager getStringManager() {
        return stringManager;
    }

}
