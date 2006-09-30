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

import org.bloggers4labour.feed.FeedList;
import com.hiatus.WebApp;
import com.hiatus.htl.*;
import java.util.Properties;
import org.apache.log4j.*;
// import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.gui.MaintenanceForm;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.jmx.Management;
// import org.bloggers4labour.mail.DigestSender;

/**
 *
 * @author andrewre
 */
public class Launcher
{
	private Logger		m_Logger;
	private Properties	m_Props = new Properties();

	/********************************************************************
	********************************************************************/
	public static void main(String[] a)
	{
/*		Properties	theProps = new Properties();
		theProps.setProperty("log4j.rootLogger","DEBUG");
		theProps.setProperty("log4j.logger.Main","DEBUG, FOO");
		theProps.setProperty("log4j.appender.FOO","org.apache.log4j.ConsoleAppender");
		theProps.setProperty("log4j.appender.FOO.layout","org.apache.log4j.PatternLayout");
		PropertyConfigurator.configure(theProps);
		Logger.getLogger("Main").info("hello");
*/
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
		Configuration.getInstance().setDirectoryIfNotSet("/Users/andrewre/www/htdocs/bloggers4labour/conf/");

		m_Props.setProperty( "bm.docs_directory_path", "/home/htdocs/bloggers4labour/htl/");
		m_Props.setProperty( "bm.locales_dir_name", "locales/");
		m_Props.setProperty( "bm.default_dir_name", "default");

		WebApp.setProperties(m_Props);

		HTL.initHTL(m_Props);
		HTLCache.init();

		InstallationManager.getInstance();

//		HeadlinesMgr.getInstance();	// (AGR) 19 Feb 2006, 22 May 2005
//		IndexMgr.getInstance();
//		Poller.getInstance();		// start polling straightaway

		////////////////////////////////////////

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

		////////////////////////////////////////////////////////////////

//		CategoriesTable.getInstance().reconnect();
//		Poller.getInstance().startPolling();

		_start();
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _start()
	{
		m_Logger.info("Props: " + m_Props);

		////////////////////////////////////////

		InstallationManager.getInstance();	// (AGR) 19 Feb 2006

//		DigestSender.getInstance();
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
