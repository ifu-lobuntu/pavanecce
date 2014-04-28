package org.pavanecce.cmmn.jbpm.ocm;

import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.ocm.manager.atomictypeconverter.AtomicTypeConverter;


public class EnumConverter implements AtomicTypeConverter {

	@Override
	public Value getValue(ValueFactory valueFactory, Object object) {
		if(object instanceof Enum){
			return valueFactory.createValue(object.getClass().getName() +":" + ((Enum<?>) object).name());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getObject(Value value) {
		try {
			String s = value.getString();
			if(s!= null){
				String[] split = s.split("\\:");
				Class enumClass = Class.forName(split[0]);
				return Enum.valueOf(enumClass, split[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getXPathQueryValue(ValueFactory valueFactory, Object object) {
		if(object instanceof Enum){
			return ((Enum<?>) object).name();
		}
		return "";
	}

}
