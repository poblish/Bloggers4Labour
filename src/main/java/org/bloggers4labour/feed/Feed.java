/*
 * Feed.java
 *
 * Created on 02 June 2006, 22:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import java.io.Serializable;
import org.bloggers4labour.ItemType;

/**
 *
 * @author andrewre
 */
public class Feed implements Comparable<Feed>, Serializable
{
	private String		m_URL;
	private FeedType	m_FeedType;
	private ItemType	m_ItemType;

	private static final long serialVersionUID = 1L;

	/********************************************************************
	********************************************************************/
	public Feed( String inURL, FeedType inFeedType)
	{
		this( inURL, inFeedType, ItemType.POST);
	}

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
	public int getFeedTypeScore()
	{
		switch (m_FeedType)
		{
			case RSS:
				return 5;
			case ATOM:
				return 4;
			case RSD:
				return 1;
		}

		return 0;
	}

	/********************************************************************
	********************************************************************/
	public String getFeedTypeName()
	{
		switch (m_FeedType)
		{
			case RSS:
				return "RSS 2.0";
			case ATOM:
				return "Atom";
			case RSD:
				return "RSD";
			case FOAF:
				return "FOAF";
		}

		return "???";
	}

	/********************************************************************
	********************************************************************/
	public ItemType getItemType()
	{
		return m_ItemType;
	}

	/********************************************************************
	********************************************************************/
	@Override public String toString()
	{
		return "[" + getFeedTypeName() + ": " + m_URL + "]";
	}

	/********************************************************************
	********************************************************************/
	public int compareTo( Feed inOther)
	{
		int	theScoreA = getFeedTypeScore();
		int	theScoreB = inOther.getFeedTypeScore();

		if ( theScoreA != 0)
		{
			return ( theScoreA < theScoreB) ? 1 : -1;
		}

		return m_URL.compareTo( inOther.m_URL );
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int hashCode()
	{
		int hash = 5;
		hash = 11 * hash + (this.m_FeedType != null ? this.m_FeedType.hashCode() : 0);
		hash = 11 * hash + (this.m_URL != null ? this.m_URL.hashCode() : 0);
//		hash = 11 * hash + (this.m_ItemType != null ? this.m_ItemType.hashCode() : 0);
		return hash;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals( Object inOther)
	{
		if ( inOther == null || !( inOther instanceof Feed))
		{
			return false;
		}

		return ( compareTo((Feed) inOther) == 0);
	}
}
