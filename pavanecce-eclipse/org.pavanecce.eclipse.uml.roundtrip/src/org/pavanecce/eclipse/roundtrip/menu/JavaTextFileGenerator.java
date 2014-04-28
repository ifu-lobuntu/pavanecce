package org.pavanecce.eclipse.roundtrip.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.Model;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.text.workspace.ISourceFolderIdentifier;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextSource;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JavaTextFileGenerator extends DefaultCodeModelVisitor {
	public static final ISourceFolderIdentifier DOMAIN = new ISourceFolderIdentifier() {
	};
	private TextWorkspace textWorkspace;
	private Map<ISourceFolderIdentifier, TextProjectDefinition> projectDefinitions = new HashMap<ISourceFolderIdentifier, TextProjectDefinition>();
	private Map<ISourceFolderIdentifier, SourceFolderDefinition> sourceFolderDefinitions = new HashMap<ISourceFolderIdentifier, SourceFolderDefinition>();
	public void mapSourceFolder(ISourceFolderIdentifier id, TextProjectDefinition tpd, SourceFolderDefinition sfd){
		this.projectDefinitions.put(id, tpd);
		this.sourceFolderDefinitions.put(id, sfd);
	}
	private JavaCodeGenerator javaCodeGenerator;
	
	@Override
	public void visitClassifier(final CodeClassifier cc) {
		if(cc.getTypeReference().getMappedType("java")!=null){
			return;
		}
		ISourceFolderIdentifier sfi = cc.getData(ISourceFolderIdentifier.class);
		if(sfi==null){
			sfi=DOMAIN;
		}
		Model model = cc.getData(Model.class);
		TextProject p = textWorkspace.findOrCreateTextProject(projectDefinitions.get(sfi), model.getName(), new VersionNumber("0.0.1"));
		SourceFolder sf = p.findOrCreateSourceFolder(sourceFolderDefinitions.get(sfi), model.getName(), new VersionNumber("0.0.1"));
		List<String> qn = cc.getTypeReference().getQualifiedNameInLanguage("java");
		qn.set(qn.size()-1, qn.get(qn.size()-1)+".java");
		TextFile tf = sf.findOrCreateTextFile(qn);
		tf.setTextSource(new TextSource() {
			
			@Override
			public char[] toCharArray() {
				return javaCodeGenerator.toClassifierDeclaration((CodeClass) cc).toCharArray();
			}
			
			@Override
			public boolean hasContent() {
				return true;
			}
		});
	}

	@Override
	public void visitPackage(CodePackage cp) {
	}

	public void setTextWorkspace(TextWorkspace tw) {
		this.textWorkspace=tw;
		
	}

	public JavaTextFileGenerator(TextWorkspace textWorkspace, JavaCodeGenerator javaCodeGenerator) {
		super();
		this.textWorkspace = textWorkspace;
		this.javaCodeGenerator = javaCodeGenerator;
	}
}
