package org.pavanecce.uml.uml2code;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.osgi.framework.wiring.BundleWiring;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
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
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.common.util.IFileLocator;
import org.pavanecce.uml.uml2code.java.EmfJavaCodeGenerator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.TestPersistenceUnitInfo;
import org.phidias.compile.BundleJavaManager;
import org.phidias.compile.ResourceResolver;

public abstract class AbstractJavaCompilingTest extends Assert {

	protected IFileLocator fileLocator = new AdaptableFileLocator();
	protected JavaCodeGenerator javaCodeGenerator;
	protected ScriptEngine javaScriptEngine;
	protected ScriptContext javaScriptContext;

	public AbstractJavaCompilingTest() {
		super();
	}

	protected TextFileGenerator generateJava(CodeModel codeModel) {
		TextWorkspace tw = new TextWorkspace("thisgoesnowhere");
		File outputRoot = calculateOutputRoot();
		TextFileGenerator tfg = new TextFileGenerator(outputRoot);
		TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, "org.pavanecce.uml.test.domain");
		VersionNumber vn = new VersionNumber("0.0.1");
		TextProject tp = tw.findOrCreateTextProject(tfd, "", vn);
		SourceFolder sf = tp.findOrCreateSourceFolder(new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "gen-src"), "", vn);
		TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
		createText(codeModel, sf);
		tnva.startVisiting(tw, tfg);
		return tfg;
	}

	protected void createText(CodePackage codeModel, SourceFolder sf) {
		Collection<CodeClassifier> values = codeModel.getClassifiers().values();
		for (CodeClassifier codeClassifier : values) {
			String javaSource = javaCodeGenerator.toClassifierDeclaration(codeClassifier);
			List<String> path = codeClassifier.getPackage().getPath();
			path.add(codeClassifier.getName() + ".java");
			sf.findOrCreateTextFile(path).setTextSource(new CharArrayTextSource(javaSource.toCharArray()));
		}
		for (CodePackage codePackage : codeModel.getChildren().values()) {
			createText(codePackage, sf);
		}
	}

	protected void createText(SourceFolder sf, Collection<CodeClassifier> values) {
		for (CodeClassifier codeClassifier : values) {
			String javaSource = javaCodeGenerator.toClassifierDeclaration(codeClassifier);
			List<String> path = codeClassifier.getPackage().getPath();
			path.add(codeClassifier.getName() + ".java");
			sf.findOrCreateTextFile(path).setTextSource(new CharArrayTextSource(javaSource.toCharArray()));
		}
	}

	public void setup() throws Exception {
		javaCodeGenerator = new EmfJavaCodeGenerator();
	}

	protected void initScriptingEngine() {
		ScriptEngineManager manager = new ScriptEngineManager();
		javaScriptEngine = manager.getEngineByName("js");
		javaScriptContext = javaScriptEngine.getContext();
	}

	private File calculateOutputRoot() {
		File f = new File(System.getProperty("user.home"), "tmp/" + getTestName() + "/java");
		f.mkdirs();
		deleteAll(f);
		return f;
	}

	protected void deleteAll(File f) {
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			if(file.isFile()){
				file.delete();
			}else{
				deleteAll(file);
			}
		}
	}

	private File calculateClasspath() {
		File cp = new File(System.getProperty("user.home"), "tmp/" + getTestName() + "/classes");
		cp.mkdirs();
		deleteAll(cp);
		return cp;
	}

	protected abstract String getTestName();

	private void compileInOsgi(Set<File> set, File destination) {
		try {
			// compiler options
			List<String> options = new ArrayList<String>();

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

	protected ClassLoader compile(Set<File> set) throws IOException {
		ClassLoader newClassLoader = null;
		File destination = calculateClasspath();
		newClassLoader = new URLClassLoader(new URL[] { destination.toURI().toURL() }, getClass().getClassLoader());
		if (Thread.currentThread().getContextClassLoader() instanceof URLClassLoader) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!Compiling in STandalone architecture!!!!!!!!!!!!!!!!!!");
			compileInStandalone(set, destination);
		} else {
			System.out.println("!!!!!!!!!!!!!!!!!!!!Compiling in OSGi architecture!!!!!!!!!!!!!!!!!!");
			compileInOsgi(set, destination);
		}
		return newClassLoader;
	}

	private void compileInStandalone(Set<File> set, File destination) throws IOException {
		StringBuilder cp = new StringBuilder();
		URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		appendClassPath(cp, urlClassLoader);
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
	}

	protected void appendClassPath(StringBuilder cp, URLClassLoader urlClassLoader) {
		URL[] urLs = urlClassLoader.getURLs();
		for (URL url : urLs) {
			System.out.println("adding class path entry:" + url);
			cp.append(fileLocator.resolve(url).toExternalForm());
			cp.append(File.pathSeparatorChar);
		}
		if(urlClassLoader.getParent() instanceof URLClassLoader){
			appendClassPath(cp, (URLClassLoader) urlClassLoader.getParent());
		}
	}

	public void addMappedClasses(TestPersistenceUnitInfo pui, CodePackage codePackage) {
		for (CodeClassifier cc : codePackage.getClassifiers().values()) {
			pui.getManagedClassNames().add(javaCodeGenerator.toQualifiedName(cc.getTypeReference()));
		}
		Collection<CodePackage> values = codePackage.getChildren().values();
		for (CodePackage child : values) {
			addMappedClasses(pui, child);
		}

	}

}