package org.pavanecce.uml.ocltocode.common;

import org.eclipse.ocl.uml.FeatureCallExp;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.PropertyCallExp;
import org.eclipse.ocl.uml.VariableExp;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.uml.common.util.EmulatedVariable;

public class EmfPropertyCallHelper{
	public static boolean resultsInMany(OCLExpression body){
		if(body instanceof PropertyCallExp){
			PropertyCallExp pce=(PropertyCallExp) body;
			if(pce.getReferredProperty().getQualifiers().size() > 0 && pce.getQualifier().isEmpty()){
				return true;
			}else if(pce.getSource() instanceof FeatureCallExp){
				return resultsInMany( (OCLExpression) pce.getSource());
			}
		}else if(body instanceof VariableExp){
			VariableExp ve=(VariableExp) body;
			if(ve.getReferredVariable() instanceof EmulatedVariable){
				EmulatedVariable ev=(EmulatedVariable) ve.getReferredVariable();
				if(ev.getOriginalElement() instanceof Property){
					if(((Property)ev.getOriginalElement()).getQualifiers().size() > 0 ){
						return true;//NB!! This is a potential bug: we won't support qualified invocations of implicitVar's properties
					}
				}
				if(ev.getOriginalElement() instanceof MultiplicityElement){
					MultiplicityElement me=(MultiplicityElement) ev.getOriginalElement();
					return me.isMultivalued();
				}
			}
		}
		return false;
	}
}
