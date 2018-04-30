package com.khetanshu.machinelearning.id3.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.khetanshu.corelib.CSVReader;
import com.khetanshu.corelib.Randomizer;
import com.khetanshu.machinelearning.id3.util.Initializer;
import com.khetanshu.machinelearning.id3.vo.AttributeVo;
import com.khetanshu.machinelearning.id3.vo.ChosenNodeVo;
import com.khetanshu.machinelearning.id3.vo.ConditionVo;
import com.khetanshu.machinelearning.id3.vo.DecisionTreeVo;

/*
 * Features and contents of the Program : 
 * 	1. RANDOM FOREST learning 
 * 	2. Decision Trees implementation using  ID3 ALGORITHM
 * 	3. Accuracy optimization using PRUNING 
 *  4. Further accuracy optimization using PROBABILISTIC RANDOMIZATION
 * 	5. Prevention towards multi-valued attributes biasing by using GAIN RATIO
 * 	6. CONFIGURATION based
 * 	7. Modularization
 * 	8. Data Encapsulation
 * 	9. NO EXTERNAL or 3rd party libraries/codes used
 * 
 * Data structures used : 
 * 	1. Linked Hash-Maps
 * 	2. Trees
 * 	3. Linked Lists
 * 
 * Developed by @khetanshu.Chauhan
 */
public class DecisionForest {
	private static final String configFilename="config/config.properties";

	private static boolean debugPrint;
	private static boolean printTree;
	private static int manualForestSize;
	private static int testDataDistributionPercent;
	private static boolean pruning;
	private static boolean probabilisticRandomization; 
	private static int randomizeIterationCount;
	private static boolean predictionFunctionality;

	private static String trainingDataFileName;
	private static String predictionDataFileName;

	private List<List<String[]>> forestTrainingData;
	private List<DecisionTreeVo> forest;
	private int totalAttributes=0;
	private int forestSize;
	public HashMap<String,Integer> attributeIndexMap= new HashMap<>();

	/*Constructor would initialize the object with the #of trees object = forestSize*/
	public DecisionForest(int forestSize) {
		this.setForestSize(forestSize);
		setForest(new LinkedList<>());
		setForestTrainingData(new ArrayList<>());
		for (int i = 0; i < forestSize; i++) {
			forest.add(new DecisionTreeVo());
		}
	}

	public static void main(String[] args) {
		List<String[]> rawTrainingData;
		List<String[]> rawTrainingDataCopy = new LinkedList<>();
		List<Map<String, String>> testData = null;
		List<Map<String, String>> predictionData;
		List<Map<String, String>> untouchedTestData = null;
		double maxTrainingAccuracy =0;
		double maxTestAccuracy=0;
		/*Initializes the program with the properties given in the user defined configuration file*/
		Initializer.initialize(configFilename);

		/*Reading training Data file*/
		rawTrainingData=CSVReader.readCSV(getTrainingDataFileName());
		rawTrainingDataCopy.addAll(rawTrainingData);
		/*Preparation of Forest and initiation of ID3 Algorithm*/
		int forestSize;
		if(getManualForestSize()>0) {
			forestSize=manualForestSize;
		}else{
			forestSize = ((int) (Math.log(rawTrainingData.size())/Math.log(32)));
			forestSize=(forestSize==0?1:forestSize);
		}
		System.out.println("Forest Size = "+ forestSize +" , Raw training data size = "+rawTrainingData.size());

		DecisionForest optimizedForest = null;
		DecisionForest forest=null;
		double maxAccuracy=0;
		
		

		for (int k = 0; k < (isProbabilisticRandomization()?getRandomizeIterationCount():1); k++) {
			forest  = new DecisionForest(forestSize);
			/*Preparing or reading the test Data */
			forest.randomizeRawData(rawTrainingData);
			if(untouchedTestData==null) {
				untouchedTestData=forest.segregateTestData(rawTrainingData, getTestDataDistributionPercent());
			}
			testData = forest.segregateTestData(rawTrainingData, getTestDataDistributionPercent());

			forest.prepareForest(rawTrainingData);
			/*creating a index table for mapping the attribute to its array index*/
			for (int j = 0; j < rawTrainingData.get(0).length-1; j++) {
				forest.attributeIndexMap.put(rawTrainingData.get(0)[j], j);
			}
			forest.totalAttributes = rawTrainingData.get(0).length-1;
			if(isDebugPrint()) {
				System.out.println("\nDecision Forest(size="+forest.forestSize+") :");
			}
			/*Training the trees in the forest*/
			for (int i=0;i< forest.forestTrainingData.size();i++) {
				DecisionTreeVo headNode = forest.getForest().get(i);
				List<String[]> trainingData = forest.forestTrainingData.get(i);
				forest.trainTree(new HashMap<>(),headNode,null,trainingData);
				double trainingAccuracy = forest.testDecisionForest(forest.convertListOfStringsToListOfMaps(rawTrainingData), rawTrainingData);
				if(trainingAccuracy>maxTrainingAccuracy) {
					maxTrainingAccuracy=trainingAccuracy;
				}
				double testAccuracy = forest.testDecisionForest(testData, rawTrainingData);
				if(testAccuracy>maxTestAccuracy) {
					maxTestAccuracy=testAccuracy;
				}
				if(isDebugPrint()) {
					System.out.println("\nTree#" + (i+1) +"\n");
					forest.printTree(forest.getForest().get(i), 0);
				}
				//Bottom-up pruning for tree accuracy optimization
				if(isDebugPrint()) {
					forest.printTree(headNode, 0);
					System.out.println();
				}
				if(isPruning()) {
					List<String> currentLeaves = new LinkedList<>();
					forest.optimizeDecisionTreeUsingPruning(headNode, testData, trainingData, headNode, currentLeaves);
				}
			}
			if(isDebugPrint()) {
				System.out.println("\nTesting Decision Forest:");
				System.out.println("\nTest data:");
				forest.printTestData(testData);
			}
			/*Testing the forest and getting its accuracy*/
			double accuracy = forest.testDecisionForest(testData, rawTrainingData);
			//System.out.println("#"+(k+1)+" Overall Forest's Optimized Accuracy after pruning= " + accuracy+ "%");
			/*If the newest forest is having more accuracy then save its as optimizedForest(The better one)*/
			if(accuracy > maxAccuracy) {
				maxAccuracy=accuracy;
				optimizedForest=forest;
			}
			/*Refresh the training data*/
			rawTrainingData.clear();
			rawTrainingData.addAll(rawTrainingDataCopy);
		}
		if(maxAccuracy==0) {
			optimizedForest=forest;
		}

		if(isPrintTree()) {
			System.out.println("\nOptimized Decision Forest (size="+optimizedForest.forestSize+") :");
			for (int i=0;i< optimizedForest.forestTrainingData.size();i++) {
				System.out.println("\nTree#" + (i+1) +"\n");
				optimizedForest.printTree(optimizedForest.getForest().get(i), 0);
				//optimizedForest.testDecisionTree(optimizedForest.getForest().get(i),testData,rawTrainingData);
			}
		}
		System.out.println("Training Accuracy       ="+ maxTrainingAccuracy);
		System.out.println("Test Accuracy           ="+maxTestAccuracy);
		System.out.println("Validation Accuracy     ="+forest.testDecisionForest(untouchedTestData, rawTrainingData));
//		if(isProbabilisticRandomization() && isPruning()) {
//			System.out.println("Overall Forest's Validation Accuracy after PRUNING and PROBABILISTIC RANDOMIZATION = " + maxAccuracy+ "%");
//		}else if(isProbabilisticRandomization() && !isPruning()) {
//			System.out.println("Overall Forest's Validation Accuracy after PROBABILISTIC RANDOMIZATION = " + maxAccuracy+ "%");
//		}else if(!isProbabilisticRandomization() && isPruning()) {
//			System.out.println("Overall Forest's Validation Accuracy after PRUNING = " + maxAccuracy+ "%");
//		}else {
//						System.out.println("\nOverall Forest's Validation Accuracy \"WITHOUT\" pruning or Probabilistic Randomization = " + maxAccuracy+ "%");
//		}	

		if(isPredictionFunctionality()) {
			/*Predicting the results as per the forest*/
			System.out.println("\nForest Prediction :");
			predictionData = optimizedForest.convertListOfStringsToListOfMaps((CSVReader.readCSV(predictionDataFileName)));
			for (Map<String, String> instance : predictionData) {
				optimizedForest.printTestInstance(instance);
				System.out.print("->"+optimizedForest.predictAnsFromTheForest(instance)+"\n");
			}
		}

	}

	/*
	 * Function would randomly choose the percentage of training data rows based on the "distributionPercent"
	 */
	public List<Map<String, String>> segregateTestData(List<String[]> trainingData, int distributionPercent) {
		List<String[]> data= new ArrayList<>();
		int total = (distributionPercent * trainingData.size())/100;
		Random random = new Random();
		data.add(trainingData.get(0));
		for (int i = 1; i <= total; i++) {
			int j;
			do {
				j = random.nextInt(trainingData.size()-1);
			}while(j==0);
			data.add(trainingData.remove(j));
		}
		if(isDebugPrint()) {
			System.out.println("\nSegregated Test Data:("+distributionPercent+"% of actual data)");
		}
		return convertListOfStringsToListOfMaps(data);
	}

	/*
	 * Function perform the randomization in the training Data so that, in case, the training data is in sorted manner,
	 * this function would improve the efficiency of the overall forest.
	 * I observed that there would be variance in the accuracy depending the random distribution of the training data
	 */
	public void randomizeRawData(List<String[]> rawTrainingData) {
		String[] header = rawTrainingData.remove(0);;
		Randomizer.randomizeTheList(rawTrainingData);
		rawTrainingData.add(0,header);
	}

	/*
	 * Does the initialization of the forest using the raw training data 
	 */
	public void prepareForest(List<String[]> rawTrainingData) {
		for (int i = 0; i < getForestSize(); i++) {
			randomizeRawData(rawTrainingData);
			List<String[]> data = new LinkedList<>();
			/*Select 70% of data from the randomized data*/
			for (int j = 0; j < (rawTrainingData.size() * .4); j++) {
				data.add(rawTrainingData.get(j));
				
			}
			forestTrainingData.add(data);
			//forestTrainingData.add(rawTrainingData);
			
		}
	}


	public double testDecisionForest(List<Map<String,String>> testData,List<String[]> trainingData) {
		int totalPassed=0;
		String labelName = trainingData.get(0)[trainingData.get(0).length-1];
		if(isDebugPrint()) {
			System.out.println("\nTest Results : ");
		}
		for (Map<String,String> testInstance : testData) {
			if(isDebugPrint()) {
				printTestInstance(testInstance);
			}
			String ans = predictAnsFromTheForest(testInstance);
			if(ans.equals(testInstance.get(labelName))){
				++totalPassed;
			}
			if(isDebugPrint()) {
				System.out.println(" = {" +ans+"} ["+ (ans.equals(testInstance.get(labelName))?"Pass":"Fail") +"]");
			}
		}
		double accurary=Math.round(((double)totalPassed/testData.size()*(double)100));
		return accurary;
	}

	/*
	 * Given the test instance this function would return the prediction based on the consensus of all the trees
	 * in the forest {Majority vote}
	 */
	public String predictAnsFromTheForest(Map<String,String> testInstance) {
		Map<String,Integer> results = new HashMap<>();
		String topHitAns=null;
		int topHits=0;
		for (int i = 0; i < getForestSize(); i++) {
			DecisionTreeVo headNode= getForest().get(i);
			String ans = predictAnswerFromATree(headNode, testInstance);
			if(results.containsKey(ans)) {
				results.put(ans, results.get(ans)+1);
			}else {
				results.put(ans, 1);
			}
			if(results.get(ans) > topHits) {
				topHitAns = ans;
				++topHits;
			}
		}
		return topHitAns;
	}

	void getCurrentNodeLeaf(DecisionTreeVo currentNode, List<String> leaves) {
		if(currentNode.getChildsCondition().isEmpty()) {
			leaves.add(currentNode.getAttribute());
		}else {
			for (DecisionTreeVo child : currentNode.getChildsCondition()) {
				getCurrentNodeLeaf(child, leaves);
			}
		}
	}

	public void optimizeDecisionTreeUsingPruning(DecisionTreeVo headNode, List<Map<String,String>> testData,List<String[]> trainingData, DecisionTreeVo currentNode,List<String> currentLeaves) {
		if(currentNode.getChildsCondition().isEmpty()) {
			currentLeaves.add(currentNode.getAttribute());
			return;
		}else {
			/*Traverse till the leaves*/
			for (DecisionTreeVo child : currentNode.getChildsCondition()) {
				List<String> newleaves = new LinkedList<>();
				/*Recurrence*/
				optimizeDecisionTreeUsingPruning(headNode, testData, trainingData, child,newleaves);
				//currentLeaves.clear();
				currentLeaves.addAll(newleaves);
			}	
			if(currentNode==headNode) {
				/*As this would the child node hence just return as no pruning can happen at leaves*/
				return;
			}
			/*Saving current node information which would be changes in below pruning logic*/
			String currentNodeAttribute = currentNode.getAttribute();
			Set<DecisionTreeVo> currentNodeChildern = currentNode.getChildsCondition();
			/*	Pruning logic
			 *	Now need to make current node's as leaf therefore setting its attribute 
			 * 	with leaf value and making its children empty for a while
			 */
			double beforePruningAccuracy = testDecisionTree(headNode, testData, trainingData);
			double maxAccuracyDuringPruning=0;
			String leafWithMaxAccuracy=null;
			currentNode.setChildsCondition(new HashSet<DecisionTreeVo>());/*as the leaf node would have no children*/
			for (String leaf : currentLeaves) {
				currentNode.setAttribute(leaf);/*setting it with the Label(leaf attribute name)*/
				//finding accuracy after pruning the current node with the leaf
				double newAccuracy = testDecisionTree(headNode, testData, trainingData);
				if(newAccuracy>maxAccuracyDuringPruning) {
					maxAccuracyDuringPruning=newAccuracy;
					leafWithMaxAccuracy=leaf;
				}
			}
			if(maxAccuracyDuringPruning>beforePruningAccuracy) {
				currentNode.setAttribute(leafWithMaxAccuracy);
				//System.out.println("$$$Tree accuracy optimized using pruning  from:"+beforePruningAccuracy +" To:"+maxAccuracyDuringPruning +" at node="+currentNodeAttribute);
			}else {
				/*If no improvement found after pruning then revert the changes made in the tree*/
				currentNode.setAttribute(currentNodeAttribute);
				currentNode.setChildsCondition(currentNodeChildern);
			}
		}
		return;
	}

	/*Tests the given decision tree by checking against the test data*/ 
	public double testDecisionTree(DecisionTreeVo headNode, List<Map<String,String>> testData,List<String[]> trainingData) {
		int totalPassed=0;
		String labelName = trainingData.get(0)[trainingData.get(0).length-1];
		if(isDebugPrint()) {
			System.out.println("\nTest Results : ");
		}
		for (Map<String,String> testInstance : testData) {
			if(isDebugPrint()) {
				printTestInstance(testInstance);
			}
			String ans = predictAnswerFromATree(headNode, testInstance);
			if(ans.equals(testInstance.get(labelName))){
				++totalPassed;
			}
			if(isDebugPrint()) {
				System.out.println(" = {" +ans+"} ["+ (ans.equals(testInstance.get(labelName))?"Pass":"Fail") +"]");
			}
		}
		double accuracy =Math.round(((double)totalPassed/testData.size()*(double)100));
		if(isDebugPrint()) {
			System.out.println("\nAccuracy = " +accuracy + "%");
		}
		return accuracy;
	}


	/* The function prints the given test instance*/
	public void printTestInstance(Map<String,String> testInstance){
		StringBuilder str = new StringBuilder();
		str.append("{");
		for (String attribute : testInstance.keySet()) {
			str.append("<"+attribute + " - " + testInstance.get(attribute)+"> ");
		}
		str.append("}");
		System.out.print(str.toString());
	}

	/* The function converts a list of strings To list of maps*/
	public List<Map<String,String>> convertListOfStringsToListOfMaps(List<String[]> data){
		List<Map<String,String>> list = new LinkedList<Map<String,String>>();
		for (int i = 1; i < data.size(); i++) {
			Map<String,String> map = new LinkedHashMap<String, String>();
			for (int j = 0; j < data.get(0).length; j++) {
				map.put(data.get(0)[j], data.get(i)[j]);
			}
			list.add(map);
		}
		return list;
	}

	/* Given the test instance this function would return the prediction based on the given decision tree*/
	private String predictAnswerFromATree(DecisionTreeVo parentNode, Map<String,String> testInstance)  {
		if(parentNode ==null || parentNode.getChildsCondition().isEmpty()) {
			/*if this is the leave node then return the label which is present as a node's attribute*/
			return new String("Not sufficient data to predict!");
		}else {
			/*Then in the child nodes if the test data is matching , if not then return NULL as a sign of NOT FOUND or NOT MATCHES*/
			DecisionTreeVo childNode=null;
			for (Iterator<DecisionTreeVo> iterator = parentNode.getChildsCondition().iterator(); iterator.hasNext();) {
				childNode = (DecisionTreeVo) iterator.next();
				if(childNode.getChildsCondition().isEmpty()) {
					/*if this is leaf node*/
					if(childNode.getParentCondition()==null || testInstance.get(parentNode.getAttribute()).equals(childNode.getParentCondition())) {
						return childNode.getAttribute(); 
					}
				}else {
					/*if this is intermediate node i.e. not leaf then traverse accordingly*/
					if(testInstance.get(parentNode.getAttribute()).equals(childNode.getParentCondition())){
						/*if testData matches the tree sequence, so far then BREAK the loop and recur to check further*/
						//System.out.println("\n##"+childNode.getAttribute()+ "-" +childNode.getParentCondition()+"\n");
						break;
					}
				}
			}
			if(childNode!=null) {
				return predictAnswerFromATree(childNode, testInstance);
			}
		}
		return null;
	}

	/*Print the tree using DFS approach for  the tree traversal*/
	public void printTree(DecisionTreeVo parentNode, int noOfSpace) {
		if(parentNode.getChildsCondition().isEmpty()) {
			return;
		}else 
		{
			//System.out.println("Condition[" + parentNode.getParentCondition() +"] -> Attribute[" + parentNode.getAttribute()+"]");
			for (Iterator<DecisionTreeVo> iterator = parentNode.getChildsCondition().iterator(); iterator.hasNext();) {
				DecisionTreeVo childNode = (DecisionTreeVo) iterator.next();
				//adding number of spaces for children so as to get the visual effect of the tree
				String space=" ";
				for (int i = 0; i < noOfSpace; i++) {
					space = space + "   ";
				}
				noOfSpace++;
				if(childNode.getChildsCondition().isEmpty()) {
					System.out.println(space+ "* Parent["+parentNode.getAttribute()+"] : Condition[" + childNode.getParentCondition() +"] -> Leaf<" + 
							childNode.getAttribute()+">");
				}else {
					System.out.println(space+ "- Parent["+parentNode.getAttribute()+"] - GainRatio{"+parentNode.getGainRatio()+"} : Condition[" + childNode.getParentCondition() +"] -> Child[" + 
							childNode.getAttribute()+"] - GainRatio{"+childNode.getGainRatio()+"}");
				}
				printTree(childNode, noOfSpace);
				noOfSpace--;
			}
		}
	}

	/*This procedure develop a tree using the training Data. The tree object would be in form of DecisionTreeVo
	 *This is an initialization process before expecting the prediction from the system 
	 */
	public void trainTree(HashMap<String, String> presentRequirementMap,DecisionTreeVo parentNode,String condition,List<String[]> trainingData) {
		HashMap<String,AttributeVo> schema = prepareSchema(presentRequirementMap, trainingData);
		//		System.out.println("\n\nBuilt Schema:\n");
		//		printSchema(schema);
		if(schema.keySet().isEmpty()) {
			return;
		}
		ChosenNodeVo choosenNode = chooseAndBuild(schema);
		String chosenAttribute = choosenNode.getName();
		DecisionTreeVo node;
		if(choosenNode.isLeaf()) {
			DecisionTreeVo childNode = new DecisionTreeVo();
			if(condition==null) {
				/*choose any attribute from the schema and any of its condition because all rows in the schema would have SINGLE label i.e. all YES, or all NO or etc*/
				/*These loops as as fetcher - its not actual loop : once the value is obtained in the 1st go the loop would BREAK*/	
				for (String attributeKey : schema.keySet()) {
					parentNode.setAttribute(attributeKey);
					for(String conditionKey:schema.get(attributeKey).getConditionMap().keySet()) {
						condition=conditionKey;
						break;
					}
					break;
				}
				parentNode.setParentCondition("^");
			}
			childNode.setAttribute(chosenAttribute);
			childNode.setGainRatio(choosenNode.getGainRatio());
			childNode.setParentCondition(condition);
			parentNode.getChildsCondition().add(childNode);
			return;
		}	
		/*if a valid child is found add that to parents */
		if(parentNode.getAttribute().equals("#")) {
			/*i.e. this is the first node*/
			parentNode.setAttribute(chosenAttribute);
			parentNode.setParentCondition("^");
			node = parentNode;
		}else {
			/*create a new instance of and add to the previous node*/
			DecisionTreeVo childNode = new DecisionTreeVo();
			childNode.setAttribute(chosenAttribute);
			childNode.setParentCondition(condition);
			parentNode.getChildsCondition().add(childNode);
			node = childNode;
		}
		node.setGainRatio(choosenNode.getGainRatio());
		//		System.out.println("** Chosen Attribute: "+chosenAttribute);
		HashMap<String, ConditionVo> conditionMap = schema.get(chosenAttribute).getConditionMap();

		for (String conditionKey : conditionMap.keySet()) {
			/*for each condition we need to make a requirement and recurrence
			 *adding the old requirement which has been received from the parent computation */
			HashMap<String, String> futureRequirementMap = new HashMap<>();
			for (String key : presentRequirementMap.keySet()) {
				futureRequirementMap.put(key, presentRequirementMap.get(key));
			}
			futureRequirementMap.put(chosenAttribute, conditionKey);
			//			System.out.println("Working for Attribute ["+ chosenAttribute+"], Condition["+ conditionKey+"]");
			/*if all the attributes are selected don't perform the recurrence*/
			if(futureRequirementMap.size()<totalAttributes) {
				trainTree(futureRequirementMap,node, conditionKey,trainingData);
			}else {
				/*That means this is the leaf node now*/
				for ( String leafNodeName : conditionMap.get(conditionKey).getLabelMap().keySet()) {
					DecisionTreeVo childNode = new DecisionTreeVo();
					childNode.setAttribute(leafNodeName);
					childNode.setParentCondition(conditionKey);
					//childNode.setInformationGain(choosenNode.getInformationGain());
					node.getChildsCondition().add(childNode);
				}
			}
		}
	}

	/* This procedure helps in developing the decision tree. 
	 * This develops a schema mapping which is used to to choose a node among rest of the available nodes for selection. 
	 * This would be used in "trainTree" procedure
	 */
	private HashMap<String,AttributeVo> prepareSchema(HashMap<String, String> presentRequirementMap, List<String[]> trainingData) { 		
		int lo =0;
		int hi=trainingData.get(0).length-2;
		HashMap<String,AttributeVo> schema = new HashMap<String,AttributeVo>();
		int maxRow=trainingData.size();
		for (int j = lo; j <= hi; j++) {
			if(!presentRequirementMap.containsKey(trainingData.get(0)[j])) {
				AttributeVo attribute = new AttributeVo();
				/*Condition separation operation*/
				HashMap<String,ConditionVo> conditionMap = new HashMap<>(); 
				for (int i = 1; i < maxRow ; i++) {
					if(followsPresentRequirement(presentRequirementMap,i,trainingData)) {
						String key = trainingData.get(i)[j];
						String label = trainingData.get(i)[hi+1];
						/*Attribute to Label operation*/
						Map<String, Integer> attributeLableMap = attribute.getAttributeTolabelMap();
						if(attributeLableMap.containsKey(label)) {
							attributeLableMap.put(label,attributeLableMap.get(label)+1);
						}else {
							attributeLableMap.put(label,1);
						}
						/*Condition to Label operation*/
						ConditionVo condition;
						if(conditionMap.containsKey(key)) {
							condition = conditionMap.get(key);
							condition.setconditionToLabelCnt(condition.getconditionToLabelCnt()+1);
							/*Label Count operation w.r.t condition*/
							Map<String, Integer> conditionLableMap = condition.getLabelMap();
							if(conditionLableMap.containsKey(label)) {
								conditionLableMap.put(label,conditionLableMap.get(label)+1);
							}else {
								conditionLableMap.put(label,1);
							}
						}else {
							condition = new ConditionVo();
							condition.setconditionToLabelCnt(1);
							condition.getLabelMap().put(label,1);
							conditionMap.put(key, condition);
						}
						attribute.setConditionMap(conditionMap);
						schema.put(trainingData.get(0)[j], attribute);
					}	
				}
			}	
		}
		return schema;
	}

	private ChosenNodeVo chooseAndBuild(HashMap<String,AttributeVo> schema){
		double maxGainRatio=0.0;
		String maxGainRatioAttribute=null;
		for (String attributeKey : schema.keySet()) {
			double gainRatio =calculatedNodeGainRatio(schema, attributeKey);
			//System.out.println("Information gain for (" +attributeKey+ "): "+gainRatio );
			if(gainRatio>maxGainRatio) {
				maxGainRatio=gainRatio;
				maxGainRatioAttribute=attributeKey;
			}
		}
//				System.out.println("\nMax info gain is with attribute ("+ maxInformationGainAttribute +") = "+ maxInformationGain);
		/* This is the leaf node - operation*/ 
		if(maxGainRatioAttribute ==null) {
			AttributeVo attribute = schema.get(schema.keySet().toArray()[0]);
			Map<String, Integer> map = attribute.getAttributeTolabelMap();
			String leafNodeName =(String)map.keySet().toArray()[0];
			return new ChosenNodeVo(leafNodeName,0.0,true);
		}
		return new ChosenNodeVo(maxGainRatioAttribute,maxGainRatio,false);
	}

	private boolean followsPresentRequirement(HashMap<String, String> presentRequirementMap, int i,List<String[]> trainingData) {
		for (String attribute : presentRequirementMap.keySet()) {
			String condition = presentRequirementMap.get(attribute);
			int attributeIndex = attributeIndexMap.get(attribute);
			if(!condition.equals(trainingData.get(i)[attributeIndex])) {
				return false;
			}
		}
		return true;
	}

	/*This procedure calculates the Gain Ration of the given attribute for the given data-set schema*/
	private double calculatedNodeGainRatio(HashMap<String,AttributeVo> schema, String attributeKey) {
		//calculating the node entropy
		double nodeEntropy=0;
		AttributeVo attribute = schema.get(attributeKey);
		Map<String, Integer> attributeTolabelMap = attribute.getAttributeTolabelMap();
		double totalTrainingInstances =0 ;//trainingDataSize;
		for(String labelkey : attributeTolabelMap.keySet()) {
			totalTrainingInstances = totalTrainingInstances + attributeTolabelMap.get(labelkey);
		}
		//System.out.println("****attributeTolabelMap.size()  = " + totalTrainingInstances);
		for(String labelkey : attributeTolabelMap.keySet()) {

			double nodeProbability = attributeTolabelMap.get(labelkey)/totalTrainingInstances;
			nodeEntropy = (nodeEntropy - (nodeProbability * (Math.log10(nodeProbability) / Math.log10(2))));
		}
		//calculating the partition entropy - all child nodes
		double partitionEntropy=0;
		Map<String, ConditionVo> conditionMap = attribute.getConditionMap();
		for (String conditionKey : conditionMap.keySet()) {
			double childEntropy=0;
			double totalConditions = conditionMap.get(conditionKey).getconditionToLabelCnt();
			Map<String, Integer> labelMap = conditionMap.get(conditionKey).getLabelMap();
			for(String labelkey : labelMap.keySet()) {
				double partitionProbability = labelMap.get(labelkey)/totalConditions;
				childEntropy = (childEntropy - (partitionProbability * (Math.log(partitionProbability) / Math.log(2))));
			}
			childEntropy= childEntropy* totalConditions;
			partitionEntropy=partitionEntropy+childEntropy;
		}
		partitionEntropy=partitionEntropy/totalTrainingInstances;
		//System.out.println("partitionEntropy "+partitionEntropy + "of attributeKey="+ attributeKey);
		double infoGain = nodeEntropy-partitionEntropy;
		double splitInformation=0;
		int totalConditions;
		for(String conditionKey : schema.get(attributeKey).getConditionMap().keySet()) {
			totalConditions=schema.get(attributeKey).getConditionMap().get(conditionKey).getconditionToLabelCnt();
			double splitProbability = totalConditions/totalTrainingInstances;
			splitInformation = splitInformation - (splitProbability * (Math.log(splitProbability)/Math.log(2)));
		}
		double gainRatio = infoGain/splitInformation;
		/* Calculate the gain ratio*/
		return gainRatio;
	}

	/*This procedure prints the the training data*/
	public void printTrainingData(List<String[]> data) {
		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < data.get(0).length; j++) {
				System.out.print(data.get(i)[j] + ((j<data.get(0).length-1)?", ":""));
			}
			System.out.println();
		}
	}

	public void printTestData(List<Map<String, String>> data) {
		for (int i = 0; i < data.size(); i++) {
			System.out.print(data.get(i));
			System.out.println();
		}
	}

	//	private void copyData(List<String[]> dest, List<String[]> src){
	//		for (String[] strings : src) {
	//			dest.ad
	//			
	//		}
	//		
	//	}


	/*This procedure prints the data-set schema*/
	private void printSchema(HashMap<String,AttributeVo> schema) {
		for (String attributeKey : schema.keySet()) {
			System.out.println("- Attribute ["+ attributeKey+"]");
			AttributeVo attribute = schema.get(attributeKey);
			Map<String, Integer> attributeTolabelMap = attribute.getAttributeTolabelMap();
			for (String lableKey : attributeTolabelMap.keySet()) {
				System.out.println("	-Label ["+lableKey+"] = Count["+ attributeTolabelMap.get(lableKey) +"]");
			}
			System.out.println("-----------------------------------");
			HashMap<String,ConditionVo> conditionMap = attribute.getConditionMap();
			for (String valueKey : conditionMap.keySet()) {
				System.out.println(" -Condition ["+valueKey+"] = Count["+ conditionMap.get(valueKey).getconditionToLabelCnt() +"]");
				for (String lableKey : conditionMap.get(valueKey).getLabelMap().keySet()) {
					System.out.println("		-Label ["+lableKey+"] = Count["+ conditionMap.get(valueKey).getLabelMap().get(lableKey) +"]");
				}
			}
			System.out.println("*************************************");
		}
	}


	/*Following are the encapsulation procedures (getters and setters of the private data members)*/

	public List<List<String[]>> getForestTrainingData() {
		return forestTrainingData;
	}

	public void setForestTrainingData(List<List<String[]>> forestTrainingData) {
		this.forestTrainingData = forestTrainingData;
	}

	public List<DecisionTreeVo> getForest() {
		return forest;
	}

	public void setForest(List<DecisionTreeVo> forest) {
		this.forest = forest;
	}

	public int getForestSize() {
		return forestSize;
	}

	public void setForestSize(int forestSize) {
		this.forestSize = forestSize;
	}

	public static int getTestDataDistributionPercent() {
		return testDataDistributionPercent;
	}

	public static void setTestDataDistributionPercent(int testDataDistributionPercent) {
		DecisionForest.testDataDistributionPercent = testDataDistributionPercent;
	}

	public static int getManualForestSize() {
		return manualForestSize;
	}

	public static void setManualForestSize(int manualForestSize) {
		DecisionForest.manualForestSize = manualForestSize;
	}

	public static boolean isProbabilisticRandomization() {
		return probabilisticRandomization;
	}

	public static void setProbabilisticRandomization(boolean randomizeRawTrainingData) {
		DecisionForest.probabilisticRandomization = randomizeRawTrainingData;
	}

	public static boolean isPredictionFunctionality() {
		return predictionFunctionality;
	}

	public static void setPredictionFunctionality(boolean predictionFunctionality) {
		DecisionForest.predictionFunctionality = predictionFunctionality;
	}

	public static String getTrainingDataFileName() {
		return trainingDataFileName;
	}

	public static void setTrainingDataFileName(String trainingDataFileName) {
		DecisionForest.trainingDataFileName = trainingDataFileName;
	}

	public static String getPredictionDataFileName() {
		return predictionDataFileName;
	}

	public static void setPredictionDataFileName(String predictionDataFileName) {
		DecisionForest.predictionDataFileName = predictionDataFileName;
	}

	public static boolean isDebugPrint() {
		return debugPrint;
	}

	public static void setDebugPrint(boolean debugPrint) {
		DecisionForest.debugPrint = debugPrint;
	}

	public static int getRandomizeIterationCount() {
		return randomizeIterationCount;
	}

	public static void setRandomizeIterationCount(int randomizeIterationCount) {
		DecisionForest.randomizeIterationCount = randomizeIterationCount;
	}

	public static boolean isPrintTree() {
		return printTree;
	}

	public static void setPrintTree(boolean printTree) {
		DecisionForest.printTree = printTree;
	}

	public static boolean isPruning() {
		return pruning;
	}

	public static void setPruning(boolean pruning) {
		DecisionForest.pruning = pruning;
	}


}
