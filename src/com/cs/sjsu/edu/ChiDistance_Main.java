package com.cs.sjsu.edu;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ChiDistance_Main {


	public static void main(String[] args) 
	{
				
		try
		{
				String fname = args[0];            // Training data
				String fnameTest = args[1];        // Scoring data
				
				// Train MD distance scheme on normal data
				TestDataGenerator packetReader = new TestDataGenerator();
				
				packetReader.loadPackets(-1,fname); // Use all (-1) packets for training 
				
				
				//Array of frequency Vectors for each training packet
				ArrayList<HashMap> mdVectors = new ArrayList<HashMap>();
				
				// mean Vector for MD distace calculation
				HashMap meanMdVector = new HashMap();
				
				//Read payload size and nGram size from properties file
				FileReader reader = new FileReader("ChiDistance.properties");
				Properties properties = new Properties();
				properties.load(reader);
				
				int nGramSize=Integer.parseInt(properties.getProperty("nGramsize"));
				
				// Populate the frequency Vectors for payloads
				for(int i=0;i<packetReader.listOfExtractedSequencesFromAllPayloads.size();i++)
				{
					//Extract the payload
					ArrayList<Integer> payload = new ArrayList<Integer>();
					payload = packetReader.listOfExtractedSequencesFromAllPayloads.get(i);
					
					// frequency Vector for payload
					HashMap mdVector = new HashMap();
					
					// Extract n grams from payload
					for(int j=0 ; j<payload.size()-(nGramSize-1);j++) // length - 2 done avoid ArrayIndexOutOfBoundsException
					{
						//Extract the nGram
						List nGram = payload.subList(j, j+nGramSize);
						
						//Increment the frequency count if nGram already present in payload
						if(mdVector.containsKey(nGram))
						{
							double count = (double)mdVector.get(nGram);
							count+=1.0;
							mdVector.put(nGram, count);
						}
						
						// Create a new nGram in the vector with frequency count 1
						else
						{
							mdVector.put(nGram, 1.0);
						}
						
					}
					
					//Normalize the frequency by the size of the payload in the mdVector
					Iterator it = mdVector.entrySet().iterator();
					
					while(it.hasNext())
					{
						 Map.Entry pairs = (Map.Entry)it.next();
						 
						 List nGram= (List) pairs.getKey();
						 double frequency = (double)pairs.getValue();
						 
						 
						 mdVector.put(nGram, (double) frequency/(payload.size()));
						 
					}
					// Add the frequency vector to the list of md Vectors
					mdVectors.add(mdVector);
				
				}
				
				
				// Calculate the mean and stdev vectors
				for(int i=0;i<mdVectors.size();i++)
				{
					HashMap mdVector = mdVectors.get(i);
					
					Iterator it = mdVector.entrySet().iterator();
					
					while(it.hasNext())
					{
						 Map.Entry pairs = (Map.Entry)it.next();
						 
						 List nGram= (List) pairs.getKey();
						 double frequency = (double)pairs.getValue();
						 
						 // Accumulate the score of the nGram if already present in the vector
						 if(meanMdVector.containsKey(nGram))
						 {
							 double score = (double)meanMdVector.get(nGram);
							 double add_scores = score + frequency ;
							 
							 meanMdVector.put(nGram, add_scores);
						 }
						 
						 // Create a new entry in the vector
						 else
						 {
							 meanMdVector.put(nGram, frequency);
						 }
						 
					}
				}
				
				Iterator it = meanMdVector.entrySet().iterator();
				
				double minMean =1000;
				// Divide the scores of nGrams by the total number of payloads
				while(it.hasNext())
				{
					Map.Entry pairs = (Map.Entry)it.next();
					
					List nGram = (List) pairs.getKey();
					double score = (double)pairs.getValue();
					
					double normalized_score = score/mdVectors.size();
					
					if(normalized_score<minMean)
						minMean=normalized_score;
					
					meanMdVector.put(nGram,normalized_score);
				}
				
				System.out.println("The minimum mean ="+ minMean);
				// Calculate the ChiSquared distance between mean Vector and input vector
				
				// Populate the input vector
				
				TestDataGenerator testDataGenerator = new TestDataGenerator();
				testDataGenerator.loadPackets(-1,fnameTest);
				
				//Change names of train and test files. Remove .pcap
				fname=fname.replaceAll(".pcap", "");
				fnameTest=fnameTest.replaceAll(".pcap", "");
				
				// Write the scores to a csv file
				BufferedWriter out = new BufferedWriter(new FileWriter(fname+"_"+fnameTest+".dat"));
				DecimalFormat df = new DecimalFormat("#.###############");
				
				// Md Distance object 
				ChiSquaredDistance md = new ChiSquaredDistance(minMean);
				
				
				int size=Integer.parseInt(properties.getProperty("payloadSize"));
				int temp_size=size;
				
				//Time to begin testing
				double begTime=System.currentTimeMillis();
				
				for(int i=0;i<testDataGenerator.listOfExtractedSequencesFromAllPayloads.size();i++)
				{
					//Restore the saved input size
					size=temp_size;
					// Extract a payload
					ArrayList<Integer> payload = new ArrayList<Integer>();
					payload = testDataGenerator.listOfExtractedSequencesFromAllPayloads.get(i);
					
					
					// Frequency vector for test payloads
					HashMap mdVectorTest = new HashMap();
					
					//Test the incoming payload by taking a subset of bytes
					if(size==-1 || size>=payload.size())
					{
						size=payload.size();
					}
					else
					{
						
					}
					for(int j=0;j<size-(nGramSize-1);j++) // payload length - 2 to avoid ArrayIndexOutOfBounds Exception
					{
						List nGram = (List)payload.subList(j, j+nGramSize);
						
						if(mdVectorTest.containsKey(nGram))
						{
							double score =(double) mdVectorTest.get(nGram);
							score = score + 1.0;
							mdVectorTest.put(nGram, score);
						}
						
						else
						{
							mdVectorTest.put(nGram, 1.0);
						}
										
					}
					
					// Normalize the frequency counts by payload size
					
					 it = mdVectorTest.entrySet().iterator();
					 
					 while(it.hasNext())
					 {
						 Map.Entry pairs = (Map.Entry)it.next();
						 
						 List key = (List)pairs.getKey();
						 double score = (double)pairs.getValue();
						 
						 score = score /(payload.size()); 
						 
						 mdVectorTest.put(key, score);
					 }
					 
					 
					// Calculate the MD distance for each payload
					 out.write(i+"\t"+df.format(md.calcDistance(mdVectorTest,meanMdVector))+"\n");
				}
		
				out.close();
				
				double endTime = System.currentTimeMillis();
				System.out.println("The total time to test "+(endTime-begTime));
				
				// Write performance related details to time.txt file
				BufferedWriter outTime = new BufferedWriter(new FileWriter(nGramSize+"_time.txt",true));
				outTime.write(testDataGenerator.listOfExtractedSequencesFromAllPayloads.size()+" "+(endTime-begTime)+"\n");
				outTime.close();
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

}		
