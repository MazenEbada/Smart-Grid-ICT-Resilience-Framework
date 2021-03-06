/**
 *
 * $Id$
 */
package smartgridoutput.validation;

import org.eclipse.emf.common.util.EList;

import smartgridoutput.Cluster;
import smartgridoutput.EntityState;
import smartgridtopo.SmartGridTopology;

/**
 * A sample validator interface for {@link smartgridoutput.ScenarioResult}. This doesn't really do
 * anything, and it's not a real EMF artifact. It was generated by the
 * org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can
 * be extended. This can be disabled with -vmargs
 * -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface ScenarioResultValidator {
    boolean validate();

    boolean validateStates(EList<EntityState> value);

    boolean validateClusters(EList<Cluster> value);

    boolean validateScenario(SmartGridTopology value);
}
