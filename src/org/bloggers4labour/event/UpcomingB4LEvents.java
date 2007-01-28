/*
 * UpcomingB4LEvents.java
 *
 * Created on 27 January 2007, 23:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.event;

import org.bloggers4labour.InstallationManager;

/**
 *
 * @author andrewre
 */
public class UpcomingB4LEvents extends UpcomingEvents
{
	/*******************************************************************************
	*******************************************************************************/
	public UpcomingB4LEvents()
	{
		super( InstallationManager.getDefaultInstallation(), 1);
	}
}