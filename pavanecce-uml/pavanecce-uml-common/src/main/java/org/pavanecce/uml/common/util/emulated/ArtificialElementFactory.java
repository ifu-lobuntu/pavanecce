package org.pavanecce.uml.common.util.emulated;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.emf.common.util.URI;
import org.eclipse.ocl.uml.MessageType;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.CallAction;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateMachine;
import org.pavanecce.uml.common.ocl.OpaqueExpressionContext;
import org.pavanecce.uml.common.util.DefaultElementComparator;
import org.pavanecce.uml.common.util.EmfAssociationUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;

@SuppressWarnings("restriction")
public class ArtificialElementFactory implements IPropertyEmulation {
	Logger logger = Logger.getLogger(getClass().getName());
	private Map<NamedElement, Classifier> emulatedClassifiers = new HashMap<NamedElement, Classifier>();
	private Map<Classifier, IEmulatedPropertyHolder> classifierAttributes = new HashMap<Classifier, IEmulatedPropertyHolder>();
	private UriToFileConverter uriToFileConverter;
	private Map<Model, Map<String, String>> implementationCode = new HashMap<Model, Map<String, String>>();
	private TreeSet<IEmulatedElement> emulatedElements = new TreeSet<IEmulatedElement>(new DefaultElementComparator());
	private OclContextFactory oclContextFactory;

	public ArtificialElementFactory(OclContextFactory oclContextFactory, UriToFileConverter uriToFileConverter) {
		this.uriToFileConverter = uriToFileConverter;
		this.oclContextFactory = oclContextFactory;
	}

	public Classifier getMessageStructure(CallAction action) {
		if (action instanceof CallBehaviorAction) {
			return ((CallBehaviorAction) action).getBehavior();
		} else if (action instanceof CallOperationAction) {
			return getMessageStructure(((CallOperationAction) action).getOperation());
		} else {
			return null;
		}
	}

	public Property getEndToComposite(Classifier entity) {
		Property etc = EmfPropertyUtil.getEndToComposite(entity);
		if (etc == null && !(entity instanceof IEmulatedElement)) {
			return getArtificialEndToComposite(entity);
		}
		return etc;
	}

	public Property getArtificialEndToComposite(Classifier entity) {
		if (!(entity instanceof MessageType)) {
			IEmulatedPropertyHolder eph = getEmulatedPropertyHolder(entity);
			for (AbstractEmulatedProperty p : eph.getEmulatedAttributes()) {
				if (p.getOtherEnd() != null && p.getOtherEnd().isComposite()) {
					return p;
				}
			}
		}
		return null;
	}

	@Override
	public Classifier getMessageStructure(Namespace container) {
		if (container instanceof Behavior) {
			return (Behavior) container;
		} else if (container != null) {
			Classifier classifier = emulatedClassifiers.get(container);
			return classifier;
		} else {
			return null;
		}
	}

	public Set<Property> getDirectlyImplementedAttributes(Classifier c) {
		Set<Property> propertiesInScope = EmfPropertyUtil.getDirectlyImplementedAttributes(c);
		addEmulatedProperties(c, propertiesInScope);
		if (c instanceof BehavioredClassifier) {
			for (Interface intf : ((BehavioredClassifier) c).getImplementedInterfaces()) {
				// TODO filter out emulated properties that may already have
				// been implemented by the superclass
				addAllEmulatedProperties(intf, propertiesInScope);
			}
		}
		Iterator<Property> iterator = propertiesInScope.iterator();
		while (iterator.hasNext()) {
			Property property = iterator.next();
			if (!property.isNavigable()) {
				iterator.remove();
			}
		}
		return propertiesInScope;
	}

	public List<Property> getEffectiveAttributes(Classifier bc) {
		List<Property> props = EmfPropertyUtil.getEffectiveProperties(bc);
		List<Property> result = new ArrayList<Property>(props);
		addAllEmulatedProperties(bc, result);
		Iterator<Property> iterator = result.iterator();
		while (iterator.hasNext()) {
			Property property = iterator.next();
			if (!(property.isNavigable())) {
				iterator.remove();
			}
		}
		return result;
	}

	protected void addAllEmulatedProperties(Classifier bc, Collection<Property> propertiesInScope) {
		addEmulatedProperties(bc, propertiesInScope);
		for (Classifier classifier : bc.getGenerals()) {
			addAllEmulatedProperties(classifier, propertiesInScope);
		}
		if (bc instanceof BehavioredClassifier) {
			for (Interface i : ((BehavioredClassifier) bc).getImplementedInterfaces()) {
				addAllEmulatedProperties(i, propertiesInScope);
			}
		}
	}

	private void addEmulatedProperties(Classifier bc, Collection<Property> propertiesInScope) {
		IEmulatedPropertyHolder holder = getEmulatedPropertyHolder(bc);
		if (holder != null) {
			for (AbstractEmulatedProperty aep : holder.getEmulatedAttributes()) {
				if (aep.shouldEmulate()) {
					propertiesInScope.add(aep);
				}
			}
		}
		for (Association a : bc.getAssociations()) {
			if (EmfAssociationUtil.isClass(a)) {
				EmulatedPropertyHolderForAssociation epha = (EmulatedPropertyHolderForAssociation) getEmulatedPropertyHolder(a);
				for (Property m : a.getMemberEnds()) {
					if (m.getOtherEnd().getType() == bc) {
						propertiesInScope.add(epha.getEndToAssociation(m));
					}
				}
			}
		}
	}

	@Override
	public IEmulatedPropertyHolder getEmulatedPropertyHolder(Classifier bc) {
		IEmulatedPropertyHolder holder = classifierAttributes.get(bc);
		if (holder == null) {
			if (bc instanceof StateMachine) {
				holder = new EmulatedPropertyHolderForStateMachine((StateMachine) bc, this);
			} else if (bc instanceof Association) {
				holder = new EmulatedPropertyHolderForAssociation((Association) bc, this);
			} else if (bc instanceof Behavior) {
				holder = new EmulatedPropertyHolderForBehavior((Behavior) bc, this);
			} else if (bc instanceof BehavioredClassifier) {
				holder = new EmulatedPropertyHolderForBehavioredClassifier((BehavioredClassifier) bc, this);
			} else {
				holder = new EmulatedPropertyHolder(bc, this);
			}
			classifierAttributes.put(bc, holder);
		}
		return holder;
	}

	public Property findEffectiveAttribute(Classifier fromClass, String name) {
		for (Property p : getDirectlyImplementedAttributes(fromClass)) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		for (Classifier classifier : fromClass.getGenerals()) {
			Property found = findEffectiveAttribute(classifier, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public String getImplementationCodeFor(Model m, String artefactName) {
		Map<String, String> map = implementationCode.get(m);
		if (map == null) {
			ZipFile zipFile = null;
			try {
				map = new HashMap<String, String>();
				implementationCode.put(m, map);
				URI uri = m.eResource().getURI().trimFileExtension().appendFileExtension("zip");
				uri = m.eResource().getResourceSet().getURIConverter().normalize(uri);
				File file = uriToFileConverter.resolveUri(uri);
				if (file != null) {
					zipFile = new ZipFile(file);
					java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry zipEntry = entries.nextElement();
						if (!zipEntry.isDirectory()) {
							BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
							StringBuilder sb = new StringBuilder();
							String line;
							while ((line = reader.readLine()) != null) {
								sb.append(line);
								sb.append("\n");
							}
							map.put(zipEntry.getName(), sb.toString());
						}
					}

				}
			} catch (IOException e) {
				logger.info(e.toString());
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return map.get(artefactName);
	}

	public void reset() {
		emulatedClassifiers.clear();
		classifierAttributes.clear();
	}

	public SortedSet<IEmulatedElement> getAllEmulatedElements() {
		return emulatedElements;
	}

	@Override
	public DataType getDateTimeType() {
		return getLibrary().getDateTimeType();
	}

	@Override
	public DataType getDurationType() {
		return getLibrary().getDurationType();
	}

	@Override
	// TODO move
	public OpaqueExpressionContext getOclExpressionContext(OpaqueExpression valueSpec) {
		return oclContextFactory.getOclExpressionContext(valueSpec);
	}

	@Override
	public DataType getCumulativeDurationType() {
		return getLibrary().getCumulativeDurationType();
	}

	@Override
	public Classifier getQuantityBasedCost() {
		return getLibrary().getQuantityBasedCost();
	}

	@Override
	public Classifier getDurationBasedCost() {
		return getLibrary().getDurationBasedCost();
	}

	private OclRuntimeLibrary getLibrary() {
		return oclContextFactory.getLibrary();
	}
}
