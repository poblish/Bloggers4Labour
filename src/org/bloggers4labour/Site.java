/*
 * Site.java
 *
 * Created on 22 March 2005, 19:08
 */

package org.bloggers4labour;

import com.hiatus.UText;
import de.nava.informa.core.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.bloggers4labour.favicon.FaviconManager;

/**
 *
 * @author andrewre
 */
public final class Site implements Serializable, Comparable<Site>	// (AGR) 6 June 2005. Now Serializable
{
	private transient ChannelIF	m_Channel;		// (AGR) 6 June 2005. Far too much data if we include Channel (+ Items)
	private transient ChannelIF	m_CommentsChannel;	// (AGR) 29 Nov 2005
	private long			m_SiteRecno;
	private String			m_Name;			// (AGR) 20 April 2005
	private String			m_SiteURL;
	private String			m_FeedURL;
	private String			m_DB_Category;
	private List<String>		m_Creators;
	private int			m_DB_CreatorStatusRecno;	// (AGR) 21 March 2006

	private transient String	m_FaviconLocation = null;
//	private transient boolean	m_GotFavicon = false;
	private transient String	m_DB_FaviconLocation;	// (AGR) 4 April 2005

	private static Logger		s_Site_Logger = Logger.getLogger("Main");

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
	public boolean equals( Object inObj)
	{
		Site	other = (Site) inObj;

		return m_FeedURL.equals( other.m_FeedURL );
	}

	/*******************************************************************************
	*******************************************************************************
	public boolean findFavicon()
	{
		if (UText.isValidString(m_DB_FaviconLocation))	// (AGR) 4 April 2005
		{
			m_GotFavicon = true;
			m_FaviconLocation = m_DB_FaviconLocation;
			return true;
		}

		if (UText.isValidString(m_SiteURL))
		{
			if (m_SiteURL.contains("blogspot."))	// (AGR) 23 Feb 2006. This was done purely for HUGE performance reasons. Ideally we'd search individually...
			{
				m_GotFavicon = true;
				m_FaviconLocation = "http://www.blogger.com/favicon.ico";
				return true;
			}

			if (m_SiteURL.contains(".typepad."))	// (AGR) 23 Feb 2006. Ditto
			{
				m_GotFavicon = true;
				m_FaviconLocation = "http://www.typepad.com/favicon.ico";
				return true;
			}
		}

		return _searchForFavicon();
	}/

	/*******************************************************************************
	*******************************************************************************
	private boolean _searchForFavicon()
	{
		// s_Site_Logger.info("===> Trying: " + m_SiteURL);

		try
		{
			URL	theBasicURL = new URL(m_SiteURL);
			String	theBasicFile = theBasicURL.getFile();
			String	theNewFileStr;

			// s_Site_Logger.info("theBasicFile \"" + theBasicFile + "\"");

			if (theBasicFile.endsWith("/"))
			{
				theNewFileStr = theBasicFile;
			}
			else
			{
				int	sp = theBasicFile.lastIndexOf('/');
				String	token;
				String	pfx;

				// s_Site_Logger.info("   sp = " + sp);

				if ( sp > 0)
				{
					token = theBasicFile.substring(sp);
					pfx = theBasicFile.substring(0,sp);
					// s_Site_Logger.info("   token.1 \"" + token + "\"");
					// s_Site_Logger.info("   pfx.1 \"" + pfx + "\"");
				}
				else
				{
					token = theBasicFile;
					pfx = "";
					// s_Site_Logger.info("   token.2 \"" + token + "\"");
					// s_Site_Logger.info("   pfx.2 \"" + pfx + "\"");
				}

				if ( token.indexOf('.') > 0)
				{
					theNewFileStr = pfx + "/";
				}
				else
				{
					theNewFileStr = theBasicFile + "/";
				}
			}

			// s_Site_Logger.info("theNewFileStr \"" + theNewFileStr + "\"");

			URL	theIconURL = new URL( theBasicURL.getProtocol(), theBasicURL.getHost(), theNewFileStr + "favicon.ico");
		//	s_Site_Logger.info("theIconURL \"" + theIconURL + "\"");
		//	s_Site_Logger.info("getFile() \"" + theIconURL.getFile() + "\"");

			// s_Site_Logger.info("URL: " + theURL);

			String	theCT = _requestFavicon(theIconURL);

			if ( UText.isValidString(theCT) && theCT.equals("image/x-icon"))
			{
				m_GotFavicon = true;
				m_FaviconLocation = theIconURL.toString();
			}
			else
			{
				// s_Site_Logger.info("returned \"" + theCT + "\", len = " + theConn.getContentLength() + " @ " + theConn.getURL());
			}

			return m_GotFavicon;
		}
		catch (Exception e)
		{
			// s_Site_Logger.error("Err: " + e);
		}

		return false;
	}/

	/*******************************************************************************
	*******************************************************************************
	private String _requestFavicon( URL inURL) throws IOException
	{
		URLConnection	theConn = inURL.openConnection();

		theConn.setRequestProperty("User-Agent", "mozilla");
		theConn.setRequestProperty("Pragma", "no-cache");
		theConn.connect();

//		int	contentLen = theConn.getContentLength();	// bit dodgy, but assume -1 (unknown) means there's a > 0 % chance!

		return theConn.getContentType();
	}/

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
	public Iterator getCreators()
	{
		return ( m_Creators != null) ? m_Creators.iterator() : null;
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
	public String toString()
	{
		return ( "[" + m_SiteRecno + "] " + m_Name + " (" + m_DB_Category + "), Channel = " + m_Channel);
	}
}
