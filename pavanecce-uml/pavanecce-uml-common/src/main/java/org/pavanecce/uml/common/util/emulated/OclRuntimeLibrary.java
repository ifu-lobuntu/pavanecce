package org.pavanecce.uml.common.util.emulated;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.types.OCLStandardLibrary;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.pavanecce.uml.common.util.StereotypeNames;

public class OclRuntimeLibrary {
	protected ResourceSet resourceSet;
	private Interface emailAddressType;
	private DataType dateType;
	private DataType dateTimeType;
	private DataType durationType;
	private DataType cumulativeDurationType;
	private DataType realType;
	private DataType stringType;
	private DataType integerType;
	private DataType booleanType;
	private Interface businessRole;
	private Interface taskObject;
	private Interface timedResource;
	private Interface quantifiedResource;
	private Interface requestObject;
	private Interface responsibilityObject;
	private Class person;
	private StateMachine taskRequest;
	private StateMachine processRequest;
	private Interface processObject;
	private Interface businessCollaboration;
	private StateMachine abstractRequest;
	private Interface business;
	private Interface businessActor;
	private Class businessNetwork;
	private Interface participant;
	private Interface notificationReceiver;
	private Class businessCalendar;
	private DataType durationBasedCost;
	private DataType quantityBasedCost;
	// protected UMLEnvironment parentEnvironment;
	protected Map<String, Operation> additionalOperations = new HashMap<String, Operation>();
	private OCLStandardLibrary<Classifier> oclLibrary;

	public OclRuntimeLibrary(ResourceSet resourceSet, OCLStandardLibrary<Classifier> oclLibrary) {
		super();
		this.resourceSet = resourceSet;
		this.oclLibrary = oclLibrary;
	}

	public DataType getDateTimeType() {
		return dateTimeType = findClassifier(dateTimeType, StereotypeNames.STANDARD_SIMPLE_TYPES, "DateTime");
	}

	public DataType getDateType() {
		return dateType = findClassifier(dateType, StereotypeNames.STANDARD_SIMPLE_TYPES, "Date");
	}

	public DataType getDefaultType() {
		return getStringType();
	}

	public Interface getEmailAddressType() {
		return emailAddressType = findClassifier(emailAddressType, StereotypeNames.BUSINESS_LIBRARY, "IPersonEMailAddress");
	}

	public DataType getBooleanType() {
		return booleanType = findPrimitiveType("Boolean", booleanType);
	}

	public DataType getIntegerType() {
		return integerType = findPrimitiveType("Integer", integerType);
	}

	public DataType getRealType() {
		realType = findPrimitiveType("Real", realType);
		if (realType == null) {
			realType = findClassifier(realType, StereotypeNames.STANDARD_SIMPLE_TYPES, "Real");
		}
		return realType;
	}

	public DataType getStringType() {
		return stringType = findPrimitiveType("String", stringType);
	}

	protected DataType findPrimitiveType(String name, DataType value) {
		if (value == null) {
			Resource resource = resourceSet.getResource(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI, true), true);
			TreeIterator<EObject> it = resource.getAllContents();
			while (it.hasNext()) {
				EObject eObject = it.next();
				if (eObject instanceof DataType && ((DataType) eObject).getName().equals(name)) {
					value = (DataType) eObject;
					break;
				}
			}
		}
		return value;
	}

	public Classifier lookupStandardType(String standardType) {
		if (standardType.equals("Integer")) {
			return getIntegerType();
		}
		if (standardType.equals("Real")) {
			return getRealType();
		}
		if (standardType.equals("String")) {
			return getStringType();
		}
		if (standardType.equals("Boolean")) {
			return getBooleanType();
		}
		return null;
	}

	public Interface getBusinessRole() {
		return businessRole = findClassifier(businessRole, StereotypeNames.BUSINESS_LIBRARY, "IBusinessRole");
	}

	public Interface getTaskObject() {
		return taskObject = findClassifier(taskObject, StereotypeNames.BUSINESS_LIBRARY, "ITaskObject");
	}

	public Interface getRequestObject() {
		return requestObject = findClassifier(requestObject, StereotypeNames.BUSINESS_LIBRARY, "IRequestObject");
	}

	public Interface getResponsibilityObject() {
		return responsibilityObject = findClassifier(responsibilityObject, StereotypeNames.BUSINESS_LIBRARY, "IResponsibilityObject");
	}

	@SuppressWarnings("unchecked")
	protected <T extends Classifier> T findClassifier(T c, String libName, String classifierName) {
		if (c == null) {
			Resource found = null;
			for (Resource resource : resourceSet.getResources()) {
				// HACK until Topcased becomes Papyris
				String lastSegment = resource.getURI().trimFileExtension().lastSegment();
				if (lastSegment == null) {
				} else {
					if (lastSegment.contains(".")) {
						lastSegment = lastSegment.substring(0, lastSegment.indexOf('.'));
					}
					if (libName.startsWith(lastSegment)) {
						found = resource;
					}
				}
			}
			if (found == null) {
				return null;
				// URI uri = URI.createURI(StereotypeNames.MODELS_PATHMAP + "libraries/" + libName);
				// found=resourceSet.getResource(uri, true);
			}
			EList<EObject> contents = found.getContents();
			if (contents.size() >= 1 && contents.get(0) instanceof Model) {
				Model model = (Model) contents.get(0);
				TreeIterator<EObject> eAllContents = model.eAllContents();
				while (eAllContents.hasNext()) {
					EObject eObject = eAllContents.next();
					if (eObject instanceof Classifier && ((Classifier) eObject).getName().equals(classifierName)) {
						return (T) eObject;
					}
				}
			}
		}
		return c;
	}

	public StateMachine getTaskRequest() {
		return taskRequest = findClassifier(taskRequest, StereotypeNames.BUSINESS_LIBRARY, "TaskRequest");
	}

	public DataType getDurationType() {
		return durationType = findClassifier(durationType, StereotypeNames.BUSINESS_LIBRARY, "Duration");
	}

	public DataType getCumulativeDurationType() {
		return cumulativeDurationType = findClassifier(cumulativeDurationType, StereotypeNames.BUSINESS_LIBRARY, "CumulativeDuration");
	}

	public Class getPersonNode() {
		return person = findClassifier(person, StereotypeNames.BUSINESS_LIBRARY, "PersonNode");
	}

	public Interface getProcessObject() {
		return this.processObject = findClassifier(processObject, StereotypeNames.BUSINESS_LIBRARY, "IProcessObject");
	}

	public StateMachine getAbstractRequest() {
		return abstractRequest = findClassifier(abstractRequest, StereotypeNames.BUSINESS_LIBRARY, "AbstractRequest");
	}

	public StateMachine getProcessRequest() {
		return processRequest = findClassifier(processRequest, StereotypeNames.BUSINESS_LIBRARY, "ProcessRequest");
	}

	public Interface getBusinessCollaboration() {
		return businessCollaboration = findClassifier(businessCollaboration, StereotypeNames.BUSINESS_LIBRARY, "IBusinessCollaboration");
	}

	public Interface getBusiness() {
		return business = findClassifier(business, StereotypeNames.BUSINESS_LIBRARY, "IBusiness");
	}

	public Interface getBusinessActor() {
		return businessActor = findClassifier(businessActor, StereotypeNames.BUSINESS_LIBRARY, "IBusinessActor");
	}

	public Class getBusinessNetwork() {
		return businessNetwork = findClassifier(businessNetwork, StereotypeNames.BUSINESS_LIBRARY, "BusinessNetwork");
	}

	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public Model findLibrary(String librName) {
		Model library = null;
		for (Resource resource : getResourceSet().getResources()) {
			if (resource.getContents().size() > 0 && resource.getContents().get(0) instanceof Model) {
				Model m = (Model) resource.getContents().get(0);
				if (!m.eIsProxy() && resource.getURI().lastSegment().equals(librName)) {
					library = m;
				}
			}
		}
		return library;
	}

	public OCLStandardLibrary<Classifier> getOclLibrary() {
		return this.oclLibrary;
	}

	public Interface getParticipant() {
		return this.participant = findClassifier(this.participant, StereotypeNames.BUSINESS_LIBRARY, "IParticipant");
	}

	public Interface getNotificationReceiver() {
		return this.notificationReceiver = findClassifier(this.notificationReceiver, StereotypeNames.BUSINESS_LIBRARY, "INotificationReceiver");
	}

	public Interface getTimedResource() {
		return this.timedResource = findClassifier(this.timedResource, StereotypeNames.BUSINESS_LIBRARY, "ITimedResource");
	}

	public Interface getQuantifiedResource() {
		return this.quantifiedResource = findClassifier(this.quantifiedResource, StereotypeNames.BUSINESS_LIBRARY, "IQuantifiedResource");
	}

	public Class getBusinessCalendar() {
		return this.businessCalendar = findClassifier(this.businessCalendar, StereotypeNames.BUSINESS_LIBRARY, "BusinessCalendar");
	}

	public Type getLibraryType(LibraryType typeOfChild) {
		switch (typeOfChild) {
		case BUSINESS_CALENDAR:
			return getBusinessCalendar();
		case BUSINESS_ROLE:
			return getBusinessRole();
		case TIMED_RESOURCE:
			return getTimedResource();
		case PARTICIPANT:
			return getParticipant();
		case REAL:
			return getRealType();
		case RECIPIENT:
			return getNotificationReceiver();
		case QUANTIFIED_RESOURCE:
			return getQuantifiedResource();
		case DURATION:
			return getDurationType();
		case STRING:
			return getStringType();
		default:
			return null;
		}
	}

	public DataType getDurationBasedCost() {
		return this.durationBasedCost = findClassifier(this.durationBasedCost, StereotypeNames.BUSINESS_LIBRARY, "DurationBasedCost");
	}

	public DataType getQuantityBasedCost() {
		return this.quantityBasedCost = findClassifier(this.quantityBasedCost, StereotypeNames.BUSINESS_LIBRARY, "QuantityBasedCost");
	}

	public Map<String, Operation> getAdditionalOperations() {
		return additionalOperations;
	}

	public void setAdditionalOperations(Map<String, Operation> additionalOperations) {
		this.additionalOperations = additionalOperations;
	}
}