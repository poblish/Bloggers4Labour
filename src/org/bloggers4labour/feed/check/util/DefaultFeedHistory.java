/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check.util;

import java.util.HashMap;
import java.util.Map;
import org.bloggers4labour.feed.check.FeedCheckerNotificationIF;

/**
 *
 * @author andrewregan
 */
public class DefaultFeedHistory implements FeedHistoryIF
{
	private Map<String,MutableFeedHistoryEntryIF>		m_Map = new HashMap<String,MutableFeedHistoryEntryIF>();

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFeedHistory()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public void onNotify( final FeedCheckerNotificationIF inNotification)
	{
		MutableFeedHistoryEntryIF	theHistoryEntry = m_Map.get( inNotification.getAffectedURL() );

		if ( theHistoryEntry == null)
		{
			theHistoryEntry = new DefaultFeedHistoryEntry();

			m_Map.put( inNotification.getAffectedURL(), theHistoryEntry);
		}

		_updateHistoryEntry( theHistoryEntry, inNotification);
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _updateHistoryEntry( final MutableFeedHistoryEntryIF ioHistoryEntry,
					  final FeedCheckerNotificationIF inNewNotification)
	{
		if (inNewNotification.wasSuccess())
		{
			if (_isMoreRecent( inNewNotification, ioHistoryEntry.getLastSuccess()))
			{
				ioHistoryEntry.setLastSuccess(inNewNotification);
			}
		}
		else if (inNewNotification.wasFailure())
		{
			if (_isMoreRecent( inNewNotification, ioHistoryEntry.getLastFailure()))
			{
				ioHistoryEntry.setLastFailure(inNewNotification);
			}
		}
		else if (inNewNotification.wasTimeout())
		{
			if (_isMoreRecent( inNewNotification, ioHistoryEntry.getLastTimeout()))
			{
				ioHistoryEntry.setLastTimeout(inNewNotification);
			}
		}
		else
		{
			throw new RuntimeException("Unknown Notification state: " + inNewNotification);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _isMoreRecent( final FeedCheckerNotificationIF inNewNotification, final FeedCheckerNotificationIF inExistingNotification)
	{
		return ( inExistingNotification == null || ( inNewNotification.getTaskFinishTime() >= inExistingNotification.getTaskFinishTime()));	// heard from it since then...
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedHistoryEntryIF getForURL( final String inURL)
	{
		return m_Map.get(inURL);
	}

	/*******************************************************************************
	*******************************************************************************/
	public int size()
	{
		return m_Map.size();
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