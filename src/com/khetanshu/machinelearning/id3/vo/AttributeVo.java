package com.khetanshu.machinelearning.id3.vo;

import java.util.HashMap;
import java.util.Map;

public class AttributeVo {
	private HashMap<String,ConditionVo> conditionMap;
	private Map<String, Integer> attributeTolabelMap;
	
	public AttributeVo(){
		conditionMap = new HashMap<>();
		attributeTolabelMap = new HashMap<>();
	}

	public HashMap<String, ConditionVo> getConditionMap() {
		return conditionMap;
	}

	public void setConditionMap(HashMap<String, ConditionVo> conditionMap) {
		this.conditionMap = conditionMap;
	}


	public Map<String, Integer> getAttributeTolabelMap() {
		return attributeTolabelMap;
	}


	public void setAttributeTolabelMap(Map<String, Integer> attributeTolabelMap) {
		this.attributeTolabelMap = attributeTolabelMap;
	}
	
	
	
}
