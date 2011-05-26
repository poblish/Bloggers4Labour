/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.feed.api.FeedChannelIF;

/**
 *
 * @author andrewregan
 */
public class FeedChannel implements FeedChannelIF
{
	public String		m_URL;
	public ChannelIF	m_Channel;

	/*******************************************************************************
	*******************************************************************************/
	public FeedChannel( final String a, final ChannelIF b)
	{
		m_URL = a;
		m_Channel = b;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getURL()
	{
		return m_URL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelIF getChannel()
	{
		return m_Channel;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return "[url=\"" + m_URL + "\",Channel=" + m_Channel + "]";
	}
}