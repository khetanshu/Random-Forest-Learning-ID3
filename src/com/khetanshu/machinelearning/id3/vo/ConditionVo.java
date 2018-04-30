package com.khetanshu.machinelearning.id3.vo;

import java.util.HashMap;
import java.util.Map;

public class ConditionVo {
	private int conditionToLableCnt; // Total #of row for a condition of an attribute in the Training set
	private Map<String, Integer> labelMap; // Name of Label , Count of label for this condition in the Training set
	

	public ConditionVo(){
		labelMap = new HashMap<>();
	}

	public int getconditionToLabelCnt() {
		return conditionToLableCnt;
	}

	public void setconditionToLabelCnt(int count) {
		this.conditionToLableCnt = count;
	}

	public Map<String, Integer> getLabelMap() {
		return labelMap;
	}

	public void setLabelMap(Map<String, Integer> labelMap) {
		this.labelMap = labelMap;
	}
}
