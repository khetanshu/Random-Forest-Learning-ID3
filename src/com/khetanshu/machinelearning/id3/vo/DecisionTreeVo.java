package com.khetanshu.machinelearning.id3.vo;

import java.util.HashSet;
import java.util.Set;

public class DecisionTreeVo {
	private String parentCondition;
	private String attribute;
	private double gainRatio;
	private Set<DecisionTreeVo> childsCondition;
	
	public DecisionTreeVo() {
		childsCondition = new HashSet<DecisionTreeVo>();
		setAttribute("#");
	}

	public String getParentCondition() {
		return parentCondition;
	}

	public void setParentCondition(String parentCondition) {
		this.parentCondition = parentCondition;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public Set<DecisionTreeVo> getChildsCondition() {
		return childsCondition;
	}

	public void setChildsCondition(Set<DecisionTreeVo> childsCondition) {
		this.childsCondition = childsCondition;
	}

	public double getGainRatio() {
		return gainRatio;
	}

	public void setGainRatio(double gainRatio) {
		this.gainRatio = gainRatio;
	}

	
	
	
	
}
