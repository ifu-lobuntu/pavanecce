package org.pavanecce.uml.jbpm;

import java.util.HashMap;

import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.ResourceTypeBuilder;
import org.drools.compiler.compiler.ResourceTypeBuilderRegistry;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.SemanticModule;
import org.drools.core.xml.SemanticModules;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.jbpm.process.core.Process;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.pavanecce.cmmn.jbpm.xml.handler.CMMNSemanticModule;
import org.pavanecce.cmmn.jbpm.xml.handler.CaseHandler;
import org.pavanecce.uml.common.util.StandaloneLocator;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UmlBuilder implements ResourceTypeBuilder {
	public static final ResourceType UML_RESOURCE_TYPE = ResourceType.addResourceTypeToRegistry("UML", "UML", "src/main/resources", "uml");
	public static final String UML_RESOURCE_SET = "uml.resource.set";
	static {
		ResourceTypeBuilderRegistry.getInstance().register(UML_RESOURCE_TYPE, new UmlBuilder());
	}
	private PackageBuilder packageBuilder;
	private ResourceSet rst = new UmlResourceSetFactory(new StandaloneLocator()).prepareResourceSet();

	@Override
	public void setPackageBuilder(PackageBuilder packageBuilder) {
		this.packageBuilder = packageBuilder;
		SemanticModules modules = this.packageBuilder.getPackageBuilderConfiguration().getSemanticModules();
		// yeah don't know about this
		SemanticModule cmmnSemanticModule = modules.getSemanticModule(CMMNSemanticModule.CMMN_URI);
		if (cmmnSemanticModule == null) {
			modules.addSemanticModule(cmmnSemanticModule = new CMMNSemanticModule());
		}
		cmmnSemanticModule.addHandler("case", new CaseHandler() {
			@Override
			public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
				Process process = (Process) super.start(uri, localName, attrs, parser);
				process.setMetaData(UML_RESOURCE_SET, rst);
				return process;
			}

		});
		this.packageBuilder.getPackageBuilderConfiguration().getSemanticModules().addSemanticModule(cmmnSemanticModule);

	}

	@Override
	public void addKnowledgeResource(Resource resource, ResourceType type, ResourceConfiguration configuration) throws Exception {
		rst.createResource(URI.createFileURI(resource.getSourcePath())).load(resource.getInputStream(), new HashMap<Object, Object>());
	}
}
