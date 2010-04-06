/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

/**
 *
 * @author andrewregan
 */
public interface MutableFeedListIF extends FeedListIF
{
	void disconnect();
	void reconnect();
}