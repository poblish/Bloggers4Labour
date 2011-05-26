/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.check.FeedCheckerNotificationIF;

/**
 *
 * @author andrewregan
 */
public class DefaultFeedHistory implements FeedHistoryIF
{
	private final Map<String,FeedHistoryEntryIF>	m_Map = new Object2ObjectOpenHashMap<String,FeedHistoryEntryIF>();

	private static Logger				s_Logger = Logger.getLogger( DefaultFeedHistory.class );

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFeedHistory()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean onNotify( final FeedCheckerNotificationIF inNotification)
	{
		MutableFeedHistoryEntryIF	theHistoryEntry;
		boolean				addedEntry = false;

		synchronized (m_Map)
		{
			theHistoryEntry = (MutableFeedHistoryEntryIF) m_Map.get( inNotification.getAffectedURL() );

			if ( theHistoryEntry == null)
			{
				theHistoryEntry = new DefaultFeedHistoryEntry();

				addedEntry = ( m_Map.put( inNotification.getAffectedURL(), theHistoryEntry) == null);
			}
		}

		if (_updateHistoryEntry( theHistoryEntry, inNotification))
		{
			s_Logger.trace("UPDATE: " + inNotification + " ... (" + size() + " entries)");
		}

		return addedEntry;
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedHistoryEntryIF getForURL( final String inURL)
	{
		synchronized (m_Map)
		{
			return m_Map.get(inURL);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public Iterable<FeedHistoryEntryIF> entries()
	{
		synchronized (m_Map)
		{
			return m_Map.values();
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public int size()
	{
		synchronized (m_Map)
		{
			return m_Map.size();
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		StringBuilder	sb = new StringBuilder();

		synchronized (m_Map)
		{
			sb.append("[DefaultFeedHistory: ").append(m_Map).append("]");
		}

		return sb.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _updateHistoryEntry( final MutableFeedHistoryEntryIF ioHistoryEntry,
						final FeedCheckerNotificationIF inNewNotification)
	{
		if (inNewNotification.wasSuccess())
		{
			if (_isMoreRecent( inNewNotification, ioHistoryEntry.getLastSuccess()))
			{
				ioHistoryEntry.setLastSuccess(inNewNotification);
				return true;
			}
		}
		else if (inNewNotification.wasFailure())
		{
			if (_isMoreRecent( inNewNotification, ioHistoryEntry.getLastFailure()))
			{
				ioHistoryEntry.setLastFailure(inNewNotification);
				return true;
			}
		}
		else if (inNewNotification.wasTimeout())
		{
			if (_isMoreRecent( inNewNotification, ioHistoryEntry.getLastTimeout()))
			{
				ioHistoryEntry.setLastTimeout(inNewNotification);
				return true;
			}
		}
		else
		{
			throw new RuntimeException("Unknown Notification state: " + inNewNotification);
		}

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _isMoreRecent( final FeedCheckerNotificationIF inNewNotification, final FeedCheckerNotificationIF inExistingNotification)
	{
		return ( inExistingNotification == null || ( inNewNotification.getTaskFinishTime() >= inExistingNotification.getTaskFinishTime()));	// heard from it since then...
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class DefaultFeedHistoryEntry implements MutableFeedHistoryEntryIF
	{
		private FeedCheckerNotificationIF	m_LastSuccess;
		private FeedCheckerNotificationIF	m_LastFailure;
		private FeedCheckerNotificationIF	m_LastTimeout;

		/*******************************************************************************
		*******************************************************************************/
		public FeedCheckerNotificationIF getLastSuccess()
		{
			return m_LastSuccess;
		}

		/*******************************************************************************
		*******************************************************************************/
		public FeedCheckerNotificationIF getLastFailure()
		{
			return m_LastFailure;
		}

		/*******************************************************************************
		*******************************************************************************/
		public FeedCheckerNotificationIF getLastTimeout()
		{
			return m_LastTimeout;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setLastSuccess( final FeedCheckerNotificationIF inNotification)
		{
			m_LastSuccess = inNotification;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setLastFailure( final FeedCheckerNotificationIF inNotification)
		{
			m_LastFailure = inNotification;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setLastTimeout( final FeedCheckerNotificationIF inNotification)
		{
			m_LastTimeout = inNotification;
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public String toString()
		{
			StringBuilder	sb = new StringBuilder();

			sb.append("FeedHistoryEntry: {lastSuccess: ").append(m_LastSuccess)
				.append(", lastFailure: ").append(m_LastFailure)
				.append(", lastTimeout: ").append(m_LastTimeout).append("}");

			return sb.toString();
		}
	}
}