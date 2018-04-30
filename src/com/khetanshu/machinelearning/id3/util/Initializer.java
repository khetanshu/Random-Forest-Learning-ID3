package com.khetanshu.machinelearning.id3.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.khetanshu.machinelearning.id3.main.DecisionForest;

public class Initializer {
	public static void initialize(String propertiesFileName) {
		File file = new File(propertiesFileName);
		FileInputStream fileInput;
		try {
			fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			
			Enumeration<Object> enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				
				switch(key) {
				case "debugPrint":
					DecisionForest.setDebugPrint(Boolean.valueOf(value));
					break;
				case "printTree":
					DecisionForest.setPrintTree((Boolean.valueOf(value)));
					break;
				case "manualForestSize":
					DecisionForest.setManualForestSize(Integer.valueOf(value));
					break;
				case "testDataDistributionPercent":
					DecisionForest.setTestDataDistributionPercent(Integer.valueOf(value));
					break;
				case "pruning":
					DecisionForest.setPruning(Boolean.valueOf(value));
					break;
				case "probabilisticRandomization":
					DecisionForest.setProbabilisticRandomization(Boolean.valueOf(value));
					break;
				case "randomizeIterationCount":
					DecisionForest.setRandomizeIterationCount(Integer.valueOf(value));
					break;
				case "predictionFunctionality":
					DecisionForest.setPredictionFunctionality(Boolean.valueOf(value));
					break;
				case "trainingDataFileName":
					DecisionForest.setTrainingDataFileName(value);
					break;
				case "predictionDataFileName":
					DecisionForest.setPredictionDataFileName(value);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
