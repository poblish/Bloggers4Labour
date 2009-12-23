/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check.util;

import org.bloggers4labour.feed.check.consume.FeedCheckerListenerIF;

/**
 *
 * @author andrewregan
 */
public interface FeedHistoryIF extends FeedCheckerListenerIF
{
	FeedHistoryEntryIF getForURL( final String inURL);

	int size();
}