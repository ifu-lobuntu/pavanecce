package org.pavanecce.uml.test.uml2code.cmmn;

import java.io.File;

import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.test.uml2code.ocm.OcmTests;
import org.pavanecce.uml.uml2code.cmmn.CmmnTextGenerator;
import org.pavanecce.uml.uml2code.java.HashcodeDecorator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.JpaCodeDecorator;
import org.pavanecce.uml.uml2code.ocm.CndTextGenerator;
import org.pavanecce.uml.uml2code.ocm.OcmCodeDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CmmnTestArtifactGenerator extends OcmTests {
	static Logger logger = LoggerFactory.getLogger(CmmnTestArtifactGenerator.class);

	public static void main(String[] args) throws Exception {
		final File outputRoot = new File("/home/ampie/Code/pavanecce/pavanecce-cmmn/");
		// final File outputRoot = new File("/home/ampie/Code/pavanecce/pavanecce-uml");
		example = new ConstructionCaseExample("") {
			@Override
			protected TextFileGenerator generateSourceCode(CodeModel codeModel) {
				TextWorkspace tw = new TextWorkspace("thisgoesnowhere");
				TextFileGenerator tfg = new TextFileGenerator(outputRoot);
				TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, "pavanecce-cmmn-jbpm");
				// TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY,
				// "pavanecce-uml-jbpm");
				VersionNumber vn = new VersionNumber("0.0.1");
				TextProject tp = tw.findOrCreateTextProject(tfd, "", vn);
				SourceFolder sf = tp.findOrCreateSourceFolder(new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "src/test/generated-java"),
						"", vn);
				TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
				createText(codeModel, sf);
				tnva.startVisiting(tw, tfg);
				return tfg;
			}
		};
		// example.generateCode(new JavaCodeGenerator(), new JpaCodeDecorator());
		example.generateCode(new JavaCodeGenerator(), new OcmCodeDecorator(), new JpaCodeDecorator(), new HashcodeDecorator() {
			@Override
			public void appendAdditionalFields(JavaCodeGenerator sb, CodeClassifier cc) {
				sb.append("  @Field(jcrName = \"test:uuid\", jcrType = \"String\")\n");
				sb.append("  @javax.persistence.Basic()\n");
				super.appendAdditionalFields(sb, cc);
			}
		});
		CndTextGenerator cndTextGenerator = new CndTextGenerator() {
			@Override
			public void appendExtraFields() {
				append("  - test:uuid ( STRING )  mandatory\n");
			}
		};
		generateCndFile(new File(outputRoot, "pavanecce-cmmn-jbpm/src/test/generated-resources"), cndTextGenerator);
		CmmnTextGenerator cmmn = new CmmnTextGenerator();
		logger.info(cmmn.generateCaseFileItemDefinitions(example.getModel()));
	}
}
