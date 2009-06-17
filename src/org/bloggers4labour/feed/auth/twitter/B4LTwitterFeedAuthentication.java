/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.auth.twitter;

import java.net.PasswordAuthentication;

/**
 *
 * @author andrewregan
 */
public class B4LTwitterFeedAuthentication extends TwitterFeedAuthentication
{
	/*******************************************************************************
	*******************************************************************************/
	public PasswordAuthentication getPassword()
	{
		return new PasswordAuthentication( "us@bloggers4labour.org", "Militant".toCharArray());
	}
}