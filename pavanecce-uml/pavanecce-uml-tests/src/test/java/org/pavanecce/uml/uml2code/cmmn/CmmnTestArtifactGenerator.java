package org.pavanecce.uml.uml2code.cmmn;

import java.io.File;

import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.ConstructionCaseExample;
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.JpaCodeDecorator;
import org.pavanecce.uml.uml2code.ocm.OcmCodeDecorator;
import org.pavanecce.uml.uml2code.ocm.OcmTests;

public class CmmnTestArtifactGenerator extends OcmTests {
	public static void main(String[] args) throws Exception{
		final File outputRoot = new File("/home/ampie/Code/pavanecce/pavanecce-cmmn/");
		example = new ConstructionCaseExample(""){
			protected TextFileGenerator generateSourceCode(CodeModel codeModel) {
				TextWorkspace tw = new TextWorkspace("thisgoesnowhere");
				TextFileGenerator tfg = new TextFileGenerator(outputRoot);
				TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, "pavanecce-cmmn-jbpm");
				VersionNumber vn = new VersionNumber("0.0.1");
				TextProject tp = tw.findOrCreateTextProject(tfd, "", vn);
				SourceFolder sf = tp.findOrCreateSourceFolder(new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "src/test/generated-java"), "", vn);
				TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
				createText(codeModel, sf);
				tnva.startVisiting(tw, tfg);
				return tfg;
			}
		};
//		example.generateCode(new JavaCodeGenerator(), new OcmCodeDecorator(), new JpaCodeDecorator());
//		generateCndFile(new File(outputRoot, "pavanecce-cmmn-jbpm/src/test/generated-resources"));
		CmmnTextGenerator cmmn = new CmmnTextGenerator();
		System.out.println(cmmn.generateCaseFileItemDefinitions(example.getModel()));
	}
}
