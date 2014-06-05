package org.pavanecce.uml.reverse.owl;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

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
	private static Logger logger = Logger.getLogger(Test.class.getName());

	public static void main(String[] args) throws OWLOntologyCreationException {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		// OWLOntology o = m.loadOntologyFromOntologyDocument(new
		// File("/home/ampie/Ampie/Code/vdfp/uml2code/org.vdfp.reverse.owl/src/Museum.owl"));
		OWLOntology o = m.loadOntologyFromOntologyDocument(new File("/home/ampie/Ampie/Code/vdfp/uml2code/org.vdfp.reverse.owl/src/observation.rdf"));
		logger.info(o.getOntologyID().toString());
		for (OWLClass owlClass : o.getClassesInSignature()) {
			logger.info("Class : " + owlClass.getIRI());
			for (OWLObjectProperty p : owlClass.getObjectPropertiesInSignature()) {
				logger.info("Object Property : " + p.getIRI());
			}
			for (OWLDataProperty p : owlClass.getDataPropertiesInSignature()) {
				p.getDatatypesInSignature();
				logger.info("Data Property : " + p.getIRI());
			}
		}
		for (OWLObjectProperty p : o.getObjectPropertiesInSignature()) {
			logger.info("Object Property : " + p.getIRI());
			Set<OWLClassExpression> ranges = p.getRanges(o);
			for (OWLClassExpression owlDataRange : ranges) {
				if (owlDataRange instanceof OWLClass) {
					logger.info("Entity Property Type: " + ((OWLClass) owlDataRange).getIRI());
				} else {
					logger.info(owlDataRange.getClass().getName());
				}
			}
		}
		for (OWLDataProperty p : o.getDataPropertiesInSignature()) {
			logger.info("Data Property : " + p.getIRI());
			Set<OWLDataRange> ranges = p.getRanges(o.getImportsClosure());
			for (OWLDataRange owlDataRange : ranges) {
				if (owlDataRange instanceof OWLDatatype) {
					logger.info("Data Property Type: " + ((OWLDatatype) owlDataRange).getIRI());
				} else {
					logger.info(owlDataRange.getClass().getName());
				}
			}
		}
	}

}
