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

import com.hiatus.UDates;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.core.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.Installation;
import org.bloggers4labour.ItemCleanerTask;
import org.bloggers4labour.options.Options;
import org.bloggers4labour.options.TaskOptionsBeanIF;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class CategoriesTable
{
	private Map<String,List<ItemIF> >	m_Map;
	private int				m_EntriesCount;
	private int				m_MinEntryCount;
	private int				m_MaxEntryCount;

	private Timer				m_ExpiryTimer;
	private ItemCleanerTask			m_ExpiryTask;

	private String				m_LogPrefix;

	private static Logger			s_Headlines_Logger = Logger.getLogger("Main");

	private final static long		MAX_CATEGORY_AGE_MSECS = ONE_DAY_MSECS * 5;    // (AGR) 4 March 2006. Was a week. (AGR) 19 May 2005

	/*******************************************************************************
	*******************************************************************************/
	public CategoriesTable( final Installation inInstall)
	{
//		m_Map = new TreeMap<String,List<ItemIF> >( new CategoryNameSorter() );
		m_Map = new HashMap<String,List<ItemIF> >();

		m_LogPrefix = inInstall.getLogPrefix();

		inInstall.getFeedList().addObserver( new CountEvent(this) );    // (AGR) 22 June 2005

		reconnect();
	}

	/*******************************************************************************
		(AGR) 4 March 2006
	*******************************************************************************/
	public static long getMaxPermissibleItemAge()
	{
		return MAX_CATEGORY_AGE_MSECS;
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
			m_ExpiryTask = new ExpiryTask( MAX_CATEGORY_AGE_MSECS );
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
		(AGR) 5 June 2005 - made synchronized.
	*******************************************************************************/
	public synchronized void addCategories( ItemIF inItem, Collection inCats)
	{
		ChannelIF	ch = inItem.getChannel();

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

			List<ItemIF>	theList = getCategoryEntries(theKey);

			if ( theList == null)
			{
				// s_Headlines_Logger.info( m_LogPrefix + "...Adding new Category \"" + theKey + "\"");

				theList = new ArrayList<ItemIF>();
				m_Map.put( theKey, theList);

				// s_Headlines_Logger.info("  => Add category \"" + theKey + "\" from \"" + ch.getTitle() + "\"");
			}

			// (AGR) 25 May 2005. We need to supply a Channel, because otherwise the Site
			// object lookups will fail, and this'll cause us to see "???" in the posts on
			// the Tags page, not the actual site name or icon.

			theList.add( FeedUtils.cloneItem( ch, inItem, true) );

			m_EntriesCount++;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public List getEntriesForCategory( String inName)
	{
		return m_Map.get(inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	public int entriesCount()
	{
		return m_EntriesCount;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean hasEntries()
	{
		return !m_Map.isEmpty();
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public synchronized Set<RankingObject> getRankedCategories()
	{
		Set<RankingObject>	theRankedSet = new TreeSet<RankingObject>();

		for ( String theCatName : m_Map.keySet())
		{
			theRankedSet.add( new RankingObject( theCatName, m_Map.get(theCatName).size() ) );
		}

		return theRankedSet;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized Set<String> getCategories()
	{
		Set<String>	theSortedSet = new TreeSet<String>( /* For case-insensitivity */ new CategoryNameSorter() );

		for ( String theCatName : m_Map.keySet())
		{
			theSortedSet.add(theCatName);
		}

		return theSortedSet;
	}

	/*******************************************************************************
	*******************************************************************************/
	public List<ItemIF> getCategoryEntries( String inCategoryName)
	{
		return m_Map.get(inCategoryName);
	}

	/*******************************************************************************
		(AGR) 5 June 2005 - made synchronized.
	*******************************************************************************/
	public synchronized void clearTable()
	{
		if ( m_ExpiryTimer != null)
		{
			s_Headlines_Logger.info( m_LogPrefix + "CategoriesTable: cancelling Timer: " + m_ExpiryTimer);

			m_ExpiryTimer.cancel();
			m_ExpiryTimer = null;
		}

		m_ExpiryTask = null;

		/////////////////////////////////////

		if ( m_Map != null)
		{
			m_Map.clear();
		}

		m_EntriesCount = 0;
	}

	/*******************************************************************************
		(AGR) 5 June 2005 - made synchronized.
	*******************************************************************************/
	private synchronized void calculateCounts()
	{
		if (m_Map.isEmpty())
		{
			m_MinEntryCount = m_MaxEntryCount = 0;
			return;
		}
		
		//////////////////////////////////////////////////////////////////////

		m_MinEntryCount = Integer.MAX_VALUE;
		m_MaxEntryCount = 0;

		for ( List l : m_Map.values())
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

	/*******************************************************************************
	*******************************************************************************/
	public int getFontSize( int inCount, int inMinFontSize, int inDefaultSize, int inMaxFontSize)
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

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		StringBuilder	sb = new StringBuilder(2000);

		sb.append("Entries: " + m_EntriesCount + "\n");

		for ( String theKey : m_Map.keySet())
		{
			List<ItemIF>	theEntries = m_Map.get(theKey);
			boolean		gotOne = false;

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

		return sb.toString();
	}

	/********************************************************************
	********************************************************************/
	public class CategoryNameSorter implements Comparator<String>
	{
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

			for ( String theKey : m_Map.keySet())
			{
				List<ItemIF>	theEntries = m_Map.get(theKey);

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
					info( m_LogPrefix + "  Expired items " + tempRemovalList + " for category \"" + theKey + "\"");
					// info("WAS: " + theEntries);

					theEntries.removeAll(tempRemovalList);
					if ( theEntries.size() < 1)
					{
						if ( theEmptyCats == null)
							theEmptyCats = new ArrayList<String>();

						theEmptyCats.add(theKey);
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

			////////////////////////////////////////////////////////

/*			List<RankingObject>	theRankingList = new ArrayList<RankingObject>();

			for ( String theCatName : m_Table.getCategories())
			{
				theRankingList.add( new RankingObject( theCatName, m_Table.m_Map.get(theCatName).size() ) );
			}

			java.util.Collections.sort(theRankingList);

			////////////////////////////////////////////////////////

			Map<String,List<ItemIF> >	theRankedMap = new LinkedHashMap<String,List<ItemIF> >();

			for ( RankingObject each : theRankingList)
			{
				theRankedMap.put( each.m_Key + " - " + m_Table.m_Map.get( each.m_Key ).size(), m_Table.m_Map.get( each.m_Key ));
			}

			s_Headlines_Logger.info("Ranked Map: " + theRankedMap);
*/		}
	}
}
