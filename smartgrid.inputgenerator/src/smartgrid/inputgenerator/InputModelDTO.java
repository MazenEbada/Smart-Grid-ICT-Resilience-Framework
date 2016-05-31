/**
 *
 */
package smartgrid.inputgenerator;

import smartgridinput.ScenarioState;
import smartgridtopo.SmartGridTopology;

/**
 * @author Christian
 *
 */
public class InputModelDTO {

    private SmartGridTopology myScenarioTopo;
    private ScenarioState myScenarioStates;

    /**
     * @return the myScenarioTopo
     */
    public SmartGridTopology getMyScenarioTopo() {
        return this.myScenarioTopo;
    }

    /**
     * @param myScenarioTopo
     *            the myScenarioTopo to set
     */
    public void setMyScenarioTopo(final SmartGridTopology myScenarioTopo) {
        this.myScenarioTopo = myScenarioTopo;
    }

    /**
     * @return the myScenarioStates
     */
    public ScenarioState getMyScenarioStates() {
        return this.myScenarioStates;
    }

    /**
     * @param myScenarioStates
     *            the myScenarioStates to set
     */
    public void setMyScenarioStates(final ScenarioState myScenarioStates) {
        this.myScenarioStates = myScenarioStates;
    }

    /**
     * Constructs a Input Model Data Transfer Object
     *
     * @param myScenarioTopo
     * @param myScenarioStates
     */
    public InputModelDTO(final SmartGridTopology myScenarioTopo, final ScenarioState myScenarioStates) {
        super();
        this.myScenarioTopo = myScenarioTopo;
        this.myScenarioStates = myScenarioStates;
    }
}