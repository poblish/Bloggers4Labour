/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.installation;

/**
 *
 * @author andrewregan
 */
public class InstallationNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public InstallationNotFoundException( final String inName)
	{
		super("Installation '" + inName + "' not found.");
	}
}