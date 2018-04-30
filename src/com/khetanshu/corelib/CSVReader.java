package com.khetanshu.corelib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVReader {
	public static List<String[]> readCSV(String csvFilePath) {
		String delimiter =",";
		List<String[]> data = new ArrayList<>();
		try(Scanner in = new Scanner(new File(csvFilePath))){
			while(in.hasNextLine()) {
				data.add(in.nextLine().split(delimiter));
			}
			if(null!=in) {
				in.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	/*
	 * Old version
	 */
//	public static String[][] readCSV(String csvFilePath) {
//		String delimiter =",";
//		ArrayList<String[]> data = new ArrayList<>();
//		try(Scanner in = new Scanner(new File(csvFilePath))){
//			while(in.hasNextLine()) {
//				data.add(in.nextLine().split(delimiter));
//			}
//			if(null!=in) {
//				in.close();
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		return data.toArray(new String[data.size()][]);
//	}
}
