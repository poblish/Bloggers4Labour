/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public class DefaultPollerAllocator implements PollerAllocatorIF
{
	private InstallationIF				m_Install;

	private final List<PollerIF>			m_TempPollersList = new ArrayList<PollerIF>();

	private Map<String,Collection<PollerIF>>	m_Map = new HashMap<String,Collection<PollerIF>>();

	private static Logger				s_Logger = Logger.getLogger( DefaultPollerAllocator.class );

	/*******************************************************************************
	*******************************************************************************/
	public DefaultPollerAllocator( final InstallationIF inInstall)
	{
		m_Install = inInstall;

		m_TempPollersList.addAll( m_Install.getPollers() );

		s_Logger.info("CREATED " + this + ", using Pollers: " + m_TempPollersList);
	}

	/*******************************************************************************
	*******************************************************************************/
	public Iterable<PollerIF> allocate( final String inFeedURL, final ChannelIF inChannel)
	{
		Iterable<PollerIF>	thePollers = m_Map.get(inFeedURL);

		if ( thePollers != null)
		{
			return thePollers;
		}

		s_Logger.error("Could not allocate() Pollers for: " + inFeedURL);

		return Collections.emptyList();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void success( final String inFeedURL, ChannelIF inChannel)
	{
		for ( PollerIF eachPoller : m_TempPollersList)
		{
			if (eachPoller.getFeedApprover().accept( inFeedURL, inChannel))
			{
				m_Map.put( inFeedURL, Collections.singletonList(eachPoller));

				s_Logger.trace("SUCCESS '"  + inFeedURL + "'");

				// FIXME. Now have to handle the registration!!! Or should we leave that to allocate()? NO!!! Because that's only called once...?

				return;
			}
		}

		s_Logger.warn("FAILED TO ALLOCATE '"  + inFeedURL + "', among " + m_TempPollersList);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void failed( final String inFeedURL)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public void timedOut( final String inFeedURL)
	{
		failed(inFeedURL);
	}
}
