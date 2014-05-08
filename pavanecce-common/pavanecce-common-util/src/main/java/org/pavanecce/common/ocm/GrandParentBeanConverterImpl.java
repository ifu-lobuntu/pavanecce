package org.pavanecce.common.ocm;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.ocm.exception.JcrMappingException;
import org.apache.jackrabbit.ocm.exception.ObjectContentManagerException;
import org.apache.jackrabbit.ocm.exception.RepositoryException;
import org.apache.jackrabbit.ocm.manager.atomictypeconverter.AtomicTypeConverterProvider;
import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ParentBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.objectconverter.ObjectConverter;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.model.BeanDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;

public class GrandParentBeanConverterImpl extends ParentBeanConverterImpl {

	public GrandParentBeanConverterImpl(Mapper mapper, ObjectConverter objectConverter, AtomicTypeConverterProvider atomicTypeConverterProvider) {
		super(mapper, objectConverter, atomicTypeConverterProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getObject(Session session, Node parentNode, BeanDescriptor beanDescriptor, ClassDescriptor beanClassDescriptor, Class beanClass, Object parent)
			throws ObjectContentManagerException, RepositoryException,JcrMappingException {
        try
        {			
			Node grandParentNode = parentNode.getParent().getParent();
			if (grandParentNode.getPath().equals("/"))
			{
				return null;
			}
			return objectConverter.getObject(session, grandParentNode.getPath());
			
		}
        catch (javax.jcr.RepositoryException e)
		{
			throw new RepositoryException(e);
		}
		
	}
}
