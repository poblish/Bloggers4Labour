/*
 * Headlines.java
 *
 * Created on 12 March 2005, 02:27
 */

package org.bloggers4labour;

import com.hiatus.dates.UDates;
import com.hiatus.text.UText;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.feed.FeedListIF;
import org.bloggers4labour.feed.GeneratedFeed;
import org.bloggers4labour.headlines.AddHandler;
import org.bloggers4labour.headlines.ExpiryHandler;
import org.bloggers4labour.headlines.Handler;
import org.bloggers4labour.headlines.HeadlinesIF;
import org.bloggers4labour.headlines.RemoveHandler;
import org.bloggers4labour.index.SearchMatch;
import org.bloggers4labour.options.Options;
import org.bloggers4labour.options.TaskOptionsBeanIF;
import org.bloggers4labour.jmx.Stats;
import org.bloggers4labour.site.SiteIF;
import org.bloggers4labour.sql.QueryBuilder;
import org.bloggers4labour.tag.Link;

/**
 *
 * @author andrewre
 */
public class Headlines implements HeadlinesIF, Iterable<ItemIF>
{
	private TreeMap<ItemIF,ItemIF>	m_Coll = new TreeMap<ItemIF,ItemIF>( new ItemsComparator() );
	private Timer			m_CleanTimer;
	private CleanerTask		m_CleanerTask;
	private String			m_HeadlinesXMLString;	// (AGR) 19 April 2005
	private long			m_MaxAgeMsecs;		// (AGR) 21 May 2005
	private long			m_MinAgeMsecs;		// (AGR) 22 May 2005
	private String			m_Name;			// (AGR) 22 May 2005
	private boolean			m_AllowPosts;		// (AGR) 29 Nov 2005
	private boolean			m_AllowComments;	// " " "

	private InstallationIF		m_Install;

	////////////////////////////////////////////////////////////////////////  (AGR) 25-26 June 2005. Handlers

	private List<AddHandler>	m_AddHandlers = new CopyOnWriteArrayList<AddHandler>();
	private List<RemoveHandler>	m_RemoveHandlers = new CopyOnWriteArrayList<RemoveHandler>();
	private List<ExpiryHandler>	m_ExpiryHandlers = new CopyOnWriteArrayList<ExpiryHandler>();

	////////////////////////////////////////////////////////////////////////  (AGR) 24 March 2006

	private List<Number>		m_FilterCreatorStatuses;
	private String			m_Description;

	////////////////////////////////////////////////////////////////////////

	private static Logger		s_Headlines_Logger = Logger.getLogger( Headlines.class );

	private final static int	MAX_FEED_BLOGS_LISTED = 10;		// (AGR) 10 June 2006

	/*******************************************************************************
	*******************************************************************************/
	public Headlines( final InstallationIF inInstall, String inName, String inDescr, long inMinAgeMsecs, long inMaxAgeMsecs)
	{
		m_Install = inInstall;
		m_Name = inName;
		m_Description = inDescr;
		m_MinAgeMsecs = inMinAgeMsecs;
		m_MaxAgeMsecs = inMaxAgeMsecs;
		s_Headlines_Logger.info( m_Install.getLogPrefix() + "Headlines: created \"" + UDates.getFormattedTimeDiff(m_MaxAgeMsecs) + "\" instance, \"" + m_Name + "\".");

		////////////////////////////////////////////////////////////////  (AGR) 29 Nov 2005

		m_AllowPosts = true;
		m_AllowComments = false;

		////////////////////////////////////////////////////////////////

		if ( m_MaxAgeMsecs != Long.MAX_VALUE)	// (AGR) 5 October 2008
		{
			m_CleanTimer = new Timer("Headlines: cleaner Timer");
			m_CleanerTask = new CleanerTask( this, m_MaxAgeMsecs);

		////////////////////////////////////////////////////////////////

		TaskOptionsBeanIF	theOptionsBean = Options.getOptions().getHeadlinesCleanerTaskOptions();

		m_CleanTimer.scheduleAtFixedRate( m_CleanerTask,
						  theOptionsBean.getDelayMsecs(),
						  theOptionsBean.getPeriodMsecs());
	}
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public void setAllowPosts( boolean x)
	{
		m_AllowPosts = x;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public void setAllowComments( boolean x)
	{
		m_AllowComments = x;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public boolean allowsPosts()
	{
		return m_AllowPosts;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public boolean allowsComments()
	{
		return m_AllowComments;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void shutdown()
	{
		if ( m_CleanTimer != null)
		{
			s_Headlines_Logger.info( m_Install.getLogPrefix() + "Headlines: cancelling Timer: " + m_CleanTimer);

			m_CleanTimer.cancel();
			m_CleanTimer = null;
		}

		m_CleanerTask = null;

		/////////////////////////  (AGR) 13 April 2005

		synchronized (this)	// (AGR) 3 Feb 2007. FindBugs said m_Coll wasn't sync-protected. Looks to be right, too, hence change.
		{
		if ( m_Coll != null)
		{
			m_Coll.clear();
			m_Coll = null;
		}
	}
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized Iterator<ItemIF> iterator()
	{
		return m_Coll.keySet().iterator();
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized ItemIF[] toArray()
	{
		if ( m_Coll == null || m_Coll.isEmpty())
		{
			return new ItemIF[0];
		}

		return m_Coll.values().toArray( new ItemIF[ m_Coll.size() ] );
	}

	/*******************************************************************************
		(AGR) 11 July 2005
	*******************************************************************************/
	public long getMaxAgeMsecs()
	{
		return m_MaxAgeMsecs;
	}

	/*******************************************************************************
		(AGR) 11 July 2005
	*******************************************************************************/
	public long getMinAgeMsecs()
	{
		return m_MinAgeMsecs;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int size()
	{
		synchronized (this)
		{
			return m_Coll.size();
		}
	}

	/*******************************************************************************
		(AGR) 14 March 2005
		A Set of all the Blog URLs represented in the Headlines list
	*******************************************************************************/
	public Collection<String> getBlogs()
	{
		Set<String>	theSet = new HashSet<String>();
		ItemIF[]	theItems = toArray();

		for ( ItemIF eachItem : theItems)
		{
			Object	theSite = eachItem.getChannel().getSite();

				if ( theSite != null)
				{
					theSet.add( theSite.toString() );
				}
			}

		return theSet;
	}

	/*******************************************************************************
		(AGR) 23 May 2005
		A count of all the Blog URLs represented in the Headlines list
	*******************************************************************************/
	public int getBlogsCount()
	{
		synchronized (this)
		{
			return getBlogs().size();
		}
		}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isItemAgeOK( long inItemAgeMSecs)
	{
		return ( inItemAgeMSecs >= m_MinAgeMsecs && inItemAgeMSecs < m_MaxAgeMsecs);
	}

	/*******************************************************************************
	*******************************************************************************/
	public AddResult put( final ItemIF inNewItem, final SiteIF inItemsOwnerSite, final ItemContext inCtxt)
	{
		if ( inNewItem == null)
		{
			return AddResult.FAILED_GENERAL;
		}

		////////////////////////////////////////////////////////////////  (AGR) 4 October 2008

		int			maxNumberItemsPerSite = ( inItemsOwnerSite != null) ? inItemsOwnerSite.getMaximumPostsToAggregate() : Integer.MAX_VALUE;
		Map<ItemIF,ItemIF>	allItemsForThisSite;

		if ( maxNumberItemsPerSite != Integer.MAX_VALUE)
		{
			allItemsForThisSite = new TreeMap<ItemIF,ItemIF>( new ItemsComparator() );
			allItemsForThisSite.put( inNewItem, inNewItem);
		}
		else
		{
			allItemsForThisSite = null;
		}

		////////////////////////////////////////////////////////////////

		URL	ourLink = inNewItem.getLink();
		ItemIF	theOldOneToRemove = null;

		synchronized (this)
		{
			for ( ItemIF theExistingItem : m_Coll.keySet())
			{
				if (theExistingItem.equals(inNewItem))	// Look for complete dupe
				{
					s_Headlines_Logger.info( m_Install.getLogPrefix() + "\"" + m_Name + "\".put(): ignoring DUPE: " + inNewItem + ", existing: " + theExistingItem + ", count = " + m_Coll.size());
					return AddResult.FAILED_DUPLICATE;
				}

				////////////////////////////////////////////////////////  (AGR) 10 June 2005

				if ( ourLink != null)
				{
					URL	itsLink = theExistingItem.getLink();

					if ( itsLink != null && ourLink.equals(itsLink))
					{
						// s_Headlines_Logger.info("put(): replacement entry???");
						// s_Headlines_Logger.info("NEW: " + FeedUtils.adjustTitle(inNewItem));
						// s_Headlines_Logger.info("OLD: " + FeedUtils.adjustTitle(theItem));

						if ( theOldOneToRemove == null)
						{
							theOldOneToRemove = theExistingItem;  // to prevent ConcurrentModificationException
						}
						else
						{
							s_Headlines_Logger.warn( m_Install.getLogPrefix() + "theOldOneToRemove is NOT null!");
						}
					}
				}

				////////////////////////////////////////////////////////  (AGR) 4 October 2008

				if ( allItemsForThisSite != null)
				{
					SiteIF	theExistingItemsSite = m_Install.getFeedList().lookupChannel( theExistingItem.getOurChannel() );

					if ( theExistingItemsSite != null && theExistingItemsSite.equals(inItemsOwnerSite))
					{
						allItemsForThisSite.put( theExistingItem, theExistingItem);
					}
				}
			}

			////////////////////////////////////////////////////////////////  Now remove the old version

			if ( theOldOneToRemove != null)
			{
				/* boolean  removedOldOK = */ _remove(theOldOneToRemove);
			}

			////////////////////////////////////////////////////////////////  (AGR) 4 October 2008. Now examine the existing Items...

			if ( allItemsForThisSite != null && allItemsForThisSite.size() > maxNumberItemsPerSite)
			{
				// Now find the 'maxNumberItemsPerSite' youngest. Remove the rest, and exit if 'inNewItem' is one of them.

				ItemIF[]	theItemsArray = allItemsForThisSite.keySet().toArray( new ItemIF[ allItemsForThisSite.size() ] );
				boolean		doNotAddNewItem = false;

				for ( int i = allItemsForThisSite.size() - 1; i >= maxNumberItemsPerSite; i--)
				{
					ItemIF	theNextOldest = theItemsArray[i];

					if (theNextOldest.equals(inNewItem))
					{
						doNotAddNewItem = true;
					}
					else
					{
						_remove(theNextOldest);
					}
				}

				if (doNotAddNewItem)
				{
					return AddResult.SUCCEEDED;
				}
			}

			////////////////////////////////////////////////////////////////

			_put(inNewItem);

/*			for ( AddHandler ah : m_AddHandlers)	// FIXME. Temporary. See below!
			{
				ah.onAdd( m_Install, this, inNewItem, inCtxt);
			}
*/		}

		///////////////////////////////  (AGR) 25 June 2005. The following needn't be synchronised
		///////////////////////////////  on this Headlines obj, so calling .size() on us within the
		///////////////////////////////  Handler may produce an 'unexpected' result, e.g. different to
		///////////////////////////////  the index size after the Item is removed. For performance reasons!

//		System.out.println( m_Install.getLogPrefix() + "######## Processing: context = " + inCtxt + ", this = " + this);

		for ( AddHandler ah : m_AddHandlers)
		{
			ah.onAdd( m_Install, this, inNewItem, inCtxt);
		}

		return AddResult.SUCCEEDED;
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _put( ItemIF inItem)
	{
/*		if (allowsComments())
		{
			s_Headlines_Logger.info( m_Install.getLogPrefix() + "==> ADDING \"" + FeedUtils.getDisplayTitle(inItem) + "\" from \"" + FeedUtils.channelToString( inItem.getChannel() ) + "\" to " + this);
		}
*/
		m_Coll.put( inItem, inItem);
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _remove( ItemIF inItem)
	{
		synchronized (this)
		{
			if ( m_Coll.remove(inItem) == null)
			{
				return false;	// Nothing removed...
			}
		}

		////////////////////////////////////////////////////////////////  Call RemoveHandler for the removed ItemIF

		for ( RemoveHandler rh : m_RemoveHandlers)
		{
			rh.onRemove( m_Install, this, inItem);
		}

		return true;
	}

	/*******************************************************************************
		(AGR) 1 June 2005
	*******************************************************************************/
	public int countLinks()
	{
		TextCleaner	tc = new TextCleaner();

		return _getLinks(tc).size();
		}

	/*******************************************************************************
		(AGR) 13 April 2005
	*******************************************************************************/
	public Collection<Link> getLinksByName()
	{
		TextCleaner	tc = new TextCleaner();
		List<Link>	theFullList = _getLinks(tc);

		Collections.sort( theFullList, tc.newLinkNameSorter());

		return theFullList;
	}

	/*******************************************************************************
		(AGR) 13 April 2005
	*******************************************************************************/
	public Collection<Link> getLinksByURL()
	{
		TextCleaner	tc = new TextCleaner();
		List<Link>	theFullList = _getLinks(tc);

		Collections.sort( theFullList, tc.newLinkURLSorter());

		return theFullList;
	}

	/*******************************************************************************
		(AGR) 13 April 2005
	*******************************************************************************/
	private List<Link> _getLinks( TextCleaner inTC)
	{
		List<Link>	theFullList = new ArrayList<Link>(50);
		ItemIF[]	theItems = toArray();

		for ( ItemIF theItem : theItems)
		{
			List<Link>	theLinks = inTC.collectLinks( theItem.getDescription(), theItem.getLink());

			if ( theLinks != null)
			{
				theFullList.addAll(theLinks);
			}
		}

		return theFullList;
	}

	/*******************************************************************************
		Remove all entries in our table that come from the specified Channel
	*******************************************************************************/
	public void removeFor( final ChannelIF inChannel)
	{
		List<ItemIF>	keysList = new ArrayList<ItemIF>();
		ItemIF[]	theItems = toArray();

		for ( ItemIF theItem : theItems)
		{
			if (theItem.getChannel().equals(inChannel))
			{
				keysList.add(theItem);	// do this to prevent ConcurrentModificationExceptions!
			}
		}

		////////////////////////////////////////////

		if (!keysList.isEmpty())
		{
			// s_Headlines_Logger.info("removing keys: " + keysList);

			for ( ItemIF remItem : keysList)
			{
				_remove(remItem);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public Collection<ItemIF> createSortedCollection()
	{
		return new TreeSet<ItemIF>( new ItemsComparator() );
	}
	
	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return ("Headlines \"" + m_Install.getName() + ": " + m_Name + "\"");	// m_Coll.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	class CleanerTask extends ItemCleanerTask
	{
		private final Headlines	m_Parent;

		/*******************************************************************************
		*******************************************************************************/
		public CleanerTask( final Headlines inParent, long inMaxItemAgeMS)
		{
			super(inMaxItemAgeMS);

			m_Parent = inParent;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void run()
		{
			List<ItemIF>	keysList = null;
			long		currMS = System.currentTimeMillis();

			synchronized (m_Parent)		// (AGR) 5 Oct 2006. Moved up here, because of ConcurrentModException seen at (1)
			{
				for ( ItemIF theItem : m_Coll.keySet())		// (1). CME seen here
				{
					if (isOutOfDate( currMS, theItem))
					{
						if ( keysList == null)
						{
							keysList = new ArrayList<ItemIF>();
						}

						keysList.add(theItem);	// (AGR) 5 Oct 2006. Still reqd??? FIXME ... (AGR) '05. do this to prevent ConcurrentModificationExceptions!
					}
				}

				////////////////////////////////////////////

				if ( keysList != null)
				{
					// info("removing keys: " + keysList);

					for ( ItemIF remItem : keysList)
					{
						for ( ExpiryHandler eh : m_ExpiryHandlers)	// (AGR) 26 June 2005
						{
							eh.onExpire( m_Parent, remItem);
						}

						_remove(remItem);
					}
				}
			}

			///////////////////////////  (AGR) 5 Oct 2006. This stuff does not need to be sync-ed

			if ( keysList != null)
			{
				keysList.clear();
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void publishSnapshot()
	{
		ItemIF[]	theItems = toArray();
		ResourceBundle	theBundle = m_Install.getBundle();

		if ( theItems == null || theItems.length < 1)
		{
			s_Headlines_Logger.info( theBundle.getString("feed.generation.none") );
		}

		publishSnapshot( theItems, null, m_Name, m_Description);
	}

	/*******************************************************************************
		(AGR) 19 April 2005
	*******************************************************************************/
	public void publishSnapshot( Stats ioStats)
	{
		ItemIF[]	theItems = toArray();
		ResourceBundle	theBundle = m_Install.getBundle();

		if ( theItems == null || theItems.length < 1)
		{
			s_Headlines_Logger.info( theBundle.getString("feed.generation.none"));	// (AGR) 21 May 2005
		}

		publishSnapshot( theItems, ioStats, theBundle.getString("feed.headlines.name"), theBundle.getString("feed.headlines.description"));
	}

	/*******************************************************************************
		(AGR) 19 April 2005
	*******************************************************************************/
	public void publishSnapshot( ItemIF[] inItems, Stats ioStats, String inName, String inDescription)
	{
		FeedCreator	fc = new FeedCreator(s_Headlines_Logger);	// (AGR) 21 May 2005. Factored-out
		GeneratedFeed	theFeed = fc.createChannel( ioStats, inName, inDescription, inItems);

		if ( theFeed == null)
		{
			throw new RuntimeException("Feed creation failed!");
		}

		m_HeadlinesXMLString = theFeed.getXML();

//		s_Headlines_Logger.info("Done snapshot: " + inItems.length);
	}

	/*******************************************************************************
		(AGR) 6 June 2006
	*******************************************************************************/
	public String publishSnapshot_Included( ItemIF[] inItems, final String inIncludeOnlyTheseBlogs)
	{
		return publishSnapshot_Filtered( inItems, inIncludeOnlyTheseBlogs, true);
	}

	/*******************************************************************************
		(AGR) 6 June 2006
	*******************************************************************************/
	public String publishSnapshot_Excluded( ItemIF[] inItems, final String inExcludeOnlyTheseBlogs)
	{
		return publishSnapshot_Filtered( inItems, inExcludeOnlyTheseBlogs, false);
	}

	/*******************************************************************************
		(AGR) 6 June 2006
	*******************************************************************************/
	public String publishSnapshot_Filtered( ItemIF[] inItems, final String inBase36BitmapString, boolean inIncludeNotExclude)
	{
		BigInteger	theBitmap;

		// System.out.println("inBase36BitmapString = " + inBase36BitmapString);

		if ( UText.isNullOrBlank(inBase36BitmapString) || inBase36BitmapString.equals("0"))	// (AGR) 7 June 2006
		{
			theBitmap = BigInteger.ZERO;
		}
		else	theBitmap = new BigInteger( inBase36BitmapString, Character.MAX_RADIX);

		////////////////////////////////////////////////////////////////

		// System.out.println("==> theBitmap = " + theBitmap.toString(2));

		FeedListIF	theFL = m_Install.getFeedList();
		ItemIF[]	theFilteredArray = filterItemsList( theFL, inItems, theBitmap, inIncludeNotExclude);

		// System.out.println("Result: " + Arrays.deepToString(theFilteredArray));

		////////////////////////////////////////////////////////////////

		FeedCreator	fc = new FeedCreator(s_Headlines_Logger);	// (AGR) 21 May 2005. Factored-out
		ResourceBundle	theBundle = m_Install.getBundle();
		String		theContent;
		String		theAppropriateDesc;			// (AGR) 10 June 2006
		int		numBlogsChosen = theBitmap.bitCount();	// (AGR) 10 June 2006

		if ( theFilteredArray == inItems)
		{
			theAppropriateDesc = theBundle.getString("feed.headlines.description");
		}
		else if ( numBlogsChosen == 0)
		{
			theAppropriateDesc = theBundle.getString("feed.headlines.none");
		}
		else
		{
			Formatter	theFormatter = new Formatter();

			if ( numBlogsChosen <= MAX_FEED_BLOGS_LISTED)
			{
				ArrayList<String>	theNames = new ArrayList<String>( MAX_FEED_BLOGS_LISTED );
				SiteIF			theSiteObj;
				int			theBitmapSize = theBitmap.bitLength();

				for ( int theIndex = 0; theIndex < theBitmapSize; theIndex++)
				{
					if (theBitmap.testBit(theIndex))
					{
						theSiteObj = theFL.lookup(theIndex);
						if ( theSiteObj != null)
						{
							theNames.add( theSiteObj.getName() );
						}
					}
				}

				///////////////////////////////////////////////////////////////////////

				theFormatter.format( theBundle.getString( inIncludeNotExclude ? "feed.headlines.some" : "feed.headlines.not_some"),
						     FeedUtils.listToString(theNames));
			}
			else
			{
				theFormatter.format( theBundle.getString( inIncludeNotExclude ? "feed.headlines.lots" : "feed.headlines.not_lots"),
						     numBlogsChosen);
			}

			theAppropriateDesc = theFormatter.toString();
		}

		theContent = fc.createChannel( null,
						theBundle.getString("feed.headlines.name"),
						theAppropriateDesc,
						theFilteredArray).getXML();

//		s_Headlines_Logger.info("Done snapshot: " + theFilteredArray.length);

		return theContent;
	}

	/*******************************************************************************
		(AGR) 6 June 2006
	*******************************************************************************/
	private ItemIF[] filterItemsList( final FeedListIF inFL, final ItemIF[] inItems, final BigInteger inBitmapToUse,
					  boolean inIncludeNotExclude)
	{
		if ( inBitmapToUse == BigInteger.ZERO ||		// For efficiency...
		     inBitmapToUse.equals( BigInteger.ZERO ))
		{
			if (inIncludeNotExclude)	// if you only want to include NOTHING, simply return nothing now...
			{
				return new ItemIF[0];
			}
			else
			{
				return inItems;		// if you only want to exclude NOTHING, just return the original list...
			}
		}

		////////////////////////////////////////////////////////////////

		List<ItemIF>			theFilteredList = new ArrayList<ItemIF>( inItems.length / 2);
		HashMap<ChannelIF,SiteIF>	theMapping = new HashMap<ChannelIF,SiteIF>();	// Done to cut down on Channel lookups

		for ( ItemIF eachItem : inItems)
		{
			ChannelIF	eachChannel = eachItem.getOurChannel();
			SiteIF		eachSiteObj = theMapping.get(eachChannel);

			if ( eachSiteObj == null)
			{
				eachSiteObj = inFL.lookupPostsChannel(eachChannel);
				theMapping.put( eachChannel, eachSiteObj);

				if ( eachSiteObj == null)	// (AGR) 31 July 2008. Not good! Log, and skip this site.
				{
					s_Headlines_Logger.warn("Looking up " + eachChannel + " returned NULL Site! Cannot use in filtered list.");
					continue;
				}
			}

			// System.out.println("# Item: " + FeedUtils.adjustTitle(eachItem) + " => " + eachSiteObj);

			////////////////////////////////////////////////////////

			boolean		foundMatch = inBitmapToUse.testBit((int) eachSiteObj.getRecno() );

			if (( foundMatch && inIncludeNotExclude) || ( !foundMatch && !inIncludeNotExclude))
			{
				theFilteredList.add(eachItem);

				// System.out.println("### Recno " + eachSiteObj.getRecno() + " is included in list");
			}
		}

		return theFilteredList.toArray( new ItemIF[ theFilteredList.size() ] );
	}

	/*******************************************************************************
		(AGR) 19 April 2005
	*******************************************************************************/
	public String getHeadlinesXMLString()
	{
		return m_HeadlinesXMLString;
	}

	/*******************************************************************************
		(AGR) 10 July 2005
	*******************************************************************************/
	public void addHandler( Handler inHandler)
	{
		if ( inHandler instanceof AddHandler)
		{
			m_AddHandlers.add((AddHandler) inHandler);
		}
		else if ( inHandler instanceof RemoveHandler)
		{
			m_RemoveHandlers.add((RemoveHandler) inHandler);
		}
		else if ( inHandler instanceof ExpiryHandler)
		{
			m_ExpiryHandlers.add((ExpiryHandler) inHandler);
		}
	}

	/*******************************************************************************
		(AGR) 24 March 2006
	*******************************************************************************/
	public String getName()
	{
		return m_Name;
	}

	/*******************************************************************************
		(AGR) 24 March 2006
	*******************************************************************************/
	public void setFilterCreatorStatuses( List<Number> inList)
	{
		m_FilterCreatorStatuses = new CopyOnWriteArrayList<Number>(inList);
	}

	/*******************************************************************************
		(AGR) 24 March 2006
	*******************************************************************************/
	public List<Number> getFilterCreatorStatuses()
	{
		return m_FilterCreatorStatuses;
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public String getRecommendationCountsQuery()
	{
		ItemIF[]	theContents = toArray();
		StringBuilder	sb = new StringBuilder( 40 * theContents.length);
		int		count = 0;

		for ( int i = 0; i < theContents.length; i++)
		{
			if ( theContents[i].getLink() != null)	// (AGR) 29 October 2006 (!)
			{
				sb.append(( i > 0) ? ",'" : "'").append( theContents[i].getLink() ).append("'");
				count++;
			}
		}

		if ( count == 0)	// (AGR 29 October 2006
		{
			return null;
		}

		return QueryBuilder.getRecommendationCountsQueryString( sb.toString() );
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public static String getRecommendationCountsQuery( final ItemIF[] inEntries)
	{
		return getRecommendationCountsQuery( inEntries, Integer.MAX_VALUE);
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public static String getRecommendationCountsQuery( final ItemIF[] inEntries, int inMaxEntries)
	{
		int		actualCount = inEntries.length > inMaxEntries ? inMaxEntries : inEntries.length;
		StringBuilder	sb = new StringBuilder( 40 * actualCount);
		int		count = 0;

		for ( int i = 0; i < actualCount; i++)
		{
			if ( inEntries[i].getLink() != null)	// (AGR) 29 October 2006 (!)
			{
				sb.append(( count > 0) ? ",'" : "'").append( inEntries[i].getLink() ).append("'");
				count++;
			}
		}

		// s_Headlines_Logger.info("RCQ -> \"" + QueryBuilder.getRecommendationCountsQueryString( sb.toString() ) + "\", list = " + Arrays.deepToString(inEntries));

		if ( count == 0)	// (AGR 29 October 2006
		{
			return null;
		}

		return QueryBuilder.getRecommendationCountsQueryString( sb.toString() );
	}

	/*******************************************************************************
		(AGR) 1 October 2006. For Tags page.
	*******************************************************************************/
	public static String getRecommendationCountsQuery( final Collection<ItemIF> inEntries)
	{
		StringBuilder	sb = new StringBuilder( 40 * inEntries.size());
		int		i = 0;

		for ( ItemIF eachItem : inEntries)
		{
			if ( eachItem.getLink() != null)	// (AGR) 29 October 2006 (!)
			{
				sb.append(( i > 0) ? ",'" : "'").append( eachItem.getLink() ).append("'");
				i++;
			}
		}

		if ( i == 0)	// (AGR 29 October 2006
		{
			return null;
		}

		return QueryBuilder.getRecommendationCountsQueryString( sb.toString() );
	}

	/*******************************************************************************
		(AGR) 11 October 2006
	*******************************************************************************/
	public static String getSearchRecommendationCountsQuery( final Collection<SearchMatch> inEntries)
	{
		if ( inEntries == null || inEntries.isEmpty())	// (AGR) 28 May 2009
		{
			return null;
		}

		/////////////////////////////////////////////////////////////////////

		StringBuilder	sb = new StringBuilder( 40 * inEntries.size());
		int		i = 0;

		for ( SearchMatch eachItem : inEntries)
		{
			if ( eachItem.getLink() != null)	// (AGR) 29 October 2006 (!)
			{
				sb.append(( i > 0) ? ",'" : "'").append( eachItem.getLink() ).append("'");
				i++;
			}
		}

		// s_Headlines_Logger.info("SRCQ -> \"" + QueryBuilder.getRecommendationCountsQueryString( sb.toString() ) + "\", list = " + inEntries);

		if ( i == 0)	// (AGR 29 October 2006
		{
			return null;
		}

		return QueryBuilder.getRecommendationCountsQueryString( sb.toString() );
	}

	/*******************************************************************************
		(AGR) 30 September 2006
	*******************************************************************************/
	public static Map<String,Number> getRecommendationCountsMap( final java.sql.ResultSet ioRS) throws java.sql.SQLException
	{
		Map<String,Number>	theMap = new HashMap<String,Number>();

		if (ioRS.next())
		{
			do
			{
				theMap.put( ioRS.getString(1), ioRS.getInt(2));
			}
			while (ioRS.next());
		}

		return theMap;
	}
}