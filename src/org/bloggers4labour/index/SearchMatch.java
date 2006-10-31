/*
 * SearchMatch.java
 *
 * Created on June 23, 2005, 12:31 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.index;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import org.apache.lucene.document.*;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.Installation;
import org.bloggers4labour.ItemType;
import org.bloggers4labour.Site;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.jsp.AbstractDisplayable;

/**
 *
 * @author andrewre
 */
public class SearchMatch extends AbstractDisplayable implements java.io.Serializable
{
	private float				m_Score;
	private Document			m_Doc;
	private /*transient */ long		m_ItemID;
	private /*transient */ Site		m_Site;
	private /*transient */ String		m_SiteURL;
	private /*transient */ long		m_ItemTimeMsecs;

	/*******************************************************************************
	*******************************************************************************/
	public SearchMatch( final Installation inInstall, final float inScore, final Document inDoc)
	{
		m_Score = inScore;
		m_Doc = inDoc;
		m_ItemID = Long.parseLong( m_Doc.getField("item_id").stringValue() );

		m_ItemTimeMsecs = Long.parseLong( m_Doc.getField("item_time_ms").stringValue() );

		m_SiteURL = m_Doc.getField("channel_site").stringValue();

		m_Site = inInstall.getFeedList().lookupSiteURL(m_SiteURL);
//		System.out.println(">>>> m_Site = " + m_Site);
	}

	/*******************************************************************************
		(AGR) 10 October 2006
	*******************************************************************************/
	public String getDescriptionStyle( int inNumRecommendations)
	{
		if ( inNumRecommendations > 0 && ( m_Site != null))
		{
			return  m_Site.getDescriptionStyle( null, inNumRecommendations);
		}

		return DEFAULT_DESCRIPTION_STYLE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Site getSite()
	{
		return m_Site;
	}

	/*******************************************************************************
	*******************************************************************************/
	public float getScore()
	{
		return m_Score;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Document getDocument()
	{
		return m_Doc;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDescription()
	{
		return FeedUtils.newAdjustDescription( m_Doc.getField("desc").stringValue() ); // , 1024);
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getItemID()
	{
		return m_ItemID;
	}

	/*******************************************************************************
	*******************************************************************************/
	public URL getLink()
	{
		try
		{
			return new URL( m_Doc.getField("item_link").stringValue() );
		}
		catch (Exception e)
		{
			;
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getEncodedLink()
	{
		try
		{
			return URLEncoder.encode( m_Doc.getField("item_link").stringValue(), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			;
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDispTitle()
	{
		return FeedUtils.getDisplayTitle( m_Doc.getField("title").stringValue() );
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getEncodedTitle()
	{
		try
		{
			return URLEncoder.encode( m_Doc.getField("title").stringValue(), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			;
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getSiteURL()
	{
		return m_SiteURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDescriptionStyle()
	{
		return ( m_Site != null) ? m_Site.getDescriptionStyle(null) : DEFAULT_DESCRIPTION_STYLE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDateString()
	{
		return FeedUtils.getAgeDifferenceString( System.currentTimeMillis() - m_ItemTimeMsecs);
	}

	/*******************************************************************************
		(AGR) 1 Dec 2005
	*******************************************************************************/
	public ItemType getItemType()
	{
		return ItemType.UNKNOWN;
	}

	/*******************************************************************************
		(AGR) 1 Dec 2005
	*******************************************************************************/
	public String getCommentTitle()
	{
		return null;
	}

	/*******************************************************************************
		(AGR) 1 Dec 2005
	*******************************************************************************/
	public String getCommentAuthor()
	{
		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return m_Doc.toString();
	}
}
