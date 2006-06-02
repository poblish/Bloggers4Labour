/*
 * Feed.java
 *
 * Created on 02 June 2006, 22:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import org.bloggers4labour.ItemType;

/**
 *
 * @author andrewre
 */
public class Feed
{
	private String		m_URL;
	private FeedType	m_FeedType;
	private ItemType	m_ItemType;

	/********************************************************************
	********************************************************************/
	public Feed( String inURL, FeedType inFeedType, ItemType inItemType)
	{
		m_URL = inURL;
		m_FeedType = inFeedType;
		m_ItemType = inItemType;
	}

	/********************************************************************
	********************************************************************/
	public String getURL()
	{
		return m_URL;
	}

	/********************************************************************
	********************************************************************/
	public FeedType getFeedType()
	{
		return m_FeedType;
	}

	/********************************************************************
	********************************************************************/
	public ItemType getItemType()
	{
		return m_ItemType;
	}
}
