/*
 * HeadlineFilter.java
 *
 * Created on 22 March 2006, 01:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.headlines;

import java.util.Collection;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public class HeadlineFilter
{
	private InstallationIF	m_Install;
	private ChannelIF	m_Channel;

	private SiteIF		m_Site;		// (AGR) 29 Jan 2007. Removed pointless 'transient'

	/*******************************************************************************
	*******************************************************************************/
	public HeadlineFilter( final InstallationIF inInstall, final ChannelIF inChannel)
	{
		m_Install = inInstall;
		m_Channel = inChannel;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean filterMessage( final HeadlinesIF inHeads, final ItemIF inItem)
	{
		Collection<Number>	theCSsWanted = inHeads.getFilterCreatorStatuses();

		if ( theCSsWanted != null)	// use categories?
		{
			if (_findSite())		// m_Site should not be NULL, but assume NULL is acceptable for safety
			{
				// System.out.println( "got " + m_Site.getCreatorStatusRecno() + ", want: " + theCSsWanted);

				if (!theCSsWanted.contains( m_Site.getCreatorStatusRecno() ))
				{
					return false;
				}
			}
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _findSite()
	{
		if ( m_Site == null)
		{
			m_Site = m_Install.getFeedList().lookupChannel(m_Channel);
		}

		return ( m_Site != null);
	}
}