package smartgrid.simcontrol.wrapper.powerloadsimulation;

import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;

import smartgrid.simcontrol.baselib.ErrorCodeEnum;
import smartgrid.simcontrol.baselib.coupling.IPowerLoadSimulationWrapper;
import smartgrid.simcontrol.coupling.IPowerLoadSimulation;
import smartgrid.simcontrol.coupling.ISmartMeterState;
import smartgrid.simcontrol.coupling.PowerSpec;
import smartgrid.simcontrol.iip.PowerLoadSimulation;

public class PowerLoadSimulationWrapper implements IPowerLoadSimulationWrapper {

    private IPowerLoadSimulation powerSim;

    @Override
    public ErrorCodeEnum init(final ILaunchConfiguration config) {
        powerSim = new PowerLoadSimulation();
        return ErrorCodeEnum.SUCCESS;
    }

    @Override
    public String getName() {
        return "Power Load Simulation Wrapper";
    }

    @Override
    public Map<String, Map<String, Double>> run(Map<String, Map<String, PowerSpec>> kritisDemands, Map<String, Map<String, ISmartMeterState>> smartMeterStates) {
        return powerSim.run(kritisDemands, smartMeterStates);
    }
}
