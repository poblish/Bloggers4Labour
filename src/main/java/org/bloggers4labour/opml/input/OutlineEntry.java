/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.opml.input;

/**
 *
 * @author andrewregan
 */
public class OutlineEntry implements OutlineEntryIF, Comparable<OutlineEntryIF>
{
	private String		m_URL;
	private String		m_FeedURL;
	private String		m_BlogName;

	/*******************************************************************************
	*******************************************************************************/
	public OutlineEntry( final String inBlogName, final String inURL, final String inFeedURL)
	{
		m_BlogName = inBlogName;
		m_URL = inURL;
		m_FeedURL = inFeedURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogName()
	{
		return m_BlogName;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogURL()
	{
		return m_URL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getFeedURL()
	{
		return m_FeedURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int compareTo( final OutlineEntryIF inOther)
	{
		int	x = getFeedURL().compareTo( inOther.getFeedURL() );

		if ( x != 0)
		{
			return x;
		}

		x = getBlogURL().compareTo( inOther.getBlogURL() );
		if ( x != 0)
		{
			return x;
		}

		return getBlogName().compareTo( inOther.getBlogName() );
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		StringBuilder	sb = new StringBuilder();

		sb.append(m_BlogName).append(" [").append(m_URL).append("]");

		return sb.toString();
	}
}