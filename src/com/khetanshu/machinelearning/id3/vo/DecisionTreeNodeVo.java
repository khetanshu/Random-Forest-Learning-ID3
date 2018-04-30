package com.khetanshu.machinelearning.id3.vo;

import java.util.HashSet;

public class DecisionTreeNodeVo {
	private String attribute;
	private String value;
	private DecisionTreeNodeVo parent;
	/*
	 * As the number of children are not fixed hence need to use dynamic fast lookup based data-structure [Lookup would cost : O(1)]
	 */
	private HashSet<DecisionTreeNodeVo> childs;
	
	public DecisionTreeNodeVo(String attribute,String value, DecisionTreeNodeVo parent) {
		this.childs= new HashSet<DecisionTreeNodeVo>();
		this.attribute=attribute;
		this.value=value;
		this.parent=parent;
	}
	
	
	@Override
	public String toString() {
		return "[attribute=" + attribute + ", value=" + value + ", Total childs=" + childs.size() + "] :: {Parent's attribute= "+ 
				(parent!=null?parent.getAttribute():"--")+"}";
	}
	
	
	/*
	 * Encapsulation logic
	 */
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public HashSet<DecisionTreeNodeVo> getChilds() {
		return childs;
	}
	public void setChilds(HashSet<DecisionTreeNodeVo> childs) {
		this.childs = childs;
	}

	public DecisionTreeNodeVo getParent() {
		return parent;
	}
	public void setParent(DecisionTreeNodeVo parent) {
		this.parent = parent;
	}
	
	
}
