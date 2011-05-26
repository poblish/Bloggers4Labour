/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.polling.api.PollerFeedApproverIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author aregan
 */
public interface PollerIF
{
	void setInstallation( final InstallationIF inInstallation);

	boolean registerChannelWithInforma( final ChannelIF inChannel);
	void unregisterChannelWithInforma( final ChannelIF inChannel);

	boolean registerChannel( SiteIF inSite, ChannelIF inChannel, long inCurrentTimeMSecs);
	boolean registerCommentsChannel( SiteIF inSite, ChannelIF inChannel, long inCurrentTimeMSecs);
	boolean unregisterChannel( ChannelIF inChannel);

	void startPolling();
	void cancelPolling();

	PollerFeedApproverIF getFeedApprover();
}