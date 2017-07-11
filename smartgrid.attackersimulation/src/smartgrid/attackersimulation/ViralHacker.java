/**
 *
 */
package smartgrid.attackersimulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import smartgrid.helper.ScenarioModelHelper;
import smartgrid.simcontrol.baselib.Constants;
import smartgrid.simcontrol.baselib.ErrorCodeEnum;
import smartgrid.simcontrol.baselib.HackingStyle;
import smartgrid.simcontrol.baselib.coupling.IAttackerSimulation;
import smartgridoutput.Cluster;
import smartgridoutput.On;
import smartgridoutput.ScenarioResult;
import smartgridtopo.NetworkNode;
import smartgridtopo.SmartGridTopology;

/**
 * This Class represents an viral operating Hacker.
 *
 * Viral hacking means that every Node that has been hacked can hack other Nodes that have a logical
 * Connction to them.
 *
 * @author Christian
 *
 */
public class ViralHacker implements IAttackerSimulation {

    private static final Logger LOG = Logger.getLogger(ViralHacker.class);

    // Private Fields
    private boolean firstRun = true;
    private boolean initDone = false;
    private int hackingSpeed;
    private HackingStyle usedHackingStyle;

    private List<Integer> seedNodeIDs;

    private NodeMode mode;

    // Temp Variables
    private SmartGridTopology myScenario;
    private ScenarioResult myResult;
    private List<On> seedNodes;

    /**
     * For ExtensionPoints .. use this together with the init() Method
     */
    public ViralHacker() {

        // Init Lists
        this.seedNodeIDs = new LinkedList<Integer>();
        this.seedNodes = new LinkedList<On>();
    }

    /**
     *
     * @param hackingSpeed
     */
    public ViralHacker(final int hackingSpeed) {

        this.hackingSpeed = hackingSpeed;
        this.mode = NodeMode.RandomNode;
        this.initDone = true;
    }

    /**
     * @param hackingSpeed
     * @param seedNodeID
     *
     */
    public ViralHacker(final int hackingSpeed, final List<Integer> seedNodeIDs) {

        this(hackingSpeed);

        this.seedNodeIDs = seedNodeIDs;
        this.mode = NodeMode.NodeIDs;
        this.initDone = true;
    }

    /**
     * @param seedNode
     * @param hackingSpeed
     */
    public ViralHacker(final List<On> seedNodes, final int hackingSpeed) {

        this(hackingSpeed);

        this.seedNodes = seedNodes;
        this.mode = NodeMode.Nodes;
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Remark Root NodeIDs {@link smartgrid.simcontrol.baselib.Constants} have to be List of String
     * !
     */
    @Override
    public ErrorCodeEnum init(final ILaunchConfiguration config) throws CoreException {

        String myModeString;
        ErrorCodeEnum myError = ErrorCodeEnum.NOT_SET;
        boolean defaultUsed = false;

        myModeString = config.getAttribute(Constants.NODE_MODE, Constants.FAIL);

        if (myModeString.equals(Constants.FAIL)) {
            myModeString = Constants.DEFAULT_NODE_MODE;
            defaultUsed = true;
        }

        // Set nodeMode for Viral Hacker
        this.mode = NodeMode.valueOf(myModeString);

        this.hackingSpeed = Integer.parseInt(config.getAttribute(Constants.HACKING_SPEED_KEY, Constants.DEFAULT_HACKING_SPEED));

        // Getting Hacking Style
        String hackingStyleString = config.getAttribute(Constants.HACKING_STYLE_KEY, Constants.FAIL);
        if (hackingStyleString.equals(Constants.FAIL)) {
            defaultUsed = true;
            hackingStyleString = Constants.DEFAULT_HACKING_STYLE;
        }

        this.usedHackingStyle = HackingStyle.valueOf(hackingStyleString);

        switch (this.mode) {
        case RandomNode:
            // Nothing else to do here ...
            break;
        case NodeIDs:

            final List<String> fail = new LinkedList<String>();
            fail.add(Constants.FAIL);

            // Getting NodeID
            final List<String> nodeIDStrings = config.getAttribute(Constants.ROOT_NODE_ID_KEY, fail);

            for (final String idStrings : nodeIDStrings) {
                if (idStrings.equals(Constants.FAIL)) {
                    defaultUsed = true;
                    this.mode = NodeMode.RandomNode;
                    break;
                }
                this.seedNodeIDs.add(Integer.parseInt(idStrings));
            }

            break;
        case Nodes:

            final String nodePath = config.getAttribute(Constants.NODE_PATH, Constants.FAIL);

            if (nodePath.equals(Constants.FAIL)) {
                defaultUsed = true;
                this.mode = NodeMode.RandomNode;
                break;
            }

            // TODO Implement in ScenarioModelHelper !
            // this.seedNodes = ScenarioModelHelper.loadNodes(nodePath);

            break;

        default:
            myError = ErrorCodeEnum.FAIL;

            break;
        }

        if (defaultUsed) {
            myError = ErrorCodeEnum.DEFAULT_VALUES_USED;
        }

        LOG.info("Hacking style is: " + this.usedHackingStyle);
        LOG.info("Hacking speed is: " + this.hackingSpeed);

        LOG.debug("Init done");

        this.initDone = true;
        return myError;
    }

    /*
     * (non-Javadoc)
     *
     * @see smartgrid.simcontrol.interfaces.IAttackerSimulation#run(smartgridtopo .Scenario,
     * smartgridoutput.ScenarioResult)
     */
    @Override
    public ScenarioResult run(final SmartGridTopology smartGridTopo, final ScenarioResult impactAnalysisOutput) {

        assert (this.initDone) : "Init wasn't run! Run init() first !";

        this.myResult = impactAnalysisOutput;
        this.myScenario = smartGridTopo;

        this.doFirstRun();

        // Rebuild Nodes List for every run because Object References change
        // between runs
        if (!this.firstRun) {
            this.buildSeedNodesList();
        }

        /* *** At this point we have valid Node(ID)Lists **** */

        this.startHacking();

        return this.myResult;
    }

    private void doFirstRun() {
        if (this.firstRun) {
            switch (this.mode) {
            case RandomNode:
                final List<On> hackedNodes = ScenarioModelHelper.getHackedNodes(this.myResult.getStates());

                // In case there are no hacked nodes, choose one randomly,
                // otherwise build seed node list
                if (hackedNodes.isEmpty()) {
                    this.chooseRandomSeedNodes();
                } else {
                    this.seedNodes = hackedNodes;
                    this.buildSeedNodeIDsList();
                }
                break;
            case NodeIDs: // --> Get Nodes
                this.buildSeedNodesList();
                break;
            case Nodes: // --> Get NodeIDs
                this.buildSeedNodeIDsList();
                break;
            }// End Swtich

            // Hack Seed Nodes
            assert (!this.seedNodes.isEmpty());

            // Done First Run operations
            this.firstRun = false;

        } // End FirstRun
    }

    /*
     *
     */
    private void startHacking() {
        // Switch Hacking Modes here
        LOG.debug("Start Hacking with Viral Hacker");
        switch (this.usedHackingStyle) {
        case BFS_HACKING:
            this.bfsHacking();
            break;
        case DFS_HACKING:
            this.dfsHacking();
            break;
        case FULLY_MESHED_HACKING:
            this.fullMeshedHacking();
            break;
        default:
            break;

        }
    }

    /*
     * Does hacking in Deep First Search manner
     */
    private void dfsHacking() {

        // --> Attention in parallel critical section needed?
        final List<On> freshHackedNodes = new LinkedList<On>();
        final int hackCount = 0;

        for (int i = 0; i < this.seedNodes.size(); i++) {

            final On node = this.seedNodes.get(i);
            final Cluster clusterToHack = this.seedNodes.get(i).getBelongsToCluster();

            /*
             * Reads from Scenario so this List don't respects changes in States of the Entities -->
             * It's only the "hardwired" logical Connection neighbors
             */
            final Map<Integer, LinkedList<Integer>> IDtoHisNeighborLinks = ScenarioModelHelper.genNeighborMapbyID(this.myScenario);

            this.dfs(clusterToHack, node, IDtoHisNeighborLinks, hackCount, freshHackedNodes);

        } // End for hacked seed nodes
        this.seedNodes.addAll(freshHackedNodes); // Attention during if parallel
        LOG.debug("Done Hacking with DFS");
    }

    /*
     * Helper Method for the DFS Hacking
     *
     *
     * @param clusterToHack
     *
     * @param Node
     *
     * @param IDtoHisNeighborLinks
     */
    private int dfs(final Cluster clusterToHack, final On node, final Map<Integer, LinkedList<Integer>> iDtoHisNeighborLinks, int hackCount, final List<On> freshHackedNodes) {

        // getting Neighbors
        final int nodeID = ScenarioModelHelper.getIDfromEntityOnState(node);

        // Getting Neighbor List with ID from Node
        // These Nodes could be in my Cluster but not sure
        final LinkedList<Integer> neighborIDList = iDtoHisNeighborLinks.get(nodeID);

        if (neighborIDList == null) {
            return this.hackingSpeed;
        }

        /*
         * Here it Filters the hardwired Logical Connections of the Neighbors out that because of
         * their State (e.g. destroyed) don't functions
         */
        final LinkedList<On> neighborOnList = ScenarioModelHelper.getNeighborsFromCluster(clusterToHack, neighborIDList);

        /* Now I have my alive (in my Cluster) Neighbor OnState List */
        for (final On neighbor : neighborOnList) {

            if (hackCount >= this.hackingSpeed) {
                break; // Stopp hacking for this run
            } else if (neighbor.isIsHacked()) {
                this.dfs(clusterToHack, neighbor, iDtoHisNeighborLinks, hackCount + 1, freshHackedNodes);
            } else if (neighbor.getOwner() instanceof NetworkNode) {
                return this.dfs(clusterToHack, neighbor, iDtoHisNeighborLinks, hackCount, freshHackedNodes);
            } else {
                LOG.debug("Hacked with DFS node " + neighbor.getOwner().getId());
                neighbor.setIsHacked(true);
                // Add fresh hacked node
                freshHackedNodes.add(neighbor);

                hackCount++;
                return this.dfs(clusterToHack, neighbor, iDtoHisNeighborLinks, hackCount, freshHackedNodes);
            }
        }
        return hackCount;
    }

    /*
     * Does hacking the bright first search way
     */
    private void bfsHacking() {

        final List<On> freshHackedNodes = new LinkedList<On>();

        for (final On hackedNode : this.seedNodes) {
            int hackedNodesCount = 0;

            // For The BFS Algo
            List<On> currentLayer = new LinkedList<On>();
            List<On> nextLayer = new LinkedList<On>();
            final Cluster clusterToHack = hackedNode.getBelongsToCluster();

            @SuppressWarnings("unused")
            int layerCount; // Not used atm but just in case

            /*
             * Reads from Scenario so this List don't respects changes in States of the Entities -->
             * It's only the "hardwired" logical Connection neighbors
             */
            final Map<Integer, LinkedList<Integer>> IDtoHisNeighborLinks = ScenarioModelHelper.genNeighborMapbyID(this.myScenario);

            // Root is in cluster to hack --> so Root is StartNode
            currentLayer.add(hackedNode);

            // Starting BFS Algorithm
            hackingDone: for (layerCount = 0; !currentLayer.isEmpty(); layerCount++) {
                for (final On Node : currentLayer) {

                    // getting Neighbors
                    final int nodeID = ScenarioModelHelper.getIDfromEntityOnState(Node);

                    // Getting Neighbor List with ID from Node
                    // These Nodes could be in my Cluster but not sure
                    final LinkedList<Integer> neighborIDList = IDtoHisNeighborLinks.get(nodeID);

                    if (neighborIDList == null) {
                        return;
                    }
                    /*
                     * Here it Filters the hardwired Logical Connections of the Neighbors out that
                     * because of their State (e.g. destroyed) don't functions
                     */
                    final LinkedList<On> neighborOnList = ScenarioModelHelper.getNeighborsFromCluster(clusterToHack, neighborIDList);

                    /*
                     * Now I have my alive (in my Cluster) Neighbor OnState List
                     */
                    for (final On neighbor : neighborOnList) {
                        if (!neighbor.isIsHacked() && !(neighbor.getOwner() instanceof NetworkNode)) {
                            LOG.debug("Hacked with BFS node " + neighbor.getOwner().getId());
                            neighbor.setIsHacked(true);

                            // Add new hacked one to seed Nodes
                            freshHackedNodes.add(neighbor);
                            hackedNodesCount++;

                            if (hackedNodesCount > this.hackingSpeed) {
                                break hackingDone;
                            }
                            nextLayer.add(neighbor);

                        }
                        // Found an hacked Node that can hack in the next Layer
                        else {
                            nextLayer.add(neighbor);
                        }
                    }
                }

                /*
                 * Q := Q' Q' := Clear
                 */
                currentLayer = nextLayer;
                nextLayer = new LinkedList<On>();

            } // Layer Loop most outer

            LOG.debug("Done hacking with BFS");

        } // End For hacked seedNodes

        // Now add new hacked Nodes to seed List
        this.seedNodes.addAll(freshHackedNodes);
    }

    /*
     * Uses the seedNodeID List to search for On Entity State in Topo
     */
    private void buildSeedNodesList() {
        this.seedNodes = new LinkedList<On>();

        for (final int id : this.seedNodeIDs) {
            final On node = ScenarioModelHelper.findEntityOnStateFromID(id, this.myResult);
            this.seedNodes.add(node);
        }
    }

    /*
     * Uses the On Entity States List to search for their ID and generates a Int List of Node IDs
     */
    private void buildSeedNodeIDsList() {
        this.seedNodeIDs = new LinkedList<Integer>();

        for (final On node : this.seedNodes) {
            final int id = ScenarioModelHelper.getIDfromEntityOnState(node);
            this.seedNodeIDs.add(id);
        }
    }

    /*
     * Each already hacked Node hacks hackingSpeed others and because of fully meshed --> every node
     * in Cluster can be a victim
     */
    private void fullMeshedHacking() {
        // foreach Cluster in the ScenarioResult
        for (final Cluster myCluster : this.myResult.getClusters()) {

            int hackedNodesinCluster = 0;
            final List<On> notHackedNodes = new LinkedList<On>();

            // Count hacked Node in that certain Cluster
            for (final On myNode : myCluster.getHasEntities()) {

                // Check if Node is already hacked --> So he is able to hack
                if (myNode.isIsHacked()) {
                    hackedNodesinCluster++;
                } else if (!(myNode.getOwner() instanceof NetworkNode)) {
                    notHackedNodes.add(myNode);
                }

            } // End Nodes in certain Cluster

            // Each Hacked Nodes in the Cluster hacks "hackingSpeed" other Nodes
            int howManyToHack = hackedNodesinCluster * this.hackingSpeed;
            final Random rand = new Random();

            while (howManyToHack > 0 && !notHackedNodes.isEmpty()) {
                final int nextID = rand.nextInt(notHackedNodes.size());
                notHackedNodes.get(nextID).setIsHacked(true);
                LOG.debug("Hacked with Full Meshed Hacking node " + notHackedNodes.get(nextID).getOwner().getId());
                notHackedNodes.remove(nextID);
                howManyToHack--;
            }
        }
        LOG.debug("Done hacking with Full Meshed Hacking");
    }

    /*
     * Expensive operation ! Assert only
     */
    @SuppressWarnings("unused")
    private boolean seedNodeIdsValid() {

        boolean iDsValid;

        for (final Integer seedNodeId : this.seedNodeIDs) {

            for (final Cluster myCluster : this.myResult.getClusters()) {
                for (final On myNode : myCluster.getHasEntities()) {
                    iDsValid = myNode.getOwner().getId() == seedNodeId;

                    if (!iDsValid) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * Generates Random Seed Nodes and builds both ID and Node (On Entity State) Lists !
     */
    private void chooseRandomSeedNodes() {
        this.seedNodes.clear();
        this.seedNodeIDs.clear();
        final Cluster myCluster[] = new Cluster[this.hackingSpeed];
        int myClusterNumber;

        final Random myRandom = new Random();

        final int clusterCount = this.myResult.getClusters().size();

        for (int i = 0; i < myCluster.length; i++) {

            do {
                myClusterNumber = myRandom.nextInt(clusterCount);
                myCluster[i] = this.myResult.getClusters().get(myClusterNumber);

            } while (myCluster[i].getHasEntities().isEmpty());

            // Get the Count of ON EntityStates in chosen Cluster
            final int entityCount = myCluster[i].getHasEntities().size();

            // Choose one by Random and make it the hacking Root Node

            final int myEntityNumber = myRandom.nextInt(entityCount); // [0 -
            // entityCount)

            final On node = myCluster[i].getHasEntities().get(myEntityNumber);

            if (!(node.getOwner() instanceof NetworkNode)) {
                node.setIsHacked(true);
                this.seedNodes.add(node);

                this.seedNodeIDs.add(node.getOwner().getId());
                break; // End seedNodes initialization since hacking should
                       // start with 1 node
            }
        } // End For
    }

    /**
     * @return the hackingSpeed
     */
    public int getHackingSpeed() {
        return this.hackingSpeed;
    }

    /**
     * @param hackingSpeed
     *            the hackingSpeed to set
     */
    public void setHackingSpeed(final int hackingSpeed) {
        this.hackingSpeed = hackingSpeed;

    }

    @Override
    public String getName() {
        return "Viral Hacker";
    }

    @Override
    public boolean enableHackingSpeed() {
        return true;
    }

    @Override
    public boolean enableRootNode() {
        return false;
    }

    @Override
    public boolean enableLogicalConnections() {
        return true;
    }
}
