/*
 * Installation.java
 *
 * Created on 19 February 2006, 13:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

// import com.facebook.api.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.activity.LastPostTable;
import org.bloggers4labour.activity.LastPostTableIF;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.cats.CategoriesTableIF;
import org.bloggers4labour.feed.*;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.installation.tasks.InstallationTaskIF;
import org.bloggers4labour.jmx.Management;
import org.bloggers4labour.mail.DigestSender;
import org.bloggers4labour.mail.DigestSenderIF;
import org.bloggers4labour.polling.PollerIF;

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

	private HeadlinesMgr		m_HeadlinesMgr = null;
	private FeedList		m_FeedList;
	private CategoriesTable		m_Categories = null;
	private Management		m_Management;
	private Collection<PollerIF>	m_Pollers = new ArrayList<PollerIF>();
	private DigestSenderIF		m_DigestSender;
	private IndexMgr		m_IndexMgr;

	private LastPostTableIF		m_LastPostTable;			// (AGR) 9 Sep 2006

	private InstallationStatus	m_Status = InstallationStatus.UNCONFIGURED;

	private Collection<InstallationTaskIF>		m_Tasks;		// (AGR) 27 October 2008
	private ScheduledExecutorService		m_TasksExecutor;	// (AGR) 27 October 2008

	private static Logger		s_Install_Logger = Logger.getLogger( Installation.class );

	/*******************************************************************************
		(AGR) 19 Feb 2006
	*******************************************************************************/
	public Installation( String inName, String inBundleName, DataSource inDataSource, String inStatsBeanName,
				Collection<PollerIF> inPollers)
	{
		m_Name = inName;
		m_BundleName = inBundleName;
		m_LogPrefix = "[" + m_Name + "] ";

		m_DataSource = inDataSource;

		m_Management = new Management( this, inStatsBeanName);

		m_FeedList = new FeedList(this);

		m_LastPostTable = new LastPostTable( m_FeedList, m_Name.equals("b4l"));		// (AGR) 9 Sep 2006

		if (!inPollers.isEmpty())	// (AGR) 29 May 2009
		{
			m_Pollers.addAll(inPollers);

			for ( PollerIF eachPoller : inPollers)
			{
				eachPoller.setInstallation(this);
			}
		}

		// must call: complete() at some point!
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setTasks( Collection<InstallationTaskIF> inTasks)
	{
		if ( inTasks != null && !inTasks.isEmpty())
		{
			m_TasksExecutor = Executors.newScheduledThreadPool(1);

			s_Install_Logger.info( getLogPrefix() + "TasksExecutor: " + m_TasksExecutor);

			for ( InstallationTaskIF eachTask : inTasks)
			{
				s_Install_Logger.info( getLogPrefix() + "Registering Task: " + eachTask);

				m_TasksExecutor.scheduleAtFixedRate( eachTask, eachTask.getDelayMS(), eachTask.getFrequencyMS(), TimeUnit.MILLISECONDS);
			}
	}

		m_Tasks = inTasks;
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

		for ( PollerIF eachPoller : m_Pollers)
		{
			eachPoller.startPolling();
		}

		m_DigestSender = new DigestSender(this);

		///////////////////////////////////////  (AGR) 13 March 2007. Turns out this is impossible with FB API. Commenting-out

/*		if ( m_FacebookGroupID != null && m_FacebookTimer == null)
		{
			m_FacebookTimer = new Timer();
			m_FacebookTimer.scheduleAtFixedRate( new FacebookGroupCheckTask(), 50, Constants.ONE_HOUR_SECS);
		}
*/
		m_Status = InstallationStatus.STARTED;
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

		for ( PollerIF eachPoller : m_Pollers)
		{
			eachPoller.startPolling();
		}

		m_Status = InstallationStatus.STARTED;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void stop()
	{
		m_Categories.clearTable();
		m_FeedList.disconnect();

		for ( PollerIF eachPoller : m_Pollers)
		{
			eachPoller.cancelPolling();
		}

		m_HeadlinesMgr.shutdown();

		////////////////////////////////////////////////////////////////  (AGR) 27 October 2008

		if ( m_TasksExecutor != null)
		{
			m_TasksExecutor.shutdownNow();
		}

		////////////////////////////////////////////////////////////////

		m_Status = InstallationStatus.STOPPED;
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
	public FeedListIF getFeedList()
	{
		return m_FeedList;
	}

	/*******************************************************************************
	*******************************************************************************/
	public CategoriesTableIF getCategories()
	{
		return m_Categories;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean hasPollers()
	{
		return !m_Pollers.isEmpty();
	}

	/*******************************************************************************
	*******************************************************************************/
	public Iterable<PollerIF> getPollers()
	{
		return new Iterable<PollerIF>() {

			public Iterator<PollerIF> iterator()
			{
				return m_Pollers.iterator();
			}
		};
	}

	/*******************************************************************************
	*******************************************************************************/
	public Management getManagement()
	{
		return m_Management;
	}

	/*******************************************************************************
		(AGR) 9 Sep 2006
	*******************************************************************************/
	public LastPostTableIF getLastPostDateTable()
	{
		return m_LastPostTable;
	}

	/*******************************************************************************
	*******************************************************************************/
	public DigestSenderIF getDigestSender()
	{
		return m_DigestSender;
	}

	/*******************************************************************************
		(AGR) 13 March 2007
	*******************************************************************************
	public void setFacebookGroupID( final Number inNum)
	{
		m_FacebookGroupID = inNum;
	} /

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
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
/*			if ( m_ActivityTable != null)
			{
				m_ActivityTable.complete();

				// s_Install_Logger.info( getLogPrefix() + "Activity table: " + m_ActivityTable);
			}
*/
			if ( m_LastPostTable != null)
			{
				m_LastPostTable.complete();

				// s_Install_Logger.info( getLogPrefix() + "Last Posts table: " + m_LastPostTable);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public InstallationStatus getStatus()
	{
		return m_Status;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Iterable<? extends InstallationTaskIF> getTasks()
	{
		return new Iterable<InstallationTaskIF>() {

			public Iterator<InstallationTaskIF> iterator()
			{
				return m_Tasks.iterator();
			}
		};
	}

	/*******************************************************************************
		(AGR) 13 March 2007
	*******************************************************************************
	private static class FacebookGroupCheckTask extends TimerTask
	{
		public void run()
		{
			try
			{
				FacebookRestClient	theFRC = new FacebookRestClient( "0340236d55995cbcaebd9648bb3be220", "ef6d7b90060faebbf0229e5023501fd1");
				theFRC.setDebug(true);

				String			theToken = theFRC.auth_createToken();
				String			theSessionID = theFRC.auth_getSession(theToken);

				Logger.getLogger("Main").info("Facebook: Token: " + theToken + ", session: " + theSessionID);

				Document		theResult = theFRC.groups_getMembers(m_FacebookGroupID);

				FacebookRestClient.printDom( theResult, "  ");
			}
			catch (Exception e)
			{
				Logger.getLogger("Main").error( getLogPrefix() + "FacebookGroupCheckTask", e);
			}
		}
	}
	*/
}
