package org.pavanecce.cmmn.jbpm.xml.handler;

import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConstraintExtractor {

	public static ConstraintImpl extractExpression(Element parentElement, String epxressionElementName) {
		NodeList conditionList = parentElement.getElementsByTagName(epxressionElementName);
		ConstraintImpl constraint = null;
		if (conditionList.getLength() == 1) {
			Element condition = (Element) conditionList.item(0);
			String dialect = SentryHandler.getDialect(condition);
			String bodyText = "";
			NodeList bodyList = condition.getElementsByTagName("body");
			if (bodyList.getLength() == 1) {
				Element body = (Element) bodyList.item(0);
				bodyText = body.getFirstChild().getNodeValue();
				constraint = new ConstraintImpl();
				constraint.setConstraint(bodyText);
				constraint.setDialect(dialect);
			}
		}
		return constraint;
	}

}
