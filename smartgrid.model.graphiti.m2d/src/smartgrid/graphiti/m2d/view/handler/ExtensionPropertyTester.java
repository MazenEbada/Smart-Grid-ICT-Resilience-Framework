package smartgrid.graphiti.m2d.view.handler;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.viewers.TreeSelection;

@SuppressWarnings("restriction")
public class ExtensionPropertyTester extends PropertyTester {

    public ExtensionPropertyTester() {

    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        boolean result = false;
        if (receiver instanceof TreeSelection) {
            Object element = ((TreeSelection) receiver).getFirstElement();
            if (element instanceof File) {
                String extension = ((File) element).getFileExtension();
                if (extension != null) {
                    result = extension.equals(expectedValue);
                }
            }
        }
        return result;
    }

}