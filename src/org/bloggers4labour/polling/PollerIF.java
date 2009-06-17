/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import org.bloggers4labour.Installation;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author aregan
 */
public interface PollerIF
{
	public void setInstallation( final Installation inInstallation);

	public boolean registerChannelWithInforma( final ChannelIF inChannel);
	public void unregisterChannelWithInforma( final ChannelIF inChannel);

	public boolean registerChannel( SiteIF inSite, ChannelIF inChannel, long inCurrentTimeMSecs);
	public boolean registerCommentsChannel( SiteIF inSite, ChannelIF inChannel, long inCurrentTimeMSecs);
	public boolean unregisterChannel( ChannelIF inChannel);

	public void startPolling();
	public void cancelPolling();
}