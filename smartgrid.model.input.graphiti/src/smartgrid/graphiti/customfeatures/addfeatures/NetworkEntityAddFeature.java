package smartgrid.graphiti.customfeatures.addfeatures;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;

import smartgridinput.EntityState;
import smartgridsecurity.graphiti.ConstantProvider;
import smartgridsecurity.graphiti.helper.FeatureRepresentationHelper;
import smartgridtopo.ControlCenter;
import smartgridtopo.GenericController;
import smartgridtopo.InterCom;
import smartgridtopo.NetworkNode;
import smartgridtopo.SmartMeter;

/**
 * Custom AddFeature to draw network entities pe's for input models.
 * @author mario
 *
 */
public class NetworkEntityAddFeature extends AbstractAddFeature {
	private EObject bo;

	public NetworkEntityAddFeature(IFeatureProvider fp, EObject o) {
		super(fp);
		bo =o;
	}

	@Override
	public boolean canAdd(IAddContext context) {
		return true;
	}

	@Override
	public PictogramElement add(IAddContext context) {
        final Diagram targetDiagram = (Diagram) context.getTargetContainer();

        // CONTAINER SHAPE
        final IPeCreateService peCreateService = Graphiti.getPeCreateService();
        final ContainerShape containerShape = peCreateService.createContainerShape(targetDiagram, true);

        // add a chopbox anchor to the shape
        peCreateService.createChopboxAnchor(containerShape);

        // define a default size for the shape
        // int width = 100;
        // int height = 50;
        final IGaService gaService = Graphiti.getGaService();
        final GraphicsAlgorithm shape = getGraphicalPatternRepresentation(containerShape);
        gaService.setLocationAndSize(shape, context.getX(), context.getY(), 20, 20);
        
        if (bo instanceof EntityState && ((EntityState)bo).isIsDestroyed()==true) {
        	drawDestroyed(containerShape);
        }
      //  gaService.setLocationAndSize(p, context.getX(), context.getY(), 20, 20);
        link(containerShape, context.getNewObject());

        return containerShape;
	}
	
	/**
	 * Draw a specific network entity.
	 * @param containerShape the current container shape
	 * @return the specific network entity ga
	 */
	private GraphicsAlgorithm getGraphicalPatternRepresentation(
			ContainerShape containerShape) {
		if (bo instanceof EntityState && ((EntityState)bo).getOwner() instanceof SmartMeter) {
			return FeatureRepresentationHelper.createEllipse(containerShape, this.manageColor(new ColorConstant(140, 0, 0)), this.manageColor(new ColorConstant(255, 0, 0)));
		}
		if (bo instanceof EntityState && ((EntityState)bo).getOwner() instanceof ControlCenter) {
			return FeatureRepresentationHelper.createEllipse(containerShape, this.manageColor(new ColorConstant(0, 0, 0)), this.manageColor(new ColorConstant(51, 102, 0)));
		}
		if (bo instanceof EntityState && ((EntityState)bo).getOwner() instanceof GenericController) {
			return FeatureRepresentationHelper.createEllipse(containerShape,this.manageColor(new ColorConstant(210, 60, 0)),this.manageColor(new ColorConstant(255, 140, 0)));
		}
		if (bo instanceof EntityState && ((EntityState)bo).getOwner() instanceof InterCom) {
			return FeatureRepresentationHelper.createEllipse(containerShape,this.manageColor(new ColorConstant(0, 139, 0)),this.manageColor(new ColorConstant(127, 255, 0)));
		}
		if (bo instanceof EntityState && ((EntityState)bo).getOwner() instanceof NetworkNode) {
			return FeatureRepresentationHelper.createRect(containerShape,this.manageColor(new ColorConstant(0, 51, 102)),this.manageColor(new ColorConstant(0, 102, 204)));
		}
		return null;
	}
	
	/**
	 * Method to draw a cross. This is used to show that a network entity is destroyed.
	 * @param containerShape the current container shape
	 */
	private void drawDestroyed(ContainerShape containerShape) {
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		final IGaService gaService = Graphiti.getGaService();
		//create line
        Shape lineShape = peCreateService.createShape(containerShape, false);
        int xy[] = new int[] {0, 20, 20, 0 };
        Polygon p = gaService.createPolygon(lineShape, xy);
        p.setForeground(manageColor(new ColorConstant(0, 0, 0)));
        p.setLineWidth(ConstantProvider.shapeLineWidth);
        xy = new int[] {0, 0, 20, 20 };
        Shape secondLine = peCreateService.createShape(containerShape, false);
        Polygon poly = gaService.createPolygon(secondLine, xy);
        poly.setForeground(manageColor(new ColorConstant(0, 0, 0)));
        poly.setLineWidth(ConstantProvider.shapeLineWidth);
	}

}