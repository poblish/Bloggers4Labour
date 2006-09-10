/*
 * Installation.java
 *
 * Created on 19 February 2006, 13:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.bloggers4labour.activity.*;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.feed.*;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.jmx.Management;
import org.bloggers4labour.mail.DigestSender;
import org.bloggers4labour.sql.*;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class Installation implements InstallationIF
{
	private String			m_Name;
	private String			m_BundleName;
	private String			m_LogPrefix;
	private DataSource		m_DataSource;

	private HeadlinesMgr		m_HeadlinesMgr;
	private FeedList		m_FeedList;
	private CategoriesTable		m_Categories;
	private Management		m_Management;
	private Poller			m_Poller;
	private DigestSender		m_DigestSender;
	private IndexMgr		m_IndexMgr;

	private ActivityTable		m_ActivityTable = null; // new ActivityTable();
	private LastPostTable		m_LastPostTable;		// (AGR) 9 Sep 2006

	private static Logger		s_Install_Logger = Logger.getLogger("Main");

	/*******************************************************************************
		(AGR) 19 Feb 2006
	*******************************************************************************/
	public Installation( String inName, String inBundleName, DataSource inDataSource, String inStatsBeanName) //, HeadlinesMgr inHeadsMgr)
	{
		m_Name = inName;
		m_BundleName = inBundleName;
		m_LogPrefix = "[" + m_Name + "] ";

		m_DataSource = inDataSource;

		m_Management = new Management( this, inStatsBeanName);

		m_FeedList = new FeedList(this);
//		m_HeadlinesMgr = new HeadlinesMgr(this);		// needs a FeedList...
//		m_HeadlinesMgr = inHeadsMgr;				// (AGR) 21 March 2006. needs a FeedList...

		m_LastPostTable = new LastPostTable( m_FeedList, m_Name.equals("b4l"));		// (AGR) 9 Sep 2006

		// must call: complete() at some point!
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setHeadlinesMgr( HeadlinesMgr inHeadsMgr)
	{
		m_HeadlinesMgr = inHeadsMgr;			// (AGR) 21 March 2006. needs a FeedList...
	}

	/*******************************************************************************
	*******************************************************************************/
	public void complete()
	{
		m_FeedList.connect(true);
		m_FeedList.addObserver( new FeedDoneHandler() );

		m_IndexMgr = new IndexMgr(this);

		m_Categories = new CategoriesTable(this);
		m_Poller = new Poller(this);
		m_DigestSender = new DigestSender(this);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getName()
	{
		return m_Name;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getLogPrefix()
	{
		return m_LogPrefix;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ResourceBundle getBundle()
	{
		return getBundle( Locale.UK );
	}

	/*******************************************************************************
	*******************************************************************************/
	public ResourceBundle getBundle( Locale inLocale)
	{
//		try
		{
			return ResourceBundle.getBundle( "org/bloggers4labour/" + m_BundleName, inLocale);
		}
/*		catch (java.util.MissingResourceException e)
		{
			return ResourceBundle.getBundle( "org/bloggers4labour/" + InstallationManager.DEFAULT_INSTALL, inLocale);
		}
*/	}

	/*******************************************************************************
	*******************************************************************************/
	public void restart()
	{
		m_FeedList.reconnect();
		m_Categories.reconnect();
		m_Poller.startPolling();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void stop()
	{
		m_Categories.clearTable();
		m_FeedList.disconnect();
		m_Poller.cancelPolling();
		m_HeadlinesMgr.shutdown();

		m_DigestSender.cancelTimer();
	}

	/*******************************************************************************
	*******************************************************************************/
	public DataSource getDataSource()
	{
		return m_DataSource;
	}

	/*******************************************************************************
	*******************************************************************************/
	public HeadlinesMgr getHeadlinesMgr()
	{
		return m_HeadlinesMgr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public IndexMgr getIndexMgr()
	{
		return m_IndexMgr;
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedList getFeedList()
	{
		return m_FeedList;
	}

	/*******************************************************************************
	*******************************************************************************/
	public CategoriesTable getCategories()
	{
		return m_Categories;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Poller getPoller()
	{
		return m_Poller;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Management getManagement()
	{
		return m_Management;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ActivityTable getActivityTable()
	{
		return m_ActivityTable;
	}

	/*******************************************************************************
		(AGR) 9 Sep 2006
	*******************************************************************************/
	public LastPostTable getLastPostDateTable()
	{
		return m_LastPostTable;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return "[Installation: \"" + m_Name + "\"]";
	}

	/*******************************************************************************
	*******************************************************************************/
	private class FeedDoneHandler implements Observer
	{
		/*******************************************************************************
		*******************************************************************************/
		public void update( Observable inFeedList, Object inUnusedArgs)
		{
			if ( m_ActivityTable != null)
			{
				m_ActivityTable.complete();

				// s_Install_Logger.info( getLogPrefix() + "Activity table: " + m_ActivityTable);
			}

			if ( m_LastPostTable != null)
			{
				m_LastPostTable.complete();

				s_Install_Logger.info( getLogPrefix() + "Last Posts table: " + m_LastPostTable);
			}
		}
	}
}
