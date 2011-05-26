/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.mail;

import java.util.Observer;
import org.bloggers4labour.feed.FeedListIF;

/**
 *
 * @author andrewregan
 */
public interface DigestSenderIF 
{
	public void test( FeedListIF ioFL);
	public void test( FeedListIF ioFL, Observer inTestObserver);
}