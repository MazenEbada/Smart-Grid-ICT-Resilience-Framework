/**
 */
package topoextension.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

import topoextension.Aggregation;
import topoextension.ExtensionRepository;
import topoextension.Replication;
import topoextension.TopoextensionPackage;

/**
 * <!-- begin-user-doc --> The <b>Adapter Factory</b> for the model. It provides an adapter
 * <code>createXXX</code> method for each class of the model. <!-- end-user-doc -->
 * 
 * @see topoextension.TopoextensionPackage
 * @generated
 */
public class TopoextensionAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected static TopoextensionPackage modelPackage;

    /**
     * Creates an instance of the adapter factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public TopoextensionAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = TopoextensionPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object. <!-- begin-user-doc
     * --> This implementation returns <code>true</code> if the object is either the model's package
     * or is an instance object of the model. <!-- end-user-doc -->
     * 
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    @Override
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject) object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch that delegates to the <code>createXXX</code> methods. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected TopoextensionSwitch<Adapter> modelSwitch = new TopoextensionSwitch<>() {
        @Override
        public Adapter caseExtensionRepository(ExtensionRepository object) {
            return createExtensionRepositoryAdapter();
        }

        @Override
        public Adapter caseReplication(Replication object) {
            return createReplicationAdapter();
        }

        @Override
        public Adapter caseAggregation(Aggregation object) {
            return createAggregationAdapter();
        }

        @Override
        public Adapter defaultCase(EObject object) {
            return createEObjectAdapter();
        }
    };

    /**
     * Creates an adapter for the <code>target</code>. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param target
     *            the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    @Override
    public Adapter createAdapter(Notifier target) {
        return modelSwitch.doSwitch((EObject) target);
    }

    /**
     * Creates a new adapter for an object of class '{@link topoextension.ExtensionRepository
     * <em>Extension Repository</em>}'. <!-- begin-user-doc --> This default implementation returns
     * null so that we can easily ignore cases; it's useful to ignore a case when inheritance will
     * catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see topoextension.ExtensionRepository
     * @generated
     */
    public Adapter createExtensionRepositoryAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link topoextension.Replication
     * <em>Replication</em>}'. <!-- begin-user-doc --> This default implementation returns null so
     * that we can easily ignore cases; it's useful to ignore a case when inheritance will catch all
     * the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see topoextension.Replication
     * @generated
     */
    public Adapter createReplicationAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link topoextension.Aggregation
     * <em>Aggregation</em>}'. <!-- begin-user-doc --> This default implementation returns null so
     * that we can easily ignore cases; it's useful to ignore a case when inheritance will catch all
     * the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see topoextension.Aggregation
     * @generated
     */
    public Adapter createAggregationAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case. <!-- begin-user-doc --> This default
     * implementation returns null. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} //TopoextensionAdapterFactory
