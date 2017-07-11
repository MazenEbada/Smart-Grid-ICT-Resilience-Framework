/*******************************************************************************
 * Copyright (c) 2012 University of Luxembourg and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Max E. Kramer - initial API and implementation
 ******************************************************************************/

package tools.vitruv.framework.util.bridges;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;

/**
 * A utility class hiding details of the Eclipse Modeling Framework API for recurring tasks that are
 * not project-specific. Methods for Ecore metamodels, Ecore metamodel creation, Ecore metamodel
 * variants and Ecore resources should not be put in this utility but in the corresponding separate
 * utilities!<br/>
 * <br/>
 * (Note that it is disputable whether this class conforms to the bridge pattern as we are currently
 * only providing one implementation and the "abstractions" can be regarded as low-level.)
 *
 * @author Max E. Kramer
 *
 * @see EcoreBridge
 * @see EcoreFactoryBridge
 * @see EcorePkgVariantsBridge
 * @see EcoreResourceBridge
 */
public final class EMFBridge {
    /** Utility classes should not have a public or default constructor. */
    private EMFBridge() {
    }

    /**
     * Creates and returns an EMF platform resource URI for the given Eclipse resource.
     *
     * @param iResource
     *            an Eclipse resource
     * @return a platform resource URI for the resource
     */
    public static URI getEMFPlatformUriForIResource(final IResource iResource) {
        return URI.createPlatformResourceURI(iResource.getFullPath().toString(), true);
    }

    /**
     * Creates and returns a new Eclipse path for the given EMF URI.
     *
     * @param uri
     *            an EMF URI
     * @return a new Eclipse path for the given URI
     */
    public static IPath getIPathForEMFUri(final URI uri) {
        if (uri.isPlatform()) {
            return new Path(uri.toPlatformString(true));
        } else if (uri.isFile()) {
            return new Path(uri.toFileString());
        }
        throw new UnsupportedOperationException("Getting the path is currently only implemented for file and platform URIs.");
    }

    /**
     * Returns an Eclipse file for the given EMF URI.
     *
     * @param uri
     *            an EMF URI
     * @return an Eclipse file for the given URI
     */
    public static IFile getIFileForEMFUri(final URI uri) {
        if (uri.isPlatform()) {
            IPath path = getIPathForEMFUri(uri);
            return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        }
        throw new UnsupportedOperationException("Getting the IFile is currently only implemented for platform URIs. URI: " + uri);
    }

    /**
     * Return whether a resource exists at the specified {@link URI}.
     * 
     * @param uri
     *            an EMF URI
     * @return true if a resource exists at the {@link URI}, false otherwise
     */
    public static boolean existsResourceAtUri(final URI uri) {
        if (uri.isPlatform()) {
            return getIFileForEMFUri(uri).exists();
        } else if (uri.isFile()) {
            return new File(uri.toFileString()).exists();
        }
        throw new UnsupportedOperationException("Checking if a resource at an URI exists is currently only implemented for file and platform URIs.");
    }

    /**
     * Creates a new URI from the given URI by appending the given string to the file name of the
     * URI (i.e. before the dot "." and the file extension)
     *
     * @param uri
     *            an URI
     * @param toAppend
     *            the string to be appended to the file name of the URI
     * @return the new URI
     */
    public static URI newURIWithStringAppendedToFilename(final URI uri, final String toAppend) {
        String fileExt = uri.fileExtension();
        if (fileExt != null) {
            URI uriWithoutFileExt = uri.trimFileExtension();
            String resultFileName = uriWithoutFileExt.lastSegment() + toAppend;
            return uriWithoutFileExt.trimSegments(1).appendSegment(resultFileName).appendFileExtension(fileExt);
        } else {
            throw new RuntimeException("The uri '" + uri + "' has no file extension so '" + toAppend + "' cannot be appended before the file extension!");
        }

    }

    public static URI createPlatformResourceURI(final String pathAfterPlatformResource) {
        return URI.createPlatformResourceURI(pathAfterPlatformResource, true);
    }

    public static IFolder createFolderInProjectIfNecessary(IProject project, String folderName) {
        String[] folderNames = folderName.split(File.separator);
        IContainer currentContainer = project;
        IFolder folder = null;
        for (int i = 0; i < folderNames.length; i++) {
            folder = currentContainer.getFolder(new Path(folderNames[i]));
            if (!folder.exists()) {
                try {
                    folder.create(true, true, new NullProgressMonitor());
                } catch (CoreException e) {
                    // soften
                    throw new RuntimeException(e);
                }
            }
            currentContainer = folder;
        }
        return folder;
    }

    public static boolean doesResourceExist(final URI resourceURI) {
        return getIFileForEMFUri(resourceURI).exists();
    }
}
