/**
 *  Felinx - Integration link between Felix and Eclipse
    Copyright (C) 2013  Michiel Vermandel

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jerry.felinx.plugin.builder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jerry.felinx.plugin.Activator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class BundleBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			//System.out.println("SampleDeltaVisitor: " + delta.getKind() + " "+ resource.getFullPath());
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkXML(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkXML(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			//System.out.println("SampleResourceVisitor: " + resource.getFullPath());
			checkXML(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	class XMLErrorHandler extends DefaultHandler {

		private IFile file;

		public XMLErrorHandler(IFile file) {
			this.file = file;
		}

		private void addMarker(SAXParseException e, int severity) {
			BundleBuilder.this.addMarker(file, e.getMessage(), e.getLineNumber(), severity);
		}

		public void error(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void warning(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}
	}

	public static final String BUILDER_ID = "FelixPlugin.BundleBuilder";

	private static final String MARKER_TYPE = "FelixPlugin.xmlProblem";

	private SAXParserFactory parserFactory;

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD || kind == CLEAN_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkXML(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".xml")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			XMLErrorHandler reporter = new XMLErrorHandler(file);
			try {
				getParser().parse(file.getContents(), reporter);
			} catch (Exception e1) {
			}
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			System.out.println("fullBuild");
			getProject().accept(new SampleResourceVisitor());
			createBundle(getProject(), monitor);
		} catch (CoreException e) {
		}
	}

	private SAXParser getParser() throws ParserConfigurationException, SAXException {
		if (parserFactory == null) {
			parserFactory = SAXParserFactory.newInstance();
		}
		return parserFactory.newSAXParser();
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		System.out.println("incrementalBuild");
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
		createBundle(getProject(), monitor);
	}

	public IPath getJarPath(IProject project) throws CoreException {
		IPath location = null;

		final IFolder generated = project.getFolder(new Path("generated"));
		if (!generated.exists()) {
			generated.create(true, true, null);
		}
		location = generated.getFile(project.getName() + ".jar").getLocation();
		return location;
	}

	public ActionResult createBundle(IProject project, IProgressMonitor monitor) {
		ActionResult result = new ActionResult("Creating bundle...");
		try {
			IJavaProject javaProject = JavaCore.create(project);
			final IFolder generated = javaProject.getProject().getFolder(new Path("generated"));
			if (!generated.exists()) {
				generated.create(true, true, monitor);
			}
			IPath location = getJarPath(project);

			IFolder metaFolder = javaProject.getProject().getFolder("META-INF");
			IFile file = metaFolder.getFile("MANIFEST.MF");
			Manifest manifest = new Manifest(file.getContents());
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			JarOutputStream target = new JarOutputStream(new FileOutputStream(location.toOSString()), manifest);
			// add(new File("inputDirectory"), target);

			// // http://stackoverflow.com/questions/3273022/getting-all-classes-from-the-current-workspace-in-eclipse
			// System.out.println("Class files:");
			// for (IPackageFragment packageFragment : javaProject.getPackageFragments()) {
			// // Package fragments include all packages in the class path. We will only look at the package from the source folder.
			// // K_BINARY would include also included JARS, e.g. rt.jar
			// if (packageFragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
			// System.out.println("Handling packageFragment " + packageFragment.getElementName() +" "+ packageFragment.getPath() + " ");
			// for (final IJavaElement element : packageFragment.getChildren()) {
			// System.out.println(">> "+element.getPath());
			// }
			// }
			// }

			System.out.println("Adding classes...");
			File projectRoot = project.getLocation().toFile();
			File classesFolder = getClassesFolder(javaProject);
			//do not add bin or classes folder, but directly its content
			addFiles(classesFolder, classesFolder, target);

			final IFolder lib = javaProject.getProject().getFolder(new Path("lib"));
			if (lib.exists()) {
				monitor.setTaskName("Adding libraries...");
				addFiles(new File(lib.getLocationURI()), projectRoot, target);
			}

			target.close();
			System.out.println("Installing... ");
			File jar = new File(location.toOSString());
			String symbolicName=null;
			String bundleVersion=null;
			Map<Object, Object> bundleSymbolicNameAtrrs = manifest.getMainAttributes();
			for( Object obj : bundleSymbolicNameAtrrs.keySet()){
				if (obj.toString().equals("Bundle-SymbolicName")){
					symbolicName = bundleSymbolicNameAtrrs.get(obj).toString();
				}else if (obj.toString().equals("Bundle-Version")){
					bundleVersion = bundleSymbolicNameAtrrs.get(obj).toString();
				}
				if (symbolicName!=null && bundleVersion!=null){
					break;
				}
			}
			Activator.updateBundle(symbolicName, bundleVersion, jar);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			result.setError(e);
		}
		return result;
	}

	private File getClassesFolder(IJavaProject javaProject) throws JavaModelException {
		IPath classesFolder = javaProject.getOutputLocation();
		return new File(getProjectRoot(javaProject).getParent(), classesFolder.toOSString());
	}

	private File getProjectRoot(IJavaProject javaProject) {
		return javaProject.getProject().getLocation().toFile();
	}

	private void addFiles(File fullPath, File projectRoot, JarOutputStream target) throws Exception {
		BufferedInputStream in = null;
		try {
			File relative = getRelativeFile(projectRoot, fullPath);
			if (fullPath.isDirectory()) {
				String name = relative.getPath().replace("\\", "/");
				if (!name.isEmpty()) {
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(fullPath.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();	
				}
				for (File nestedFile : fullPath.listFiles())
					addFiles(nestedFile, projectRoot, target);
				return;
			}
			//System.out.println("Writing " + fullPath.getAbsolutePath());
			JarEntry entry = new JarEntry(relative.getPath().replace("\\", "/"));
			entry.setTime(fullPath.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(fullPath));

			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		} finally {
			if (in != null)
				in.close();
		}
	}

	private File getRelativeFile(File base, File childOfBase) {
		URI relative = base.toURI().relativize(childOfBase.toURI());
		return new File(relative.getPath());
	}

}
