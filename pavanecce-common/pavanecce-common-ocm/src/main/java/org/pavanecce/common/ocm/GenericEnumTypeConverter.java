package org.pavanecce.common.ocm;

import java.io.InvalidClassException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.TimeUnit;

import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.ocm.manager.enumconverter.EnumTypeConverter;

public abstract class GenericEnumTypeConverter<T extends Enum<?>> extends EnumTypeConverter {
	@Override
	public Value getValue(ValueFactory valueFactory, Object object) {
		if (object == null) {
			return null;
		}

		if (!(object instanceof Enum)) {
			throw new RuntimeException(new InvalidClassException(EnumTypeConverter.class.getSimpleName() + " Can only convert simple Enumerations"));
		}
		Enum<?> anEnum = (Enum<?>) (object);
		return valueFactory.createValue(anEnum.name());
	}

	@SuppressWarnings("unchecked")
	public Class<T> getEnumClass() {
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
	}

	@Override
	public Object getObject(Value value) {
		try {
			Enum<?>[] enumerations = getEnumClass().getEnumConstants();
			int size = enumerations.length;
			String stringValue = value.getString();
			for (int i = 0; i < size; i++) {
				if (enumerations[i].name().equals(stringValue)) {
					return enumerations[i];
				}
			}

			throw new RuntimeException(new InvalidClassException(getEnumClass().getCanonicalName() + " Does not contain an enumeration " + stringValue));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getXPathQueryValue(ValueFactory valueFactory, Object object) {
		Value value = getValue(valueFactory, object);
		try {
			return "'" + value.getString() + "'";
		} catch (Exception e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
	}

	public static void main(String[] args) {
		System.out.println(new GenericEnumTypeConverter<TimeUnit>() {
		}.getEnumClass());
	}
}
