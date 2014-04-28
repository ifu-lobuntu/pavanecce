package org.pavanecce.eclipse.uml.visualization;

import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class UmlVisualizationPlugin extends AbstractUIPlugin implements IRegistryChangeListener {
	public static final String PLUGIN_ID = "org.pavanecce.eclipse.uml.visualization";
	private static final String DIAGRAM_CREATOR = "diagramCreator";

	private static UmlVisualizationPlugin plugin;
	private ResourceBundle resourceBundle;
	private Set<IDiagramCreator> diagramCreators = new HashSet<IDiagramCreator>();

	public UmlVisualizationPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.pavanecce.eclipse.uml.visualization.plugin.messages");//$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		IExtensionRegistry r = Platform.getExtensionRegistry();
		addDiagramCreators(r.getConfigurationElementsFor(PLUGIN_ID, DIAGRAM_CREATOR));
		r.addRegistryChangeListener(this);

	}


	protected void addDiagramCreators(IConfigurationElement[] configurationElementsFor) {
		for (IConfigurationElement e : configurationElementsFor) {
			try {
				diagramCreators.add((IDiagramCreator) e.createExecutableExtension("className"));
			} catch (CoreException e1) {
				logError("Could not load DiagramCreator " + e.getAttribute("className"), e1);
			}
		}
	}

	public static UmlVisualizationPlugin getDefault() {
		return plugin;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = UmlVisualizationPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	@Override
	public void registryChanged(IRegistryChangeEvent event) {

		for (IExtensionDelta ed : event.getExtensionDeltas(PLUGIN_ID, DIAGRAM_CREATOR)) {
			IConfigurationElement[] configurationElements = ed.getExtension().getConfigurationElements();
			addDiagramCreators(configurationElements);
		}

	}

	public void registerExtensionDeltas(IExtensionDelta[] extensionDeltas, @SuppressWarnings("rawtypes") Set transformationSteps2) {
		for (IExtensionDelta delta : extensionDeltas) {
			if (delta.getKind() == IExtensionDelta.ADDED) {
				registerExtensions(delta.getExtension().getConfigurationElements(), transformationSteps2);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerExtensions(IConfigurationElement[] configurationElements, Set classes) {
		for (IConfigurationElement ce : configurationElements) {
			try {
				Class<? extends Object> clz = ce.createExecutableExtension("className").getClass();
				classes.add(clz);
				getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, clz.getName() + " registered"));
			} catch (CoreException e) {
				logError(e.getMessage(), e);
			}
		}
	}

	public static String getId() {
		return PLUGIN_ID;
	}

	public static void logWarning(String msg) {
		getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, msg));
	}

	public static void logInfo(String msg) {
		getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, msg));
	}

	public static IStatus logError(String message, Throwable t) {
		Status result = new Status(IStatus.ERROR, PLUGIN_ID, message, t);
		getDefault().getLog().log(result);
		return result;
	}

	public Set<IDiagramCreator> getDiagramCreators() {
		return diagramCreators;
	}

}
