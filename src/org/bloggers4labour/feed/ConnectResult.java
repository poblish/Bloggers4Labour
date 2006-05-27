/*
 * ConnectResult.java
 *
 * Created on November 30, 2005, 7:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import de.nava.informa.core.*;

/**
 *
 * @author andrewre
 */
public class ConnectResult
{
	private ChannelIF	m_Channel;
	private ConnectStatus	m_Status;

	/*******************************************************************************
	*******************************************************************************/
	public ConnectResult( ChannelIF x, ConnectStatus y)
	{
		m_Channel = x;
		m_Status = y;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean succeded()
	{
		return ( m_Status == ConnectStatus.SUCCESS);
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean failed()
	{
		return ( m_Status == ConnectStatus.FAILURE);
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean timedOut()
	{
		return ( m_Status == ConnectStatus.TIMEOUT);
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelIF getChannel()
	{
		return m_Channel;
	}
}
