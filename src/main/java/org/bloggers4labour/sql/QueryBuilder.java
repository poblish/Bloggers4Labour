/*
 * QueryBuilder.java
 *
 * Created on May 3, 2005, 4:10 AM
 */

package org.bloggers4labour.sql;

import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author andrewre
 */
public class QueryBuilder implements QueryBuilderIF
{
	private ResourceBundle		m_Bundle;
	private String			m_AdjustedNameQuery;

	/*******************************************************************************
	*******************************************************************************/
	public QueryBuilder( final ResourceBundle inBundle)
	{
		m_Bundle = inBundle;
		m_AdjustedNameQuery = m_Bundle.getString("sql.adjusted.name.query");
	}

	/*******************************************************************************
		(AGR) 12 July 2005
	*******************************************************************************/
	public String getDigestEmailQuery( long inHrs, long inMins)
	{
		Formatter	theFormatter = new Formatter();

		theFormatter.format( m_Bundle.getString("sql.digest.email.query"), inHrs, inMins);

		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getAllBlogFeeds()
	{
		return m_Bundle.getString("sql.all.blog.feeds");
	}

	/*******************************************************************************
	*******************************************************************************/
	private String getBlogsByCategory( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, m_Bundle.getString("sql.category_blogs.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByCategoryAscending()
	{
		return getBlogsByCategory(" ORDER BY cat.ordering," + m_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByCategoryDescending()
	{
		return getBlogsByCategory(" ORDER BY cat.ordering," + m_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsListing( String inOrderStr)
	{
		return getBlogsByCategory(inOrderStr);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByLocation( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, m_Bundle.getString("sql.location_blogs.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByLocationAscending()
	{
		return getBlogsByLocation(" ORDER BY loc.ordering," + m_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByLocationDescending()
	{
		return getBlogsByLocation(" ORDER BY loc.ordering," + m_AdjustedNameQuery + " DESC");
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByStatus( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, m_Bundle.getString("sql.status_blogs.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByStatusAscending()
	{
		return getBlogsByStatus(" ORDER BY crs.ordering," + m_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogsByStatusDescending()
	{
		return getBlogsByStatus(" ORDER BY crs.ordering," + m_AdjustedNameQuery + " DESC");
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getSimpleBlogsListing( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, m_Bundle.getString("sql.simple_blogs.query"), m_Bundle.getString("sql.name_initial.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getSimpleBlogsListingAscending()
	{
		return getSimpleBlogsListing(" ORDER BY " + m_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getSimpleBlogsListingDescending()
	{
		return getSimpleBlogsListing(" ORDER BY " + m_AdjustedNameQuery + " DESC");
	}

	/*******************************************************************************
		(AGR) 22 May 2005
	*******************************************************************************/
	public String getBlogsTotalQuery()
	{
		return m_Bundle.getString("sql.total.blogs");
	}

	/*******************************************************************************
		(AGR) 27 May 2006
	*******************************************************************************/
	public String getUnapprovedBlogsQuery()
	{
		return m_Bundle.getString("sql.unapproved.query");
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public String getRecommendationCountsQueryString( String inURLsList)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( m_Bundle.getString("sql.recommendationcounts.query"), inURLsList);
		return theFormatter.toString();
	}

	/*******************************************************************************
		(AGR) 16 January 2007
	*******************************************************************************/
	public String getUpcomingEventsQuery()
	{
		return m_Bundle.getString("sql.events.upcoming");
	}
}
