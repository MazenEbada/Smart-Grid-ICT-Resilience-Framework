/**
 * 
 */
package smartgrid.simcontrol.test.rmi;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

import smartgrid.log4j.LoggingInitializer;
import smartgrid.simcontrol.test.rmi.RmiServer;

/**
 * @author mazenebada
 *
 */
public class Startup implements IStartup {

    private static final Logger LOG = Logger.getLogger(Startup.class);
    
    @Override
    public void earlyStartup() {
        LoggingInitializer.initialize();
        LOG.info("Performing early startup");
        RmiServer.ensureRunning();
        
    }

}
