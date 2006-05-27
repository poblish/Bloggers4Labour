/*
 * First.java
 *
 * Created on June 21, 2005, 11:33 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.test;

import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import java.util.Observable;
import java.util.Observer;
import org.bloggers4labour.feed.*;
import org.bloggers4labour.*;

/**
 *
 * @author andrewre
 */
public class First implements Observer
{
	static FeedList		s_FL = InstallationManager.getDefaultInstallation().getFeedList();

	public static void main( String[] args)
	{
		s_FL.addObserver( new First() );
	}

	public First()
	{
	}

	public void update(Observable o, Object arg)
	{
		for ( int i = 0; i < 5; i++)
			new Thread( new Runner() ).start();
	}

	private class Runner implements Runnable
	{
		private ChannelIF	m_Channel;

		public Runner()
		{
			m_Channel = new ChannelBuilder().createChannel("foo " + this);
		}

		public void run()
		{
			Site		s = s_FL.lookupPostsChannel(m_Channel);
			System.out.println( this + " got " + s);
		}
	}
}
