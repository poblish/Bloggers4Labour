/*
 * FaviconManager.java
 *
 * Created on 25 February 2006, 16:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.favicon;

import com.hiatus.text.UText;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bloggers4labour.Site;

/**
 *
 * @author andrewre
 */
public class FaviconManager
{
	private List<FeedsRec>		m_List = new CopyOnWriteArrayList<FeedsRec>();

	private final static String	DUMMY_FAVICON_URL = "#";

	/*******************************************************************************
	*******************************************************************************/
	private FaviconManager()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public String findURL( Site inSiteObj)
	{
		String	result = _lookUpURL( inSiteObj.getSiteURL() );

		return result.equals(DUMMY_FAVICON_URL) ? null : result;	// (AGR) 29 Jan 2007. FindBugs recommended this!
	}

	/*******************************************************************************
	*******************************************************************************/
	private String _lookUpURL( String inSiteURL)
	{
		int	theFoundIndex = m_List.indexOf( new FeedsRec(inSiteURL) );

		return ( theFoundIndex >= 0) ? m_List.get(theFoundIndex).faviconURL : null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void rememberFavicon( Site inSiteObj)
	{
		String	theSiteURL = inSiteObj.getSiteURL();
		String	theDBFeedURL = inSiteObj.getDatabaseFeedURL();

//		synchronized (this)
		{
/*			if ( m_Map.get(theSiteURL) != null)
			{
				return false;
			}

			if (m_List.contains( new FeedsRec(theSiteURL) ))
			{
				return false;
			}
*/
			////////////////////////////////////////////////////////

			if (UText.isValidString(theDBFeedURL))		// (AGR) 4 April 2005
			{
				_storeURL( theSiteURL, theDBFeedURL);
				return;
			}
			else if (theSiteURL.contains("blogspot."))	// (AGR) 23 Feb 2006. This was done purely for HUGE performance reasons. Ideally we'd search individually...
			{
				_storeURL( theSiteURL, "http://www.blogger.com/favicon.ico");
				return;
			}
			else if (theSiteURL.contains(".typepad."))	// (AGR) 23 Feb 2006. Ditto
			{
				_storeURL( theSiteURL, "http://www.typepad.com/favicon.ico");
				return;
			}

			////////////////////////////////////////////////////////  OK, now we have to *search* for this one...

			while ( _lookUpURL(theSiteURL) == null)
			{
				_searchForFavicon(theSiteURL);

				return;
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _searchForFavicon( String inSiteURL)
	{
		// s_OurLogger.info("@@@ Trying: " + inSiteURL);

		try
		{
			URL	theBasicURL = new URL(inSiteURL);
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
				return _storeURL( inSiteURL, theIconURL.toString());
			}
			else
			{
				// s_Site_Logger.info("returned \"" + theCT + "\", len = " + theConn.getContentLength() + " @ " + theConn.getURL());
			}

			// return m_GotFavicon;
		}
		catch (IOException e)
		{
			// s_Site_Logger.error("Err: " + e);
		}

		return _storeURL( inSiteURL, DUMMY_FAVICON_URL);
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean _storeURL( String inSiteURL, String inFaviconURL)
	{
		while ( _lookUpURL(inSiteURL) == null)
		{
/*			if ( inFaviconURL != DUMMY_FAVICON_URL)
			{
				// s_OurLogger.info("@@@ Storing Favicon \"" + inFaviconURL + "\" for site \"" + inSiteURL + "\". size = " + m_List.size());
			}
*/
			m_List.add( new FeedsRec( inSiteURL, inFaviconURL) );
			return true;
		}

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	private String _requestFavicon( URL inURL) throws IOException
	{
		URLConnection	theConn = inURL.openConnection();

		theConn.setRequestProperty("User-Agent", "mozilla");
		theConn.setRequestProperty("Pragma", "no-cache");
		theConn.connect();

//		int	contentLen = theConn.getContentLength();	// bit dodgy, but assume -1 (unknown) means there's a > 0 % chance!

		return theConn.getContentType();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static FaviconManager getInstance()
	{
		return LazyHolder.s_Mgr;
	}

	/*******************************************************************************
		(AGR) 5 June 2005. See:
		    <http://www-106.ibm.com/developerworks/java/library/j-jtp03304/>
	*******************************************************************************/
	private static class LazyHolder
	{
		private static FaviconManager	s_Mgr = new FaviconManager();
	}

	/*******************************************************************************
	*******************************************************************************/
	static class FeedsRec
	{
		String	siteURL;
		String	faviconURL;

		/*******************************************************************************
		*******************************************************************************/
		public FeedsRec( String inSiteURL)
		{
			siteURL = inSiteURL;
		}

		/*******************************************************************************
		*******************************************************************************/
		public FeedsRec( String inSiteURL, String inFaviconURL)
		{
			siteURL = inSiteURL;
			faviconURL = inFaviconURL;
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public boolean equals( Object inOther)
		{
			if ( inOther == null || !( inOther instanceof FeedsRec))	// (AGR) 29 Jan 2007. FindBugs made me add this test!
			{
				return false;
			}

			return siteURL.equals( ((FeedsRec) inOther).siteURL );
		}

		/*******************************************************************************
			(AGR) 29 Jan 2007

			FindBugs told me to fix this. It's entirely based around equals()
			above, which declares that the siteURL is all-important. I guess that's
			right!
		*******************************************************************************/
		@Override public int hashCode()
		{
			return siteURL.hashCode();
		}
	}
}
