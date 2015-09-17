package com.cs.sjsu.edu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChiSquaredDistance 
{

	public double minMean;
	
	ChiSquaredDistance(double min)
	{
		minMean=min;
	}
	public double calcDistance(HashMap frequencyVector , HashMap meanVector)
	{
		double score = 0.0;
		
		
		Iterator it = meanVector.entrySet().iterator();
		
		while(it.hasNext())
		{
			Map.Entry pairs = (Map.Entry)it.next();
			
			List nGram = (List)pairs.getKey();
			double temp_score = (double)pairs.getValue();
			
			if(frequencyVector.containsKey(nGram))
			{
				double freScore =(double)frequencyVector.get(nGram);
				
				score = score + (Math.pow(freScore-temp_score, 2)/temp_score);
				
			}
			
			else
			{
				
				score = score + (Math.pow(temp_score,2)/temp_score);
			}
		}
		
		it= frequencyVector.entrySet().iterator();
		
		while(it.hasNext())
		{
			Map.Entry pairs = (Map.Entry)it.next();
			
			List nGram = (List)pairs.getKey();
			double temp_score = (double)pairs.getValue();
			
			if(meanVector.containsKey(nGram))
			{
				// do nothing
			}
			
			else
			{
				
				score = score + (Math.pow(temp_score, 2)/minMean);
			}
		}
		
		return score;
	}
	
}
