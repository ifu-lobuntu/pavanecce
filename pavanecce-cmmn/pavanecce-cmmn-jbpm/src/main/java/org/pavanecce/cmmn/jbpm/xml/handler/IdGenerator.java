package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;
import java.util.Set;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.CMMNElement;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdGenerator {
	private static Logger logger = LoggerFactory.getLogger(IdGenerator.class);
	static Set<Long> ids = new HashSet<Long>();

	@SuppressWarnings("unchecked")
	public static long next(ExtensibleXmlParser p) {
		Long object = (Long) p.getMetaData().get("HighestId");
		if (object == null) {
			object = 20L;
		}
		object++;
		p.getMetaData().put("HighestId", object);
		return object;

	}

	public static long getIdAsUniqueAsUuid(ExtensibleXmlParser p, CMMNElement e) {
		String elementId = e.getElementId();
		return getIdAsUniqueAsId(p, elementId);
	}

	protected static long getIdAsUniqueAsId(ExtensibleXmlParser p, String elementId) {
		Case c = (Case) p.getParent(Case.class);
		char[] charArray = (c.getCaseKey() + elementId).toCharArray();
		long result = 1;
		int atSignIndex = 0;
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] == '@') {
				atSignIndex = i;
			}
			result = (result * 31) + charArray[i] - i;
		}
		if (charArray.length > atSignIndex + 10) {
			// THis is where the most variation takes place in the emf id
			// Introduce some variation in the calculation
			for (int i = atSignIndex + 2; i < atSignIndex + 10; i++) {
				if (Character.isLowerCase(charArray[i])) {
					result = (result * 31) + Character.toUpperCase(charArray[i]) - i;
				} else {
					result = (result * 31) + Character.toLowerCase(charArray[i]) - i;
				}
			}
		}
		long abs = Math.abs(result);
		if (ids.contains(abs)) {
			logger.info("duplicate found:" + elementId);
		}
		ids.add(abs);
		return abs;
	}

	public static long getIdAsUniqueAsUuid(ExtensibleXmlParser parser, PlanItemInfo<?> planItem) {
		return getIdAsUniqueAsId(parser, planItem.getElementId());
	}

	public static void reset() {
		ids.clear();
	}
}
