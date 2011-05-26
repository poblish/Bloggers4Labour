/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.site;

import java.io.Serializable;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewregan
 */
public interface SiteIF extends Serializable
{
	public String getName();
	public long getRecno();
	public ChannelIF getChannel();
	public ChannelIF getCommentsChannel();
	public String getSiteURL();
	public String getFeedURL();
	public String getFaviconLocation( final InstallationIF inInstall);
	public int getCreatorStatusRecno();

	public String getDescriptionStyle( final ItemIF inItem);
	public String getDescriptionStyle( final ItemIF inItem, final int inNumRecommendations);

	public void addCreator( String inType);
	public Iterable<String> getCreators();
	public String getCreatorsString( String inBaseURL);
	public String getReducedCreatorsString( String inBaseURL);

	public String getCategoriesString( final ItemIF inItem);

	public int getMaximumPostsToAggregate();

	String getDatabaseFeedURL();
}