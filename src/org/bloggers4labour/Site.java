/*
 * Site.java
 *
 * Created on 22 March 2005, 19:08
 */

package org.bloggers4labour;

import com.hiatus.text.UText;
import de.nava.informa.core.CategoryIF;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.favicon.FaviconManager;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public final class Site implements SiteIF, Comparable<Site>	// (AGR) 6 June 2005. Now Serializable
{
	private transient ChannelIF	m_Channel;		// (AGR) 6 June 2005. Far too much data if we include Channel (+ Items)
	private transient ChannelIF	m_CommentsChannel;	// (AGR) 29 Nov 2005
	private long			m_SiteRecno;
	private String			m_Name;			// (AGR) 20 April 2005
	private String			m_SiteURL;
	private String			m_FeedURL;
	private String			m_DB_Category;
	private List<String>		m_Creators = null;
	private int			m_DB_CreatorStatusRecno;	// (AGR) 21 March 2006

	private transient String	m_FaviconLocation = null;
//	private transient boolean	m_GotFavicon = false;
	private transient String	m_DB_FaviconLocation;	// (AGR) 4 April 2005

	private final static List<String>	EMPTY_CREATORS_LIST = new ArrayList<String>();

	private static final long	serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public Site( final ChannelIF inChannel, final ChannelIF inCommentsChannel,
			long inRecno, String inName, String inSiteURL, String inFeedURL,
			int inCreatorStatusRecno, String inCat, String inDB_FaviconLocation)
	{
		m_Channel = inChannel;
		m_CommentsChannel = inCommentsChannel;			// (AGR) 29 Nov 2005
		m_SiteRecno = inRecno;
		m_Name = inName;					// (AGR) 20 April 2005
		m_SiteURL = inSiteURL;
		m_FeedURL = inFeedURL;
		m_DB_CreatorStatusRecno = inCreatorStatusRecno;		// (AGR) 21 March 2006
		m_DB_Category = inCat;
		m_DB_FaviconLocation = inDB_FaviconLocation;		// (AGR) 4 April 2005
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getRecno()
	{
		return m_SiteRecno;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelIF getChannel()
	{
		return m_Channel;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public ChannelIF getCommentsChannel()
	{
		return m_CommentsChannel;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getSiteURL()
	{
		return m_SiteURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getFeedURL()
	{
		return m_FeedURL;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDatabaseFeedURL()
	{
		return m_DB_FaviconLocation;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getFaviconLocation()
	{
		if ( m_FaviconLocation == null)
		{
			m_FaviconLocation = FaviconManager.getInstance().findURL(this);
		}

		return m_FaviconLocation;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals( Object inObj)
	{
		if ( inObj == null || !(inObj instanceof Site))	// (AGR) 29 Jan 2007, 26 May 2007. FindBugs made me add these tests!
		{
			return false;
		}

		//////////////////////////////////

		Site	other = (Site) inObj;

		return m_FeedURL.equals( other.m_FeedURL );
	}

	/*******************************************************************************
		(AGR) 29 Jan 2007
	
		FindBugs told me to fix this. It's entirely based around equals()
		above, which declares that the feedURL is all-important. I guess that's
		right!
	*******************************************************************************/
	@Override public int hashCode()
	{
		return m_FeedURL.hashCode();
	}

	/*******************************************************************************
		(AGR) 24 March 2005
	*******************************************************************************/
	public void addCreator( String inType)
	{
		if ( m_Creators == null)
		{
			m_Creators = new ArrayList<String>(2);
		}

		m_Creators.add(inType);
	}

	/*******************************************************************************
		(AGR) 24 March 2005
	*******************************************************************************/
	public Iterable<String> getCreators()
	{
		return new Iterable<String>()
		{
			public Iterator<String> iterator()
			{
				return ( m_Creators != null) ? m_Creators.iterator() : EMPTY_CREATORS_LIST.iterator();
			}
		};
	}

	/*******************************************************************************
		(AGR) 25 September 2006
	*******************************************************************************/
	public String getReducedCreatorsString( String inBaseURL)
	{
		return getCreatorsString( inBaseURL, false);
	}

	/*******************************************************************************
		(AGR) 24 March 2005
	*******************************************************************************/
	public String getCreatorsString( String inBaseURL)
	{
		return getCreatorsString( inBaseURL, true);
	}

	/*******************************************************************************
		(AGR) 24 March 2005
	*******************************************************************************/
	private String getCreatorsString( String inBaseURL, boolean inAddSurroundingCell)
	{
		StringBuilder	sb = new StringBuilder();
		String		imageName;
		boolean		gotOne = false;

		for ( String s : m_Creators)
		{
			if (s.startsWith("Former "))	// (AGR) 12 August 2006
			{
				continue;
			}
			else if (s.equalsIgnoreCase("MEP"))
			{
				imageName = "mep.png";
			}
			else if (s.equalsIgnoreCase("MP"))
			{
				imageName = "mp.png";
			}
			else if (s.indexOf("Councillor") >= 0)
			{
				imageName = "cllr.png";
			}
			else if (s.equalsIgnoreCase("AM"))
			{
				imageName = "am.png";
			}
			else
			{
				continue;
			}

			////////////////////////////////////////////////////////

			if (!gotOne)
			{
				if (inAddSurroundingCell)
				{
					sb.append("<td class=\"creator\">");
				}

				gotOne = true;
			}

			sb.append("<img src=\"").append(inBaseURL).append(imageName).append("\" alt=\"\" />");
		}

		if ( gotOne && inAddSurroundingCell)
		{
			sb.append("</td>");
		}

		return sb.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getCategoriesString( final ItemIF inItem)
	{
		StringBuilder	sb = new StringBuilder();

		// (AGR) 6 June 2005. Don't want to display this 'category' any more,
		// just the real ones specified in the feed.

/*		if (UText.isValidString(m_DB_Category))
		{
			sb.append(m_DB_Category);
		}
*/
		////////////////////////////////////////////

		boolean	addedBracket = false;

		if ( inItem != null)
		{
			Collection	theCatList = inItem.getCategories();
			boolean		isFirst = true;

			if ( theCatList != null)
			{
				for ( Iterator i = theCatList.iterator(); i.hasNext(); )
				{
					CategoryIF	theCat = (CategoryIF) i.next();

					if ( theCat != null)
					{
						String	theCatTitle = theCat.getTitle();

						if ( theCatTitle != null)	// (AGR) 5 July 2005
						{
							theCatTitle = theCatTitle.trim();
						}

						if (UText.isValidString(theCatTitle))
						{
							if (isFirst)
							{
								if ( sb.length() > 0)
								{
									sb.append(" (" + theCatTitle);
									addedBracket = true;
								}
								else
								{
									sb.append(theCatTitle);
								}

								isFirst = false;
							}
							else
							{
								sb.append(", " + theCatTitle);
							}
						}
					}
				}
			}
		}

		////////////////////////////////////////////

		if (addedBracket)
		{
			sb.append(")");
		}

		////////////////////////////////////////////

		return sb.toString();
	}

	/*******************************************************************************
		FIXME - tidy up
	*******************************************************************************/
	public String getDescriptionStyle( final ItemIF inItem)
	{
		return getDescriptionStyle( inItem, 0);
	}

	/*******************************************************************************
		(AGR) 30 September 2006. Added recommendations count arg

		FIXME - tidy up
	*******************************************************************************/
	public String getDescriptionStyle( final ItemIF inItem, final int inNumRecommendations)
	{
		////////////////////////////////////////////////  (AGR) 30 September 2006

		if ( inNumRecommendations > 5)
		{
			return "item-description-morevotes";
		}

		if ( inNumRecommendations >= 1)
		{
			return "item-description-" + inNumRecommendations + "votes";
		}

		////////////////////////////////////////////////  (AGR) 21 Jan 2007. Deactivated - not useful, just confusing. Look for RSS Categories

/*		if ( inItem != null)
		{
			final Collection	theCatList = inItem.getCategories();

			if ( theCatList != null)
			{
				for ( Object theCatObj : theCatList)
				{
					CategoryIF	theCat = (CategoryIF) theCatObj;
					String		theCatTitle = theCat.getTitle();

					if (UText.isValidString(theCatTitle))
					{
						if (theCatTitle.equalsIgnoreCase("news"))
						{
							return "item-description-news";
						}
					}
				}
			}
		}
*/
		////////////////////////////////////////////////  Check the DB Category

		if (UText.isValidString(m_DB_Category))
		{
			if (m_DB_Category.equalsIgnoreCase("news"))
			{
				return "item-description-news";
			}
		}

		////////////////////////////////////////////////  No idea...

		return "item-description";
	}

	/*******************************************************************************
		(AGR) 20 April 2005
	*******************************************************************************/
	public String getName()
	{
		return m_Name;
	}

	/*******************************************************************************
		(AGR) 4 March 2006
	*******************************************************************************/
	public int compareTo( Site inOther)
	{
		return ( m_SiteRecno < inOther.m_SiteRecno ? -1 : ( m_SiteRecno == inOther.m_SiteRecno ? 0 : 1));
	}

	/*******************************************************************************
		(AGR) 21 March 2006
	*******************************************************************************/
	public int getCreatorStatusRecno()
	{
		return m_DB_CreatorStatusRecno;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toMiniString()
	{
		return ( "[" + m_SiteRecno + "] " + m_Name);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return ( "[" + m_SiteRecno + "] " + m_Name + " (" + m_DB_Category + "), Channel = " + m_Channel);
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getMaximumPostsToAggregate()
	{
		if ( m_FeedURL != null)
		{
			if (m_FeedURL.equals("http://www.labourmatters.com/category/Labour%20Party%20News/feed"))
			{
				return 3;
			}

			if (m_FeedURL.equals("http://www.labourmatters.com/category/Featured/feed"))
			{
				return 1;
			}
		}

		return Integer.MAX_VALUE;
	}
}
