/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.auth;

import java.net.Authenticator;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.auth.twitter.B4LTwitterFeedAuthentication;

/**
 *
 * @author andrewregan
 */
public class AuthenticationManager
{
	private static Logger		s_Logger = Logger.getLogger( AuthenticationManager.class );

	/*******************************************************************************
	*******************************************************************************/
	private AuthenticationManager()
	{
		Authenticator	theDefaultAuthenticator = new B4LTwitterFeedAuthentication().getAuthenticator();

		s_Logger.info("Using HTTP Feed Authenticator: " + theDefaultAuthenticator);

		Authenticator.setDefault(theDefaultAuthenticator);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static AuthenticationManager getInstance()
	{
		return LazyHolder.s_Inst;
	}

	/*******************************************************************************
		(AGR) 5 June 2005. See:
		    <http://www-106.ibm.com/developerworks/java/library/j-jtp03304/>
	*******************************************************************************/
	private static class LazyHolder
	{
		private static AuthenticationManager	s_Inst = new AuthenticationManager();
	}

}
