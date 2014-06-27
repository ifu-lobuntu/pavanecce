package org.pavanecce.uml.test.uml2code.jpa;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.ejb.HibernatePersistence;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class TestPersistenceUnitInfo implements PersistenceUnitInfo {

	private Properties properties = new Properties();
	private List<String> managedClassNames = new ArrayList<String>();
	private DataSource jtaDataSource;
	private String name;
	private ClassLoader classLoader;

	public TestPersistenceUnitInfo(String name) {
		super();
		this.name = name;
	}

	public TestPersistenceUnitInfo(String string, ClassLoader cl) {
		this.classLoader = cl;
		System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
		getProperties().put("hibernate.max_fetch_depth", "3");
		getProperties().put("hibernate.hbm2ddl.auto", "create");
		getProperties().put("hibernate.show_sql", "false");

		getProperties().put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

		getProperties().put("hibernate.id.new_generator_mappings", "false");
		getProperties().put("hibernate.transaction.jta.platform", "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform");

	}

	@Override
	public String getPersistenceUnitName() {
		return this.name;
	}

	@Override
	public String getPersistenceProviderClassName() {
		return HibernatePersistence.class.getName();
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return PersistenceUnitTransactionType.JTA;
	}

	@Override
	public DataSource getJtaDataSource() {
		if (jtaDataSource == null) {
			jtaDataSource = setupPoolingDataSource();
		}
		return jtaDataSource;
	}

	protected PoolingDataSource setupPoolingDataSource() {
		String uniqueName = "jdbc/" + name + "ds";
		try {
			return (PoolingDataSource) new InitialContext().lookup(uniqueName);
		} catch (Exception e) {
			PoolingDataSource pds = new PoolingDataSource();
			pds.setClassName("org.h2.jdbcx.JdbcDataSource");
			pds.setUniqueName(uniqueName);
			// pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
			pds.setMaxPoolSize(5);
			pds.setAllowLocalTransactions(true);
			pds.getDriverProperties().put("user", "sa");
			pds.setApplyTransactionTimeout(false);
			pds.getDriverProperties().put("password", "");
			pds.getDriverProperties().put("URL", "jdbc:h2:mem:" + name + "-db;MVCC=true");
			pds.init();
			return pds;
		}
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return null;
	}

	@Override
	public List<String> getMappingFileNames() {
		return Collections.emptyList();
	}

	@Override
	public List<URL> getJarFileUrls() {
		return Collections.emptyList();
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		return getClass().getResource("/");
	}

	@Override
	public List<String> getManagedClassNames() {
		return managedClassNames;
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return true;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return SharedCacheMode.NONE;
	}

	@Override
	public ValidationMode getValidationMode() {
		return ValidationMode.NONE;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return "2.0";
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {

	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		return new URLClassLoader(new URL[0], getClassLoader());
	}

}
