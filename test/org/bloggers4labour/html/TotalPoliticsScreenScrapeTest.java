/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewregan
 */
public class TotalPoliticsScreenScrapeTest
{
	/*******************************************************************************
	*******************************************************************************/
	public void testLabourBlogScrape()
	{
		testPoliticalPartyScrape( 2, false);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Test public void testToryBlogScrape()
	{
		testPoliticalPartyScrape( 1, false);
	}

	/*******************************************************************************
	*******************************************************************************/
	private void testPoliticalPartyScrape( int inPartyId, boolean inTryToSubmitToB4L)
	{
		NodeFilter		theFilter = new AndFilter( new TagNameFilter("A"), new HasParentFilter( new TagNameFilter("DIV") ));
		Collection<BlogEntry>	theColl = new ArrayList<BlogEntry>();
		int			startIndex = 0;

		while (true)
		{
			try
			{
				String		theURL = "http://www.totalpolitics.com/politicalblogs/blogs.php?party_id=" + inPartyId + "&start=" + startIndex;

				System.out.println("Requesting: " + theURL + " (count = " + theColl.size() + ")");

				Parser		p = new Parser(theURL);
				NodeList	nl = p.extractAllNodesThatMatch(theFilter);
				Node[]		nArray = nl.toNodeArray();
				int		blogsFound = 0;

				for ( Node eachNode : nArray)
				{
					if ( eachNode instanceof LinkTag)
					{
						LinkTag	theLink = (LinkTag) eachNode;
						String	theLinkURL = theLink.getLink();

						if (theLinkURL.equals("http://www.thumbshots.net"))
						{
							continue;
						}

						theColl.add( new BlogEntry( theLink.getLinkText().trim(), theLinkURL) );

						blogsFound++;
					}
				}

				if ( blogsFound == 0)	// None found on this page? Stop searching.
				{
					break;
				}

				startIndex += 24;
			}
			catch (ParserException ex)
			{
				ex.printStackTrace();
				break;
			}
		}

		////////////////////////////////////////////////////////////////

		System.out.println(theColl);

		if (inTryToSubmitToB4L)
		{
			for ( BlogEntry each : theColl)
			{
				try
				{
					String	theURL = each.toURL();

					_requestURL(theURL);
				}
				catch (UnsupportedEncodingException e)
				{
					// e.printStackTrace();
				}
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _requestURL( final String inURL)
	{
		InputStream	theStream = null;
		URL		theURL = null;

		try
		{
			theURL = new URL(inURL);

			System.out.println("Connecting to: " + theURL);

			URLConnection	theConn = theURL.openConnection();

			System.out.println("Connected: " + theConn);

			theConn.setConnectTimeout( 30 * (int) ONE_SECOND_MSECS);
			theConn.setReadTimeout( 30 * (int) ONE_SECOND_MSECS);
			theConn.connect();

			theConn.getInputStream();
		}
		catch (SocketTimeoutException se)
		{
			se.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( theStream != null)
			{
				try
				{
					theStream.close();
				}
				catch (IOException e2)
				{
					e2.printStackTrace();
				}
				finally
				{
					theStream = null;
				}
			}

			// System.out.println("lookupLocation() took " + UDates.getFormattedTimeDiff( System.currentTimeMillis() - startTimeMSecs));
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class BlogEntry
	{
		private String			name;
		private String			siteURL;

		private final static String	FEED_SECTION = "&findFeed=ALL_POSTS&errorIfFeedFindFails=0";

		private final static String	DUMMY_FNAMES = "???";
		private final static String	DUMMY_SNAME = "???";
		private final static String	DUMMY_EMAIL = "???";
		private final static String	DUMMY_DESC = "";
		private final static int	DUMMY_STATUS_RECNO = 9;
		private final static int	DUMMY_LOC_RECNO = 19;
		private final static int	DUMMY_SITE_CAT_RECNO = 1;

		/*******************************************************************************
		*******************************************************************************/
		public BlogEntry( String x, String y)
		{
			name = x;
			siteURL = y;
		}

		/*******************************************************************************
		*******************************************************************************/
		public String toURL() throws UnsupportedEncodingException
		{
			return "http://www.bloggers4labour.org/api/submitBlog/" +
//			return "http://localhost:8080/b4l/api/submitBlog/" +
						"?outputFormat=XML&blog_name=" + URLEncoder.encode( name, "UTF-8") +
						"&blog_url=" + URLEncoder.encode( siteURL, "UTF-8") +
						"&fnames=" + DUMMY_FNAMES +
						"&sname=" + DUMMY_SNAME +
						"&email=" + DUMMY_EMAIL +
						"&blog_descr=" + DUMMY_DESC +
						"&status=" + DUMMY_STATUS_RECNO + "&location=" + DUMMY_LOC_RECNO + "&site_category=" + DUMMY_SITE_CAT_RECNO + FEED_SECTION;

		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public String toString()
		{
			return "[" + name + " @ <" + siteURL + ">]";
		}
	}
}
