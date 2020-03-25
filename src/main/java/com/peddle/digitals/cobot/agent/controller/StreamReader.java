package com.peddle.digitals.cobot.agent.controller;

import org.apache.log4j.Logger;

import java.io.*;
public class StreamReader extends Thread
{
	InputStream is;
	String type;

	final static Logger logger = Logger.getLogger(StreamReader.class);
	
	StreamReader(InputStream is, String type)
	{
		this.is=is;
		this.type = type;
	}
	
	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ( (line = br.readLine()) != null)
			{
				logger.info(type + " > " + line);    
			}
		} 
		catch (IOException ioe)
		{
			ioe.printStackTrace();  
		}
	}
	
	
}
