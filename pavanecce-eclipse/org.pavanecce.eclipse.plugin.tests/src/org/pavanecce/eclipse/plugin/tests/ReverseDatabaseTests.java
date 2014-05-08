package org.pavanecce.eclipse.plugin.tests;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.IManagedConnection;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCDriverDefinitionConstants;
import org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionInfo;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.Test;
import org.pavanecce.eclipse.uml.reverse.db.ReverseEngineerTablesAction;

public class ReverseDatabaseTests {

	private static final String OUR_DRIVER_ID = "Our Driver ID";

	@Test
	public void test() throws Exception {
		DriverInstance[] list = DriverManager.getInstance().getAllDriverInstances();
		for (int i = 0; i < list.length; i++) {
			System.out.println("=============");
			System.out.println("Driver ID: " + list[i].getId());
			System.out.println("Driver Jar List: " + list[i].getJarList());
			System.out.println("Driver Name: " + list[i].getName());
			Properties baseProperties = list[i].getPropertySet().getBaseProperties();
			list(baseProperties);
		}
		IConnectionProfile[] plist = ProfileManager.getInstance().getProfiles();
		for (int i = 0; i < plist.length; i++) {
			System.out.println("=============");
			System.out.println("Profile Name: " + plist[i].getName());
			System.out.println("Profile Provider ID: " + plist[i].getProviderId());
			System.out.println("Profile Provider Name: " + plist[i].getProviderName());
			Properties baseProperties = plist[i].getBaseProperties();
			list(baseProperties);
		}
		Properties baseProperties = new Properties();
		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, "/home/ampie/.m2/repository/mysql/mysql-connector-java/5.1.23/mysql-connector-java-5.1.23.jar");
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.DRIVER_CLASS_PROP_ID, "com.mysql.jdbc.Driver"); //$NON-NLS-1$
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.URL_PROP_ID, "jdbc:mysql://localhost:3306/test");
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.USERNAME_PROP_ID, "root");
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.PASSWORD_PROP_ID, "ainnikki");
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID, "MySql");
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.DATABASE_VERSION_PROP_ID, "5.1");
		baseProperties.setProperty(IJDBCDriverDefinitionConstants.DATABASE_NAME_PROP_ID, "test");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf(true));
		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, "org.eclipse.datatools.enablement.mysql.5_1.driverTemplate");
		IPropertySet ips = new PropertySetImpl("Our Driver Name", OUR_DRIVER_ID);
		ips.setBaseProperties(baseProperties);
		DriverInstance di = new DriverInstance(ips);
		DriverManager.getInstance().addDriverInstance(di);
		ProfileManager pm = ProfileManager.getInstance();
		String CONNECTION_PROFILE_NAME = "Our Connection Profile";
		IConnectionProfile icp = pm.getProfileByName(CONNECTION_PROFILE_NAME);
		if (icp != null) {
			pm.deleteProfile(icp);
		}
		baseProperties.setProperty("org.eclipse.datatools.connectivity.driverDefinitionID", OUR_DRIVER_ID);
		String providerID = "org.eclipse.datatools.enablement.mysql.connectionProfile";
		IConnectionProfile p = pm.createProfile(CONNECTION_PROFILE_NAME, "Our Profile Description", providerID, baseProperties);
		p.connect();
		IManagedConnection conn = p.getManagedConnection("org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionInfo");
		ConnectionInfo connectionInfo = (ConnectionInfo) conn.getConnection().getRawConnection();
		if (connectionInfo != null) {
			Database database = connectionInfo.getSharedDatabase();
			ReverseEngineerTablesAction action = new ReverseEngineerTablesAction(new StructuredSelection(database.getSchemas()));
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("org.vdfp.reverse.test.input");
			IWorkbenchPage ap = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFile modelFile = project.getFile(new Path("models/model.di"));
			ap.openEditor(new FileEditorInput(modelFile), "org.eclipse.papyrus.infra.core.papyrusEditor");
			action.reverseInto(modelFile);
		}
	}

	public void list(Properties baseProperties) {
		Set<Entry<Object, Object>> entrySet = baseProperties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}
}
