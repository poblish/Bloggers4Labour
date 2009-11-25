/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
	private Collection<PollerIF>			m_StandardPollers = new ArrayList<PollerIF>();
	private PollerIF				m_SlowPoller;

	private Map<String,Collection<PollerIF>>	m_Map = new HashMap<String,Collection<PollerIF>>();

	private static Logger				s_Logger = Logger.getLogger( DefaultPollerAllocator.class );

	/*******************************************************************************
	*******************************************************************************/
	public DefaultPollerAllocator( final InstallationIF inInstall)
	{
		m_Install = inInstall;

		for ( PollerIF eachPoller : m_Install.getPollers())
		{
			if ( eachPoller instanceof SlowPoller)
			{
				m_SlowPoller = eachPoller;
			}
			else
			{
				m_StandardPollers.add(eachPoller);
			}
		}
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
		Collection<PollerIF>	theCurrPollers = m_Map.get(inFeedURL);

		if ( theCurrPollers == null)
		{
			m_Map.put( inFeedURL, m_StandardPollers);
		}
		else if (theCurrPollers.contains(m_SlowPoller))
		{
			m_Map.clear();
			m_Map.put( inFeedURL, m_StandardPollers);

			// FIXME. Now have to handle the registration!!! Or should we leave that to allocate()? NO!!! Because that's only called once...?
		}
		else
		{
			return;
		}

		s_Logger.debug("SUCCESS '"  + inFeedURL + "' -> " + m_Map.get(inFeedURL));
	}

	/*******************************************************************************
	*******************************************************************************/
	public void failed( final String inFeedURL)
	{
		Collection<PollerIF>	theCurrPollers = m_Map.get(inFeedURL);

		if ( theCurrPollers == null)
		{
			m_Map.put( inFeedURL, Collections.singletonList(m_SlowPoller));
		}
		else if (!theCurrPollers.contains(m_SlowPoller))
		{
			m_Map.clear();
			m_Map.put( inFeedURL, Collections.singletonList(m_SlowPoller));

			// FIXME. Now have to handle the DE-registration!!!

			// m_SlowPoller.unregisterChannel(theFailedChannel);
		}
		else
		{
			return;
		}

		s_Logger.debug("FAILED '"  + inFeedURL + "' -> " + m_Map.get(inFeedURL));
	}

	/*******************************************************************************
	*******************************************************************************/
	public void timedOut( final String inFeedURL)
	{
		failed(inFeedURL);
	}
}
