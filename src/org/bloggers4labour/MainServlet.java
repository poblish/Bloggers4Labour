/*
 * MainServlet.java
 *
 * Created on 12 March 2005, 01:31
 */

package org.bloggers4labour;

import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.recommend.RecommendationHandler;
import org.bloggers4labour.recommend.RecommendationResult;
import org.bloggers4labour.sql.DataSourceConnection;
import com.hiatus.CharEncoding;
import com.hiatus.UDates;
import com.hiatus.UHTML;
import com.hiatus.ULocale2;
import com.hiatus.USQL_Utils;
import com.hiatus.UText;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.Item;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.Query;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.index.SearchMatch;

/**
 *
 * @author andrewre
 */
public class MainServlet extends HttpServlet
{
	private Launcher	m_Launcher;

	private static Logger	s_Servlet_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************/
	public MainServlet()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public void init( ServletConfig inConfig)
	{
		try
		{
			super.init(inConfig);

			s_Servlet_Logger.info("===============================");
			s_Servlet_Logger.info("In Servlet: " + this);

			////////////////////////////////////////  (AGR) 22 June 2005

			String	theParam = inConfig.getInitParameter("lucene_index_dir");
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
		catch (Exception ex)
		{
			s_Servlet_Logger.error( "init error", ex);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void doGet( HttpServletRequest inRequest, HttpServletResponse inResponse)
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

				FeedList	theFL = InstallationManager.getDefaultInstallation().getFeedList();
				String		s = theFL.getOPMLOutputStr();

				theOutputBuffer = ( s != null) ? new StringBuffer(s) : null;
			}
			else if (hasParameter( inRequest, "rss"))	// (AGR) 15 April 2005
			{
				inResponse.setLocale( GetClientLocale(inRequest) );
				inResponse.setContentType("text/xml; charset=\"UTF-8\"");

				////////////////////////////////////////////////

				InstallationIF	theInstall = InstallationManager.getDefaultInstallation();
				String		theOneWeWant = inRequest.getParameter("rss");
				String		theIncludedSitesParam = inRequest.getParameter("includedsites");  // (AGR) 6 June 2006
				String		theExcludedSitesParam = inRequest.getParameter("excludedsites");  // (AGR) 6 June 2006
				Headlines	theRightHeads;
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

					//	theRightHeads.getRightHeadlines

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
			else if (hasParameter( inRequest, "search"))	// (AGR) 17 July 2005. Ok, can be GET too
			{
				handleSearch( inRequest, inResponse);
			}
			else if (hasParameter( inRequest, "dsw"))	// (AGR) 20 September 2005
			{
				theOutputBuffer = handleDSW( inRequest, inResponse).toString();
				// s_Servlet_Logger.info("... theOutputBuffer = \"" + theOutputBuffer + "\"");
				// keepAlive = false;
			}
			else if (hasParameter( inRequest, "recommend"))	// (AGR) 20 June 2006. Value is a URL...
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
	public void doPost( HttpServletRequest inRequest, HttpServletResponse inResponse)
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
		catch (Exception e)
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
	private void handleSearch( HttpServletRequest inRequest, HttpServletResponse inResponse) throws IOException
	{
s_Servlet_Logger.info( inRequest.getMethod() + " query = " + inRequest.getQueryString());

		HttpSession	theSsn = inRequest.getSession();
		String		theQueryStr = inRequest.getParameter("q");

		s_Servlet_Logger.info("q = \"" + theQueryStr  + "\"");

		boolean		gotQuery = UText.isValidString(theQueryStr);
		String		theWhereStr = inRequest.getParameter("where");
		boolean		gotTextVal, gotTitlesVal, gotBothVal;
		Analyzer	theAnalyzer = new StandardAnalyzer();
		Query		theQuery = null;
		boolean		isNewSession = theSsn.isNew();

		try
		{
			if ( isNewSession || UText.isNullOrBlank(theWhereStr))
			{
				gotTextVal = gotTitlesVal = false;
				gotBothVal = true;
			}
			else if (theWhereStr.equals("text"))
			{
				gotTextVal = true;
				gotTitlesVal = gotBothVal = false;

				if (gotQuery)
				{
					theQuery = new QueryParser( "desc", theAnalyzer).parse(theQueryStr);
				}
			}
			else if (theWhereStr.equals("titles"))
			{
				gotTitlesVal = true;
				gotTextVal = gotBothVal = false;

				if (gotQuery)
				{
					theQuery = new QueryParser( "title", theAnalyzer).parse(theQueryStr);
				}
			}
			else
			{
				gotTextVal = gotTitlesVal = false;
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
			String[]	theArray = {"desc","title"};

			try
			{
//				QueryParser	theParser = new MultiFieldQueryParser( "desc", theAnalyzer);	// (AGR) Lucene 1.4.3
				QueryParser	theParser = new MultiFieldQueryParser( theArray, theAnalyzer);	// (AGR) Lucene 1.9

//				theQuery = theParser.parse( theQueryStr, theArray, theAnalyzer);		// (AGR) Lucene 1.4.3
				theQuery = theParser.parse(theQueryStr);					// (AGR) Lucene 1.9
			}
			catch (org.apache.lucene.queryParser.ParseException e)
			{
			}
		}

		////////////////////////////////////////////////

		if ( theQuery != null)
		{
			s_Servlet_Logger.info("Search2: " + theQuery);

			InstallationIF		theInstall = InstallationManager.getDefaultInstallation();
			List<SearchMatch>	x = theInstall.getIndexMgr().runQuery(theQuery);

			theSsn.setAttribute("search_matches", x);
		}
		else	theSsn.removeAttribute("search_matches");

		////////////////////////////////////////////////

		String	theRedirURLStr = inRequest.getParameter("redir");
		if (UText.isValidString(theRedirURLStr))
		{
			inResponse.sendRedirect( inResponse.encodeRedirectURL(theRedirURLStr) );
		}
		else	inResponse.sendRedirect( inResponse.encodeRedirectURL("http://www.bloggers4labour.org/") );
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
				theConnectionObject = null;
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
			int	theCurrDayOfTheWeek = theCurrLocalTime.get( Calendar.DAY_OF_WEEK );
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
			boolean		showComma = false;

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
				int	theDeathMonth = theDDCal.get( Calendar.MONTH );
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
			HttpSession    theSession = inRequest.getSession(false);

			if ( theSession != null)
			{
/*				String    theLangStr = SessionUtils.GetCurrentLanguage(theSession);

				if (UText.isValidString(theLangStr))
				{
					String    theCountryStr = SessionUtils.GetCurrentCountry(theSession);

					return new Locale( theLangStr, ( theCountryStr != null) ? theCountryStr : "");
				}
*/			}

			return inRequest.getLocale();    // the previous default!
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void Finish( HttpServletResponse inResponse)
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
	static public void myOutputBuffer( HttpServletResponse inResponse, StringBuffer inBuffer)
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
	public void destroy()
	{
		if ( m_Launcher != null)
		{
			m_Launcher.stop();
		}
	}
}