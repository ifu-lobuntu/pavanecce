package org.pavanecce.common.test.text.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.VersionNumber;

public class TextMetamodelVersionTests {
	VersionNumber vn;
	TextWorkspace tw;
	TextNodeVisitorAdapter a;
	File tmpDir = new File(System.getProperty("user.dir"), "tmp");

	@Before
	public void setup() {
		vn = new VersionNumber("0.0.1");
		tw = new TextWorkspace("org.bla.bla-parent");
		a = new TextNodeVisitorAdapter();

	}

	@Test
	public void testProjectWithVersion() {
		TextProjectDefinition tpd = new TextProjectDefinition(ProjectNameStrategy.MODEL_NAME_AND_SUFFIX, "suffix", true);
		TextProject tp = tw.findOrCreateTextProject(tpd, "project1", vn);
		SourceFolderDefinition sfd = new SourceFolderDefinition(SourceFolderNameStrategy.MODEL_NAME_AND_SUFFIX, "/src");
		SourceFolder sf = tp.findOrCreateSourceFolder(sfd, "model1", vn);
		List<String> textPath = Arrays.asList("org", "vdfp", "metamodels", "vdml");
		TextFile tf = sf.findOrCreateTextFile(textPath);
		assertSame(sf, tf.getParent().getParent().getParent().getParent());
		assertSame(tp, tf.getParent().getParent().getParent().getParent().getParent());
		assertSame(sf, tf.getSourceFolder());
		assertSame(tw, tf.getParent().getParent().getParent().getParent().getParent().getParent());
		assertSame(tp, tw.findTextProject("project1suffix_0_0_1"));
		assertSame(sf, tp.findOrCreateSourceFolder(sfd, "model1", vn));
		assertSame(tf, sf.findOrCreateTextFile(textPath));
		assertEquals("model1/src", sf.getName());
	}

	@Test
	public void testSourceFolderWithVersion() {
		TextProjectDefinition tpd = new TextProjectDefinition(ProjectNameStrategy.WORKSPACE_NAME_AND_SUFFIX, "-suffix");
		TextProject tp = tw.findOrCreateTextProject(tpd, "", vn);
		SourceFolderDefinition sfd = new SourceFolderDefinition(SourceFolderNameStrategy.MODEL_NAME_AND_PREFIX, "src/main/java/", true);
		SourceFolder sf = tp.findOrCreateSourceFolder(sfd, "model1", vn);
		List<String> textPath = Arrays.asList("org", "vdfp", "metamodels", "vdml");
		TextFile tf = sf.findOrCreateTextFile(textPath);
		assertEquals("bla-parent-suffix", tp.getName());
		assertSame(sf, tf.getParent().getParent().getParent().getParent());
		assertSame(tp, tf.getParent().getParent().getParent().getParent().getParent());
		assertSame(sf, tf.getSourceFolder());
		assertSame(tw, tf.getParent().getParent().getParent().getParent().getParent().getParent());
		assertSame(tp, tw.findTextProject("bla-parent-suffix"));
		assertSame(sf, tp.findOrCreateSourceFolder(sfd, "model1", vn));
		assertSame(tf, sf.findOrCreateTextFile(textPath));
		assertEquals("src/main/java/model1_0_0_1", sf.getName());
	}
}
