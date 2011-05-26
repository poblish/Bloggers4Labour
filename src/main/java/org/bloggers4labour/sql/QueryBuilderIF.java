/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

/**
 *
 * @author andrewregan
 */
public interface QueryBuilderIF 
{
	String getRecommendationCountsQueryString( String inURLsList);
	String getAllBlogFeeds();
	String getBlogsTotalQuery();
	String getUnapprovedBlogsQuery();
	String getUpcomingEventsQuery();
	String getDigestEmailQuery( long inHrs, long inMins);
	String getBlogsByStatusAscending();
	String getSimpleBlogsListingAscending();
	String getBlogsByCategoryAscending();
	String getBlogsByCategoryDescending();
	String getBlogsByLocationAscending();
}
