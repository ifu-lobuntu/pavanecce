package org.pavanecce.common.test.text.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.pavanecce.common.text.filegeneration.TextFileDeleter;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.CharArrayTextSource;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.PropertiesSource;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.VersionNumber;

public class TextMetamodelTests {
	VersionNumber vn;
	TextWorkspace tw;
	TextNodeVisitorAdapter a;
	File tmpDir = new File(System.getProperty("user.home"), "tmp");

	@Before
	public void setup() {
		vn = new VersionNumber("0.0.1");
		tw = new TextWorkspace("org.bla.bla-parent");
		a = new TextNodeVisitorAdapter();

	}

	@Test
	public void testIt() {
		TextProjectDefinition tpd = new TextProjectDefinition(ProjectNameStrategy.MODEL_NAME_AND_SUFFIX, "suffix");
		TextProject tp = tw.findOrCreateTextProject(tpd, "project1", vn);
		SourceFolderDefinition sfd = new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "src");
		sfd.cleanDirectories();
		sfd.overwriteFiles();
		SourceFolder sf = tp.findOrCreateSourceFolder(sfd, "", vn);
		List<String> textPath = Arrays.asList("org", "vdfp", "metamodels", "vdml.java");
		TextFile tf = sf.findOrCreateTextFile(textPath);
		assertEquals("project1suffix/src/org/vdfp/metamodels/vdml.java", tf.getWorkspaceRelativePath());
		assertTrue(sf.shouldClean());
		assertTrue(tf.overwrite());
		assertSame(sf, tf.getParent().getParent().getParent().getParent());
		assertSame(tp, tf.getParent().getParent().getParent().getParent().getParent());
		assertSame(sf, tf.getSourceFolder());
		assertSame(tw, tf.getParent().getParent().getParent().getParent().getParent().getParent());
		assertSame(tp, tw.findTextProject("project1suffix"));
		assertSame(sf, tp.findOrCreateSourceFolder(sfd, "", vn));
		assertSame(tf, sf.findOrCreateTextFile(textPath));
		assertEquals("src", sf.getName());
	}

	@Test
	public void testFileGen() {
		TextProjectDefinition tpd = new TextProjectDefinition(ProjectNameStrategy.MODEL_NAME_AND_SUFFIX, "suffix");
		TextProject tp = tw.findOrCreateTextProject(tpd, "project1", vn);
		SourceFolderDefinition sfd = new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "src");
		final List<String> textPath = Arrays.asList("org", "vdfp", "metamodels", "vdml.java");
		SourceFolder sf = tp.findOrCreateSourceFolder(sfd, "", vn);
		TextFile tf = sf.findOrCreateTextFile(textPath);
		tf.setTextSource(new CharArrayTextSource("asdbasdfasdfas".toCharArray()));
		tmpDir.mkdir();
		TextFileGenerator gen = new TextFileGenerator(tmpDir);
		a.startVisiting(tw, gen);
		assertTrue(new File(tmpDir, "project1suffix/src/org/vdfp/metamodels/vdml.java").exists());
	}

	@Test
	public void testFileDelete() {
		TextProjectDefinition tpd = new TextProjectDefinition(ProjectNameStrategy.MODEL_NAME_AND_SUFFIX, "suffix");
		TextProject tp = tw.findOrCreateTextProject(tpd, "project1", vn);
		SourceFolderDefinition sfd = new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "src");
		final List<String> textPath = Arrays.asList("org", "vdfp", "metamodels", "vdml.java");
		SourceFolder sf = tp.findOrCreateSourceFolder(sfd, "", vn);
		Properties props = new Properties();
		vn.writeTo(props);
		sf.findOrCreateTextFile(textPath).setTextSource(new PropertiesSource(props));
		tmpDir.mkdir();
		TextFileGenerator gen = new TextFileGenerator(tmpDir);
		a.startVisiting(tw, gen);
		assertTrue(new File(tmpDir, "project1suffix/src/org/vdfp/metamodels/vdml.java").exists());
		TextFileDeleter del = new TextFileDeleter(tmpDir);
		a.startVisiting(tw, del);
		assertTrue(new File(tmpDir, "project1suffix/src/org/vdfp/metamodels/vdml.java").exists());
		sf.markNodeForDeletion(textPath, true);
		a.startVisiting(tw, del);
		assertFalse(new File(tmpDir, "project1suffix/src/org/vdfp/metamodels/vdml.java").exists());
	}
}
