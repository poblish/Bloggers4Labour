/*
 * Launcher.java
 *
 * Created on May 17, 2005, 8:14 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

import com.hiatus.htl.HTL;
import com.hiatus.htl.HTLCache;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.gui.MaintenanceForm;

/**
 *
 * @author andrewre
 */
public class Launcher
{
	private Logger		m_Logger;
	private Properties	m_Props = new Properties();

	private static String	s_ConfigDirectory;
	private static String	s_LoggingConfigFile;

	/********************************************************************
	********************************************************************/
	public static void main( final String[] inArgs)
	{
		for (int i = 0; i < inArgs.length; i++)
		{
			if (inArgs[i].equalsIgnoreCase("-configDir"))
			{
				s_ConfigDirectory = inArgs[++i];
			}
			else if (inArgs[i].equalsIgnoreCase("-logConfigFile"))
			{
				s_LoggingConfigFile = inArgs[++i];
			}
		}

		if ( s_LoggingConfigFile == null)
		{
			s_LoggingConfigFile = "/Users/andrewregan/www/htdocs/WEB-INF/bio-LOCAL.properties";
		}

		if ( s_ConfigDirectory == null)
		{
			s_ConfigDirectory = "/Users/andrewregan/www/htdocs/bloggers4labour/conf/";
		}

		System.out.println("Loading Logging Config: " + s_LoggingConfigFile);
		PropertyConfigurator.configure(s_LoggingConfigFile);

		System.out.println("Configuration dir: " + s_ConfigDirectory);

		////////////////////////////////////////////////////////////////

		Launcher		l = new Launcher( Logger.getLogger("Main") );
		l.start();

		MaintenanceForm		theForm = new MaintenanceForm();
		theForm.setLauncher(l);
		theForm.setVisible(true);
	}

	/*******************************************************************************
	*******************************************************************************/
	public Launcher( Logger inLogger)
	{
		m_Logger = inLogger;
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public void start()
	{
		Configuration.getInstance().setDirectoryIfNotSet(s_ConfigDirectory);

		HTL.initHTL(m_Props);
		HTLCache.init();

		_start();
	}

	/*******************************************************************************
		(AGR) 5 June 2005
	*******************************************************************************/
	public void restart()
	{
		m_Logger.info("================== Restarting ==================");

		////////////////////////////////////////////////////////////////

		InstallationManager.getInstance().restart();

		_start();
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _start()
	{
		m_Logger.info("Props: " + m_Props);

		////////////////////////////////////////

		InstallationManager.getInstance().startIfStopped();	// (AGR) 19 Feb 2006, 10 Aug 2008
	}

	/*******************************************************************************
	*******************************************************************************/
	public void stop()
	{
		try
		{
			m_Logger.info("=================== Stopping ===================");

//			CategoriesTable.clear();	// (AGR) 19 May 2005

			////////////////////////////////////////////////////////////////

			InstallationManager.getInstance().stop();

			////////////////////////////////////////////////////////////////

//			DigestSender.cancelTimer();
//			Poller.getInstance().cancelPolling();

			/////////////////////////////////  (AGR) 21 May 2005

//			HeadlinesMgr.shutdown();

			m_Logger.info("===================== DONE =====================");
		}
		catch (Exception e)
		{
			m_Logger.error("Error during application stop", e);
		}
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public void reboot()
	{
		stop();
		restart();
	}
}
