package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.pavanecce.uml.common.ocl.OpaqueExpressionContext;

public class PropertyEmulationLibrary  implements IPropertyEmulation{
	
	private OclContextFactory ocl;
	private ArtificialElementFactory artificialElementFactory;

	public PropertyEmulationLibrary(ResourceSet resourceSet,UriToFileConverter uriToFileConverter){
		this.setOcl(new OclContextFactory(resourceSet));
		this.setArtificialElementFactory(new ArtificialElementFactory(getOcl(), uriToFileConverter));
	}

	public Classifier getMessageStructure(Namespace container){
		return getArtificialElementFactory().getMessageStructure(container);
	}


	public IEmulatedPropertyHolder getEmulatedPropertyHolder(Classifier bc){
		return getArtificialElementFactory().getEmulatedPropertyHolder(bc);
	}

	public void reset(){
		getArtificialElementFactory().reset();
		getOcl().reset();
	}

	public DataType getDateTimeType(){
		return getArtificialElementFactory().getDateTimeType();
	}

	public DataType getDurationType(){
		return getArtificialElementFactory().getDurationType();
	}

	public OpaqueExpressionContext getOclExpressionContext(OpaqueExpression valueSpec){
		return getArtificialElementFactory().getOclExpressionContext(valueSpec);
	}

	public DataType getCumulativeDurationType(){
		return getArtificialElementFactory().getCumulativeDurationType();
	}


	public DataType getDurationBasedCost(){
		return getOcl().getLibrary().getDurationBasedCost();
	}

	public DataType getQuantityBasedCost(){
		return getOcl().getLibrary().getQuantityBasedCost();
	}


	public OclContextFactory getOcl(){
		return ocl;
	}

	private void setOcl(OclContextFactory ocl){
		this.ocl = ocl;
	}


	private ArtificialElementFactory getArtificialElementFactory(){
		return artificialElementFactory;
	}

	private void setArtificialElementFactory(ArtificialElementFactory artificialElementFactory){
		this.artificialElementFactory = artificialElementFactory;
	}
	
	
}
