package com.cs.sjsu.edu;

import java.util.ArrayList;

import jpcap.JpcapCaptor;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;

public class TestDataGenerator implements PacketReceiver 
{
	public int countOfBytes ;
	public ArrayList <ArrayList <Integer>>  listOfExtractedSequencesFromAllPayloads ; // List of all payloads
	
	public TestDataGenerator()
	{
		countOfBytes =0 ;
		listOfExtractedSequencesFromAllPayloads = new ArrayList <ArrayList <Integer>> ();
	}
	public void receivePacket(Packet packet) 
	{
	    //just print out a captured packet
	    //System.out.println(packet);
		
	
		ArrayList <Integer> listOfSequences = new ArrayList<Integer>(); // List of all bytes in a payload
	    
	    //Extract Bytes
	    byte [] packetData =packet.data ;
	    
	    // Integer array for converting bytes to integer
	    int  [] payload = new int [packetData.length];
	    
	    // Convert bytes to integers for training with HMM
	    for(int i =0 ;i<packetData.length;i++)
	    {
	    	payload[i]=(int)packetData[i] & 0xff;
	    }
	    
	    //Record the count of bytes and store in ListOfSequences
	    if(payload.length!=0)
    	{
	    	countOfBytes+=payload.length;
	    
	    	for(int i=0;i<payload.length;i++)
	    	{
	    		listOfSequences.add(payload[i]);
	    	}
	    	listOfExtractedSequencesFromAllPayloads.add(listOfSequences); //Add the payload bytes to a list of all payloads
	    	System.out.print("");
    	}
	    System.out.print("");
	    
	    
	}
	public void loadPackets(int numberOfPackets,String devName) 
	{
		
		try
		{
			  
			  //Open an interface with openDevice(NetworkInterface intrface, int snaplen, boolean promics, int to_ms)
			  JpcapCaptor captor=JpcapCaptor.openFile(devName);
			  
			  //call processPacket() to let Jpcap call PacketPrinter.receivePacket() for every packet capture.
			  captor.loopPacket(numberOfPackets,this);
	
			  captor.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
   