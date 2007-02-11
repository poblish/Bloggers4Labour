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
public class QueryBuilder
{
	private static ResourceBundle	s_Bundle;
	private static String		s_AdjustedNameQuery;

	/*******************************************************************************
	*******************************************************************************/
	static
	{
		s_Bundle = ResourceBundle.getBundle("org/bloggers4labour/Main");
		s_AdjustedNameQuery = s_Bundle.getString("sql.adjusted.name.query");
	}

	/*******************************************************************************
		(AGR) 12 July 2005
	*******************************************************************************/
	public static String getDigestEmailQuery( long inHrs, long inMins)
	{
		Formatter	theFormatter = new Formatter();

		theFormatter.format( s_Bundle.getString("sql.digest.email.query"), inHrs, inMins);

		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getAllBlogFeeds()
	{
		return s_Bundle.getString("sql.all.blog.feeds");
	}

	/*******************************************************************************
	*******************************************************************************/
	private static String getBlogsByCategory( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, s_Bundle.getString("sql.category_blogs.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByCategoryAscending()
	{
		return getBlogsByCategory(" ORDER BY cat.ordering," + s_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByCategoryDescending()
	{
		return getBlogsByCategory(" ORDER BY cat.ordering," + s_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsListing( String inOrderStr)
	{
		return getBlogsByCategory(inOrderStr);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByLocation( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, s_Bundle.getString("sql.location_blogs.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByLocationAscending()
	{
		return getBlogsByLocation(" ORDER BY loc.ordering," + s_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByLocationDescending()
	{
		return getBlogsByLocation(" ORDER BY loc.ordering," + s_AdjustedNameQuery + " DESC");
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByStatus( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, s_Bundle.getString("sql.status_blogs.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByStatusAscending()
	{
		return getBlogsByStatus(" ORDER BY crs.ordering," + s_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getBlogsByStatusDescending()
	{
		return getBlogsByStatus(" ORDER BY crs.ordering," + s_AdjustedNameQuery + " DESC");
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getSimpleBlogsListing( String inOrderStr)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( Locale.UK, s_Bundle.getString("sql.simple_blogs.query"), s_Bundle.getString("sql.name_initial.query"), inOrderStr);
		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getSimpleBlogsListingAscending()
	{
		return getSimpleBlogsListing(" ORDER BY " + s_AdjustedNameQuery);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getSimpleBlogsListingDescending()
	{
		return getSimpleBlogsListing(" ORDER BY " + s_AdjustedNameQuery + " DESC");
	}

	/*******************************************************************************
		(AGR) 22 May 2005
	*******************************************************************************/
	public static String getBlogsTotalQuery()
	{
		return s_Bundle.getString("sql.total.blogs");
	}

	/*******************************************************************************
		(AGR) 27 May 2006
	*******************************************************************************/
	public static String getUnapprovedBlogsQuery()
	{
		return s_Bundle.getString("sql.unapproved.query");
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public static String getRecommendationCountsQueryString( String inURLsList)
	{
		Formatter	theFormatter = new Formatter();
		theFormatter.format( s_Bundle.getString("sql.recommendationcounts.query"), inURLsList);
		return theFormatter.toString();
	}

	/*******************************************************************************
		(AGR) 16 January 2007
	*******************************************************************************/
	public static String getUpcomingEventsQuery()
	{
		return s_Bundle.getString("sql.events.upcoming");
	}
}
