package com.khetanshu.machinelearning.id3.vo;

public class ChosenNodeVo {
	private String name;
	private double gainRatio;
	private boolean isLeaf;
	public ChosenNodeVo(String name,double informationGain, boolean isLeaf) {
		this.setName(name);
		this.setGainRatio(informationGain);
		this.setLeaf(isLeaf);
	}
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getGainRatio() {
		return gainRatio;
	}
	public void setGainRatio(double gainRatio) {
		this.gainRatio = gainRatio;
	}
	
	
	
}
