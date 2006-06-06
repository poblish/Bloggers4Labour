/*
 * Headlines.java
 *
 * Created on 12 March 2005, 02:27
 */

package org.bloggers4labour;

import com.hiatus.UDates;
import de.nava.informa.parsers.*;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.utils.poller.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.headlines.*;
import org.bloggers4labour.opml.OPMLGenerator;
import org.bloggers4labour.options.Options;
import org.bloggers4labour.options.TaskOptionsBeanIF;
import org.bloggers4labour.jmx.Stats;
import org.bloggers4labour.tag.Link;

/**
 *
 * @author andrewre
 */
public class Headlines implements HeadlinesIF
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

	private Installation		m_Install;

	////////////////////////////////////////////////////////////////////////  (AGR) 25-26 June 2005. Handlers

	private List<AddHandler>	m_AddHandlers = new CopyOnWriteArrayList<AddHandler>();
	private List<RemoveHandler>	m_RemoveHandlers = new CopyOnWriteArrayList<RemoveHandler>();
	private List<ExpiryHandler>	m_ExpiryHandlers = new CopyOnWriteArrayList<ExpiryHandler>();

	////////////////////////////////////////////////////////////////////////  (AGR) 24 March 2006

	private List<Number>		m_FilterCreatorStatuses;
	private String			m_Description;

	////////////////////////////////////////////////////////////////////////

	private static Logger		s_Headlines_Logger = Logger.getLogger("Main");

	private static ItemIF[]		s_TempArray = new ItemIF[0];

	/*******************************************************************************
	*******************************************************************************/
	public Headlines( final Installation inInstall, String inName, String inDescr, long inMinAgeMsecs, long inMaxAgeMsecs)
	{
		m_Install = inInstall;
		m_Name = inName;
		m_Description = inDescr;
		m_MinAgeMsecs = inMinAgeMsecs;
		m_MaxAgeMsecs = inMaxAgeMsecs;
		s_Headlines_Logger.info( m_Install.getLogPrefix() + "Headlines: created \"" + UDates.getFormattedTimeDiff(m_MaxAgeMsecs) + "\" instance, \"" + m_Name + "\".");

		m_CleanTimer = new Timer("Headlines: cleaner Timer");
		m_CleanerTask = new CleanerTask( this, m_MaxAgeMsecs);

		////////////////////////////////////////////////////////////////  (AGR) 29 Nov 2005

		m_AllowPosts = true;
		m_AllowComments = false;

		////////////////////////////////////////////////////////////////

		TaskOptionsBeanIF	theOptionsBean = Options.getOptions().getHeadlinesCleanerTaskOptions();

		m_CleanTimer.scheduleAtFixedRate( m_CleanerTask,
						  theOptionsBean.getDelayMsecs(),
						  theOptionsBean.getPeriodMsecs());
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

		if ( m_Coll != null)
		{
			m_Coll.clear();
			m_Coll = null;
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
		if ( m_Coll == null)
			return s_TempArray;	// Shouldn't happen!

		return (ItemIF[]) m_Coll.values().toArray(s_TempArray);
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
	public Set getBlogs()
	{
		Set<String>	theSet = new HashSet<String>();

		synchronized (this)
		{
			for ( ItemIF theItem : m_Coll.keySet())
			{
				Object	theSite = theItem.getChannel().getSite();

				if ( theSite != null)
				{
					theSet.add( theSite.toString() );
				}
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
		Set	theBlogs;

		synchronized (this)
		{
			theBlogs = getBlogs();
		}

		int	theCount = theBlogs.size();

		theBlogs.clear();

		return theCount;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isItemAgeOK( long inItemAgeMSecs)
	{
		return ( inItemAgeMSecs >= m_MinAgeMsecs && inItemAgeMSecs < m_MaxAgeMsecs);
	}

	/*******************************************************************************
	*******************************************************************************/
	public AddResult put( ItemIF inNewItem, ItemContext inCtxt)
	{
		if ( inNewItem == null)
		{
			return AddResult.FAILED_GENERAL;
		}

		////////////////////////////////////////////////////////////////

		URL	ourLink = inNewItem.getLink();
		ItemIF	theOldOneToRemove = null;

		synchronized (this)
		{
			for ( ItemIF theItem : m_Coll.keySet())
			{
				if (theItem.equals(inNewItem))	// Look for complete dupe
				{
					s_Headlines_Logger.info( m_Install.getLogPrefix() + "\"" + m_Name + "\".put(): ignoring DUPE: " + inNewItem + ", existing: " + theItem + ", count = " + m_Coll.size());
					return AddResult.FAILED_DUPLICATE;
				}

				////////////////////////////////////////////////////////  (AGR) 10 June 2005

				if ( ourLink != null)
				{
					URL	itsLink = theItem.getLink();

					if ( itsLink != null && ourLink.equals(itsLink))
					{
						// s_Headlines_Logger.info("put(): replacement entry???");
						// s_Headlines_Logger.info("NEW: " + FeedUtils.adjustTitle(inNewItem));
						// s_Headlines_Logger.info("OLD: " + FeedUtils.adjustTitle(theItem));

						if ( theOldOneToRemove == null)
						{
							theOldOneToRemove = theItem;  // to prevent ConcurrentModificationException
						}
						else
						{
							s_Headlines_Logger.warn( m_Install.getLogPrefix() + "theOldOneToRemove is NOT null!");
						}
					}
				}
			}

			////////////////////////////////////////////////////////////////  Now remove the old version

			if ( theOldOneToRemove != null)
			{
				boolean	removedOldOK = ( _remove(theOldOneToRemove) != null);
				// s_Headlines_Logger.info("remove old OK? " + removedOldOK);
			}

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
	private ItemIF _remove( ItemIF inItem)
	{
		ItemIF	theItem = m_Coll.remove(inItem);

		////////////////////////////////////////////////////////////////  (AGR) 25 June 2005

		for ( RemoveHandler rh : m_RemoveHandlers)
		{
			rh.onRemove( m_Install, this, inItem);
		}

		return theItem;
	}

	/*******************************************************************************
		(AGR) 1 June 2005
	*******************************************************************************/
	public int countLinks()
	{
		TextCleaner	tc = new TextCleaner();
		List<Link>	theFullList;

		synchronized (this)
		{
			theFullList = _getLinks(tc);
		}

		int	theCount = theFullList.size();

		theFullList.clear();

		return theCount;
	}

	/*******************************************************************************
		(AGR) 13 April 2005
	*******************************************************************************/
	public List<Link> getLinksByName()
	{
		TextCleaner	tc = new TextCleaner();
		List<Link>	theFullList;

		synchronized (this)
		{
			theFullList = _getLinks(tc);
		}

		Collections.sort( theFullList, tc.newLinkNameSorter());

		return theFullList;
	}

	/*******************************************************************************
		(AGR) 13 April 2005
	*******************************************************************************/
	public List<Link> getLinksByURL()
	{
		TextCleaner	tc = new TextCleaner();
		List<Link>	theFullList;

		synchronized (this)
		{
			theFullList = _getLinks(tc);
		}

		Collections.sort( theFullList, tc.newLinkURLSorter());

		return theFullList;
	}

	/*******************************************************************************
		(AGR) 13 April 2005
	*******************************************************************************/
	private List<Link> _getLinks( TextCleaner inTC)
	{
		List<Link>	theFullList = new ArrayList<Link>(50);

		for ( ItemIF theItem : m_Coll.keySet())
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
	public synchronized void removeFor( ChannelIF inChannel)
	{
		List<ItemIF>	keysList = null;
		
		for ( ItemIF theItem : m_Coll.keySet())
		{
			ChannelIF	itemChannel = theItem.getChannel();

			if (itemChannel.equals(inChannel))
			{
				if ( keysList == null)
				{
					keysList = new ArrayList<ItemIF>();
				}

				keysList.add(theItem);	// do this to prevent ConcurrentModificationExceptions!
			}
		}

		////////////////////////////////////////////

		if ( keysList != null)
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
	public Set<Item> createSortedCollection()
	{
		return new TreeSet<Item>( new ItemsComparator() );
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public String toString()
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
/*			if (Options.CLEANER_MEMORY_CHECKS)
			{
				FeedUtils.logMemory();
			}
*/
//			System.out.println("##### count = " + getBlogsCount() + ", set size = " + getBlogs().size());

			//////////////////////////////////////////////////////////////
			
			List<ItemIF>	keysList = null;
			long		currMS = System.currentTimeMillis();

			for ( ItemIF theItem : m_Coll.keySet())		// FIXME. Should this be sync-ed?
			{
				if (isOutOfDate( currMS, theItem))
				{
					if ( keysList == null)
					{
						keysList = new ArrayList<ItemIF>();
					}

					keysList.add(theItem);	// do this to prevent ConcurrentModificationExceptions!
				}
			}

			////////////////////////////////////////////

			if ( keysList != null)
			{
				// info("removing keys: " + keysList);

				synchronized (m_Parent)		// (AGR) 24 June 2005
				{
					for ( ItemIF remItem : keysList)
					{
						for ( ExpiryHandler eh : m_ExpiryHandlers)	// (AGR) 26 June 2005
						{
							eh.onExpire( m_Parent, remItem);
						}

						_remove(remItem);
					}
				}

				/////////////////////  (AGR) 23 May 2005

				keysList.clear();
				keysList = null;
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

		theItems = null;
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

		theItems = null;
	}

	/*******************************************************************************
		(AGR) 19 April 2005
	*******************************************************************************/
	public void publishSnapshot( ItemIF[] inItems, Stats ioStats, String inName, String inDescription)
	{
		FeedCreator	fc = new FeedCreator(s_Headlines_Logger);	// (AGR) 21 May 2005. Factored-out

		fc.createChannel( ioStats, inName, inDescription, inItems);
		m_HeadlinesXMLString = fc.getString();

//		s_Headlines_Logger.info("Done snapshot: " + inItems.length);

		fc.clear();
		fc = null;	// (AGR) 23 May 2005
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
		FeedList	theFL = m_Install.getFeedList();

		// System.out.println("inBase36BitmapString = " + inBase36BitmapString);

		BigInteger	theBitmap = new BigInteger( inBase36BitmapString, Character.MAX_RADIX);

		// System.out.println("==> theBitmap = " + theBitmap.toString(2));

		ItemIF[]	theFilteredArray = filterItemsList( theFL, inItems, theBitmap, inIncludeNotExclude);

		// System.out.println("Result: " + Arrays.deepToString(theFilteredArray));

		////////////////////////////////////////////////////////////////

		FeedCreator	fc = new FeedCreator(s_Headlines_Logger);	// (AGR) 21 May 2005. Factored-out
		ResourceBundle	theBundle = m_Install.getBundle();
		String		theContent;

		fc.createChannel( null, theBundle.getString("feed.headlines.name"), theBundle.getString("feed.headlines.description"), theFilteredArray);
		theContent = fc.getString();

//		s_Headlines_Logger.info("Done snapshot: " + theFilteredArray.length);

		fc.clear();
		fc = null;	// (AGR) 23 May 2005

		return theContent;
	}

	/*******************************************************************************
		(AGR) 6 June 2006
	*******************************************************************************/
	private ItemIF[] filterItemsList( final FeedList inFL, final ItemIF[] inItems, final BigInteger inBitmapToUse,
					  boolean inIncludeNotExclude)
	{
		List<ItemIF>			theFilteredList = new ArrayList<ItemIF>( inItems.length / 2);
		HashMap<ChannelIF,Site>		theMapping = new HashMap<ChannelIF,Site>();	// Done to cut down on Channel lookups

		for ( ItemIF eachItem : inItems)
		{
			ChannelIF	eachChannel = eachItem.getChannel();
			Site		eachSiteObj = theMapping.get(eachChannel);

			if ( eachSiteObj == null)
			{
				eachSiteObj = inFL.lookupPostsChannel( eachChannel);
				theMapping.put( eachChannel, eachSiteObj);
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

		return theFilteredList.toArray( new ItemIF[0] );
	}

	/*******************************************************************************
		(AGR) 19 April 2005
	*******************************************************************************/
	public String getHeadlinesXMLString()
	{
		return m_HeadlinesXMLString;
	}

	/*******************************************************************************
		(AGR) 25 June 2005
	*******************************************************************************
	public synchronized List<Long> getItemIDs()
	{
		List<Long>	ll = new ArrayList<Long>();

		for ( ItemIF theItem : m_Coll.keySet())
		{
			ll.add( theItem.getId() );
		}

		Collections.sort(ll);

		return ll;
	}/

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
}