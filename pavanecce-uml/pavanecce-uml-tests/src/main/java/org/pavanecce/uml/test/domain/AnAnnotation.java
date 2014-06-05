package org.pavanecce.uml.test.domain;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

@Target({ FIELD, METHOD })
public @interface AnAnnotation {
	String stringValue();

	int intValue();

	boolean booleanValue();

	char charValue();

	float floatValue();

	double doubleValue();
}
