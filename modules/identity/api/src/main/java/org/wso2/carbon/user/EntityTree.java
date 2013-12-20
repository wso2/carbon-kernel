package org.wso2.carbon.user;

import java.util.Collections;
import java.util.List;

public class EntityTree {

	private EntityIdentifier node;
	private EntityIdentifier parentNode;
	private List<EntityTree> children;

	/**
	 * 
	 * @param node
	 * @param treeList
	 */
	public EntityTree(EntityIdentifier node, List<EntityTree> treeList) {
		this.node = node;
		this.children = treeList;
	}

	/**
	 * 
	 * @param parentIdentifier
	 * @param enitityIdentifier
	 * @param treeList
	 */
	public EntityTree(EntityIdentifier parentIdentifier, EntityIdentifier enitityIdentifier,
			List<EntityTree> treeList) {
		this.node = enitityIdentifier;
		this.children = treeList;
		this.parentNode = parentIdentifier;
	}

	/**
	 * 
	 * @return
	 */
	public EntityIdentifier getNode() {
		return node;
	}

	/**
	 * 
	 * @return
	 */
	public List<EntityTree> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * 
	 * @return
	 */
	public EntityIdentifier getParentNode() {
		return parentNode;
	}

}