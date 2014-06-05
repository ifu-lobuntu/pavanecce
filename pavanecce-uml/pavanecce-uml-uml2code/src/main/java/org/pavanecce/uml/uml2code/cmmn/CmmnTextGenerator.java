package org.pavanecce.uml.uml2code.cmmn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.uml2code.AbstractTextGenerator;

public class CmmnTextGenerator extends AbstractTextGenerator {
	protected Deque<String> elementStack = new ArrayDeque<String>();

	public String generateCaseFileItems(Class caseClass) {
		pushNewStringBuilder();
		pushPadding("      ");
		openElement("cmmn:caseFileModel").startContent();
		List<Property> props = EmfPropertyUtil.getEffectiveProperties(caseClass);
		appendCaseFileItems(props);
		closeElement();
		popPadding();
		return popStringBuilder().toString();
	}

	public String generateCaseFileItemDefinitions(Package pkg) {
		pushNewStringBuilder();
		pushPadding("      ");
		EList<Type> ownedTypes = pkg.getOwnedTypes();
		for (Type type : ownedTypes) {
			if (type instanceof Classifier && !((Classifier) type).isAbstract()) {
				openElement("cmmn:caseFileItemDefinition");
				appendAttribute("id", NameConverter.decapitalize(type.getName()) + "DefinitionId");
				appendAttribute("definitionType", "http://www.omg.org/spec/CMMN/DefinitionType/UMLClass");
				appendAttribute("structureRef", type.getQualifiedName());
				startContent();
				closeElement();
			}
		}
		for (Package child : pkg.getNestedPackages()) {
			generateCaseFileItemDefinitions(child);
		}

		return popStringBuilder().toString();
	}

	protected void appendCaseFileItems(List<Property> props) {
		for (Property property : props) {
			if (property.isComposite()) {
				openElement("cmmn:caseFileItem");
				appendAttribute("name", property.getName());
				appendAttribute("id", NameConverter.decapitalize(property.getType().getName()) + "FileItemId");
				appendAttribute("definitionRef", NameConverter.decapitalize(property.getType().getName()) + "DefinitionId");
				if (property.getType() instanceof Classifier) {
					StringBuilder targetRefs = new StringBuilder();
					for (Property child : EmfPropertyUtil.getEffectiveProperties((Classifier) property.getType())) {
						if (!child.isComposite() && EmfClassifierUtil.isPersistent(child.getType())
								&& (child.getOtherEnd() == null || !child.getOtherEnd().isComposite())) {
							targetRefs.append(NameConverter.decapitalize(child.getType().getName()) + "FileItemId");
							targetRefs.append(" ");
						}
					}

					if (targetRefs.length() != 0) {
						appendAttribute("targetRefs", targetRefs.toString());
					}
				}
				appendAttribute("multiplicity", multiplicity(property));
				startContent();
				if (property.getType() instanceof Classifier) {
					openElement("cmmn:children").startContent();
					appendCaseFileItems(EmfPropertyUtil.getEffectiveProperties((Classifier) property.getType()));
					closeElement();
				}
				closeElement();
			}
		}
	}

	private String multiplicity(Property property) {
		if (property.getLower() == 0) {
			if (property.getUpper() == LiteralUnlimitedNatural.UNLIMITED) {
				return "ZeroOrMore";
			} else if (property.getUpper() == 1) {
				return "ZeroOrOne";
			}
		} else if (property.getLower() == 1) {
			if (property.getUpper() == LiteralUnlimitedNatural.UNLIMITED) {
				return "OneOrMore";
			} else if (property.getUpper() == 1) {
				return "ExactlyOne";
			}
		}
		return "Unspecified";
	}

	private void appendAttribute(String name, String value) {
		sb.append(" ");
		sb.append(name);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");

	}

	private CmmnTextGenerator startContent() {
		sb.append(">");
		endLine();
		pushPadding(padding + "  ");
		return this;
	}

	private CmmnTextGenerator closeElement() {
		popPadding();
		sb.append(padding);
		sb.append("</");
		sb.append(elementStack.pop());
		sb.append(">");
		endLine();
		return this;
	}

	private CmmnTextGenerator openElement(String name) {
		sb.append(padding);
		sb.append("<");
		sb.append(name);
		elementStack.push(name);
		return this;
	}

	@Override
	public CmmnTextGenerator append(String string) {
		return (CmmnTextGenerator) super.append(string);
	}

	public CmmnTextGenerator endLine() {
		append("\n");
		return this;
	}
}
