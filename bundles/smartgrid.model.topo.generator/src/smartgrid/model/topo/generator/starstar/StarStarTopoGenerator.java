package smartgrid.model.topo.generator.starstar;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import couplingToICT.SmartComponentGeoData;
import couplingToICT.SmartGridTopoContainer;
import smartgrid.helper.UIDHelper;
import smartgrid.model.topo.generator.AbstractTopoGenerator;
import smartgridtopo.ControlCenter;
import smartgridtopo.NetworkNode;
import smartgridtopo.PowerGridNode;
import smartgridtopo.SmartGridTopology;
import smartgridtopo.SmartMeter;
import smartgridtopo.SmartgridtopoFactory;
import smartgridtopo.impl.SmartgridtopoPackageImpl;

public class StarStarTopoGenerator extends AbstractTopoGenerator {

    private static final Logger LOG = Logger.getLogger(StarStarTopoGenerator.class);

    @Override
    public SmartGridTopology generateTopo(SmartGridTopoContainer topoData) {

        LOG.info("Starting generation of StarStar ICT topology.");

        HashMap<String, Map<String, SmartComponentGeoData>> smartMeterGeoData = topoData.getSmartMeterContainer();

        // create root container
        SmartgridtopoPackageImpl.init();
        final SmartgridtopoFactory topoFactory = SmartgridtopoFactory.eINSTANCE;
        final SmartGridTopology topo = topoFactory.createSmartGridTopology();
        topo.setId(UIDHelper.generateUID());

        // create command center
        ControlCenter controlCenter = topoFactory.createControlCenter();
        controlCenter.setId(UIDHelper.generateUID());
        topo.getContainsNE().add(controlCenter);
        
        // iterate nodes
        for (Entry<String, Map<String, SmartComponentGeoData>> nodeEntry : smartMeterGeoData.entrySet()) {

            PowerGridNode powerGridNode = null;

            // create network node
            NetworkNode networkNode = topoFactory.createNetworkNode();
            
            // iterate smart meters
            for (Entry<String, ?> smartMeterEntry : nodeEntry.getValue().entrySet()) {

                // create node
                powerGridNode = topoFactory.createPowerGridNode();
                setNameAndId(smartMeterEntry, powerGridNode, "_PGN");
                topo.getContainsPGN().add(powerGridNode);

                // create smart meter
                SmartMeter smartMeter = topoFactory.createSmartMeter();
                setNameAndId(smartMeterEntry, smartMeter);
                topo.getContainsNE().add(smartMeter);

                // connect to power
                smartMeter.getConnectedTo().add(powerGridNode);

                // connect the smart meter
                createPhysicalConnection(topoFactory, topo, networkNode, smartMeter);
                createLogicalConnection(topoFactory, topo, controlCenter, smartMeter);
            }

            if (powerGridNode != null) {
                networkNode.getConnectedTo().add(powerGridNode);
                networkNode.setId(UIDHelper.generateUID());
                networkNode.setName("NetworkOfNode_" + nodeEntry.getKey());
                topo.getContainsNE().add(networkNode);

                // chain the network
                createPhysicalConnection(topoFactory, topo, controlCenter, networkNode);
            } else {
                LOG.info("Node " + nodeEntry.getKey() + " has no prosumers. I will not create a NetworkNode.");
            }
        }

        if (topo.getContainsPGN().size() > 0) {
            // connect command center to power
            PowerGridNode firstNode = topo.getContainsPGN().get(0);
            controlCenter.getConnectedTo().add(firstNode);
        } else {
            LOG.error("The generated topology does not have any power nodes. No meaningful results will be produced.");
        }

        LOG.info("Generation of StarStar ICT topology finished.");

        return topo;
    }
}
