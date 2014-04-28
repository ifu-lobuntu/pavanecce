package org.pavanecce.uml.reverse.owl;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Test {

	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
//		OWLOntology o = m.loadOntologyFromOntologyDocument(new File("/home/ampie/Ampie/Code/vdfp/uml2code/org.vdfp.reverse.owl/src/Museum.owl"));
		OWLOntology o = m.loadOntologyFromOntologyDocument(new File("/home/ampie/Ampie/Code/vdfp/uml2code/org.vdfp.reverse.owl/src/observation.rdf"));
		System.out.println(o.getOntologyID());
		for (OWLClass owlClass : o.getClassesInSignature()) {
			System.out.println("Class : " + owlClass.getIRI());
			for (OWLObjectProperty p : owlClass.getObjectPropertiesInSignature()) {
				System.out.println("Object Property : " + p.getIRI());
			}
			for (OWLDataProperty p : owlClass.getDataPropertiesInSignature()) {
				p.getDatatypesInSignature();
				System.out.println("Data Property : " + p.getIRI());
			}
		}
		for (OWLObjectProperty p : o.getObjectPropertiesInSignature()) {
			System.out.println("Object Property : " + p.getIRI());
			Set<OWLClassExpression> ranges = p.getRanges(o);
			for (OWLClassExpression owlDataRange : ranges) {
				if (owlDataRange instanceof OWLClass) {
					System.out.println("Entity Property Type: " + ((OWLClass) owlDataRange).getIRI());
				}else{
					System.out.println(owlDataRange.getClass());
				}
			}
		}
		for (OWLDataProperty p : o.getDataPropertiesInSignature()) {
			System.out.println("Data Property : " + p.getIRI());
			Set<OWLDataRange> ranges = p.getRanges(o.getImportsClosure());
			for (OWLDataRange owlDataRange : ranges) {
				if (owlDataRange instanceof OWLDatatype) {
					System.out.println("Data Property Type: " + ((OWLDatatype) owlDataRange).getIRI());
				}else{
					System.out.println(owlDataRange.getClass());
				}
			}
		}
	}

}
