/*
 * HeadlineFilter.java
 *
 * Created on 22 March 2006, 01:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.headlines;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import java.util.List;
import org.bloggers4labour.Headlines;
import org.bloggers4labour.Installation;
import org.bloggers4labour.Site;

/**
 *
 * @author andrewre
 */
public class HeadlineFilter
{
	private Installation		m_Install;
	private ChannelIF		m_Channel;

	private transient  Site		m_Site;

	/*******************************************************************************
	*******************************************************************************/
	public HeadlineFilter( final Installation inInstall, final ChannelIF inChannel)
	{
		m_Install = inInstall;
		m_Channel = inChannel;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean filterMessage( final Headlines inHeads, final ItemIF inItem)
	{
		List<Number>	theCSsWanted = inHeads.getFilterCreatorStatuses();

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