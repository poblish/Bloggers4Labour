/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.auth;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 *
 * @author andrewregan
 */
public interface AuthenticationFlavourIF 
{
	public Authenticator getAuthenticator();
	public PasswordAuthentication getPassword();
}