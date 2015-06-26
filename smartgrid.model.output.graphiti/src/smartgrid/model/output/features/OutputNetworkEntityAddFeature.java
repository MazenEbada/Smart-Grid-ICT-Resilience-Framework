package smartgrid.model.output.features;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;

import smartgridoutput.EntityState;
import smartgridoutput.NoUplink;
import smartgridsecurity.graphiti.ConstantProvider;
import smartgridsecurity.graphiti.helper.FeatureRepresentationHelper;
import smartgridtopo.ControlCenter;
import smartgridtopo.GenericController;
import smartgridtopo.InterCom;
import smartgridtopo.NetworkNode;
import smartgridtopo.SmartMeter;

public class OutputNetworkEntityAddFeature extends AbstractAddFeature {
	private EObject bo;

	public OutputNetworkEntityAddFeature(IFeatureProvider fp, EObject o) {
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
        
        if (bo instanceof NoUplink) {
        	drawQuestionMark(containerShape);
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
			if (((EntityState)bo).getOwner() instanceof SmartMeter) {
				return FeatureRepresentationHelper.createEllipse(containerShape, this.manageColor(new ColorConstant(140, 0, 0)), this.manageColor(new ColorConstant(225, 200, 200)));
			}
			if (((EntityState)bo).getOwner() instanceof ControlCenter) {
				return FeatureRepresentationHelper.createEllipse(containerShape, this.manageColor(new ColorConstant(0, 0, 0)), this.manageColor(new ColorConstant(124, 154, 139)));
			}
			if (((EntityState)bo).getOwner() instanceof GenericController) {
				return FeatureRepresentationHelper.createEllipse(containerShape,this.manageColor(new ColorConstant(210, 60, 0)),this.manageColor(new ColorConstant(200, 188, 168)));
			}
			if (((EntityState)bo).getOwner() instanceof InterCom) {
				return FeatureRepresentationHelper.createEllipse(containerShape,this.manageColor(new ColorConstant(0, 139, 0)),this.manageColor(new ColorConstant(199, 212, 139)));
			}
			if (((EntityState)bo).getOwner() instanceof NetworkNode) {
				return FeatureRepresentationHelper.createRect(containerShape,this.manageColor(new ColorConstant(0, 51, 102)),this.manageColor(new ColorConstant(182, 191, 204)));
			}
//		}
		return null;
	}
	
	/**
	 * Method to draw a question mark into a pe.
	 * @param containerShape the current shape
	 */
	private void drawQuestionMark(ContainerShape containerShape) {
		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
		final IGaService gaService = Graphiti.getGaService();
		//create line
        Shape lineShape = peCreateService.createShape(containerShape, false);
//        int xy[] = new int[] {4, 8, 12, 4, 8, 12, 8, 8, 8, 12, 12, 12 };
        int xy[] = new int[] {4, 2, 14, 2, 14, 8, 6, 8, 6, 15, 16, 15};//, 8, 12, 8, 8, 8, 12, 12, 12 };
        Polyline p = gaService.createPolyline(lineShape, xy);
        p.setForeground(manageColor(new ColorConstant(0, 0, 0)));
        p.setLineWidth(ConstantProvider.questionMarkLineWidth);
        
        Shape circleShape = peCreateService.createShape(containerShape, false);
        Ellipse circle = gaService.createEllipse(circleShape);
        circle.setLineWidth(ConstantProvider.questionMarkLineWidth);
        circle.setX(9);
        circle.setY(17);
        circle.setHeight(3);
        circle.setWidth(3);
        circle.setForeground(manageColor(new ColorConstant(0, 0, 0)));
	}
	
}