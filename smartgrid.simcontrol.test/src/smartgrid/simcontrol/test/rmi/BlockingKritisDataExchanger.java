package smartgrid.simcontrol.test.rmi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import couplingToICT.PowerAssigned;
import couplingToICT.PowerSpec;
import couplingToICT.SmartComponentStateContainer;
import couplingToICT.SmartGridTopoContainer;
import couplingToICT.SimcontrolException;

/**
 * This is basically an exchanger for two different data types.
 * 
 * @author Mazen
 */
public class BlockingKritisDataExchanger {

    private static final Logger LOG = Logger.getLogger(BlockingKritisDataExchanger.class);

    private static Map<String, Map<String, PowerSpec>> bufferedDemand;
    private static Thread kritisThread;

    private static Map<String, Map<String, Double>> bufferedPower;
    
    private static Thread couplingThread;

    private static Throwable storedException;

    private static SmartComponentStateContainer scsc;
    

    private static PowerAssigned bufferedPowerAssigned;
    
    /**
     * Buffers the power supply and retrieves the demanded power. Waits if no demand is buffered.
     * This method should only be called from the coupling thread.
     * 
     * @param power
     *            supply that was determined by the OPF analysis. Cannot be null.
     * @return the demanded power that was buffered by the KRITIS simulation
     * @throws InterruptedException
     *             if the simulation is interrupted by the user
     */
    public static synchronized Map<String, Map<String, PowerSpec>> bufferSupplyGetDemand(Map<String, Map<String, Double>> power) throws InterruptedException {
        assert bufferedPower == null;
        assert power != null;

        // provide own data
        bufferedPower = power;
        couplingThread = Thread.currentThread();
        BlockingKritisDataExchanger.class.notifyAll();

        // wait for data
        while (bufferedDemand == null) {
            LOG.info("SimControl is waiting for data of Kritis simulation.");
            BlockingKritisDataExchanger.class.wait();
        }

        // consume data
        Map<String, Map<String, PowerSpec>> tempDemand = bufferedDemand;
        bufferedDemand = null;
        couplingThread = null;
        LOG.info("SimControl has finished its exchange.");
        return tempDemand;
    }

    /**
     * Buffers the power demand and retrieves the power supply. Waits if no demand is buffered. This
     * method should only be called from the coupling thread.
     * 
     * @param demand
     *            the requested power from the KRITIS simulation. Cannot be null.
     * @return supply that was determined by the OPF analysis
     * @throws InterruptedException
     *             if the simulation is interrupted by the user
     * @throws Throwable
     */
    public static synchronized Map<String, Map<String, Double>> bufferDemandGetSupply(Map<String, Map<String, PowerSpec>> demand) throws Throwable {
        assert bufferedDemand == null;

        if (demand == null) {
            throw new SimcontrolException("Power demand cannot be null.");
        }
        hasExceptionOccured();

        // provide own data
        bufferedDemand = demand;
        kritisThread = Thread.currentThread();
        BlockingKritisDataExchanger.class.notifyAll();

        // wait for data
        while (bufferedPower == null) {
            hasExceptionOccured();
            LOG.info("The Kritis simulation is waiting for data of SimControl.");
            BlockingKritisDataExchanger.class.wait();
        }

        hasExceptionOccured();

        // consume data
        Map<String, Map<String, Double>> tempPower = bufferedPower;
        bufferedPower = null;
        kritisThread = null;
        LOG.info("The Kritis simulation has finished its exchange.");
        return tempPower;
    }
    
    
    /**
     * @author Mazen Ebada
     * @param power
     * @return
     * @throws Throwable
     */
    public static synchronized SmartComponentStateContainer bufferPAgetSCSC(PowerAssigned power) throws Throwable {
        
        assert bufferedPower == null;
        assert power != null;
        
        
        bufferedPowerAssigned = power;
        couplingThread = Thread.currentThread();
        BlockingKritisDataExchanger.class.notifyAll();

        // wait for data
        while (scsc == null) {
            LOG.info("SimControl is waiting for the smart component state container.");
            BlockingKritisDataExchanger.class.wait();
        }
        
        // consume data
        SmartComponentStateContainer tempStateContainer = scsc;
        scsc = null;
        couplingThread = null;
        LOG.info("SimControl has finished its exchange.");
        
        return tempStateContainer;
    }
    
    public PowerAssigned getPowerAssigned() throws Throwable {
        
        couplingThread = Thread.currentThread();
        
        // wait for data
        while (bufferedPowerAssigned == null) {
            LOG.info("SimControl is waiting for the PowerAssigned.");
            BlockingKritisDataExchanger.class.wait();
        }
        couplingThread = null;
        
        return bufferedPowerAssigned;
        
    }
    
    public void storeSCSC (SmartComponentStateContainer smartComponentStateContainer) {
        couplingThread = Thread.currentThread();
        scsc = smartComponentStateContainer;
        BlockingKritisDataExchanger.class.notifyAll();

    }

    private static void hasExceptionOccured() throws Throwable {
        if (storedException != null) {
            LOG.info("Passing exception to KRITIS simulation.");
            throw storedException;
        }
    }

    public static synchronized void storeException(Throwable e) {
        storedException = e;
        BlockingKritisDataExchanger.class.notifyAll();
    }

    private static SmartGridTopoContainer bufferedTopoData;

    public static synchronized void storeGeoData(SmartGridTopoContainer topoData) {
        if (bufferedTopoData != null) {
            LOG.error("There was already geo data present. This data is now overwritten.");
        }
        BlockingKritisDataExchanger.bufferedTopoData = topoData;
        BlockingKritisDataExchanger.class.notifyAll();
    }

    public static synchronized SmartGridTopoContainer getTopoData() throws InterruptedException {
        couplingThread = Thread.currentThread();

        // wait for data
        while (bufferedTopoData == null) {
            LOG.info("SimControl is waiting for topo data from the Kritis simulation.");
            BlockingKritisDataExchanger.class.wait();
        }

        // consume data
        SmartGridTopoContainer tempGeoData = bufferedTopoData;
        bufferedTopoData = null;
        couplingThread = null;
        LOG.info("SimControl got geo data from the Kritis simulation.");
        return tempGeoData;
    }

    public static synchronized boolean freeAll() {

        boolean threadFreed = couplingThread != null || kritisThread != null;

        // free coupling thread
        if (couplingThread != null) {
            couplingThread.interrupt();
            couplingThread = null;
            LOG.info("The SimControl thread was interrupted and freed from the data exchange sync.");
        }

        if (kritisThread != null) {
            // only free kritisThread if there was no exception (the KRITIS thread will be freed by the exception throw)
            if (storedException == null) {
                kritisThread.interrupt();
                LOG.info("The Kritis simulation thread was interrupted and freed from the data exchange sync.");
            }
            kritisThread = null;
        }

        // clear all buffered data
        bufferedDemand = null;
        bufferedPower = null;
        bufferedTopoData = null;
        storedException = null;
        LOG.info("All data was cleared.");

        return threadFreed;
    }
}
