/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check;

import de.nava.informa.core.ChannelIF;
import java.util.Date;

/**
 *
 * @author andrewregan
 */
public class DefaultFeedCheckerNotification implements FeedCheckerNotificationIF
{
	private FeedCheckerAgentIF	m_Agent;
	private FeedCheckResultIF	m_Result;
	private ChannelIF		m_AffectedChannel;
	private String			m_AffectedURL;
	private long			m_StartTimeMS;
	private long			m_EndTimeMS;

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFeedCheckerNotification( final FeedCheckerAgentIF inAgent, final String inURL, final FeedCheckResultIF inResult, long inStartTimeMS)
	{
		this( inAgent, inURL, inResult, inStartTimeMS, System.currentTimeMillis());
	}

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFeedCheckerNotification( final FeedCheckerAgentIF inAgent, final ChannelIF inChannel, final FeedCheckResultIF inResult, long inStartTimeMS)
	{
		this( inAgent, inChannel, inResult, inStartTimeMS, System.currentTimeMillis());
	}

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFeedCheckerNotification( final FeedCheckerAgentIF inAgent, final String inURL, final FeedCheckResultIF inResult, long inStartTimeMS, long inEndTimeMS)
	{
		m_Agent = inAgent;
		m_AffectedURL = inURL;
		m_Result = inResult;
		m_StartTimeMS = inStartTimeMS;
		m_EndTimeMS = inEndTimeMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public DefaultFeedCheckerNotification( final FeedCheckerAgentIF inAgent, final ChannelIF inChannel, final FeedCheckResultIF inResult, long inStartTimeMS, long inEndTimeMS)
	{
		m_Agent = inAgent;
		m_AffectedChannel = inChannel;
		m_Result = inResult;
		m_StartTimeMS = inStartTimeMS;
		m_EndTimeMS = inEndTimeMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedCheckerAgentIF getAgent()
	{
		return m_Agent;
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedCheckResultIF getResult()
	{
		return m_Result;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelIF getAffectedChannel()
	{
		return m_AffectedChannel;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getAffectedURL()
	{
		return ( m_AffectedChannel != null) ? m_AffectedChannel.getSite().toString() : m_AffectedURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getTaskStartTime()
	{
		return m_StartTimeMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getTaskFinishTime()
	{
		return m_EndTimeMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean wasSuccess()
	{
		return ( m_Result instanceof FeedCheckSuccess);
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean wasFailure()
	{
		return ( m_Result instanceof FeedCheckErrorIF);
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean wasTimeout()
	{
		return ( m_Result instanceof FeedCheckTimeout);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		StringBuilder	sb = new StringBuilder();

		sb.append("DefaultFCN: {URL='").append( getAffectedURL() )
			.append("', result: ").append( m_Result )
			.append(", start: ").append( new Date(m_StartTimeMS) )
			.append(", end: ").append( new Date(m_EndTimeMS) ).append("}");

		return sb.toString();
	}
}