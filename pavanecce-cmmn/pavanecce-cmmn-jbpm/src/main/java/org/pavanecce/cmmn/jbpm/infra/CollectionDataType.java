package org.pavanecce.cmmn.jbpm.infra;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.core.process.core.datatype.DataType;

import com.thoughtworks.xstream.XStream;

public class CollectionDataType implements DataType {

	private static final long serialVersionUID = 510L;

	private String className;
	private String elementClassName;

	public CollectionDataType() {
		this("java.util.Collection");
	}

	public CollectionDataType(String className) {
		setClassName(className);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = (String) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(className);
	}

	@Override
	public boolean verifyDataType(final Object value) {
		if (value == null) {
			return true;
		}
		try {
			Class<?> clazz = Class.forName(className);
			if (clazz.isInstance(value)) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not find data type " + className);
		}
		return false;
	}

	@Override
	public Object readValue(String value) {
		XStream xstream = new XStream();
		return xstream.fromXML(value);
	}

	@Override
	public String writeValue(Object value) {
		XStream xstream = new XStream();
		return xstream.toXML(value);
	}

	@Override
	public String getStringType() {
		return className == null ? "java.lang.Object" : className;
	}

	public String getElementClassName() {
		return elementClassName;
	}

	public void setElementClassName(String elementClassName) {
		this.elementClassName = elementClassName;
	}
}
