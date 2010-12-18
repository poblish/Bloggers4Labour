/*
 * FeedList.java
 *
 * Created on 12 March 2005, 00:01
 */

package org.bloggers4labour.feed;

import com.hiatus.sql.ResultSetList;
import com.hiatus.sql.USQL_Utils;
import com.hiatus.text.UText;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.net.URL;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.Site;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.favicon.FaviconManagerIF;
import org.bloggers4labour.feed.api.FeedChannelsIF;
import org.bloggers4labour.jmx.Stats;
import org.bloggers4labour.opml.OPMLHandlerIF;
import org.bloggers4labour.options.Options;
import org.bloggers4labour.options.TaskOptionsBeanIF;
import org.bloggers4labour.polling.DefaultPollerAllocator;
import org.bloggers4labour.polling.PollerAllocatorIF;
import org.bloggers4labour.polling.PollerIF;
import org.bloggers4labour.site.SiteIF;
import org.bloggers4labour.sql.DataSourceConnection;

/**
 *
 * @author andrewre
 */
public class FeedList implements MutableFeedListIF
{
	private ArrayList<SiteIF>		m_PostFeedSitesList = new ArrayList<SiteIF>(150);
	private ArrayList<SiteIF>		m_CommentsFeedSitesList = new ArrayList<SiteIF>(30);	// (AGR) 30 Nov 2005

	private ArrayList<SiteIF>		m_LastFeedURLsList;

	private final MutableFeedChannelsIF	m_FeedChannels;
	private ScheduledExecutorService	m_USTPE = null;
	private ScheduledExecutorService	m_STPE = null;
	private UpdaterTask			m_UpdateTask;

	private OPMLHandlerIF			m_OPMLHandler;					// (AGR) 11 January 2010
	private int				m_PostFeedsCount;				// (AGR) 31 March 2005
	private Stats				m_Stats;					// (AGR) 1 June 2005. 3 Feb 2007: removed transient

	private InstallationIF			m_Install;

	private ProcessingObservable		m_DoneObservable = new ProcessingObservable();  // (AGR) 21 June 2005. 3 Feb 2007: removed transient

	private final static byte[]		_m_PostFeedSitesMutatorLocker = new byte[0];	// (AGR) 16 September 2010

	private static Logger			s_FL_Logger = Logger.getLogger( FeedList.class );

	private final static long		UPDATER_TIMEOUT_PERIOD = 12;
	private final static TimeUnit		UPDATER_TIMEOUT_UNITS = TimeUnit.MINUTES;

	/*******************************************************************************
	*******************************************************************************/
	public FeedList( final InstallationIF inInstall)
	{
		m_Install = inInstall;

		m_FeedChannels = new FeedChannels(inInstall);

		m_Stats = m_Install.getManagement().getStats();    // (AGR) 1 June 2005. Store a reference to this
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedChannelsIF getFeedChannels()
	{
		return m_FeedChannels;
	}

	/*******************************************************************************
		(AGR) 5 June 2005
	*******************************************************************************/
	public synchronized void reconnect()
	{
		connect(true);
	}

	/*******************************************************************************
		(AGR) 5 June 2005
	*******************************************************************************/
	public synchronized void disconnect()
	{
		_disconnect();
	}

	/*******************************************************************************
		(AGR) 5 June 2005
	*******************************************************************************/
	public void connect( boolean inUseUpdater)
	{
		m_USTPE = Executors.newScheduledThreadPool(1);
		s_FL_Logger.info( m_Install.getLogPrefix() + "m_USTPE " + m_USTPE);

		m_STPE = Executors.newScheduledThreadPool( Options.getOptions().getNumSiteHandlerThreads() );
		s_FL_Logger.info( m_Install.getLogPrefix() + "m_STPE " + m_STPE);

		////////////////////////////////////////////////////////////////

		if (inUseUpdater)
		{
			TaskOptionsBeanIF	theOptionsBean = Options.getOptions().getFeedUpdaterTaskOptions();

			m_UpdateTask = new UpdaterTask();

			m_USTPE.scheduleAtFixedRate( m_UpdateTask,
							theOptionsBean.getDelayMsecs(),
							theOptionsBean.getPeriodMsecs(),
							TimeUnit.MILLISECONDS);

			////////////////////////////////////////////////////////////////

			s_FL_Logger.info( m_Install.getLogPrefix() + "created " + this + " " + theOptionsBean);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _disconnect()
	{
		String	prefix = m_Install.getLogPrefix();

		s_FL_Logger.info( prefix + "FeedList: shutting down ScheduledThreadPoolExecutors...");

		List<Runnable>	l1 = m_USTPE.shutdownNow();
		if ( l1 != null && l1.size() > 0)
		{
			s_FL_Logger.info( prefix + "FeedList: left in m_USTPE: " + l1);
		}

		List<Runnable>	l2 = m_STPE.shutdownNow();
		if ( l2 != null && l2.size() > 0)
		{
			s_FL_Logger.info( prefix + "FeedList: left in m_STPE: " + l2);
		}

		if ( m_OPMLHandler != null)
		{
			m_OPMLHandler.disconnect();
		}

		/////////////////////////////////  (AGR) 5 June 2005

		m_FeedChannels.clear();		// ensure we go and get a new snapshot next time

		synchronized (_m_PostFeedSitesMutatorLocker)	// (AGR) 20 September 2010
		{
			m_PostFeedSitesList.clear();
		}

		m_CommentsFeedSitesList.clear();

		m_LastFeedURLsList = null;
	}

	/*******************************************************************************
	 * (AGR) 16 March 2005
	 * 
	 * (AGR) 31 March 2005. Basically if the channel registering takes a long time, it could take a while for the list to be built up, and this can cause the count we display to the user to be wrong (and change!) so we only update this value once the list is completely rebuilt. So once the list has been built once, it should drift up or down, not be reset to zero.
	 * @return the number of URLs
	*******************************************************************************/
	public int countURLs()
	{
		return m_PostFeedsCount;
	}

	/*******************************************************************************
		(AGR) 30 July 2005
		Don't *think* I need to sync here...
		Second thoughts: yes we do. Different thread to the list-building one
		    so we need to ensure we don't get a stale version of the list.
		Third thoughts: yes, but we'd be adding a dependency between a 'back-end'
		    process (building the list) and a 'front-end' process (getting a
		    list for building the DisplayItem objects) if we used a shared
		    monitor, as well as slowing down the list-building, with extra
		    sync-ing. So, for now, let's leave it un-synced.
	*******************************************************************************/
	public SiteIF[] getArrayToTraverse()
	{
//		System.out.println( "--> " + Thread.currentThread() );

		synchronized (_m_PostFeedSitesMutatorLocker)	// (AGR) 16-20 September 2010. Prevent ArrayIndexOutOfBoundsException occuring in toArray() code under heavy concurrency
		{
			return m_PostFeedSitesList.toArray( new Site[ m_PostFeedSitesList.size() ] );
		}
	}

	/*******************************************************************************
		(AGR) 16 March 2005
	*******************************************************************************/
	public List<SiteIF> getSites()
	{
		return Collections.unmodifiableList( m_PostFeedSitesList );
	}

	/*******************************************************************************
		(AGR) 22 March 2005
	*******************************************************************************/
	public SiteIF lookup( long inRecno)
	{
		if ( inRecno < 1)
		{
			return null;
		}

		////////////////////////////////////////////////////////////////

		SiteIF[]	sitesArray = getArrayToTraverse();

		for ( int i = 0; i < sitesArray.length; i++)
		{
			if ( sitesArray[i].getRecno() == inRecno)
			{
				return sitesArray[i];
			}
		}

		return null;
	}

	/*******************************************************************************
		(AGR) 22 March 2005
	
		Only looks at the Site's Posts channel. The overwhelming proportion of
		total function time is - unfortunately - spent in URL.equals() within
		Channel.equals()
	*******************************************************************************/
	public SiteIF lookupPostsChannel( ChannelIF inChannel)
	{
		if ( inChannel == null)
		{
			return null;
		}

		////////////////////////////////////////////////////////////////

		SiteIF[]	sitesArray = getArrayToTraverse();

		for ( int i = 0; i < sitesArray.length; i++)
		{
			if (inChannel.equals( sitesArray[i].getChannel() ))
			{
				return sitesArray[i];
			}
		}

		return null;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	
		Only looks at the Site's Posts channel and its Comments one (if present)
	*******************************************************************************/
	public SiteIF lookupChannel( ChannelIF inChannel)
	{
		if ( inChannel == null)
		{
			return null;
		}

		////////////////////////////////////////////////////////////////

		SiteIF[]	sitesArray = getArrayToTraverse();

		for ( int i = 0; i < sitesArray.length; i++)
		{
			if ( sitesArray[i] == null)	// (AGR) 19 September 2010. Yuk! Have seen NPE here. Can't be sure if it was symptom of different problem, but safety first.
			{
				continue;
			}

			////////////////////////////////////////////////////////

			if (inChannel.equals( sitesArray[i].getChannel() ))
			{
				return sitesArray[i];
			}

			////////////////////////////////////////////////////////  (AGR) 30 Nov 2005

			ChannelIF	theCC = sitesArray[i].getCommentsChannel();

			if ( theCC != null && inChannel.equals(theCC))
			{
				return sitesArray[i];
			}

		}

		return null;
	}

	/*******************************************************************************
		(AGR) 23 June 2005
	*******************************************************************************/
	public SiteIF lookupSiteURL( String inChannelSiteURL)
	{
		if ( inChannelSiteURL == null)
		{
			return null;
		}

		////////////////////////////////////////////////////////////////

		SiteIF[]	sitesArray = getArrayToTraverse();

		for ( int i = 0; i < sitesArray.length; i++)
		{
			ChannelIF	c = sitesArray[i].getChannel();

			if ( c == null)
			{
				continue;	// good site, but no channel (site down?)
			}

			URL	theSiteURL = c.getSite();

			if ( theSiteURL == null)
			{
				continue;	// only get this for the dodgy Swift blog
			}

			if (inChannelSiteURL.equals( theSiteURL.toString() ))
			{
				// System.out.println(">>>> GOOD");
				return sitesArray[i];
			}
		}

		// System.out.println("NO GOOD");
		return null;
	}

	/*******************************************************************************
		(AGR) 9 Sep 2006. Based upon the above!
	*******************************************************************************/
	public SiteIF lookupFeedLocationURL( final String inChannelSiteURL)
	{
		if ( inChannelSiteURL == null)
		{
			return null;
		}

		////////////////////////////////////////////////////////////////

		SiteIF[]	sitesArray = getArrayToTraverse();

		for ( int i = 0; i < sitesArray.length; i++)
		{
			ChannelIF	c = sitesArray[i].getChannel();

			if ( c == null)
			{
				continue;	// good site, but no channel (site down?)
			}

			URL	theSiteURL = c.getLocation();
			if ( theSiteURL == null)
			{
				continue;	// only get this for the dodgy Swift blog
			}

			if (inChannelSiteURL.equals( theSiteURL.toString() ))
			{
				// System.out.println(">>>> GOOD");
				return sitesArray[i];
			}
		}

		// System.out.println("NO GOOD");
		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setOPMLHandler( final OPMLHandlerIF inHandler)
	{
		m_OPMLHandler = inHandler;
	}

	/*******************************************************************************
		(AGR) 9 June 2005 - confusion/race conditions could conspire so that
		we had a NULL generator when we fully expected to have a good one, e.g.
		when 'generateOPML()' was seen to get called before 'connect()'
		finished. Hopefully this 'lazy' instantiation will help.
	*******************************************************************************/
	public void generateOPML()
	{
		if ( m_OPMLHandler != null)
		{
			m_OPMLHandler.generate( getArrayToTraverse() );
		}
	}

	/*******************************************************************************
		(AGR) 15 April 2005
	*******************************************************************************/
	public String getOPMLOutputStr()
	{
		return ( m_OPMLHandler != null) ? m_OPMLHandler.getOPMLString() : "";
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized int countReferencedItems()
	{
		ChannelIF		c;
		Collection<ItemIF>	coll;
		int			theItemCount = 0;
		SiteIF[]		sitesArray = getArrayToTraverse();

		for ( int i = 0; i < sitesArray.length; i++)
		{
			c = sitesArray[i].getChannel();
			if ( c != null)
			{
				coll = c.getItems();
				if ( coll != null)
				{
					theItemCount += coll.size();
				}
			}
		}

		// System.out.println("... countStoredItems() total: " + theItemCount);

		return theItemCount;
	}

	/*******************************************************************************
	*******************************************************************************/
	class SiteHandlerTask implements Callable<Object>
	{
		private final UpdaterTask	m_Task;
		private final int		m_ID;
		private final List		m_RowsList;
		private final long		m_CurrTimeMSecs;
		private final FaviconManagerIF	m_FaviconManager;

		private Map			m_CurrentRow;

		/*******************************************************************************
		*******************************************************************************/
		public SiteHandlerTask( final UpdaterTask inTask, final InstallationIF inInstall, final int inID, final long inCurrTimeMSecs, final List inList)
		{
			m_Task = inTask;
			m_ID = inID;
			m_RowsList = inList;
			m_CurrTimeMSecs = inCurrTimeMSecs;
			m_FaviconManager = inInstall.getFaviconManager();
		}

		/*******************************************************************************
		*******************************************************************************/
		public Object call() throws Exception
		{
			try
			{
		/* 		synchronized (m_Task._m_SHTsLocker)
				{
					s_FL_Logger.info("SHT #" + m_ID + " STARTING. m_SHTs = " + m_Task.m_SHTs);
				} */

				////////////////////////////////////////////////////////

				do
				{
					if (!_run())
					{
						break;
					}
				}
				while ( m_CurrentRow != null);
			}
			catch (InterruptedException e)		// Unlikely, but just possible
			{
				s_FL_Logger.warn("SHT #" + m_ID + " TIMED-OUT!");
			}
			catch (Throwable e)
			{
				s_FL_Logger.error( m_Install.getLogPrefix() + "in SHT.run()", e);
			}
			finally
			{
				synchronized (m_Task._m_SHTsLocker)
				{
					m_Task.m_SHTs.remove(this);	// ensure this happens!

					if ( m_Stats != null)
					{
						m_Stats.setActiveSiteHandlerTasks( m_Task.m_SHTs.size() );
					}

					// s_FL_Logger.info("SHT #" + m_ID + " DONE. m_SHTs = " + m_Task.m_SHTs);
				}
			}

			return this;
		}

		/*******************************************************************************
		*******************************************************************************/
		private boolean _run() throws Exception
		{
			synchronized (m_RowsList)
			{
				if ( m_RowsList.size() < 1)
				{
					return false;
				}

				m_CurrentRow = (Map) m_RowsList.remove(0);	// take one off the top

				// s_FL_Logger.info("Remaining... " + m_RowsList);
			}

			////////////////////////////////////////////////////////

			// s_FL_Logger.info("SHT #" + m_ID + " GRABBED " + m_CurrentRow);

			if ( m_CurrentRow == null)
			{
				return true;	// Nothing to be done...
			}

			////////////////////////////////////////////////////////

			final Long	theSiteRecno = (Long) m_CurrentRow.get("site_recno");

			synchronized (m_Task._m_LastRecnoLocker)
			{
				if (m_Task.m_SiteRecnosUsed.contains(theSiteRecno))
				{
					return true;
				}
				else
				{
					m_Task.m_SiteRecnosUsed.add(theSiteRecno);
					// s_FL_Logger.info("adding recno: " + theSiteRecno);
				}
			}

			//////////////////////////////////////////////////////////////////////  Got a new siteRecno...
			//////////////////////////////////////////////////////////////////////

			String		thePostsFeedURL = (String) m_CurrentRow.get("feed_url");
			String		theFirstCreatorsType = (String) m_CurrentRow.get("creator_type");
			ChannelIF	thePostsFeedChannel = m_FeedChannels.findURL(thePostsFeedURL);
			ConnectResult	theResult;
			boolean		gotNewPostsFeedChannel = false;

			if ( thePostsFeedChannel == null)
			{
				theResult = m_FeedChannels.connectTo( m_Install, m_ID, thePostsFeedURL);
				if (theResult.succeded())
				{
					thePostsFeedChannel = theResult.getChannel();

					if (m_Install.hasPollers())	// (AGR) 29 May 2009. What on earth is this for???
					{
						// s_FL_Logger.info(">>> SHT #" + m_ID + " Trying to register: " + thePostsFeedURL);

						gotNewPostsFeedChannel = true;
					}

					m_Task.getPollerAllocator().success( thePostsFeedURL, thePostsFeedChannel);
				}
				else if (theResult.failed())	// (AGR) 7 October 2005. connection failed
				{
					synchronized (m_Task.m_FailedPostFeeds)
					{
						m_Task.m_FailedPostFeeds.add(thePostsFeedURL);
					}

					m_Task.getPollerAllocator().failed(thePostsFeedURL);
				}
				else	// (AGR) 30 Nov 2005. connection timed-out
				{
					synchronized (m_Task.m_TimedOutPostFeeds)
					{
						m_Task.m_TimedOutPostFeeds.add(thePostsFeedURL);
					}

					m_Task.getPollerAllocator().timedOut(thePostsFeedURL);
				}
			}

			// s_FL_Logger.info(">>> SHT #" + m_ID + " channel=" + thePostsFeedChannel);

			//////////////////////////////////////////////////////////////////////  (AGR) 29 Nov 2005

			String		theCommentsFeedURL = (String) m_CurrentRow.get("comments_feed_url");
			ChannelIF	theCommentsFeedChannel;
			boolean		specifiedACommentsFeed = UText.isValidString(theCommentsFeedURL);
			boolean		gotNewCommentsFeedChannel = false;

			if (specifiedACommentsFeed)
			{
				theCommentsFeedChannel = m_FeedChannels.findURL(theCommentsFeedURL);

				if ( theCommentsFeedChannel == null)
				{
					theResult = m_FeedChannels.connectTo( m_Install, m_ID, theCommentsFeedURL);
					if (theResult.succeded())
					{
						theCommentsFeedChannel = theResult.getChannel();

						if (m_Install.hasPollers())	// (AGR) 29 May 2009. What on earth is this for???
						{
							// s_FL_Logger.info(">>> SHT #" + m_ID + " Trying to register comments: " + theCommentsFeedURL);

//							m_Install.getPoller().registerCommentsChannel( theCommentsFeedChannel, m_CurrTimeMSecs);

							gotNewCommentsFeedChannel = true;
						}

						m_Task.getPollerAllocator().success( theCommentsFeedURL, theCommentsFeedChannel);
					}
					else if (theResult.failed())	// (AGR) 7 October 2005. connection failed
					{
						synchronized (m_Task.m_FailedCommentsFeeds)
						{
							m_Task.m_FailedCommentsFeeds.add(theCommentsFeedURL);	// (AGR) 1 June 2009. Was: 'thePostsFeedURL'
						}

						m_Task.getPollerAllocator().failed(theCommentsFeedURL);
					}
					else	// (AGR) 30 Nov 2005. connection timed-out
					{
						synchronized (m_Task.m_TimedOutCommentsFeeds)
						{
							m_Task.m_TimedOutCommentsFeeds.add(theCommentsFeedURL);
						}

						m_Task.getPollerAllocator().timedOut(theCommentsFeedURL);
					}
				}
			}
			else
			{
				theCommentsFeedChannel = null;
			}

			//////////////////////////////////////////////////////////////////////

			Site	theSiteObj = new Site( thePostsFeedChannel,
							theCommentsFeedChannel,
							theSiteRecno.longValue(),
							(String) m_CurrentRow.get("name"),	// (AGR) 20 April 2005
							(String) m_CurrentRow.get("url"),
							thePostsFeedURL,
							((Number) m_CurrentRow.get("creator_status_recno")).intValue(),
							(String) m_CurrentRow.get("cat"),
							(String) m_CurrentRow.get("favicon_url"));

			if ( m_FaviconManager != null)
			{
				m_FaviconManager.rememberFavicon(theSiteObj);	// (AGR) 25 Feb 2006
			}

			theSiteObj.addCreator(theFirstCreatorsType);
		//	theSiteObj.findFavicon();

			synchronized (_m_PostFeedSitesMutatorLocker)	// (AGR) 20 September 2010
			{
				if (s_FL_Logger.isTraceEnabled())
				{
					s_FL_Logger.trace(">>> SHT #" + m_ID + " Adding: " + theSiteObj);
				}

				m_PostFeedSitesList.add(theSiteObj);
			}

			if (specifiedACommentsFeed)	// (AGR) 30 Nov 2005. Keep track of how many there are...
			{
				m_CommentsFeedSitesList.add(theSiteObj);
			}

			// s_FL_Logger.info( m_Install.getLogPrefix() + ">>> SHT #" + m_ID + " adding " + theSiteObj);

			//////////////////////////////////////////////////////////////////////

			if ( gotNewPostsFeedChannel || gotNewCommentsFeedChannel)
			{
				// (AGR) 1 Dec 2005. In order to tell if an Item is a post or a comment, we
				// need a Site object (can't do anything with a Channel). So the Site must
				// exist before the Channel is registered and we get the snapshot of Items

				if (gotNewPostsFeedChannel)
				{
					// s_FL_Logger.info(">>> SHT #" + m_ID + " Trying to register: " + thePostsFeedURL);

					for ( PollerIF eachPoller : m_Task.getPollerAllocator().allocate( thePostsFeedURL, thePostsFeedChannel))
					{
						eachPoller.registerChannel( theSiteObj, thePostsFeedChannel, m_CurrTimeMSecs);
					}
				}

				if (gotNewCommentsFeedChannel)
				{
					// s_FL_Logger.info(">>> SHT #" + m_ID + " Trying to register comments: " + theCommentsFeedURL);

					for ( PollerIF eachPoller : m_Task.getPollerAllocator().allocate( theCommentsFeedURL, theCommentsFeedChannel))
					{
						eachPoller.registerCommentsChannel( theSiteObj, theCommentsFeedChannel, m_CurrTimeMSecs);
					}
				}
			}

			return true;
		}
	}

	/*******************************************************************************
		(AGR) 21 June 2005
	*******************************************************************************/
	private class ProcessingObservable extends Observable
	{
		/*******************************************************************************
		*******************************************************************************/
		void fire()
		{
			setChanged();
			notifyObservers(m_PostFeedsCount);	// (AGR) 21 June 2005
		}
	}

	/*******************************************************************************
		(AGR) 21 June 2005
	*******************************************************************************/
	public void addObserver( Observer inObs)
	{
		m_DoneObservable.addObserver(inObs);
	}

	/*******************************************************************************
	*******************************************************************************/
	class UpdaterTask implements Runnable
	{
		protected List<SiteHandlerTask>		m_SHTs = new ArrayList<SiteHandlerTask>(20);
		protected Collection<Long>		m_SiteRecnosUsed = new LongOpenHashSet(4000);		// (AGR) 16 Dec 2010

		protected final List<String>		m_FailedPostFeeds = new ArrayList<String>();		// (AGR) 7 October 2005
		protected final List<String>		m_TimedOutPostFeeds = new ArrayList<String>();		// (AGR) 30 Nov 2005

		protected final List<String>		m_FailedCommentsFeeds = new ArrayList<String>();	// (AGR) 29 Nov 2005
		protected final List<String>		m_TimedOutCommentsFeeds = new ArrayList<String>();	// (AGR) 30 Nov 2005

		protected final byte[]			_m_LastRecnoLocker = new byte[0];
		protected final byte[]			_m_SHTsLocker = new byte[0];

		private PollerAllocatorIF		m_PollerAllocator = new DefaultPollerAllocator(m_Install);

		/*******************************************************************************
		*******************************************************************************/
		public PollerAllocatorIF getPollerAllocator()
		{
			return m_PollerAllocator;
		}

		/*******************************************************************************
		*******************************************************************************/
		private void _handleStatement( Statement inS, long inCurrTimeMSecs) throws SQLException
		{
			ResultSetList	theRS = new ResultSetList( inS.executeQuery( m_Install.getQueryBuilder().getAllBlogFeeds() ) );
			List		theRowsList = null;

			try
			{
				if ( m_OPMLHandler != null)
				{
					m_OPMLHandler.clear();
				}

				synchronized (_m_PostFeedSitesMutatorLocker)	// (AGR) 20 September 2010
				{
					m_PostFeedSitesList.clear();
				}

				m_CommentsFeedSitesList.clear();

				////////////////////////////////////////////  (AGR) 7 October 2005, 29 Nov 2005

				m_FailedPostFeeds.clear();
				m_FailedCommentsFeeds.clear();

				m_TimedOutPostFeeds.clear();
				m_TimedOutCommentsFeeds.clear();

				////////////////////////////////////////////

				if (!theRS.isEmpty())
				{
					theRowsList = theRS.getRowsList();

					Collections.shuffle(theRowsList);	// (AGR) 6 April 2010

					/////////////////////////////////////////////////////////////////////////////////////  (AGR) 12 Nov 2009

					Collection<Callable<Object>>	theCollWrapper = new ArrayList<Callable<Object>>();

					for ( int i = 1; i <= Options.getOptions().getNumSiteHandlerThreads(); i++)
					{
						SiteHandlerTask		theSHT = new SiteHandlerTask( this, m_Install, i, inCurrTimeMSecs, theRowsList);

						m_SHTs.add(theSHT);

						theCollWrapper.add(theSHT);
					}

					if ( m_Stats != null)
					{
						m_Stats.setActiveSiteHandlerTasks( m_SHTs.size() );
					}

					/////////////////////////////////////////////////////////////////////////////////////

					s_FL_Logger.info("UpdaterTask: invoking SHTs");

					List<Future<Object>>		theFutures = m_STPE.invokeAll( theCollWrapper, UPDATER_TIMEOUT_PERIOD, UPDATER_TIMEOUT_UNITS);

					// Will block until completion or timeout...

					int	numCancelled = 0;

					for ( Future<Object> each : theFutures)
					{
						if (each.isCancelled())
						{
							numCancelled++;
						}
					}

					if ( numCancelled == 0)
					{
						s_FL_Logger.info("UpdaterTask: SHTs complete.");
					}
					else
					{
						s_FL_Logger.warn("UpdaterTask: SHTs aborted after " + UPDATER_TIMEOUT_PERIOD + " " + UPDATER_TIMEOUT_UNITS + ". " + numCancelled + " SHT(s) cancelled.");
					}

					/////////////////////////////////////////////////////////////////////////////////////

					m_SHTs.clear();

					if ( m_Stats != null)
					{
						m_Stats.setActiveSiteHandlerTasks(0);
					}
				}
			}
			catch (Throwable e)
			{
				s_FL_Logger.debug("UpdaterTask: Problem", e);
			}
			finally		// (AGR) 31 March 2005. Update the headline count figure
			{
				try
				{
					//////////////////////////////////////////////////////////////  (AGR) 26 Feb 2006

					if ( theRowsList != null)
					{
						for ( Object eachMap : theRowsList)
						{
							((Map) eachMap).clear();
						}

						theRowsList.clear();
					}

					//////////////////////////////////////////////////////////////

					m_PostFeedsCount = m_PostFeedSitesList.size();

					int	postFeedSuccesses = m_PostFeedsCount - m_FailedPostFeeds.size() - m_TimedOutPostFeeds.size();
					int	commentFeedTimeouts = m_TimedOutCommentsFeeds.size();
					int	commentFeedFailures = m_FailedCommentsFeeds.size();
					int	commentFeedSuccesses = m_CommentsFeedSitesList.size() - commentFeedFailures - commentFeedTimeouts;

					s_FL_Logger.info( m_Install.getLogPrefix() + " >>> PostFeeds:    " + m_PostFeedsCount + " feeds (" + postFeedSuccesses + " suceeded, " + m_FailedPostFeeds.size() + " failed, " + m_TimedOutPostFeeds.size() + " timed out)");
					s_FL_Logger.info( m_Install.getLogPrefix() + " >>> CommentFeeds: " + m_CommentsFeedSitesList.size() + " feeds (" + commentFeedSuccesses + " suceeded, " + commentFeedFailures + " failed, " + commentFeedTimeouts + " timed out)");

					m_SiteRecnosUsed.clear();

					//////////////////////////////////////////////////////////////

					if ( m_Stats != null)
					{
						m_Stats.setLastFeedCheckTimeNow();
						m_Stats.setActiveSiteHandlerTasks(0);
						m_Stats.setFeedCount(m_PostFeedsCount);

						// (AGR) 7 October 2005

						m_Stats.setSuccessfulFeedCount(postFeedSuccesses);
						m_Stats.setFailedFeedsList(m_FailedPostFeeds);

						// (AGR) 29 Nov 2005

						m_Stats.setSuccessfulCommentFeedCount( commentFeedSuccesses );
						m_Stats.setFailedCommentFeedCount( commentFeedFailures );
						m_Stats.setTimedOutCommentFeedCount( commentFeedTimeouts );
						m_Stats.setFailedCommentFeedsList(m_FailedCommentsFeeds);
					}

					//////////////////////////////////////////////////////////////  (AGR) 18 April 2005 - moved here. Originally 15 April 2005

					generateOPML();

					///////////////////////////////////////  Are there any URLs in the old list and not in the new one?

					if ( m_LastFeedURLsList != null)
					{
						// s_FL_Logger.info("==> lastFeedURLsList = "  + m_LastFeedURLsList);

						if ( m_LastFeedURLsList.removeAll( m_PostFeedSitesList ) && ( m_LastFeedURLsList.size() >= 1))
						{
							for ( SiteIF oldEntryObj : m_LastFeedURLsList)
							{
								ChannelIF	theLastChannel = m_FeedChannels.findURL( oldEntryObj.getFeedURL() );

								// s_FL_Logger.info("==> theLastChannel = "  + theLastChannel);

								if ( theLastChannel != null)
								{
									for ( PollerIF eachPoller : m_Install.getPollers())
									{
										eachPoller.unregisterChannel(theLastChannel);
									}

									m_Install.getHeadlinesMgr().removeFor(theLastChannel);
								}
							}
						}
					}

					/////////////////////  (AGR) 27 May 2005. Couldn't get clone() to work with JDK 1.5 !

					m_LastFeedURLsList = new ArrayList<SiteIF>( m_PostFeedSitesList.size() );
					m_LastFeedURLsList.addAll( m_PostFeedSitesList );

					//////////////////////////////////////////////////////////////

					try
					{
						m_DoneObservable.fire();	// (AGR) 21 June 2005
					}
					catch (RuntimeException e)		// (AGR) 20 May 2009
					{
						s_FL_Logger.error("Observable error", e);
					}

					//////////////////////////////////////////////////////////////

					for ( String eachFailure : m_FailedPostFeeds)
					{
						m_PollerAllocator.failed(eachFailure);
					}

					//////////////////////////////////////////////////////////////  (AGR) 20 June 2005

					m_Install.getIndexMgr().optimise();
				}
				catch (Throwable e2)
				{
					s_FL_Logger.debug("UpdaterTask: Finally problem!", e2);
				}
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		public void run()
		{
			// s_FL_Logger.info("Hello " + new java.util.Date());

			DataSourceConnection	theConnectionObject = null;
			long			currTimeMSecs = System.currentTimeMillis();

			try
			{
				theConnectionObject = new DataSourceConnection( m_Install.getDataSource() );
				if (theConnectionObject.Connect())
				{
					// s_FL_Logger.info("conn = " + theConnectionObject);

					Statement	theS = null;

					try
					{
						theS = theConnectionObject.createStatement();
						_handleStatement( theS, currTimeMSecs);
					}
					catch (SQLException e)
					{
						s_FL_Logger.error("creating statement", e);
					}
					finally
					{
						USQL_Utils.closeStatementCatch(theS);
					}
				}
				else
				{
					s_FL_Logger.warn("Cannot connect!");
				}
			}
			catch (RuntimeException e)
			{
			}
			finally
			{
				// s_FL_Logger.info("m_FeedChannels = " + m_FeedChannels);

				if ( theConnectionObject != null)
				{
					theConnectionObject.CloseDown();
				}
			}
		}
	}
}
