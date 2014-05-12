package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.ResourceTypeBuilder;
import org.drools.compiler.compiler.ResourceTypeBuilderRegistry;
import org.drools.core.xml.SemanticModules;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;

public class CMMNBuilder implements ResourceTypeBuilder {
	public static final ResourceType CMMN_RESOURCE_TYPE = ResourceType.addResourceTypeToRegistry("CMMN", "CMMN", "src/main/resources", "cmmn");
	static {
		ResourceTypeBuilderRegistry.getInstance().register(CMMN_RESOURCE_TYPE, new CMMNBuilder());
	}
	private PackageBuilder packageBuilder;

	@Override
	public void setPackageBuilder(PackageBuilder packageBuilder) {
		this.packageBuilder = packageBuilder;
		SemanticModules modules = this.packageBuilder.getPackageBuilderConfiguration().getSemanticModules();
		if (modules.getSemanticModule(CMMNSemanticModule.CMMN_URI) == null) {
			modules.addSemanticModule(new CMMNSemanticModule());
		}
	}

	@Override
	public void addKnowledgeResource(Resource resource, ResourceType type, ResourceConfiguration configuration) throws Exception {
		packageBuilder.addProcessFromXml(resource);

	}
}
