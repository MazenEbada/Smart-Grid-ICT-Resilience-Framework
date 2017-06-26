package smartgrid.simcontrol;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.util.EcoreUtil;

import smartgrid.helper.FileSystemHelper;
import smartgrid.helper.ScenarioModelHelper;
import smartgrid.helper.SimulationExtensionPointHelper;
import smartgrid.log4j.LoggingInitializer;
import smartgrid.simcontrol.baselib.coupling.IAttackerSimulation;
import smartgrid.simcontrol.baselib.coupling.IImpactAnalysis;
import smartgrid.simcontrol.baselib.coupling.IPowerLoadSimulationWrapper;
import smartgrid.simcontrol.baselib.coupling.ITerminationCondition;
import smartgrid.simcontrol.coupling.ISimulationController;
import smartgrid.simcontrol.coupling.ISmartMeterState;
import smartgrid.simcontrol.coupling.PowerSpec;
import smartgrid.simcontrol.coupling.SimcontrolException;
import smartgrid.simcontrol.coupling.SmartMeterState;
import smartgridinput.PowerState;
import smartgridinput.ScenarioState;
import smartgridoutput.Defect;
import smartgridoutput.EntityState;
import smartgridoutput.NoPower;
import smartgridoutput.NoUplink;
import smartgridoutput.On;
import smartgridoutput.Online;
import smartgridoutput.ScenarioResult;
import smartgridtopo.NetworkEntity;
import smartgridtopo.SmartGridTopology;
import smartgridtopo.SmartMeter;

public final class StaticSimulationController implements ISimulationController {

    private static final Logger LOG = Logger.getLogger(StaticSimulationController.class);

    private IPowerLoadSimulationWrapper powerLoadSimulation;
    private IImpactAnalysis impactAnalsis;
    private IAttackerSimulation attackerSimulation;
    private ITerminationCondition terminationCondition;

    private String workingDirPath;
    private SmartGridTopology topo;
    private ScenarioState initialState;

    private FileAppender fileAppender;

    // Simulation State
    private int timeStep;
    private ScenarioState impactInputOld;
    private ScenarioState impactInput;
    private ScenarioResult impactResultOld;
    private Map<String, Map<String, Double>> powerSupply;

    public StaticSimulationController() {
        timeStep = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see smartgrid.simcontrol.ISimulationController#run(java.util.Map)
     */
    @Override
    public Map<String, Map<String, Double>> run(Map<String, Map<String, PowerSpec>> kritisPowerDemand) {

        // Compute Initial Impact Analysis Result
        ScenarioResult impactResult = impactAnalsis.run(topo, impactInput);

        // Generates Path with default System separators
        final String timeStepPath = new File(workingDirPath + "\\Zeitschritt " + timeStep).getPath();

        impactResult = attackerSimulation.run(topo, impactResult);

        // save attack result to file
        final String attackResultFile = new File(timeStepPath + "\\AttackerSimulationResult.smartgridoutput").getPath();
        FileSystemHelper.saveToFileSystem(impactResult, attackResultFile);

        // impact and power may iterate several times
        int innerLoopIterationCount = 0;
        do {
            final String iterationPath = new File(timeStepPath + "\\Iteration " + innerLoopIterationCount).getPath();

            // run power load simulation
            final Map<String, SmartMeterState> smartMeterStates = convertToPowerLoadInput(impactResult);
//            powerSupply = powerLoadSimulation.run(kritisPowerDemand, smartMeterStates); // TODO invoke interface correctly
            powerSupply = powerLoadSimulation.run(new HashMap<String, Map<String, PowerSpec>>(), new HashMap<String, Map<String, ISmartMeterState>>());

            // copy the input
            impactInputOld = EcoreUtil.copy(impactInput);

            // convert input for impact analysis
            updateImactAnalysisInput(impactInput, impactResult, powerSupply);

            // Save input to file
            final String inputFile = new File(iterationPath + "\\PowerLoadResult.smartgridinput").getPath();
            FileSystemHelper.saveToFileSystem(impactInput, inputFile);

            impactResultOld = impactResult;
            impactResult = impactAnalsis.run(topo, impactInput);

            // Save Result
            final String resultFile = new File(iterationPath + "\\ImpactResult.smartgridoutput").getPath();
            FileSystemHelper.saveToFileSystem(impactResult, resultFile);

            //generate report
            final File resultReportPath = new File(iterationPath + "\\ResultReport.csv");
            ReportGenerator.saveScenarioResult(resultReportPath, impactResult);

            innerLoopIterationCount++;
        } while (terminationCondition.evaluate(innerLoopIterationCount, impactInput, impactInputOld, impactResult, impactResultOld));

        LOG.info("Time step " + timeStep + " terminated after " + innerLoopIterationCount + " power/impact iterations");

        return powerSupply;
    }

    /*
     * (non-Javadoc)
     * 
     * @see smartgrid.simcontrol.ISimulationController#shutDown()
     */
    @Override
    public void shutDown() {
        // remove file appender of this run
        Logger.getRootLogger().removeAppender(fileAppender);
        fileAppender.close();
    }

    // Private Methods

    private Map<String, SmartMeterState> convertToPowerLoadInput(final ScenarioResult impactResult) {
        // TODO update to conform to new interfaces?
        final Map<String, SmartMeterState> smartMeterStates = new HashMap<String, SmartMeterState>();
        for (final EntityState state : impactResult.getStates()) {
            final NetworkEntity stateOwner = state.getOwner();
            if (stateOwner instanceof SmartMeter) {
                final String id = Integer.toString(stateOwner.getId());
                smartMeterStates.put(id, stateToEnum(state));
            }
        }
        return smartMeterStates;
    }

    private SmartMeterState stateToEnum(EntityState state) {
        if (state instanceof Online) {
            if (((Online) state).isIsHacked()) {
                return SmartMeterState.ONLINE_HACKED;
            } else {
                return SmartMeterState.ONLINE;
            }
        } else if (state instanceof NoUplink) {
            if (((NoUplink) state).isIsHacked()) {
                return SmartMeterState.NO_UPLINK_HACKED;
            } else {
                return SmartMeterState.NO_UPLINK;
            }
        } else if (state instanceof NoPower) {
            return SmartMeterState.NO_POWER;
        } else if (state instanceof Defect) {
            return SmartMeterState.DEFECT;
        }
        throw new RuntimeException("Unknown EntityState");
    }

    /**
     * 
     * @param impactInput
     * @param impactResult
     * @param powerSupply
     * @return
     */
    private void updateImactAnalysisInput(final ScenarioState impactInput, final ScenarioResult impactResult, Map<String, Map<String, Double>> powerSupply) {

        // TODO update to conform to new interfaces?
        //Transfer hacked state into next input
        for (final EntityState state : impactResult.getStates()) {
            final boolean hackedState = state instanceof On && ((On) state).isIsHacked();
            final NetworkEntity owner = state.getOwner();
            for (final smartgridinput.EntityState inputEntityState : impactInput.getEntityStates()) {
                if (inputEntityState.getOwner().equals(owner)) {
                    inputEntityState.setIsHacked(hackedState);
                    break;
                }
            }
        }

        //Transfer power supply state into next input
        for (final PowerState inputPowerState : impactInput.getPowerStates()) {
            final String id = Integer.toString(inputPowerState.getOwner().getId());

            // iterate over node entries
            for (final Entry<String, Map<String, Double>> powerForNode : powerSupply.entrySet()) {
                Map<String, Double> value = powerForNode.getValue();
                Double supply = value.get(id);
                if (supply != null) {
                    inputPowerState.setPowerOutage(supply == 0.0d); //TODO when is there really a power outage? what todo with aggregated states?
                }
            }
        }
    }

    /*
     * Inits some things for all runs. Not for Interface based hook in !
     *
     * Does: # Generates Output Path String # Inits the Simulations
     */
    /*
     * (non-Javadoc)
     * 
     * @see smartgrid.simcontrol.ISimulationController#init(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void init(String outputPath, String topoPath, String inputStatePath) throws SimcontrolException {

        LoggingInitializer.initialize();

        LOG.debug("loading launch config");

        workingDirPath = determineWorkingDirPath(outputPath);

        // add fileappender for local logs
        final Logger rootLogger = Logger.getRootLogger();
        try {
            final Layout layout = ((Appender) rootLogger.getAllAppenders().nextElement()).getLayout();
            fileAppender = new FileAppender(layout, workingDirPath + "\\log.log");
            rootLogger.addAppender(fileAppender);
        } catch (final IOException e) {
            throw new RuntimeException("Error creating local log appender in the working directory. Most likely there are problems with access rights.");
        }

        // load models
        initialState = ScenarioModelHelper.loadInput(inputStatePath);
        impactInput = initialState;
        topo = ScenarioModelHelper.loadScenario(topoPath);
        LOG.info("Scenario input state: " + inputStatePath);
        LOG.info("Topology: " + topoPath);

        // Retrieve simulations from extension points
        try {
            loadCustomUserAnalysis();
        } catch (CoreException e) {
            throw new SimcontrolException("Individual simulations could not be created", e);
        }

        //TODO: currently, these are all mocks, so no init is needed atm
        // Init all simulations
//        powerLoadSimulation.init(launchConfig);
//        impactAnalsis.init(launchConfig);
//        attackerSimulation.init(launchConfig);
//        terminationCondition.init(launchConfig);

        LOG.info("Using power load simulation: " + powerLoadSimulation.getName());
        LOG.info("Using impact analysis: " + impactAnalsis.getName());
        LOG.info("Using attacker simulation: " + attackerSimulation.getName());
        LOG.info("Using termination condition: " + terminationCondition.getName());
    }

    private String determineWorkingDirPath(String initialPath) {
        initialPath = removeTrailingSeparator(initialPath);
        String currentPath = initialPath;
        int runningNumber = 0;
        while (new File(currentPath).exists()) {
            LOG.info("Exists already: " + currentPath);

            currentPath = initialPath + runningNumber + '\\';
            runningNumber++;
        }
        LOG.info("Working dir is: " + currentPath);
        return currentPath;
    }

    private String removeTrailingSeparator(String initialPath) {
        if (initialPath.endsWith("\\")) {
            return initialPath.substring(0, initialPath.length() - 1);
        }
        return initialPath;
    }

    private void loadCustomUserAnalysis() throws CoreException {
        final SimulationExtensionPointHelper helper = new SimulationExtensionPointHelper();

        final List<IAttackerSimulation> attack = helper.getAttackerSimulationExtensions();
        for (final IAttackerSimulation e : attack) {

            if ("No Attack Simulation".equals(e.getName())) {
                attackerSimulation = e;
            }
        }

        final List<IPowerLoadSimulationWrapper> power = helper.getPowerLoadSimulationExtensions();
        for (final IPowerLoadSimulationWrapper e : power) {

            if ("Mock".equals(e.getName())) {
                powerLoadSimulation = e;
            }
        }

        final List<ITerminationCondition> termination = helper.getTerminationConditionExtensions();
        for (final ITerminationCondition e : termination) {

            if ("Iteration Count".equals(e.getName())) {
                terminationCondition = e;
            }
        }

        final List<IImpactAnalysis> impact = helper.getImpactAnalysisExtensions();
        for (final IImpactAnalysis e : impact) {

            if ("Mock".equals(e.getName())) {
                impactAnalsis = e;
            }
        }
    }
}
