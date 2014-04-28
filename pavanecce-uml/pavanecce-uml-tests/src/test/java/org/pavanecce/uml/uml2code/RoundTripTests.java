package org.pavanecce.uml.uml2code;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.wiring.BundleWiring;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.CharArrayTextSource;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.DummyProgressMonitor;
import org.pavanecce.common.util.IntrospectionUtil;
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.common.util.IFileLocator;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.reverse.java.SimpleUmlGenerator;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.reflect.JavaDescriptorFactory;
import org.pavanecce.uml.uml2code.codemodel.CodeModelBuilder;
import org.pavanecce.uml.uml2code.codemodel.UmlCodeModelVisitorAdaptor;
import org.pavanecce.uml.uml2code.java.EmfJavaCodeGenerator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.phidias.compile.BundleJavaManager;
import org.phidias.compile.ResourceResolver;

public class RoundTripTests {

	protected CodeModelBuilder builder;
	protected Model model;
	private EmfJavaCodeGenerator jg;
	private IFileLocator fileLocator = new AdaptableFileLocator();
	private UmlResourceSetFactory resourceSetFactory = new UmlResourceSetFactory(fileLocator);

	@Before
	public void setup() {
		jg = new EmfJavaCodeGenerator();
		this.builder = new CodeModelBuilder();
	}

	private void compileInOsgi(Set<File> set, File destination) {
		try {
			// compiler options
			List<String> options = new ArrayList<String>();

			// http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/javac.html#options

			options.add("-proc:none"); // don't process annotations (typical for
										// jsps)
			options.add("-verbose"); // Phidias adds to the default verbose
										// output
			options.add("-d");
			options.add(destination.getCanonicalPath());
			JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

			// Diagnostics provide details about all errors/warnings observed
			// during compilation
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

			// the standard file manager knows how to load libraries from disk
			// and from the runtime
			StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(diagnostics, null, null);

			// Were using source in string format (possibly generated
			// dynamically)
			// JavaFileObject[] sourceFiles = { new
			// StringJavaFileObject(javaSourceName, javaSource) };

			// the OSGi aware file manager
			Iterable<? extends JavaFileObject> fos = standardFileManager.getJavaFileObjects((File[]) set.toArray(new File[set.size()]));
			BundleJavaManager bundleFileManager = new BundleJavaManager(TestBundle.bundle, standardFileManager, options);
			// OPtional::
			{
				// *** New since 0.1.7 ***
				// A new, optional, ResourceResolver API

				// This is optional as a default implementation is automatically
				// provided with using the exact logic below.

				ResourceResolver resourceResolver = new ResourceResolver() {
					public URL getResource(BundleWiring bundleWiring, String name) {
						return bundleWiring.getBundle().getResource(name);
					}

					public Collection<String> resolveResources(BundleWiring bundleWiring, String path, String filePattern, int options) {

						// Use whatever magic you like here to provide
						// additional
						// resolution, such as overcoming the fact that the
						// system.bundle won't return resources from the parent
						// classloader, even when those are exported by
						// framework
						// extension bundles

						return bundleWiring.listResources(path, filePattern, options);
					}
				};

				bundleFileManager.setResourceResolver(resourceResolver);
			}

			// get the compilation task
			CompilationTask compilationTask = javaCompiler.getTask(null, bundleFileManager, diagnostics, options, null, fos);

			bundleFileManager.close();

			// perform the actual compilation
			if (compilationTask.call()) {
				// Success!

				return;
			}

			// Oh no, we got errors, list them
			for (Diagnostic<?> dm : diagnostics.getDiagnostics()) {
				System.err.println("COMPILE ERROR: " + dm);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIt() throws Exception {
		ResourceSet rst = resourceSetFactory.prepareResourceSet();
		URL vdfpFile = getClass().getClassLoader().getResource("VdfpUml.uml");
		vdfpFile = fileLocator.resolve(vdfpFile);
		URI uri = URI.createURI(vdfpFile.toExternalForm());
		Resource resource = rst.getResource(uri, true);
		Model model = (Model) resource.getContents().get(0);
		UmlCodeModelVisitorAdaptor adaptor = new UmlCodeModelVisitorAdaptor(importUmlClasses(rst, jg));
		adaptor.startVisiting(builder, model);
		TextWorkspace tw = new TextWorkspace("org.vdfp.metamodels.vdfp-uml");
		File outputRoot = calculateOutputRoot();
		TextFileGenerator tfg = new TextFileGenerator(outputRoot);
		TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, "org.pavanecce.uml.test.domain");
		VersionNumber vn = new VersionNumber("0.0.1");
		TextProject tp = tw.findOrCreateTextProject(tfd, "", vn);
		SourceFolder sf = tp.findOrCreateSourceFolder(new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "gen-src"), "", vn);
		CodeClass cdfpClassifier = (CodeClass) adaptor.getCodeModel().getDescendent("vdfp_uml", "VdfpClassifier");
		String javaSource = jg.toClassifierDeclaration(cdfpClassifier);
		List<String> path = cdfpClassifier.getPackage().getPath();
		path.add(cdfpClassifier.getName() + ".java");
		sf.findOrCreateTextFile(path).setTextSource(new CharArrayTextSource(javaSource.toCharArray()));
		TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
		tnva.startVisiting(tw, tfg);
		ClassLoader cl = compile(tfg.getNewFiles(), calculateClasspath());
		Class<?> classifierClass = cl.loadClass(Classifier.class.getName());
		Class<?> vdfpClass = cl.loadClass("vdfp_uml.VdfpClassifier");
		assertTrue(classifierClass.isAssignableFrom(vdfpClass));
		System.out.println();
	}

	private File calculateOutputRoot() {
		File f = new File(System.getProperty("user.home"), "tmp/vdfproundtrip/java");
		f.mkdirs();
		return f;
	}

	private File calculateClasspath() {
		File cp = new File(System.getProperty("user.home"), "tmp/vdfproundtrip/classes");
		cp.mkdirs();
		return cp;
	}

	private ClassLoader compile(Set<File> set, File destination) throws IOException {
		ClassLoader newClassLoader = null;
		newClassLoader = new URLClassLoader(new URL[] { destination.toURI().toURL() }, getClass().getClassLoader());
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		StringBuilder cp = new StringBuilder();
		if (contextClassLoader instanceof URLClassLoader) {
			URL[] urLs = ((URLClassLoader) contextClassLoader).getURLs();
			for (URL url : urLs) {
				cp.append(fileLocator.resolve(url).toExternalForm());
				cp.append(File.pathSeparatorChar);
			}
			List<URL> list = new ArrayList<URL>(Arrays.asList(urLs));
			try {
				list.add(0, new URL("file:///" + destination.getCanonicalPath()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// newClassLoader=URLClassLoader.newInstance(list.toArray(new
			// URL[list.size()]));

			try {
				JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
				StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
				Iterable<? extends JavaFileObject> codeObjecten = sjfm.getJavaFileObjects(set.toArray(new File[set.size()]));
				String[] opties = new String[] { "-d", destination.getCanonicalPath(), "-classpath", cp.toString() };
				CompilationTask task = jc.getTask(new OutputStreamWriter(System.out), sjfm, null, Arrays.asList(opties), null, codeObjecten);
				// CompilationTask task = jc.getTask(new
				// OutputStreamWriter(System.out), sjfm ,null,
				// Arrays.asList(opties), null, codeObjecten);
				task.call();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			compileInOsgi(set, destination);
		}
		return newClassLoader;
	}

	private Map<String, Classifier> importUmlClasses(ResourceSet rst, JavaCodeGenerator jg) throws Exception {
		Map<String, Classifier> result = new HashMap<String, Classifier>();
		JavaDescriptorFactory jfd = new JavaDescriptorFactory();
		Set<Class<?>> dependencies = IntrospectionUtil.getDependencies(Classifier.class, "org.eclipse.uml2.uml");
		Set<SourceClass> sourceClasses = new HashSet<SourceClass>();
		for (Class<?> class1 : dependencies) {
			sourceClasses.add(jfd.getClassDescriptor(class1));
			jg.map(new CodeTypeReference(false, ("tmp." + class1.getName()).split("\\.")), class1.getName());
		}
		SimpleUmlGenerator sug = new SimpleUmlGenerator();
		Resource tmpResource = rst.createResource(URI.createFileURI("test-input/tmp.uml"));
		Model tmpModel = UMLFactory.eINSTANCE.createModel();
		tmpModel.setName("tmp");
		tmpResource.getContents().add(tmpModel);
		Collection<Element> generateUml = sug.generateUml(sourceClasses, tmpModel, new DummyProgressMonitor());
		for (Element element : generateUml) {
			if (element instanceof Interface) {
				Interface intf = (Interface) element;
				if (intf.getName().equals("Classifier")) {
					result.put("vdfp_uml::VdfpClassifier", (Classifier) element);
				} else if (intf.getName().equals("ParameterableElement")) {
					assertNotNull(intf.getOwnedAttribute("templateParameter", null));
				}
			}
		}
		Set<Entry<String, Classifier>> entrySet = sug.getClassMap().entrySet();
		for (Entry<String, Classifier> entry : entrySet) {
			String[] split = entry.getValue().getQualifiedName().split("\\:\\:");
			jg.map(new CodeTypeReference(false, split),entry.getKey());
		}

		return result;
	}

}
