/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import java.util.List;
import java.util.Observer;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.feed.api.FeedChannelsIF;
import org.bloggers4labour.opml.OPMLHandlerIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public interface FeedListIF 
{
	public void addObserver( Observer inObs);

	void setOPMLHandler( final OPMLHandlerIF inHandler);
	public void generateOPML();

	public int countReferencedItems();
	public int countURLs();
	public List<SiteIF> getSites();
	public String getOPMLOutputStr();

	public SiteIF lookup( long inRecno);
	public SiteIF lookupChannel( ChannelIF inChannel);
	public SiteIF lookupSiteURL( String inChannelSiteURL);
	public SiteIF lookupPostsChannel( ChannelIF inChannel);

	FeedChannelsIF getFeedChannels();
}