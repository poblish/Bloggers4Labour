/*
 * CategoriesTable.java
 *
 * Created on May 19, 2005, 9:48 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.cats;

import de.nava.informa.core.CategoryIF;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Timer;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemCleanerTask;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.options.Options;
import org.bloggers4labour.options.TaskOptionsBeanIF;

/**
 *
 * @author andrewre
 */
public class CategoriesTable implements CategoriesTableIF
{
	private Map<String,Collection<ItemIF> >	m_Map;
	private final byte[]			_m_Map_Locker = new byte[0];

	private int				m_EntriesCount;
	private int				m_MinEntryCount;
	private int				m_MaxEntryCount;

	private Timer				m_ExpiryTimer;
	private ItemCleanerTask			m_ExpiryTask;

	private String				m_LogPrefix;

	private static Logger			s_Logger = Logger.getLogger( CategoriesTable.class );

	/*******************************************************************************
	*******************************************************************************/
	public CategoriesTable( final InstallationIF inInstall)
	{
		m_Map = new HashMap<String,Collection<ItemIF> >();

		m_LogPrefix = inInstall.getLogPrefix();

		inInstall.getFeedList().addObserver( new CountEvent(this) );    // (AGR) 22 June 2005

		reconnect();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void reconnect()
	{
		if ( m_ExpiryTimer == null)
		{
			m_ExpiryTimer = new Timer("Categories: expiry Timer");
		}

		if ( m_ExpiryTask == null)
		{
			m_ExpiryTask = new ExpiryTask( getMaxAgeMSecs() );
		}

		////////////////////////////////////////////////////////////////

		TaskOptionsBeanIF	theOptionsBean = Options.getOptions().getExpiryTaskOptions();

		m_ExpiryTimer.scheduleAtFixedRate( m_ExpiryTask,
						   theOptionsBean.getDelayMsecs(),
						   theOptionsBean.getPeriodMsecs());
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addCategories( ItemIF inItem)
	{
		addCategories( inItem, inItem.getCategories());
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addCategories( ItemIF inItem, Collection inCats)
	{
		ChannelIF			ch = inItem.getOurChannel();
//		de.nava.informa.core.ChannelIF	theDEChannel = new DefaultChannelBridgeFactory().getInstance().bridge(ch);

		for ( Object obj : inCats)
		{
			String	theKey = ((CategoryIF) obj).getTitle().trim();

			if ( theKey.length() < 1 ||			// (AGR) 28 Feb 2006
			     theKey.equalsIgnoreCase("Uncategorized"))	// (AGR) 28 May 2005
			{
				continue;
			}

			if (theKey.startsWith("On "))	// (AGR) 11 March 2006
			{
				theKey = theKey.substring(3);

				// Ensure 1st letter is capitalised... but what about iPods...?

				theKey = Character.toTitleCase( theKey.charAt(0) ) + theKey.substring(1);
			}
//			else	System.out.println("---> " + theKey + ".");

			////////////////////////////////////////////////////////

			// (AGR) 25 May 2005. We need to supply a Channel, because otherwise the Site
			// object lookups will fail, and this'll cause us to see "???" in the posts on
			// the Tags page, not the actual site name or icon.

			ItemIF	theClonedItem = inItem.clone();	// FeedUtils.cloneItem( theDEChannel, inItem, true);

			synchronized (_m_Map_Locker)
			{
				Collection<ItemIF>	theList = getCategoryEntries(theKey);

			if ( theList == null)
			{
				// s_Headlines_Logger.info( m_LogPrefix + "...Adding new Category \"" + theKey + "\"");

				theList = new ArrayList<ItemIF>();

				m_Map.put( theKey, theList);
				}
				else	// (AGR) 6 June 2009. To prevent a build-up of posts with the same Title and Link, but *different* Description
				{
					Collection<ItemIF>	theRemovals = null;

					for ( ItemIF eachExistingItemForThisKey : theList)
					{
						if (theClonedItem.matchesTitleAndLink(eachExistingItemForThisKey))
						{
							if ( theRemovals == null)
							{
								theRemovals = new ArrayList<ItemIF>();
							}

							theRemovals.add(eachExistingItemForThisKey);
						}
					}

					// To Prevent ConcurrentModificationExceptions...

					if ( theRemovals != null)
					{
						for ( ItemIF itemToRemove : theRemovals)
						{
							// s_Logger.info(">>> TRY TO REMOVE OLD VERSION FOR '" + theKey + "' : " + itemToRemove + ", now (=" + theList.size() + ") list = " + theList);

							if (theList.remove(itemToRemove))
							{
								// s_Logger.info(">>> REMOVING OLD VERSION FOR '" + theKey + "' : " + itemToRemove + ", now (=" + theList.size() + ") list = " + theList);

								m_EntriesCount--;
							}
						}
					}
			}

				////////////////////////////////////////////////////////////////////////////////////////////////////

				// s_Logger.info(">>> ADDING to '" + theKey + "' : " + theClonedItem + " to list = " + theList);

				if (theList.add(theClonedItem))
				{
			m_EntriesCount++;
				}
		}
	}
	}

	/*******************************************************************************
		(AGR) 3 Feb 2007. FindBugs made me make this synchronized, but surely
		we can be more fine-grained (and faster) than this?! FIXME
	*******************************************************************************/
	public int entriesCount()
	{
		synchronized (_m_Map_Locker)
		{
		return m_EntriesCount;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean hasEntries()
	{
		synchronized (_m_Map_Locker)
		{
		return !m_Map.isEmpty();
	}
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public Collection<RankingObject> getRankedCategories()
	{
		Set<RankingObject>	theRankedSet = new TreeSet<RankingObject>();

		synchronized (_m_Map_Locker)
		{
		for ( String theCatName : m_Map.keySet())
		{
			theRankedSet.add( new RankingObject( theCatName, m_Map.get(theCatName).size() ) );
		}
		}

		return theRankedSet;
	}

	/*******************************************************************************
	*******************************************************************************/
	private String[] getCategoryKeys()
	{
		synchronized (_m_Map_Locker)
		{
			Collection<String>	theSet = m_Map.keySet();

			return theSet.toArray( new String[ theSet.size() ] );
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public Collection<String> getCategories()
	{
		Set<String>	theSortedSet = new TreeSet<String>( /* For case-insensitivity */ new CategoryNameSorter() );

		for ( String theCatName : getCategoryKeys())
		{
			theSortedSet.add(theCatName);
		}

		return theSortedSet;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Collection<ItemIF> getCategoryEntries( String inCategoryName)
	{
		synchronized (_m_Map_Locker)
	{
		return m_Map.get(inCategoryName);
	}
	}

	/*******************************************************************************
		(AGR) 5 June 2005 - made synchronized.
	*******************************************************************************/
	public void clearTable()
	{
		if ( m_ExpiryTimer != null)
		{
			s_Logger.info( m_LogPrefix + "CategoriesTable: cancelling Timer: " + m_ExpiryTimer);

			m_ExpiryTimer.cancel();
			m_ExpiryTimer = null;
		}

		m_ExpiryTask = null;

		/////////////////////////////////////

		synchronized (_m_Map_Locker)
		{
		if ( m_Map != null)
		{
			m_Map.clear();
		}

		m_EntriesCount = 0;
	}
	}

	/*******************************************************************************
		(AGR) 5 June 2005 - made synchronized.
	*******************************************************************************/
	private void calculateCounts()
	{
		synchronized (_m_Map_Locker)
	{
		if (m_Map.isEmpty())
		{
			m_MinEntryCount = m_MaxEntryCount = 0;
			return;
		}
		
		//////////////////////////////////////////////////////////////////////

		m_MinEntryCount = Integer.MAX_VALUE;
		m_MaxEntryCount = 0;

			for ( Collection<ItemIF> l : m_Map.values())
		{
			if ( l.size() > m_MaxEntryCount)
			{
				m_MaxEntryCount = l.size();
			}
			else if ( l.size() < m_MinEntryCount)
			{
				m_MinEntryCount = l.size();
			}
		}
	}
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getFontSize( int inCount, int inMinFontSize, int inDefaultSize, int inMaxFontSize)
	{
		synchronized (_m_Map_Locker)
		{
			if ( m_MinEntryCount >= m_MaxEntryCount)	// well, could have just said ==
			{
				return inDefaultSize;
			}

			if ( inCount >= m_MaxEntryCount)
			{
				return inMaxFontSize;
			}

			if ( inCount <= m_MinEntryCount)
			{
				return inMinFontSize;
			}

			double	d = (double)( inCount - m_MinEntryCount) / (double)( m_MaxEntryCount - m_MinEntryCount) * (double)( inMaxFontSize - inMinFontSize);

			return (int) Math.round( d + (double) inMinFontSize);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getMaxAgeMSecs()
	{
		return org.bloggers4labour.Constants.ONE_DAY_MSECS * 5;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		StringBuilder	sb = new StringBuilder(2000);

		synchronized (_m_Map_Locker)
		{
		sb.append("Entries: " + m_EntriesCount + "\n");

		for ( String theKey : m_Map.keySet())
		{
				Collection<ItemIF>	theEntries = m_Map.get(theKey);
				boolean			gotOne = false;

			for ( ItemIF theItem : theEntries)
			{
				if (!gotOne)
				{
					sb.append( theKey + " (" + theEntries.size() + ") : " + theItem + "\n");
					gotOne = true;
				}
				else
				{
					sb.append("                 " + theItem + "\n");
					}
				}
			}
		}

		return sb.toString();
	}

	/********************************************************************
	********************************************************************/
	public static class CategoryNameSorter implements Comparator<String>, /* (AGR) 29 Jan 2007. FindBugs recommended this */ Serializable
	{
		private static final long serialVersionUID = 1L;

		/********************************************************************
		********************************************************************/
		public int compare( String a, String b)
		{
			return a.compareToIgnoreCase(b);
		}
	}

	/*******************************************************************************
		(AGR) 20 May 2005
	*******************************************************************************/
	class ExpiryTask extends ItemCleanerTask
	{
		/*******************************************************************************
		*******************************************************************************/
		public ExpiryTask( long inMaxItemAgeMS)
		{
			super(inMaxItemAgeMS);
		}

		/*******************************************************************************
		*******************************************************************************/
		public void run()
		{
			// info("<<< Cleaning old Category items >>> " + this);

			//////////////////////////////////////////////////////////////
			
			List<String>	theEmptyCats = null;
			List<ItemIF>	tempRemovalList = new ArrayList<ItemIF>(10);
			long		currMS = System.currentTimeMillis();

			synchronized (_m_Map_Locker)
			{
				for ( Map.Entry<String,Collection<ItemIF>> eachItem : m_Map.entrySet())
				{
					Collection<ItemIF>	theEntries = eachItem.getValue();

				tempRemovalList.clear();

				for ( ItemIF theItem : theEntries)
				{
					if (isOutOfDate( currMS, theItem))
					{
						// info(".... age = " + UDates.getFormattedTimeDiff( currMS - FeedUtils.getItemDate(theItem).getTime()));

						tempRemovalList.add(theItem);
					}
				}

				if ( tempRemovalList.size() > 0)
				{
						info( m_LogPrefix + "  Expired items " + tempRemovalList + " for category \"" + eachItem.getKey() + "\"");
					// info("WAS: " + theEntries);

					theEntries.removeAll(tempRemovalList);
					if ( theEntries.size() < 1)
					{
						if ( theEmptyCats == null)
							{
							theEmptyCats = new ArrayList<String>();
							}

							theEmptyCats.add( eachItem.getKey() );
					}

					// info("NOW: " + theEntries);
					// info("===================================");
				}
			}

			////////////////////////////////////////////////////////

			if ( theEmptyCats != null)
			{
				// s_Headlines_Logger.info("... map was " + m_Map.keySet());

				for ( String theCatName : theEmptyCats)
				{
					info( m_LogPrefix + "  => Remove category \"" + theCatName + "\"");

					m_Map.remove(theCatName);
				}

				// s_Headlines_Logger.info("... map is  " + m_Map.keySet());
			}
		}
	}
	}

	/*******************************************************************************
		(AGR) 19 May 2005. Calculate count in FeedList
		(AGR) 22 June 2005. Changed to be Observer
	*******************************************************************************/
	private static class CountEvent implements Observer
	{
		private CategoriesTable		m_Table;

		/*******************************************************************************
		*******************************************************************************/
		public CountEvent( CategoriesTable inTable)
		{
			m_Table = inTable;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void update( Observable o, Object arg)
		{
			m_Table.calculateCounts();
		}
	}
}
