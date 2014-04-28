package org.pavanecce.eclipse.roundtrip.menu;

import java.util.Map.Entry;

import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;

public class CodeModelVisitorAdapter {
	public void startVisiting(CodeModel codeModel, DefaultCodeModelVisitor v){
		visitRecursively(v, codeModel); 
		
	}

	protected void visitRecursively(DefaultCodeModelVisitor v, CodePackage cp) {
		for (Entry<String, CodePackage> entry : cp.getChildren().entrySet()) {
			CodePackage value = entry.getValue();
			v.visitPackage(value);
			visitRecursively(v, value);
			for (Entry<String, CodeClassifier> entry2 : value.getClassifiers().entrySet()) {
				v.visitClassifier(entry2.getValue());
			}
		}
	}
}
