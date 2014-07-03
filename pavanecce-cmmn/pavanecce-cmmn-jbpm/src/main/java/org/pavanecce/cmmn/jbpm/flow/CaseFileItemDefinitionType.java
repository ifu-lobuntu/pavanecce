package org.pavanecce.cmmn.jbpm.flow;

public enum CaseFileItemDefinitionType {
	CMIS_FOLDER("http://www.omg.org/spec/CMMN/DefinitionType/CMISFolder"), CMIS_DOCUMENT("http://www.omg.org/spec/CMMN/DefinitionType/CMISDocument"), CMIS_RELATIONSHIP(
			"http://www.omg.org/spec/CMMN/DefinitionType/CMISRelationship"), XSD_ELEMENT("http://www.omg.org/spec/CMMN/DefinitionType/XSDElement"), XSD_COMPLEX_TYPE(
			"http://www.omg.org/spec/CMMN/DefinitionType/XSDComplexType"), XSD_SIMPLE_TYPE("http://www.omg.org/spec/CMMN/DefinitionType/XSDSimpleType"), WSDL_MESSAGE(
			"http://www.omg.org/spec/CMMN/DefinitionType/WSDLMessage"), UML_CLASS("http://www.omg.org/spec/CMMN/DefinitionType/UMLClass"), UNKNOWN(
			"http://www.omg.org/spec/CMMN/DefinitionType/Unknown"), UNSPECIFIED("http://www.omg.org/spec/CMMN/DefinitionType/Unspecified");
	private String uri;

	private CaseFileItemDefinitionType(String uri) {
		this.uri = uri;
	}

	public static CaseFileItemDefinitionType resolveByUri(String uri) {
		CaseFileItemDefinitionType[] values = values();
		for (CaseFileItemDefinitionType caseFileItemDefinitionType : values) {
			if (caseFileItemDefinitionType.uri.equals(uri)) {
				return caseFileItemDefinitionType;
			}
		}
		return null;
	}
}
