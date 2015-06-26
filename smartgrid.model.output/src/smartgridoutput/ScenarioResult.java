/**
 */
package smartgridoutput;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import smartgridtopo.Scenario;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Scenario Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link smartgridoutput.ScenarioResult#getEntityStates <em>Entity States</em>}</li>
 *   <li>{@link smartgridoutput.ScenarioResult#getClusters <em>Clusters</em>}</li>
 *   <li>{@link smartgridoutput.ScenarioResult#getScenario <em>Scenario</em>}</li>
 * </ul>
 *
 * @see smartgridoutput.SmartgridoutputPackage#getScenarioResult()
 * @model
 * @generated
 */
public interface ScenarioResult extends EObject {
    /**
     * Returns the value of the '<em><b>Entity States</b></em>' containment reference list.
     * The list contents are of type {@link smartgridoutput.EntityState}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Entity States</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Entity States</em>' containment reference list.
     * @see smartgridoutput.SmartgridoutputPackage#getScenarioResult_EntityStates()
     * @model containment="true"
     * @generated
     */
    EList<EntityState> getEntityStates();

    /**
     * Returns the value of the '<em><b>Clusters</b></em>' containment reference list.
     * The list contents are of type {@link smartgridoutput.Cluster}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Clusters</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Clusters</em>' containment reference list.
     * @see smartgridoutput.SmartgridoutputPackage#getScenarioResult_Clusters()
     * @model containment="true"
     * @generated
     */
    EList<Cluster> getClusters();

    /**
     * Returns the value of the '<em><b>Scenario</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Scenario</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Scenario</em>' reference.
     * @see #setScenario(Scenario)
     * @see smartgridoutput.SmartgridoutputPackage#getScenarioResult_Scenario()
     * @model
     * @generated
     */
    Scenario getScenario();

    /**
     * Sets the value of the '{@link smartgridoutput.ScenarioResult#getScenario <em>Scenario</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Scenario</em>' reference.
     * @see #getScenario()
     * @generated
     */
    void setScenario(Scenario value);

} // ScenarioResult