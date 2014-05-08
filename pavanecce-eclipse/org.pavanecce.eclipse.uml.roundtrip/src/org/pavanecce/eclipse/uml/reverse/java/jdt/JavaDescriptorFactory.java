package org.pavanecce.eclipse.uml.reverse.java.jdt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceCode;

public class JavaDescriptorFactory {
	private Map<String, JavaJdtClass> classDescriptors = new HashMap<String, JavaJdtClass>();
	private Map<IVariableBinding, JavaJdtVariable> fieldDescriptors = new HashMap<IVariableBinding, JavaJdtVariable>();

	public JavaJdtClass getClassDescriptor(ITypeBinding b) {
		if(b==null){
			return null;
		}
		String key = JavaJdtClass.getIdentifier(b);
		JavaJdtClass result = classDescriptors.get(key);
		if (result == null) {
			classDescriptors.put(key, result = new JavaJdtClass(b, this));
			result.init();
		}
		return result;
	}

	public JavaJdtClass getClassDescriptor(ITypeBinding b, AbstractTypeDeclaration value) {
		String key = JavaJdtClass.getIdentifier(b);
		JavaJdtClass result = classDescriptors.get(key);
		if (result == null) {
			classDescriptors.put(key, result = new JavaJdtClass(b, value, this));
			result.init();
		}
		return result;
	}

	public JavaJdtVariable getField(IVariableBinding field) {
		JavaJdtVariable result = fieldDescriptors.get(field);
		if (result == null) {
			fieldDescriptors.put(field, result = new JavaJdtVariable(field, this));
		}
		return result;
	}

	public SourceCode getSource(IMethodBinding binding) {
		JavaJdtClass dc = getClassDescriptor(binding.getDeclaringClass());
		if (dc.getAst() instanceof TypeDeclaration) {
			JavaJdtClass jdtClss = dc;
			TypeDeclaration td = (TypeDeclaration) jdtClss.getAst();
			for (final MethodDeclaration m : td.getMethods()) {
				IMethod iMethod = (IMethod) binding.getMethodDeclaration().getJavaElement();
				try {
					if (m.getStartPosition() == iMethod.getSourceRange().getOffset()) {
						return new SourceCode() {
							@Override
							public String getLanguage() {
								return "java";
							}

							@Override
							public String getCode() {
								StringBuilder sb = new StringBuilder();
								@SuppressWarnings("unchecked")
								List<org.eclipse.jdt.core.dom.Statement> statements = m.getBody().statements();
								for (org.eclipse.jdt.core.dom.Statement statement : statements) {
									sb.append(statement.toString());
								}
								return sb.toString();
							}
						};
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
}
