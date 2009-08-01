/*
 * Tester.java
 *
 * Created on 26 February 2006, 19:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import java.io.*;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

/**
 *
 * @author andrewre
 */
public class Tester
{
	private File		m_File = new File("/Users/andrewre/Desktop/b4l_main.xml");
	private ChannelBuilder	m_ChannelBuilder = new ChannelBuilder();

	/********************************************************************
	********************************************************************/
	public static void main(String[] a)
	{
		Tester	t = new Tester();

		try
		{
			while (true)
			{
				t.run();
				Thread.sleep(10000);
			}
		}
		catch (InterruptedException e)
		{
		}
	}

	/********************************************************************
	********************************************************************/
	public void run()
	{
		try
		{
			ChannelIF	channel = FeedParser.parse( m_ChannelBuilder, m_File);
			System.out.println( channel.getItems() );
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}