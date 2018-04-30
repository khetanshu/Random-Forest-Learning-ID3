package com.khetanshu.corelib;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Randomizer {
	public static void randomizeTheList(List<String[]> data){
		/*Added a prime number 19 as a seed for a better uniform randomized distribution of the data */
		Random random = new Random(19);
		int limit= data.size()-1;
		for (int i = 0; i <=limit; i++) {
			int j = random.nextInt(limit);
			Collections.swap(data, i, j);
		}
	}
}
