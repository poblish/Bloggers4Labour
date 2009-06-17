/*
 * MainServlet.java
 *
 * Created on 12 March 2005, 01:31
 */

package org.bloggers4labour;

import com.hiatus.dates.UDates;
import com.hiatus.encoding.CharEncoding;
import com.hiatus.htl.HTL;
import com.hiatus.htl.HTLTemplate;
import com.hiatus.html.UHTML;
import com.hiatus.locales.ULocale2;
import com.hiatus.sql.USQL_Utils;
import com.hiatus.text.UText;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.bloggers4labour.ajax.OutputBuilderIF;
import org.bloggers4labour.ajax.OutputElementIF;
import org.bloggers4labour.ajax.impl.JSONOutputBuilder;
import org.bloggers4labour.ajax.impl.XMLOutputBuilder;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.headlines.HeadlinesIF;
import org.bloggers4labour.htl.B4LHTLContext;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.index.SearchMatch;
import org.bloggers4labour.jsp.DisplayItem;
import org.bloggers4labour.jsp.Displayable;
import org.bloggers4labour.recommend.RecommendationHandler;
import org.bloggers4labour.recommend.RecommendationResult;
import org.bloggers4labour.site.SiteIF;
import org.bloggers4labour.sql.DataSourceConnection;

/**
 *
 * @author andrewre
 */
public class MainServlet extends HttpServlet
{
	private transient Launcher	m_Launcher;

	private static Logger		s_Servlet_Logger;
	private static String[]		s_LuceneSearchArray = {"desc","title"};

	private static final long	serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	@Override public void init( ServletConfig inConfig)
	{
		try
		{
			super.init(inConfig);

			String	theParam = inConfig.getInitParameter("properties_file");	// (AGR) 16 August 2008
			if (UText.isValidString(theParam))
			{
				PropertyConfigurator.configure(theParam);
			}
			else
			{
			PropertyConfigurator.configure("/home/htdocs/WEB-INF/bio.properties");
			}

			s_Servlet_Logger = Logger.getLogger( MainServlet.class );

			////////////////////////////////////////

			s_Servlet_Logger.info("===============================");
			s_Servlet_Logger.info("In Servlet: " + this);

			////////////////////////////////////////  (AGR) 22 June 2005

			theParam = inConfig.getInitParameter("lucene_index_dir");
			if (UText.isValidString(theParam))
			{
				IndexMgr.setDirectory(theParam);
			}

			////////////////////////////////////////  (AGR) 9 July 2005

			theParam = inConfig.getInitParameter("config_dir");
			if (UText.isValidString(theParam))
			{
				Configuration.getInstance().setDirectoryIfNotSet(theParam);
			}

			////////////////////////////////////////

			m_Launcher = new Launcher(s_Servlet_Logger);
			m_Launcher.start();
		}
		catch (ServletException ex)
		{
			s_Servlet_Logger.error( "init error", ex);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public void doGet( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		CharSequence	theOutputBuffer = null;
		boolean		keepAlive = true;

		try
		{
			if (hasParameter( inRequest, "opml"))
			{
				inResponse.setLocale( GetClientLocale(inRequest) );
				inResponse.setContentType("text/xml; charset=\"UTF-8\"");

				////////////////////////////////////////

				String	s = InstallationManager.getDefaultInstallation().getFeedList().getOPMLOutputStr();

				theOutputBuffer = ( s != null) ? new StringBuffer(s) : null;
			}
			else if (hasParameter( inRequest, "rss"))	// (AGR) 15 April 2005
			{
				inResponse.setLocale( GetClientLocale(inRequest) );
				inResponse.setContentType("text/xml; charset=\"UTF-8\"");

				////////////////////////////////////////////////

				InstallationIF	theInstall = getInstallFromParameter(inRequest);	// (AGR) 2 October 2008
				String		theOneWeWant = inRequest.getParameter("rss");
				String		theIncludedSitesParam = inRequest.getParameter("includedsites");  // (AGR) 6 June 2006
				String		theExcludedSitesParam = inRequest.getParameter("excludedsites");  // (AGR) 6 June 2006
				HeadlinesIF	theRightHeads;
				String		theStringToUse = null;

				if ( UText.isNullOrBlank(theOneWeWant) || theOneWeWant.equals("true"))	// (AGR) 25 March 2006
				{
					theRightHeads = theInstall.getHeadlinesMgr().getMainRSSFeedInstance();
					if ( theRightHeads != null)
					{
						if (UText.isValidString(theIncludedSitesParam))
						{
							theStringToUse = theRightHeads.publishSnapshot_Included( theRightHeads.toArray(), theIncludedSitesParam);
						}
						else if (UText.isValidString(theExcludedSitesParam))
						{
							theStringToUse = theRightHeads.publishSnapshot_Excluded( theRightHeads.toArray(), theExcludedSitesParam);
						}
						else
						{
							theStringToUse = theRightHeads.getHeadlinesXMLString();
						}
					}
				}
				else	// (AGR) 25 March 2006
				{
					theRightHeads = theInstall.getHeadlinesMgr().findHeadlines(theOneWeWant);
					if ( theRightHeads != null)
					{
						theRightHeads.publishSnapshot();

						if (UText.isValidString(theIncludedSitesParam))
						{
							theStringToUse = theRightHeads.publishSnapshot_Included( theRightHeads.toArray(), theIncludedSitesParam);
						}
						else if (UText.isValidString(theExcludedSitesParam))
						{
							theStringToUse = theRightHeads.publishSnapshot_Excluded( theRightHeads.toArray(), theExcludedSitesParam);
						}
						else
						{
							theStringToUse = theRightHeads.getHeadlinesXMLString();
						}
					}
				}

				////////////////////////////////////////////////

				theOutputBuffer = ( theStringToUse != null) ? new StringBuffer(theStringToUse) : null;
			}
			else if (hasParameter( inRequest, "search"))		// (AGR) 17 July 2005. Ok, can be GET too
			{
				theOutputBuffer = handleSearch( inRequest, inResponse);
			}
			else if (hasParameter( inRequest, "dsw"))		// (AGR) 20 September 2005
			{
				theOutputBuffer = handleDSW( inRequest, inResponse).toString();
				// s_Servlet_Logger.info("... theOutputBuffer = \"" + theOutputBuffer + "\"");
				// keepAlive = false;
			}
/*			else if (hasParameter( inRequest, "hatemytory"))	// (AGR) 14 October 2006
			{
				theOutputBuffer = handleHateMyTory( inRequest, inResponse).toString();
			}
*/			else if (hasParameter( inRequest, "recommendations"))	// (AGR) 31 August 2006
			{
				theOutputBuffer = handleRecommendations( inRequest, inResponse).toString();
			}
			else if (hasParameter( inRequest, "recommend"))		// (AGR) 20 June 2006. Value is a URL...
			{
				try
				{
					InstallationIF		defInstall = InstallationManager.getDefaultInstallation();
					RecommendationHandler	rh = new RecommendationHandler( defInstall.getDataSource() );
					RecommendationResult	result;
					long			theSiteRecno;

					try
					{
						theSiteRecno = Long.parseLong( inRequest.getParameter("site"));
					}
					catch (Exception e)
					{
						// s_Servlet_Logger.error("recommend site...", e);
						theSiteRecno = -1L;
					}

					result = rh.handleRequest( inRequest.getSession(true).getId(),
								   inRequest.getParameter("recommend"),
								   theSiteRecno);

					ensureNotCached(inResponse);

					inResponse.setLocale( GetClientLocale(inRequest) );
					inResponse.setContentType("text/xml; charset=\"UTF-8\"");

					StringBuffer	sb = new StringBuffer("<?xml version=\"1.0\" ?><response><status>");

					switch (result.getStatus())
					{
						case OK:		sb.append("OK");		break;
						case DUPLICATE:		sb.append("DUPLICATE");		break;
						case BAD_INPUTS:	sb.append("BAD_INPUTS");	break;
						case ERROR:		sb.append("ERROR");		break;
						case UNKNOWN_SITE:	sb.append("UNKNOWN_SITE");	break;	// (AGR) 11 July 2006
					}

					sb.append("</status><votes>").append( result.getVoteCount() ).append("</votes></response>");
					theOutputBuffer = sb;
				}
				catch (Exception e)
				{
					s_Servlet_Logger.error("recommend...", e);

					goHome( inRequest, inResponse);
					theOutputBuffer = null;
				}
			}
			else if (hasParameter( inRequest, "headlines"))		// (AGR) 23 September 2006
			{
				try
				{
					int	theNumPosts = 3;

					try
					{
						theNumPosts = Integer.parseInt( inRequest.getParameter("posts"));
					}
					catch (Exception e) { }

					//////////////////////////////////////////////////////////////////////////////////////

					Locale	theLocale = GetClientLocale(inRequest);

					ensureNotCached(inResponse);

					inResponse.setLocale(theLocale);

					//////////////////////////////////////////////////////////////////////////////////////

					String			theOutputFormat = inRequest.getParameter("format");
					OutputBuilderIF		theOutputBuilder;

					if ( theOutputFormat != null && theOutputFormat.equalsIgnoreCase("JSON"))
					{
						theOutputBuilder = new JSONOutputBuilder( "response", inRequest.getParameter("jsonCallback"));
					}
					else
					{
						theOutputBuilder = new XMLOutputBuilder("response");
					}

					theOutputBuilder.setContentType(inResponse);

					//////////////////////////////////////////////////////////////////////////////////////  (AGR) 29 September 2008

					InstallationIF	theInstallation = getInstallFromParameter(inRequest);

					//////////////////////////////////////////////////////////////////////////////////////

					HeadlinesMgr	theHMgr = theInstallation.getHeadlinesMgr();
					HeadlinesIF	theHs = theHMgr.getRecentPostsInstance();
					long		currentTimeMSecs = System.currentTimeMillis();

					theOutputBuilder.addCDataElement( "currentTime", ULocale2.getClientDateTimeFormat( theLocale, DateFormat.LONG).format( new java.util.Date() ));

					if ( theHs != null)
					{
						Map<String,Number>	theRecommendationCountMap = null;	// (AGR) 1 October 2006
						ItemIF[]		headlineItemsArray = theHs.toArray();
						int			actualNumPosts = theNumPosts > headlineItemsArray.length ? headlineItemsArray.length : theNumPosts;

						if ( actualNumPosts > 0)	// (AGR) 1 October 2006
						{
							DataSourceConnection	theConnectionObject = null;

							try
							{
								theConnectionObject = new DataSourceConnection( theInstallation.getDataSource() );
								if (theConnectionObject.Connect())
								{
									Statement	theS = null;

									try
									{
										theS = theConnectionObject.createStatement();
										theRecommendationCountMap = Headlines.getRecommendationCountsMap( theS.executeQuery( Headlines.getRecommendationCountsQuery( headlineItemsArray, actualNumPosts) ) );
									}
									catch (SQLException e)
									{
										s_Servlet_Logger.error("creating statement", e);
									}
									finally
									{
										USQL_Utils.closeStatementCatch(theS);
									}
								}
								else
								{
									s_Servlet_Logger.warn("Cannot connect!");
								}
							}
							catch (RuntimeException err)
							{
								s_Servlet_Logger.error("???", err);
							}
							finally
							{
								if ( theConnectionObject != null)
								{
									theConnectionObject.CloseDown();
								}
							}
						}

						///////////////////////////////////////////////////////////////////

						DisplayItem	di;
						SiteIF		theSiteObj;
						int		theRecommendationsCount;

						OutputElementIF	theHeadlinesWrapperElem = theOutputBuilder.newWrapperElement("headlines");

						for ( int z = 0; z < actualNumPosts; z++)
						{
							di = new DisplayItem( theInstallation, headlineItemsArray[z], currentTimeMSecs);
							theSiteObj = di.getSite();

							///////////////////////////////////////////////////////////////////  (AGR) 1 October 2006

							theRecommendationsCount = 0;

							if ( di.getLink() != null)	// (AGR) 28 October 2006. Something wrong with Warren Morgan's blog produces null links. So test here.
							{
								String	theLinkStr = di.getLink().toString();

								if ( theRecommendationCountMap != null)
								{
									try
									{
										theRecommendationsCount = theRecommendationCountMap.get(theLinkStr).intValue();
									}
									catch (Exception e)
									{
									}
								}
							}

							///////////////////////////////////////////////////////////////////

							Map<String,Object>	theAttrs = new HashMap<String,Object>();
							theAttrs.put( "index", z);

							OutputElementIF	theHeadlineElem = theOutputBuilder.newElement( "headline", theAttrs);

							theHeadlineElem.addDisplayable( di, theSiteObj, theRecommendationsCount);
							theHeadlineElem.addElement( "descStyle", di.getDescriptionStyle(theRecommendationsCount));
							theHeadlineElem.addCDataElement( "creator", di.getReducedCreatorsStr());

							theHeadlinesWrapperElem.add(theHeadlineElem);
						}

						theOutputBuilder.add(theHeadlinesWrapperElem);
					}

					theOutputBuffer = theOutputBuilder.complete().toString();
				}
				catch (Exception e)
				{
					s_Servlet_Logger.error("Headlines...", e);

					goHome( inRequest, inResponse);
					theOutputBuffer = null;
				}
			}
			else if (hasParameter( inRequest, "cricket"))		// (AGR) 25 October 2006
			{
				Locale	theLocale = GetClientLocale(inRequest);

				ensureNotCached(inResponse);

				inResponse.setLocale(theLocale);

				//////////////////////////////////////////////////////////////////////////////////////

				try
				{
					if (inRequest.getParameter("cricket").equals("initialise"))	// (AGR) 1 November 2006
					{
						theOutputBuffer = handleCricketInitialisation( inRequest, inResponse, theLocale);
					}
					else
					{
	//					int	theNumPosts = 1;

						inResponse.setContentType("text/xml; charset=\"UTF-8\"");

						//////////////////////////////////////////////////////////////////////////////////////

						InstallationIF		defInstall = InstallationManager.getInstallation("cricket");
						HeadlinesMgr		theHMgr = defInstall.getHeadlinesMgr();
						HeadlinesIF		theHs = theHMgr.getRecentPostsInstance();
						StringBuilder		sb = new StringBuilder("<?xml version=\"1.0\" ?><response>");
						long			currentTimeMSecs = System.currentTimeMillis();

						_addXMLCDataElement( sb, "currentTime", ULocale2.getClientDateTimeFormat( theLocale, DateFormat.LONG).format( new java.util.Date() ));

						if ( theHs != null)
						{
							ItemIF[]	headlineItemsArray = theHs.toArray();
	//						int		actualNumPosts = theNumPosts > headlineItemsArray.length ? headlineItemsArray.length : theNumPosts;

							///////////////////////////////////////////////////////////////////

							DisplayItem				di;
							String					theTitleStr;
							org.bloggers4labour.cricket.Score	theScore;

	//						for ( int z = 0; z < actualNumPosts; z++)
							for ( int z = 0; z < headlineItemsArray.length; z++)
							{
								di = new DisplayItem( defInstall, headlineItemsArray[z], currentTimeMSecs);

								theTitleStr = di.getDescription();	// getDispTitle();
								theScore = org.bloggers4labour.cricket.Score.parse( theTitleStr );

								sb.append("<score index=\"").append(z).append("\">");

								_addXMLCDataElement( sb, "link", di.getLink());
								_addXMLElement( sb, "batting", theScore.getBattingTeam());
								_addXMLElement( sb, "score", theScore.getCurrentScore());
								_addXMLElement( sb, "fielding", theScore.getFieldingTeam());
								_addXMLElement( sb, "lastScore", theScore.getLastScore());
								_addXMLElement( sb, "display", theScore.toString());

								sb.append("</score>");
							}
						}

						theOutputBuffer = sb.append("</response>").toString();
					}
				}
				catch (Exception e)
				{
					s_Servlet_Logger.error("Scores...", e);

					goHome( inRequest, inResponse);
					theOutputBuffer = null;
				}
			}
			else
			{
				goHome( inRequest, inResponse);
				theOutputBuffer = null;
			}
		}
		catch (Exception e)
		{
			s_Servlet_Logger.error("doGet", e);
		}
		finally
		{
			myOutputBuffer( inResponse, theOutputBuffer, keepAlive);
			Finish(inResponse);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private InstallationIF getInstallFromParameter( final HttpServletRequest inRequest)
	{
		String	theInstallationName = inRequest.getParameter("install");

		if (UText.isNullOrBlank(theInstallationName))
		{
			return InstallationManager.getDefaultInstallation();
		}
		else
		{
			return InstallationManager.getInstance().get(theInstallationName);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void goHome( HttpServletRequest inRequest, HttpServletResponse inResponse) throws IOException
	{
		setResponseLocaleAndUse_HTML( inRequest, inResponse);

		inResponse.sendRedirect( inResponse.encodeRedirectURL("http://www.bloggers4labour.org/") );
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void ensureNotCached( HttpServletResponse ioResponse)
	{
		ioResponse.addHeader("Cache-Control","must-revalidate");
		ioResponse.addHeader("Cache-Control","no-cache");
		ioResponse.setHeader("Cache-Control","no-store");	// HTTP 1.1
		ioResponse.setHeader("Pragma","no-cache");		// HTTP 1.0
		ioResponse.setDateHeader("Expires", -1 /* 0 */);	// prevents caching at the proxy server
	}

	/*******************************************************************************
		(AGR) 13 July 2005
	*******************************************************************************/
	@Override public void doPost( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		StringBuffer	theOutputBuffer = null;
		boolean		keepAlive = true;

		try
		{
			if (hasParameter( inRequest, "search"))
			{
				handleSearch( inRequest, inResponse);
			}
		}
		catch (IOException e)
		{
			s_Servlet_Logger.error("doPost", e);
		}
		finally
		{
			myOutputBuffer( inResponse, theOutputBuffer, keepAlive);
			Finish(inResponse);
		}
	}

	/*******************************************************************************
		(AGR) 17 July 2005
	*******************************************************************************/
	private String handleSearch( HttpServletRequest inRequest, HttpServletResponse inResponse) throws IOException
	{
// s_Servlet_Logger.info( inRequest.getMethod() + " query = " + inRequest.getQueryString());

		HttpSession	theSsn = inRequest.getSession();
		String		theQueryStr = inRequest.getParameter("q");

		s_Servlet_Logger.info("handleSearch() q = \"" + theQueryStr  + "\"");

		boolean		gotQuery = UText.isValidString(theQueryStr);
		String		theWhereStr = inRequest.getParameter("where");
//		boolean		gotTextVal;
//		boolean		gotTitlesVal;
		boolean		gotBothVal;
		Analyzer	theAnalyzer = new StandardAnalyzer();
		Query		theQuery = null;
		boolean		isNewSession = theSsn.isNew();

		try
		{
			if ( isNewSession || UText.isNullOrBlank(theWhereStr))
			{
//				gotTextVal = false;
//				gotTitlesVal = false;
				gotBothVal = true;
			}
			else if (theWhereStr.equals("text"))
			{
//				gotTextVal = true;
//				gotTitlesVal = false;
				gotBothVal = false;

				if (gotQuery)
				{
					theQuery = new QueryParser( "desc", theAnalyzer).parse(theQueryStr);
				}
			}
			else if (theWhereStr.equals("titles"))
			{
//				gotTitlesVal = true;
//				gotTextVal = false;
				gotBothVal = false;

				if (gotQuery)
				{
					theQuery = new QueryParser( "title", theAnalyzer).parse(theQueryStr);
				}
			}
			else
			{
//				gotTextVal = false;
//				gotTitlesVal = false;
				gotBothVal = true;
			}
		}
		catch (org.apache.lucene.queryParser.ParseException e)
		{
			gotQuery = gotBothVal = false;
		}

		////////////////////////////////////////////////

		if ( gotBothVal && gotQuery)
		{
			try
			{
				QueryParser	theParser = new MultiFieldQueryParser( s_LuceneSearchArray, theAnalyzer);	// (AGR) Lucene 1.9

				theQuery = theParser.parse(theQueryStr);							// (AGR) Lucene 1.9
			}
			catch (org.apache.lucene.queryParser.ParseException e)
			{
			}
		}

		////////////////////////////////////////////////

		InstallationIF		theInstall;
		List<SearchMatch>	theSearchResults;

		if ( theQuery != null)
		{
			s_Servlet_Logger.info("Search2: " + theQuery);

			theInstall = InstallationManager.getDefaultInstallation();
			theSearchResults = theInstall.getIndexMgr().runQuery(theQuery);
		}
		else	theSearchResults = null;

		////////////////////////////////////////////////  (AGR) 10 October 2006

		String	theDispositionStr = inRequest.getParameter("disp");

		if ( theDispositionStr != null && theDispositionStr.equals("xml"))
		{
			////////////////////////////////////////////////  (AGR) 11 October 2006. Calculate recommendation counts

			DataSourceConnection	theConnectionObject = null;
			Map<String,Number>	theRecommendationCountMap = null;
			String			theReccQueryStr = Headlines.getSearchRecommendationCountsQuery(theSearchResults);

			if (UText.isValidString(theReccQueryStr))
			{
				try
				{
					InstallationIF		defInstall = InstallationManager.getDefaultInstallation();

					theConnectionObject = new DataSourceConnection( defInstall.getDataSource() );
					if (theConnectionObject.Connect())
					{
						Statement	theS = null;

						try
						{
							theS = theConnectionObject.createStatement();
							theRecommendationCountMap = Headlines.getRecommendationCountsMap( theS.executeQuery(theReccQueryStr) );
						}
						catch (Exception e)
						{
							s_Servlet_Logger.error("creating statement", e);
						}
						finally
						{
							USQL_Utils.closeStatementCatch(theS);
						}
					}
					else
					{
						s_Servlet_Logger.warn("Cannot connect!");
					}
				}
				catch (Exception err)
				{
					s_Servlet_Logger.error("???", err);
				}
				finally
				{
					if ( theConnectionObject != null)
					{
						theConnectionObject.CloseDown();
					}
				}
			}

			////////////////////////////////////////////////////////

			StringBuilder	sb = new StringBuilder(1000);
			sb.append("<?xml version=\"1.0\" ?><response>");

			////////////////////////////////////////////////////////

			Locale		theLocale = GetClientLocale(inRequest);
			SiteIF		theSiteObj;
			String		theLinkStr;
			int		theRecommendationsCount;
			int		z = 0;

			ensureNotCached(inResponse);

			inResponse.setLocale(theLocale);
			inResponse.setContentType("text/xml; charset=\"UTF-8\"");

			if ( theSearchResults != null)
			{
			for ( SearchMatch eachMatch : theSearchResults)
			{
				theSiteObj = eachMatch.getSite();

				///////////////////////////////////////////////////////////////////  (AGR) 11 October 2006

				if ( theRecommendationCountMap != null)
				{
					try
					{
						theLinkStr = eachMatch.getLink().toString();

						theRecommendationsCount = theRecommendationCountMap.get(theLinkStr).intValue();
					}
					catch (Exception e)
					{
						theRecommendationsCount = 0;
					}
				}
				else	theRecommendationsCount = 0;

				///////////////////////////////////////////////////////////////////

				sb.append("<match index=\"").append(z).append("\">");

				addDisplayable( sb, eachMatch, theSiteObj, theRecommendationsCount);

				_addXMLElement( sb, "descStyle", eachMatch.getDescriptionStyle(theRecommendationsCount));
				_addXMLCDataElement( sb, "creator", eachMatch.getReducedCreatorsStr());
				_addXMLElement( sb, "score", eachMatch.getScore());

				sb.append("</match>");

				z++;
				}
			}

			return sb.append("</response>").toString();
		}

		////////////////////////////////////////////////
		////////////////////////////////////////////////

		if ( theQuery != null)
		{
			theSsn.setAttribute("search_matches", theSearchResults);
		}
		else	theSsn.removeAttribute("search_matches");

		////////////////////////////////////////////////

		String	theRedirURLStr = inRequest.getParameter("redir");
		if (UText.isValidString(theRedirURLStr))
		{
			inResponse.sendRedirect( inResponse.encodeRedirectURL(theRedirURLStr) );
		}
		else	inResponse.sendRedirect( inResponse.encodeRedirectURL("http://www.bloggers4labour.org/") );

		return null;
	}

	/*******************************************************************************
		(AGR) 20 September 2005
	*******************************************************************************/
	public CharSequence handleDSW( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		DataSourceConnection	theConnectionObject = null;
		StringBuilder		cs = new StringBuilder();
		Locale			theLocale = GetClientLocale(inRequest);
		String			theType = inRequest.getParameter("dsw");
		String			theFormatStr = inRequest.getParameter("format");

		inResponse.setLocale(theLocale);
		inResponse.setContentType("text/javascript");

		if (UText.isValidString(theFormatStr))
		{
			if (theFormatStr.equals("b4l"))
			{
				cs.append("document.writeln('")
				  .append("<style type=\"text/css\">")
					.append("@import url(\"http://www.bloggers4labour.org/css/DSW.css\");")
				  .append("</style>');\n");
			}
		}

		////////////////////////////////////////////////////////////////

		cs.append("document.writeln('");
		cs.append("<div id=\"b4l-dsw\">")
			.append("<p class=\"dsw-head\">")
				.append("<span class=\"dsw-head\">")
					.append("Dead Socialist Watch")
					.append("&nbsp;")
				.append("</span>")
				.append("<span class=\"dsw-online\"><a class=\"dsw\" href=\"http://www.bloggers4labour.org/DSW.jsp\" target=\"_blank\">")
					.append("[More]")
				.append("</span></a>")
			.append("</p>");

		try
		{
			InstallationIF	theInstall = InstallationManager.getDefaultInstallation();

			theConnectionObject = new DataSourceConnection( theInstall.getDataSource() );
			if (theConnectionObject.Connect())
			{
				// s_FL_Logger.info("conn = " + theConnectionObject);

				Statement	theS = null;

				try
				{
					theS = theConnectionObject.createStatement();
					_handleDSW( theType,
						    inRequest.getParameter("day"),
						    inRequest.getParameter("month"),
						    inRequest.getParameter("year"),
						    theFormatStr,
						    theLocale, theS, cs);
				}
				catch (Exception e)
				{
					s_Servlet_Logger.error("creating statement", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theS);
				}
			}
			else
			{
				s_Servlet_Logger.warn("Cannot connect!");
			}
		}
		catch (Exception err)
		{
			s_Servlet_Logger.error("???", err);
		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
			}
		}

		return cs.append("</div>');");
	}

	/*******************************************************************************
		(AGR) 20 September 2005
	*******************************************************************************/
	private void _handleDSW( String inTypeStr, String inDayStr, String inMonthStr, String inYearStr,
				 String inFormatStr,
				 Locale inLocale, Statement inS, StringBuilder ioBuf) throws SQLException
	{
		Calendar	theCurrLocalTime = ULocale2.getGregorianCalendar(inLocale);

		try {
			theCurrLocalTime.set( Calendar.DAY_OF_MONTH, Integer.parseInt(inDayStr));
		} catch (Exception e) {}

		try {
			int	 m = Integer.parseInt(inMonthStr);

			if ( m >= 1 && m <= 12)
			{
				theCurrLocalTime.set( Calendar.MONTH, m - 1);
			}
		} catch (Exception e) {}

		try {
			theCurrLocalTime.set( Calendar.YEAR, Integer.parseInt(inYearStr));
		} catch (Exception e) {}

		////////////////////////////////////////////////////////////////

		TimeZone	theTZ = ULocale2.getBestTimeZone(inLocale);
		DateFormat	ourDateFormat = DateFormat.getDateInstance( DateFormat.LONG, inLocale);
		ourDateFormat.setTimeZone(theTZ);

		DateFormat	ourDayMonthFormat = new SimpleDateFormat( "dd MMMM", inLocale);
		ourDayMonthFormat.setTimeZone(theTZ);

		DateFormat	ourDayMonthShortFormat = new SimpleDateFormat( "dd MMM", inLocale);
		ourDayMonthShortFormat.setTimeZone(theTZ);

		////////////////////////////////////////////////////////////////

		StringBuilder	qBuilder;
		int		theMonth = theCurrLocalTime.get( Calendar.MONTH ) + 1;
		int		theDay = theCurrLocalTime.get( Calendar.DAY_OF_MONTH );
		boolean		isToday = UText.isNullOrBlank(inTypeStr) || inTypeStr.equalsIgnoreCase("today");

//		s_Servlet_Logger.info("curr time: \"" + ULocale2.getClientDateTimeFormat(inLocale).format(theCurrLocalTime.getTime() ) + "\", loc = " + inLocale);

		if (isToday)
		{
			qBuilder = new StringBuilder("SELECT * FROM DSW WHERE deathDateYearOnly=0 AND DAYOFMONTH(deathDate)=")
						.append(theDay).append(" AND MONTH(deathDate)=").append(theMonth)
						.append(" ORDER BY MONTH(deathDate)");
		}
		else if (inTypeStr.equalsIgnoreCase("week"))
		{
//			int	theCurrDayOfTheWeek = theCurrLocalTime.get( Calendar.DAY_OF_WEEK );
			int	theFirstDayOfTheWeek = theCurrLocalTime.getFirstDayOfWeek();

			theCurrLocalTime.set( Calendar.DAY_OF_WEEK, theFirstDayOfTheWeek);
//			s_Servlet_Logger.info("new time: \"" + ULocale2.getClientDateTimeFormat(inLocale).format(theCurrLocalTime.getTime() ) + "\", loc = " + inLocale);

			String	dStr = ourDateFormat.format(theCurrLocalTime.getTime() );

			ioBuf.append("<p class=\"dsw-week-head\"><strong>").append("Week starting: ").append("</strong>").append( UHTML.StringToHtml(dStr) ).append("</p>");

			int	theMonth_1 = theCurrLocalTime.get( Calendar.MONTH ) + 1;
			int	theDay_1 = theCurrLocalTime.get( Calendar.DAY_OF_MONTH );

			theCurrLocalTime.roll( Calendar.WEEK_OF_YEAR, true);

			int	theMonth_2 = theCurrLocalTime.get( Calendar.MONTH ) + 1;
			int	theDay_2 = theCurrLocalTime.get( Calendar.DAY_OF_MONTH );

			qBuilder = new StringBuilder("SELECT * FROM DSW WHERE deathDateYearOnly=0 AND ");

			if ( theMonth_1 == theMonth_2)
			{
				qBuilder.append("MONTH(deathDate)=").append(theMonth_1)
					.append(" AND DAYOFMONTH(deathDate) >= ").append(theDay_1)
					.append(" AND DAYOFMONTH(deathDate) < ").append(theDay_2);

				qBuilder.append(" ORDER BY MONTH(deathDate)");
			}
			else // if ( theMonth_1 != theMonth_2)
			{
				qBuilder.append("(( MONTH(deathDate)=").append(theMonth_1)
					.append(" AND DAYOFMONTH(deathDate) >= ").append(theDay_1)
					.append(") OR ( MONTH(deathDate)=").append(theMonth_2)
					.append(" AND DAYOFMONTH(deathDate) < ").append(theDay_2)
					.append("))");

				if ( theMonth_1 < theMonth_2)
				{
					qBuilder.append(" ORDER BY MONTH(deathDate)");
				}
				else	// changed year!!!
				{
					qBuilder.append(" ORDER BY MONTH(deathDate) DESC");	// <-- !!!
				}
			}
		}
		else
		{
			return;
		}

		qBuilder.append(",DAYOFMONTH(deathDate),YEAR(deathDate),surname,forenames");

		// s_Servlet_Logger.info("q = \"" + qBuilder + "\"");

		////////////////////////////////////////////////////////////////

		ResultSet	theRS = inS.executeQuery( qBuilder.toString() );

		ioBuf.append("<p class=\"dsw-entries\">");

		if (theRS.next())
		{
			int			theLastDeathDay = -1;
			Pattern			theWikipediaSpacePattern  = Pattern.compile(" ");
			TimeZone		theUTC_TZ = TimeZone.getTimeZone("UTC");
			GregorianCalendar	theDDCal = new GregorianCalendar(theUTC_TZ);
			GregorianCalendar	theBDCal = new GregorianCalendar(theUTC_TZ);
			NumberFormat		theAgeFmt = NumberFormat.getInstance(inLocale);

			theAgeFmt.setMaximumFractionDigits(0);

			boolean		gotOne = false;

			do
			{
				String		theFNs = theRS.getString("forenames");
				String		theSN = theRS.getString("surname");
				java.sql.Date	theDeathDate = theRS.getDate("deathDate");
				java.sql.Date	theBirthDate = theRS.getDate("birthDate");
				String		theDSW_URL = theRS.getString("dsw_URL");
				String		theNameStr = UHTML.StringToHtml(theFNs) + " " + UHTML.StringToHtml(theSN) + "";
				String		theAgeStr;

//		s_Servlet_Logger.info("... got \"" + theNameStr + "\"");

				theDDCal.setTime(theDeathDate);
				theDDCal.set( Calendar.HOUR_OF_DAY, 0);
				theDDCal.set( Calendar.MINUTE, 0);
				theDDCal.set( Calendar.SECOND, 0);

				////////////////////////////////////////////////

				int	theDeathDay = theDDCal.get( Calendar.DAY_OF_MONTH );
//				int	theDeathMonth = theDDCal.get( Calendar.MONTH );
				int	theDeathYear = theDDCal.get( Calendar.YEAR );
				String	theFullNameStr;

				if (UText.isValidString(theDSW_URL))
				{
					theFullNameStr = "<a class=\"dsw\" href=\"" + theDSW_URL + "\" target=\"_blank\">" + theNameStr + "</a>";
				}
				else	theFullNameStr = theNameStr;

				////////////////////////////////////////////////

				if ( theBirthDate != null)
				{
					theBDCal.setTime(theBirthDate);
					theBDCal.set( Calendar.HOUR_OF_DAY, 0);
					theBDCal.set( Calendar.MINUTE, 0);
					theBDCal.set( Calendar.SECOND, 0);

					double	ageInYears = UDates.getCalendarDifference_Years( theBDCal, theDDCal);
					int	bDayYearOnly = theRS.getInt("birthDateYearOnly");

					if ( bDayYearOnly == 1)
					{
						theAgeStr = ", aged " + theAgeFmt.format( ageInYears - 1) + " or " + theAgeFmt.format(ageInYears);
					}
					else
					{
						theAgeStr = ", aged " + theAgeFmt.format(ageInYears);
					}
				}
				else
				{
					theAgeStr = "";
				}

//		s_Servlet_Logger.info("... age \"" + theAgeStr + "\"");

				////////////////////////////////////////////////

				StringBuilder	theWikiLinkStr = new StringBuilder();
				String		theFNConv;
				String		theSNConv;

				try
				{
					theFNConv = URLEncoder.encode( theWikipediaSpacePattern.matcher(theFNs).replaceAll("_"), "UTF-8");
					theSNConv = URLEncoder.encode( theWikipediaSpacePattern.matcher(theSN).replaceAll("_"), "UTF-8");

					// theWikiLinkStr.append(theFNConv).append("_").append(theSNConv);
					// s_Servlet_Logger.info("... theWikiLinkStr.a = \"" + theWikiLinkStr + "\"");
				}
				catch (java.io.UnsupportedEncodingException e)
				{
					theFNConv = theWikipediaSpacePattern.matcher(theFNs).replaceAll("_");
					theSNConv = theWikipediaSpacePattern.matcher(theSN).replaceAll("_");

					// theWikiLinkStr.append(theFNConv).append("_").append(theSNConv);
					// s_Servlet_Logger.info("... theWikiLinkStr.b = \"" + theWikiLinkStr + "\"");
				}

				theWikiLinkStr.append("http://en.wikipedia.org/wiki/").append(theFNConv).append("_").append(theSNConv);

				////////////////////////////////////////////////

				if ( theLastDeathDay == -1 || theDeathDay != theLastDeathDay)
				{
					if (isToday)
					{
						ioBuf.append("<span class=\"dsw-day\">")
						     .append( UHTML.StringToHtml( ourDayMonthShortFormat.format(theCurrLocalTime.getTime() ) ) )
						     .append(": </span>");
					}
					else
					{
						if (gotOne)
						{
							ioBuf.append("&nbsp;-&nbsp;");
						}
						else
						{
							gotOne = true;
						}

						ioBuf.append("<span class=\"dsw-day\">")
						     .append( UHTML.StringToHtml( ourDayMonthShortFormat.format(theDDCal.getTime() ) ) )
						     .append(": </span>");
					}

					theLastDeathDay = theDeathDay;
				}
				else
				{
					if (gotOne)
					{
						ioBuf.append(", ");
					}
					else
					{
						gotOne = true;
					}
				}

				////////////////////////////////////////////////

				ioBuf.append(theFullNameStr)
				     .append("&nbsp;")
					.append("<span class=\"dsw-wiki\">")
					    .append("<a class=\"dsw\" target=\"_blank\" href=\"").append(theWikiLinkStr).append("\">[Wiki]</a>")
					.append("</span>")
				     .append("&nbsp;")
					.append("<span class=\"dsw-death\">")
					    .append("(<a class=\"dsw\" target=\"_blank\" href=\"http://en.wikipedia.org/wiki/Category:")
					    .append(theDeathYear).append("_deaths\">").append(theDeathYear).append("</a>")
					    .append(theAgeStr).append(")")
					.append("</span>");
			}
			while (theRS.next());

			// s_Servlet_Logger.info("... DONE: ioBuf = \"" + ioBuf + "\"");
		}
		else if (isToday)
		{
			ioBuf.append("<span class=\"dsw-none-today\">No famous Socialists died on ")
			     .append( ourDayMonthFormat.format(theCurrLocalTime.getTime() ) )
			     .append(".</span>");
		}
		else
		{
			ioBuf.append("no entries");
		}

		ioBuf.append("</p>");
	}

	/*******************************************************************************
		(AGR) 31 August 2006
	*******************************************************************************/
	public static CharSequence handleRecommendations( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		String			theRecommendationsParam = inRequest.getParameter("recommendations");
		boolean			inGoogleVersion = ( theRecommendationsParam == null || theRecommendationsParam.equals("google"));

		DataSourceConnection	theConnectionObject = null;
		StringBuilder		cs = new StringBuilder();
		Locale			theLocale = GetClientLocale(inRequest);
		String			theType = inRequest.getParameter("recommendations");
		String			theFormatStr = inRequest.getParameter("format");
		int			theCount;

/*		if (inGoogleVersion)
		{
			cs.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
			  .append("<Module>\n")
			  .append("<ModulePrefs title=\"Bloggers4Labour\">\n")
				.append("<Require feature=\"analytics\"/>")
			  .append("</ModulePrefs>\n")
			  .append("<Content type=\"html\">\n")
			  .append("<![CDATA[\n")
				.append("<script>\n")
					.append("_IG_Analytics(\"UA-159388-1\", \"/widget_recommendations\");\n")
				.append("</script>\n");

			theCount = 10;
		}
		else
*/		{
		try
		{
			theCount = Integer.parseInt( inRequest.getParameter("count") );
		}
		catch (Exception e)
		{
				theCount = 10;
			}
		}

		////////////////////////////////////////////////////////////////

		inResponse.setLocale(theLocale);

		if (inGoogleVersion)
		{
			inResponse.setContentType("text/xml; charset=\"UTF-8\"");
		}
		else
		{
		inResponse.setContentType("text/javascript");
		}

		////////////////////////////////////////////////////////////////

		if (inGoogleVersion)
		{
/*
			cs.append("<style type=\"text/css\">")
				.append("@import url(\"http://www.bloggers4labour.org/css/RecommendationsGoogle.css\");")
			  .append("</style>\n");
*/
		}
		else if (UText.isValidString(theFormatStr))
		{
			if (theFormatStr.equals("b4l"))
			{
				cs.append("document.writeln('")
				  .append("<style type=\"text/css\">")
					.append("@import url(\"http://www.bloggers4labour.org/css/Recommendations.css\");")
				  .append("</style>');\n");
			}
		}

		////////////////////////////////////////////////////////////////

		if (!inGoogleVersion)
		{
		cs.append("document.writeln('");
		cs.append("<div id=\"b4l-recommendations\">")
			.append("<p class=\"recommend-head\">")
				.append("<span class=\"recommend-head\">")
					.append("Recently Recommended...")
					.append("&nbsp;")
				.append("</span>")
			.append("</p>");
		}

		try
		{
			InstallationIF	theInstall = InstallationManager.getDefaultInstallation();

			theConnectionObject = new DataSourceConnection( theInstall.getDataSource() );
			if (theConnectionObject.Connect())
			{
				// s_FL_Logger.info("conn = " + theConnectionObject);

				Statement	theS = null;

				try
				{
					if (inGoogleVersion)
					{
						theS = theConnectionObject.prepareCall("getRecentRecommendations(?)");
						((CallableStatement) theS).setInt( 1, theCount);

						_handleRecommendationsXML( theInstall, theCount, (CallableStatement) theS, cs);
					}
					else
					{
					theS = theConnectionObject.createStatement();

					_handleRecommendations( theInstall,
								theType,
								theFormatStr,
								theCount, theS, cs);
					}
				}
				catch (Exception e)
				{
					s_Servlet_Logger.error("creating statement", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theS);
				}
			}
			else
			{
				s_Servlet_Logger.warn("Cannot connect!");
			}
		}
		catch (Exception err)
		{
			s_Servlet_Logger.error("???", err);
		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
				theConnectionObject = null;
			}
		}

		////////////////////////////////////////////////////////////////

		if (inGoogleVersion)
		{
			return cs;	//.append("</div>\n]]>\n</Content>\n</Module>");
		}
		else
		{
		cs.append("<hr />")
		  .append("<p class=\"recommend-link\">")
				.append("<a class=\"recommend\" href=\"" + "http://www.bloggers4labour.org/recommended.jsp" + "\" target=\"recommendedWind\">")
					.append("More Recommendations")
				.append("</a>")
			.append("</p>");

		return cs.append("</div>');");
		}
	}

	/*******************************************************************************
		(AGR) 31 August 2006
	*******************************************************************************/
	private static void _handleRecommendations( final InstallationIF inInstall, String inTypeStr, String inFormatStr, int inNumber,
						Statement inS, StringBuilder ioBuf) throws SQLException
	{

		StringBuilder	qBuilder = new StringBuilder("SELECT U.url,S.site_recno,S.name FROM recommendations R, recommendedURLs U, site S WHERE R.url_recno=U.recno AND U.originating_site_recno=S.site_recno ORDER BY date DESC LIMIT " + inNumber);
		ResultSet	theRS = inS.executeQuery( qBuilder.toString() );

		ioBuf.append("<p class=\"recommend-entries\">");

		if (theRS.next())
		{
	//		HashMap<Number,Site>	theSitesMap = new HashMap<Number,Site>();
	//		FeedList		theFL = inInstall.getFeedList();
			Site			theSiteObj;
			int			numPostsPerLevel = inNumber / 5;
	//		int			currentLevel = 1;
			int			postCount = 0;
			boolean			usesLevels = ( inNumber % 5 == 0);
			boolean			gotOne = false;
//			boolean			showComma = false;

			do
			{
				String		theURL = theRS.getString("url");
/*				long		theSiteRecno = theRS.getLong("site_recno");

				theSiteObj = theSitesMap.get(theSiteRecno);
				if ( theSiteObj == null)
				{
					theSiteObj = theFL.lookup(theSiteRecno);
					theSitesMap.put( theSiteRecno, theSiteObj);
				}
*/
				////////////////////////////////////////////////

				if (gotOne)
				{
					ioBuf.append(", ");
				}
				else	gotOne = true;

				ioBuf.append("<a href=\"" + theURL + "\" target=\"recommendedWind\">");

				if (usesLevels)
				{
					int	currentLevel = postCount / numPostsPerLevel + 1;

					ioBuf.append("<span class=\"level").append(currentLevel).append("\">");
				}

				ioBuf.append( UHTML.StringToHtml( theRS.getString("name") ));

				if (usesLevels)
				{
					ioBuf.append("</span>");
				}

				ioBuf.append("</a>");

				postCount++;
			}
			while (theRS.next());

			// s_Servlet_Logger.info("... DONE: ioBuf = \"" + ioBuf + "\"");
		}
		else
		{
			ioBuf.append("no articles");
		}

		ioBuf.append("</p>");
	}

	/*******************************************************************************
		(AGR) 12 March 2007
	*******************************************************************************/
	private static void _handleRecommendationsXML( final InstallationIF inInstall, int inNumber,
							CallableStatement inS, StringBuilder ioBuf) throws SQLException
	{
		inS.execute();

		ResultSet	theRS = inS.getResultSet();
		int		numPostsPerLevel = inNumber / 5;
		int		postCount = 0;
		boolean		usesLevels = ( inNumber % 5 == 0);

		ioBuf.append("<?xml version=\"1.0\" ?><entries>");

		while (theRS.next())
		{
			ioBuf.append("<entry>");

//			_addXMLCDataElement( ioBuf, "url", theRS.getString("url"));
			_addXMLElement( ioBuf, "url", theRS.getString("url"));

			////////////////////////////////////////////////

			if (usesLevels)
			{
				int	currentLevel = postCount / numPostsPerLevel + 1;

				_addXMLElement( ioBuf, "level", Integer.valueOf(currentLevel));
			}

			_addXMLCDataElement( ioBuf, "site", UHTML.StringToHtml( theRS.getString("name") ) );

			ioBuf.append("</entry>");

			postCount++;
		}

		ioBuf.append("</entries>");

		USQL_Utils.closeResultSetCatch(theRS);
	}

	/*******************************************************************************
		(AGR) 14 October 2006
	*******************************************************************************
	public CharSequence handleHateMyTory( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		StringBuilder	cs = new StringBuilder();
		String		theFormatStr = inRequest.getParameter("format");

		inResponse.setLocale( GetClientLocale(inRequest) );
		inResponse.setContentType("text/javascript");

		if (UText.isValidString(theFormatStr))
		{
			if (theFormatStr.equals("b4l"))
			{
				cs.append("document.writeln('")
				  .append("<style type=\"text/css\">")
					.append("@import url(\"http://www.bloggers4labour.org/css/hateMyTory.css\");")
				  .append("</style>');\n");
			}
		}

		cs.append("document.writeln('")
			.append("<iframe src=\"http://hatemytory.com/tory-cgi/cleanptory.pl\" style=\"width:150px;height:260px;\" frameborder=\"1\" scrolling=\"no\" marginwidth=\"0\" marginheight=\"0\" frameborder=\"0\"></iframe>")
		  .append("');");

		return cs;
	} */

	/*******************************************************************************
		11 February 2002
	*******************************************************************************/
	public static boolean hasParameter( HttpServletRequest inRequest, String inParamName)
	{
 		return ( inRequest.getParameter(inParamName) != null);
 	}

	/*******************************************************************************
	*******************************************************************************/
	protected static void setResponseLocaleAndUse_HTML( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		setResponseLocaleAndUse_HTML( inRequest, inResponse, true);
	}

	/*******************************************************************************
	*******************************************************************************/
	protected static void setResponseLocaleAndUse_HTML( HttpServletRequest inRequest, HttpServletResponse inResponse,
								Locale inLocale)
	{
		setResponseLocaleAndUse_HTML( inRequest, inResponse, inLocale, true);
	}

	/*******************************************************************************
	*******************************************************************************/
	protected static void setResponseLocaleAndUse_HTML( HttpServletRequest inRequest, HttpServletResponse inResponse,
								boolean inSetType)
	{
		setResponseLocaleAndUse_HTML( inRequest, inResponse, GetClientLocale(inRequest), inSetType);
	}

	/*******************************************************************************
	*******************************************************************************/
	protected static void setResponseLocaleAndUse_HTML( HttpServletRequest inRequest, HttpServletResponse inResponse,
								Locale inLocale, boolean inSetType)
	{
		inResponse.setLocale(inLocale);

		if (inSetType)
		{
			inResponse.setContentType("text/html; charset=" + CharEncoding.getBestEncoding( inRequest, inLocale));	// 17 February 2002
		}
		else	inResponse.setContentType("text/html");
	}

	/*******************************************************************************
	*******************************************************************************/
	public static Locale GetClientLocale( HttpServletRequest inRequest)
	{
		if ( inRequest == null)		return Locale.ENGLISH;
		else
		{
/*			HttpSession    theSession = inRequest.getSession(false);

			if ( theSession != null)
			{
				String    theLangStr = SessionUtils.GetCurrentLanguage(theSession);

				if (UText.isValidString(theLangStr))
				{
					String    theCountryStr = SessionUtils.GetCurrentCountry(theSession);

					return new Locale( theLangStr, ( theCountryStr != null) ? theCountryStr : "");
				}
			}
*/
			return inRequest.getLocale();    // the previous default!
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void Finish( HttpServletResponse inResponse)
	{
		try
		{
			Writer	theWriter = inResponse.getWriter();

			if ( theWriter != null)
			{
				theWriter.close();
			}
		}
		catch (java.lang.IllegalStateException err)
		{
			s_Servlet_Logger.error("Finish", err);
		}
		catch (Exception err)
		{
			s_Servlet_Logger.error( "*** Error closing writer", err);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	static public void myOutputBuffer( HttpServletResponse inResponse, String inString)
	{
		if (UText.isValidString(inString))
		{
			try
			{
				inResponse.getWriter().println(inString);
			}
			catch (Exception e)
			{
				s_Servlet_Logger.error("???", e);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	static public void myOutputBuffer( HttpServletResponse inResponse, CharSequence inBuffer) // StringBuffer inBuffer)
	{
		myOutputBuffer( inResponse, inBuffer, false);
	}

	/*******************************************************************************
	*******************************************************************************/
	static public void myOutputBuffer( HttpServletResponse inResponse, CharSequence inBuffer, boolean inThisIsAllWeOutput)
	{
		if (UText.isValidString(inBuffer))
		{
			try
			{
				inResponse.getWriter().println(inBuffer);

				if (inThisIsAllWeOutput)
				{
					inResponse.setContentLength( inBuffer.length() );
				}
			}
			catch (Exception e)
			{
				s_Servlet_Logger.error("???", e);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static StringBuilder _addXMLElement( StringBuilder ioS, final String inElementName, final Object inContent)
	{
		return ioS.append("<" + inElementName + ">").append(inContent).append("</" + inElementName + ">");
	}

	/*******************************************************************************
		(AGR) 29 April 2007
	*******************************************************************************/
	public static StringBuilder _addXMLElement( StringBuilder ioS, final String inElementName, final String inAttrs, final Object inContent)
	{
		return ioS.append("<" + inElementName + " " + inAttrs + ">").append(inContent).append("</" + inElementName + ">");
	}

	/*******************************************************************************
	*******************************************************************************/
	public static StringBuilder _addXMLCDataElement( StringBuilder ioS, final String inElementName, final Object inContent)
	{
		return ioS.append("<" + inElementName + "><![CDATA[").append(inContent).append("]]></" + inElementName + ">");
	}

	/*******************************************************************************
	*******************************************************************************/
	@Deprecated private static StringBuilder addDisplayable( StringBuilder ioS, final Displayable inObj, final SiteIF inSite, int inReccCount)
	{
		_addXMLCDataElement( ioS, "blogName", inObj.getBlogName());
		_addXMLElement( ioS, "siteID", ( inSite != null) ? inSite.getRecno() : -1L);
		_addXMLCDataElement( ioS, "siteURL", inObj.getSiteURL());	// (AGR) 15 June 2009. Some MpURL URLs have bad ampersands, so change to CDATA

		///////////////////////////////////////////////////////

		if ( inObj.getLink() != null)	// (AGR) 28 October 2006. Something wrong with Warren Morgan's blog produces null links. So test here.
		{
			_addXMLCDataElement( ioS, "link", inObj.getLink().toString());
		}
		else	_addXMLCDataElement( ioS, "link", "");	// Is this acceptable?

		///////////////////////////////////////////////////////  (AGR) 30 Nov 2006. May be "< 1 min", so may need CDATA!

		String	dateStr = inObj.getDateString();

		if ( UText.isValidString(dateStr) && dateStr.contains("<"))	// (AGR) 21 Feb 2007. Bug-fix. Previous was causing "(< 1 min)" to go through unencoded, breaking validation.
		{
			_addXMLCDataElement( ioS, "date", dateStr);
		}
		else	_addXMLElement( ioS, "date", dateStr);

		///////////////////////////////////////////////////////

		_addXMLCDataElement( ioS, "displayTitle", UText.isValidString( inObj.getDispTitle() ) ? inObj.getDispTitle() : "<i>Untitled</i>");
		_addXMLCDataElement( ioS, "desc", inObj.getDescription());
		_addXMLElement( ioS, "iconURL", UText.isValidString( inObj.getIconURL() ) ? inObj.getIconURL() : "");

			_addXMLElement( ioS, "votes", Integer.toString(inReccCount));    // (AGR) 1 October 2006

		return ioS;
	}

	/*******************************************************************************
		(AGR) 1 November 2006
	*******************************************************************************/
	public String handleCricketInitialisation( HttpServletRequest inRequest, HttpServletResponse inResponse, final Locale inLocale)
	{
		String	theReferrer = inRequest.getHeader("Referer");

		// s_Servlet_Logger.debug("referer = \"" + theReferrer + "\"");

		if ( UText.isNullOrBlank(theReferrer) || !theReferrer.startsWith("http://www.bloggers4labour.org/"))
		{
			s_Servlet_Logger.warn("referer not accepted: " + theReferrer);
			// return "";
		}

		////////////////////////////////////////////////////////////////

		HttpSession	theSession = inRequest.getSession(true);
		Long		theLastRefreshedTimeMS = (Long) theSession.getAttribute("b4l_cricket_last_refreshed");

		if ( theLastRefreshedTimeMS == null)
		{
			theLastRefreshedTimeMS = Long.valueOf( System.currentTimeMillis() );	// (AGR) 29 Jan 2007. FindBugs: changed from new Long
			theSession.setAttribute( "b4l_cricket_last_refreshed", theLastRefreshedTimeMS);
		}

		////////////////////////////////////////////////////////////////

		inResponse.setContentType("text/javascript");

		B4LHTLContext		theCtxt = new B4LHTLContext(inLocale);
		HTLTemplate		theSectionTemplate = HTL.createTemplate( "cricket_score_layout.vm", inLocale);
		HTLTemplate		theTeam1Template = HTL.createTemplate( "cricket_score_team1_score.vm", inLocale);
		HTLTemplate		theTeam2Template = HTL.createTemplate( "cricket_score_team2_score.vm", inLocale);
		StringBuilder		sb = new StringBuilder(2000);

		theCtxt.put( "info", "1st Test at Melbourne");

		theCtxt.put( "team1_name", "Eng");
		theCtxt.put( "team1_score", "203-3");
		theCtxt.put( "team2_name", "Aus");
		theCtxt.put( "team2_score", "305");

		theCtxt.put( "team1_buf", HTL.mergeTemplate( theTeam1Template, theCtxt));
		theCtxt.put( "team2_buf", HTL.mergeTemplate( theTeam2Template, theCtxt));

		sb.append("document.writeln('");

		StringBuffer	x = HTL.mergeTemplate( theSectionTemplate, theCtxt);
		int		len = x.length();
		boolean		isEncoded = false;

		for ( int k = 0; k < len; k++)	// Yuk!!!
		{
			char	c = x.charAt(k);

			if ( c == '\n' || c == '\r')
			{
				continue;
			}
			else if ( c == '\'')
			{
				sb.append("\\\'");
				isEncoded = false;
			}
			else if (isEncoded)
			{
				sb.append("\\").append(c);
				isEncoded = false;
			}
			else if ( c == '\\')
			{
				isEncoded = true;
			}
			else	sb.append(c);
		}

		sb.append("');");

		return sb.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public void destroy()
	{
		if ( m_Launcher != null)
		{
			m_Launcher.stop();
		}
	}
}