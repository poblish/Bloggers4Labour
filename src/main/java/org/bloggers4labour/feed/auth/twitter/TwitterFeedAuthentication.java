/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.auth.twitter;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.bloggers4labour.feed.auth.AuthenticationFlavourIF;

/**
 *
 * @author andrewregan
 */
public abstract class TwitterFeedAuthentication implements AuthenticationFlavourIF
{
	/*******************************************************************************
	*******************************************************************************/
	public Authenticator getAuthenticator()
	{
		return new Authenticator() {
			/*******************************************************************************
			*******************************************************************************/
			@Override protected PasswordAuthentication getPasswordAuthentication()
			{
				if ( getRequestingScheme().equals("basic") &&
					getRequestingProtocol().equals("http") &&
					getRequestingURL().getHost().equals("twitter.com") &&
					getRequestingURL().getPath().equals("/statuses/friends_timeline.rss"))
				{
					return getPassword();
				}

				return null;
			}
		};
	}
}