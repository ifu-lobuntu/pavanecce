package org.pavanecce.eclipse.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CommonEclipsePlugin extends AbstractUIPlugin  {
	public static final String PLUGIN_ID = "org.pavanecce.eclipse.common";

	private static CommonEclipsePlugin plugin;
	private ResourceBundle resourceBundle;

	public CommonEclipsePlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.pavanecce.eclipse.common.plugin.messages");//$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}


	public static CommonEclipsePlugin getDefault() {
		return plugin;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = CommonEclipsePlugin.getDefault().getResourceBundle();
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

	
}
