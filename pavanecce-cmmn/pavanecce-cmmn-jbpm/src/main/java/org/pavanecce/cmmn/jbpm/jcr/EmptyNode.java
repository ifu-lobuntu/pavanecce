package org.pavanecce.cmmn.jbpm.jcr;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidLifecycleTransitionException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.observation.Event;
import javax.jcr.version.ActivityViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

public class EmptyNode implements Node {
	private Event event;

	public EmptyNode(Event event) {
		this.event = event;
	}

	@Override
	public void save() {

	}

	@Override
	public void remove() {

	}

	@Override
	public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {

	}

	@Override
	public boolean isSame(Item otherItem) throws RepositoryException {
		return false;
	}

	@Override
	public boolean isNode() {
		return true;
	}

	@Override
	public boolean isNew() {

		return false;
	}

	@Override
	public boolean isModified() {

		return false;
	}

	@Override
	public Session getSession() throws RepositoryException {

		return null;
	}

	@Override
	public String getPath() throws RepositoryException {
		return event.getPath();
	}

	@Override
	public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
		return null;
	}

	@Override
	public String getName() throws RepositoryException {

		return null;
	}

	@Override
	public int getDepth() throws RepositoryException {

		return 0;
	}

	@Override
	public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {

		return null;
	}

	@Override
	public void accept(ItemVisitor visitor) throws RepositoryException {

	}

	@Override
	public void update(String srcWorkspace) throws AccessDeniedException, InvalidItemStateException, RepositoryException {

	}

	@Override
	public void unlock() throws AccessDeniedException, InvalidItemStateException, RepositoryException {

	}

	@Override
	public Property setProperty(String name, String value, int type) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, String[] values, int type) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Value value, int type) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Node value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Calendar value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, long value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, BigDecimal value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, double value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, boolean value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Binary value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, InputStream value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, String value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, String[] values) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Value[] values) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Property setProperty(String name, Value value) throws ValueFormatException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, ConstraintViolationException, RepositoryException {

	}

	@Override
	public void restoreByLabel(String versionLabel, boolean removeExisting) throws InvalidItemStateException, RepositoryException {

	}

	@Override
	public void restore(Version version, String relPath, boolean removeExisting) throws ConstraintViolationException, InvalidItemStateException,
			RepositoryException {

	}

	@Override
	public void restore(Version version, boolean removeExisting) throws InvalidItemStateException, RepositoryException {

	}

	@Override
	public void restore(String versionName, boolean removeExisting) throws InvalidItemStateException, RepositoryException {

	}

	@Override
	public void removeSharedSet() throws ConstraintViolationException, RepositoryException {

	}

	@Override
	public void removeShare() throws ConstraintViolationException, RepositoryException {

	}

	@Override
	public void removeMixin(String mixinName) throws NoSuchNodeTypeException, ConstraintViolationException, RepositoryException {

	}

	@Override
	public void orderBefore(String srcChildRelPath, String destChildRelPath) throws ConstraintViolationException, ItemNotFoundException, RepositoryException {

	}

	@Override
	public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws AccessDeniedException, MergeException, InvalidItemStateException,
			RepositoryException {

		return null;
	}

	@Override
	public Lock lock(boolean isDeep, boolean isSessionScoped) throws AccessDeniedException, InvalidItemStateException, RepositoryException {

		return null;
	}

	@Override
	public boolean isNodeType(String nodeTypeName) throws RepositoryException {

		return false;
	}

	@Override
	public boolean isLocked() throws RepositoryException {

		return false;
	}

	@Override
	public boolean isCheckedOut() throws RepositoryException {

		return false;
	}

	@Override
	public boolean holdsLock() throws RepositoryException {

		return false;
	}

	@Override
	public boolean hasProperty(String relPath) throws RepositoryException {

		return false;
	}

	@Override
	public boolean hasProperties() throws RepositoryException {

		return false;
	}

	@Override
	public boolean hasNodes() throws RepositoryException {

		return false;
	}

	@Override
	public boolean hasNode(String relPath) throws RepositoryException {

		return false;
	}

	@Override
	public PropertyIterator getWeakReferences(String name) throws RepositoryException {

		return null;
	}

	@Override
	public PropertyIterator getWeakReferences() throws RepositoryException {

		return null;
	}

	@Override
	public VersionHistory getVersionHistory() throws RepositoryException {

		return null;
	}

	@Override
	public String getUUID() throws RepositoryException {

		return null;
	}

	@Override
	public NodeIterator getSharedSet() throws RepositoryException {

		return null;
	}

	@Override
	public PropertyIterator getReferences(String name) throws RepositoryException {

		return null;
	}

	@Override
	public PropertyIterator getReferences() throws RepositoryException {

		return null;
	}

	@Override
	public Property getProperty(String relPath) throws RepositoryException {

		return null;
	}

	@Override
	public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {

		return null;
	}

	@Override
	public PropertyIterator getProperties(String namePattern) throws RepositoryException {

		return null;
	}

	@Override
	public PropertyIterator getProperties() throws RepositoryException {

		return null;
	}

	@Override
	public NodeType getPrimaryNodeType() throws RepositoryException {

		return null;
	}

	@Override
	public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {

		return null;
	}

	@Override
	public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {

		return null;
	}

	@Override
	public NodeIterator getNodes(String namePattern) throws RepositoryException {

		return null;
	}

	@Override
	public NodeIterator getNodes() throws RepositoryException {

		return null;
	}

	@Override
	public Node getNode(String relPath) throws RepositoryException {

		return null;
	}

	@Override
	public NodeType[] getMixinNodeTypes() throws RepositoryException {

		return null;
	}

	@Override
	public Lock getLock() throws AccessDeniedException, RepositoryException {

		return null;
	}

	@Override
	public int getIndex() throws RepositoryException {

		return 0;
	}

	@Override
	public String getIdentifier() throws RepositoryException {
		return event.getIdentifier();
	}

	@Override
	public NodeDefinition getDefinition() throws RepositoryException {

		return null;
	}

	@Override
	public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, AccessDeniedException, RepositoryException {

		return null;
	}

	@Override
	public Version getBaseVersion() throws RepositoryException {

		return null;
	}

	@Override
	public String[] getAllowedLifecycleTransistions() throws RepositoryException {

		return null;
	}

	@Override
	public void followLifecycleTransition(String transition) throws InvalidLifecycleTransitionException, RepositoryException {

	}

	@Override
	public void doneMerge(Version version) throws InvalidItemStateException, RepositoryException {

	}

	@Override
	public void checkout() throws ActivityViolationException, RepositoryException {

	}

	@Override
	public Version checkin() throws InvalidItemStateException, RepositoryException {

		return null;
	}

	@Override
	public void cancelMerge(Version version) throws InvalidItemStateException, RepositoryException {

	}

	@Override
	public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {

		return false;
	}

	@Override
	public Node addNode(String relPath, String primaryNodeTypeName) throws NoSuchNodeTypeException, ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public Node addNode(String relPath) throws ConstraintViolationException, RepositoryException {

		return null;
	}

	@Override
	public void addMixin(String mixinName) throws NoSuchNodeTypeException, ConstraintViolationException, RepositoryException {

	}
}