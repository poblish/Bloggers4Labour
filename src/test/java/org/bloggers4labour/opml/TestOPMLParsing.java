/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.opml;

import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.InstallationManager;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.opml.input.OPMLMonitoringTask;
import org.junit.Test;

/**
 *
 * @author andrewregan
 */
public class TestOPMLParsing
{
	private static Logger	s_Logger = Logger.getLogger( TestOPMLParsing.class );

	/*******************************************************************************
	*******************************************************************************/
	@Test public void testRead()
	{
		Configuration.getInstance().setDirectoryIfNotSet("/Users/andrewregan/www/htdocs/bloggers4labour/conf/");

		InstallationIF		theInstall = InstallationManager.getDefaultInstallation();

		new OPMLMonitoringTask( theInstall, 0L, 0L).run();
	}
}
