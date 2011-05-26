/*
 * SubmitBlogHandlerServlet.java
 *
 * Created on 27 April 2007, 23:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.api;

import com.hiatus.sql.ResultSetList;
import com.hiatus.sql.USQL_Utils;
import com.hiatus.text.UText;
import java.net.SocketTimeoutException;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.MainServlet;
import org.bloggers4labour.feed.Feed;
import org.bloggers4labour.feed.FeedType;
import org.bloggers4labour.geocode.GeocodingResultCode;
import org.bloggers4labour.geocode.LRUPostCodeGeocoderCache;
import org.bloggers4labour.geocode.Location;
import org.bloggers4labour.geocode.PostCodeGeocoder;
import org.bloggers4labour.req.HttpRequestWrapper;
import org.bloggers4labour.req.ParametersIF;
import org.bloggers4labour.sql.DataSourceConnection;
import org.bloggers4labour.test.HTMLParserTest;
import org.bloggers4labour.validation.PostCodeResult;
import org.bloggers4labour.validation.PostCodeValidator;

/**
 *
 * @author andrewre
 */
public class SubmitBlogHandlerServlet extends APIHandlerServlet
{
	private final static Pattern	evilNamesPattern = Pattern.compile( "\\bfuck|\\bringtone|\\breplica watch|\\brolex|\\binsurance|\\bspyware|\\bjewelry|\\bwww\\..*\\.com|\\bphones|\\bNokia|\\bcoupon|\\bdiscount|\\bOffshore", Pattern.CASE_INSENSITIVE);	// (AGR) 17 Oct 2006, 5,19,22,24 July 2006, 18 June 2006
	private final static Pattern	evilURLsPattern = Pattern.compile( "\\bhanusoftware", Pattern.CASE_INSENSITIVE);		// (AGR) 8 December 2006
	private final static Pattern	noLinksAllowedPattern = Pattern.compile( "(http|https)://", Pattern.CASE_INSENSITIVE);		// (AGR) 5 July 2006
	private final static String	EVIL_APPEAL_STATEMENT = "If you disagree with this decision, please contact us.";	// (AGR) 8 December 2006

	private final static String	FLD_GEOCODING_RESULT = "geocoding_result";
	private final static String	FLD_NEARBY_BLOGS = "nearby_blogs";

	private static final long	serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************
	public SubmitBlogHandlerServlet()
	{
		super();
	} */

	/*******************************************************************************
	*******************************************************************************/
	@Override public void doGet( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		CharSequence	theOutputBuffer = null;
		boolean		keepAlive = true;

		try
		{
			MainServlet.ensureNotCached(inResponse);

			inResponse.setLocale( MainServlet.GetClientLocale(inRequest) );

			////////////////////////////////////////////////////////

			String			outputFormatStr = inRequest.getParameter("outputFormat");
			SubmissionResult	theResult = handleSubmission( m_DataSource, m_Logger, new HttpRequestWrapper(inRequest));

			// m_Logger.info("returned: " + theResult);

			StringBuilder		ourBuilder = new StringBuilder(1000);

			if (UText.isNullOrBlank(outputFormatStr) || outputFormatStr.equalsIgnoreCase("XML"))
			{
				inResponse.setContentType("text/xml");

				ourBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				ourBuilder.append("<B4L>");

				MainServlet._addXMLElement( ourBuilder, "code", theResult.getCode());
				MainServlet._addXMLCDataElement( ourBuilder, "message", theResult.m_Message);
				MainServlet._addXMLElement( ourBuilder, "elapsedTimeMS", theResult.m_ElapsedTimeMSecs);
				MainServlet._addXMLElement( ourBuilder, "new_blog_id", theResult.m_SiteRecno);
				MainServlet._addXMLElement( ourBuilder, FLD_GEOCODING_RESULT, theResult.m_GeocodingResult);

				////////////////////////////////////////////////

				StringBuilder	bloggersBuf = new StringBuilder();
				ResultSetList	rsl = theResult.m_Results;

				if ( rsl != null)
				{
					List	theRows = rsl.getRowsList();

					if ( theRows != null && theRows.size() > 0)
					{
						int	idx = 0;

						for ( Object eachRowObj : theRows)
						{
							Map		m = (Map) eachRowObj;
							StringBuilder	eachBloggerBuf = new StringBuilder();

							MainServlet._addXMLCDataElement( eachBloggerBuf, "name", m.get("name"));
							MainServlet._addXMLCDataElement( eachBloggerBuf, "url", m.get("url"));
							MainServlet._addXMLCDataElement( eachBloggerBuf, "blog_id", m.get("site_recno"));
							MainServlet._addXMLElement( eachBloggerBuf, "dist_km", m.get("dist_km"));

							MainServlet._addXMLElement( bloggersBuf, "blog", "index=\"" + (idx++) + "\"", eachBloggerBuf);

							// bloggersBuf.append(eachBloggerBuf);
						}
					}
				}

				MainServlet._addXMLElement( ourBuilder, FLD_NEARBY_BLOGS, bloggersBuf);	// can be empty

				ourBuilder.append("</B4L>");
			}
			else if (outputFormatStr.equalsIgnoreCase("JS"))
			{
				inResponse.setContentType("text/javascript");

				ourBuilder.append("{ ");
				ourBuilder.append("code: \"").append( theResult.getCode() ).append("\",");
				ourBuilder.append("message: \"").append( theResult.m_Message ).append("\",");
				ourBuilder.append("elapsedTimeMS: \"").append( theResult.m_ElapsedTimeMSecs ).append("\",");
				ourBuilder.append("new_blog_id: \"").append( theResult.m_SiteRecno ).append("\",");
				ourBuilder.append(FLD_GEOCODING_RESULT).append(": \"").append( theResult.m_GeocodingResult ).append("\",");
				ourBuilder.append(FLD_NEARBY_BLOGS).append(": [");	// nearby_bloggers STARTS

				ResultSetList	rsl = theResult.m_Results;

				if ( rsl != null)
				{
					List	theRows = rsl.getRowsList();

					if ( theRows != null && theRows.size() > 0)
					{
						for ( Object eachRowObj : theRows)
						{
							Map	m = (Map) eachRowObj;

							ourBuilder.append("{ ");
							ourBuilder.append("name: \"").append( m.get("name") ).append("\"");
							ourBuilder.append(", url: \"").append( m.get("url") ).append("\"");
							ourBuilder.append(", blog_id: \"").append( m.get("site_recno") ).append("\"");
							ourBuilder.append(", dist_km: \"").append( m.get("dist_km") ).append("\"");
							ourBuilder.append("}");
						}
					}
				}

				ourBuilder.append("]");	// nearby_bloggers ENDS

				ourBuilder.append("}");
			}

			theOutputBuffer = ourBuilder.toString();

			// m_Logger.info("theOutputBuffer = " + theOutputBuffer);
		}
		catch (Exception e)
		{
			m_Logger.error("SubmitBlogHandlerServlet.doGet", e);
		}
		finally
		{
			MainServlet.myOutputBuffer( inResponse, theOutputBuffer, keepAlive);
			MainServlet.Finish(inResponse);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static SubmissionResult handleSubmission( final DataSource inDS, final Logger inLogger, final ParametersIF inRequest)
	{
		String			HTTP_STR = "http://";
		EnumSet<FeedType>	theFindFeedType = _findFeedTypes( inRequest.getTrimmedRequestString("findFeed"));	
		int			theErrorIfFindFeedFails = inRequest.parseRequestInt( "errorIfFindFeedFails", 0);
		String			lastFNames = inRequest.getTrimmedRequestString( "fnames", "???");
		String			lastSurname = inRequest.getTrimmedRequestString( "sname", "???");
		String			lastFeedURL = inRequest.getTrimmedRequestString( "feed_url");
		String			lastDescr = inRequest.getTrimmedRequestString( "blog_descr");
		String			lastEmail = inRequest.getTrimmedRequestString( "email", "???");
		String			lastBlogURL = inRequest.getParameter("blog_url");
		String			lastBlogName = inRequest.getParameter("blog_name");
		String			lastPostCode = inRequest.getParameter("postCode");
		int			r_isBlogVal = inRequest.parseRequestInt( "is_blog", 1);
		int			r_siteCatVal = inRequest.parseRequestInt( "site_category", 0);
		int			r_siteLocVal = inRequest.parseRequestInt( "location", 0);
		int			r_statusVal = inRequest.parseRequestInt( "status", 0);

		if (UText.isNullOrBlank(lastBlogName))
		{
			return new SubmissionResult( "Please specify a Blog name", SubmitBlogResultCode.MISSING_BLOG_NAME);
		}

		lastBlogName = lastBlogName.trim();

		if (evilNamesPattern.matcher(lastBlogName).find())	// (AGR) 18 June 2006
		{
			return new SubmissionResult( "Your Blog's name is unacceptable. Please try again. " + EVIL_APPEAL_STATEMENT, SubmitBlogResultCode.UNACCEPTABLE_BLOG_NAME);
		}

		//////////////////////////////////////////////////////////////////  (AGR) 5 July 2006

		if ( evilNamesPattern.matcher(lastDescr).find() ||
		     noLinksAllowedPattern.matcher(lastDescr).find())
		{
			return new SubmissionResult( "Your Blog's description is unacceptable. Please try again. " + EVIL_APPEAL_STATEMENT, SubmitBlogResultCode.UNACCEPTABLE_DESCRIPTION);
		}

		//////////////////////////////////////////////////////////////////

		if (UText.isNullOrBlank(lastFNames))
		{
			return new SubmissionResult( "Please specify your forenames", SubmitBlogResultCode.MISSING_FORENAME);
		}

		//////////////////////////////////////////////////////////////////

		if (UText.isNullOrBlank(lastSurname))
		{
			return new SubmissionResult( "Please provide your surname", SubmitBlogResultCode.MISSING_SURNAME);
		}

		//////////////////////////////////////////////////////////////////

		if (UText.isNullOrBlank(lastBlogURL))
		{
			return new SubmissionResult( "Please specify a Blog URL", SubmitBlogResultCode.MISSING_BLOG_URL);
		}

		if (evilURLsPattern.matcher(lastBlogURL).find())	// (AGR) 8 December 2006
		{
			return new SubmissionResult( "Your Blog's URL is unacceptable. Please try again. " + EVIL_APPEAL_STATEMENT, SubmitBlogResultCode.UNACCEPTABLE_BLOG_URL);
		}

		if (!lastBlogURL.startsWith(HTTP_STR))
		{
			return new SubmissionResult( "The Blog URL must start with: \"http://\"", SubmitBlogResultCode.UNACCEPTABLE_BLOG_URL_PREFIX);
		}

		//////////////////////////////////////////////////////////////////

		if ( theFindFeedType.isEmpty() && UText.isValidString(lastFeedURL) && !lastFeedURL.startsWith(HTTP_STR))
		{
			return new SubmissionResult( "The Feed URL must start with: \"http://\"", SubmitBlogResultCode.UNACCEPTABLE_FEED_URL_PREFIX);
		}

		//////////////////////////////////////////////////////////////////

		if (UText.isNullOrBlank(lastEmail))
		{
			return new SubmissionResult( "Please provide a contact email address", SubmitBlogResultCode.MISSING_EMAIL);
		}

		//////////////////////////////////////////////////////////////////

		PostCodeResult	thePostCodeResult = new PostCodeValidator().validate(lastPostCode);

		if ( thePostCodeResult == PostCodeResult.INVALID)
		{
			return new SubmissionResult( "Your Post Code doesn't seem to be valid. Please try again. " + EVIL_APPEAL_STATEMENT, SubmitBlogResultCode.UNACCEPTABLE_POSTCODE);
		}

		//////////////////////////////////////////////////////////////////  (AGR) 9 September 08

		String	theFeedURLToUse = lastFeedURL;

		if (theFindFeedType.isEmpty())
		{
			theFeedURLToUse = lastFeedURL;
		}
		else
		{
			List<Feed>	theFeeds = HTMLParserTest.discoverFeeds( lastBlogURL, theFindFeedType);

			if ( theFeeds == null || theFeeds.isEmpty())
			{
				if ( theErrorIfFindFeedFails == 1)
				{
					return new SubmissionResult( "Sorry, could not find a " + theFindFeedType + " feed", SubmitBlogResultCode.FEED_URL_NOT_FOUND);
				}
			}
			else
			{
				System.out.println("... " + theFeeds);

				theFeedURLToUse = theFeeds.get(0).getURL();
			}
		}

		//////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////

		DataSourceConnection	theConnectionObject = null;
		CallableStatement	theStatement = null;
		SubmissionResult	theResult = new SubmissionResult();
		long			theStartTimeMS = System.currentTimeMillis();
		boolean			mayRequestNearbyBloggers = false;

		try
		{
			theConnectionObject = new DataSourceConnection(inDS);
			if (theConnectionObject.Connect())
			{
				theConnectionObject.setAutoCommit(false);	// (AGR) 28 April 2007

				//////////////////////////////////////////////////////////////////  (AGR) 11 Feb 2007

				String		adjustedPostCodeStr;
				double		theLatValToUse = -1;
				double		theLongValToUse = -1;

				if ( thePostCodeResult == PostCodeResult.VALID)
				{
					adjustedPostCodeStr = lastPostCode.trim().toUpperCase( Locale.UK );

					//////////////////////////////////////////////////////////////////  (AGR) 16 Feb 2007

					try
					{
						PostCodeGeocoder	theGeocoder = new PostCodeGeocoder( LRUPostCodeGeocoderCache.getInstance() );
						Location		theDiscoveredLocation = theGeocoder.lookupLocation(adjustedPostCodeStr);

						inLogger.debug("Location: " + theDiscoveredLocation);

						if (theDiscoveredLocation.isBlank())
						{
							theResult.setGeocodingResult( GeocodingResultCode.GEOCODING_NOT_FOUND );
						}
						else
						{
							theLatValToUse = theDiscoveredLocation.getLatitude();
							theLongValToUse = theDiscoveredLocation.getLongitude();

							theResult.setGeocodingResult( GeocodingResultCode.GEOCODING_FOUND );	// (AGR) 18 Feb 2007

							/////////////////////////////////////////////////////////////////////////////////////////

							String	showNearbyBloggersVal = inRequest.getParameter("showNearbyBloggers");

							mayRequestNearbyBloggers = ( showNearbyBloggersVal != null && showNearbyBloggersVal.equals("1") || Boolean.parseBoolean(showNearbyBloggersVal));
						}
					}
					catch (SocketTimeoutException e)
					{
						inLogger.error("Geocoding timed-out: " + e.getMessage());

						theResult.setGeocodingResult( GeocodingResultCode.LOOKUP_ERROR );
					}
					catch (Exception e)
					{
						inLogger.error("Create/PC", e);

						theResult.setGeocodingResult( GeocodingResultCode.LOOKUP_ERROR );		// (AGR) 18 Feb 2007
					}
				}
				else
				{
					adjustedPostCodeStr = null;
				}

				//////////////////////////////////////////////////////////////////

				int	i= 1;

				theStatement = theConnectionObject.prepareCall("submitSite(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				theStatement.setString( i++, lastBlogName);
				theStatement.setString( i++, lastBlogURL);
				theStatement.setString( i++, theFeedURLToUse);
				theStatement.setString( i++, lastDescr);
				theStatement.setInt( i++, r_isBlogVal);
				theStatement.setInt( i++, r_siteCatVal);
				theStatement.setInt( i++, r_siteLocVal);
				theStatement.setDouble( i++, theLatValToUse);
				theStatement.setDouble( i++, theLongValToUse);
				theStatement.setString( i++, adjustedPostCodeStr);
				theStatement.setString( i++, lastFNames);
				theStatement.setString( i++, lastSurname);
				theStatement.setString( i++, lastEmail);
				theStatement.setInt( i++, r_statusVal);

				int	theFirstOutParamIndex = i;

				theStatement.registerOutParameter( i++, Types.BIGINT);
				theStatement.registerOutParameter( i, Types.INTEGER);

				theStatement.execute();

			//	SQLWarning	theSW = theStatement.getWarnings();

				//////////////////////////////////////////////////////////////////

				long	theSiteRecno = theStatement.getLong(theFirstOutParamIndex);
				int	theStatusCode = theStatement.getInt( theFirstOutParamIndex + 1);

				switch (theStatusCode)
				{
					case 0:
						theConnectionObject.commit();

				//////////////////////////////////////////////////////////////////

						if (mayRequestNearbyBloggers)
						{
							CallableStatement	theNearbyBloggersS = null;

					try
					{
						theNearbyBloggersS = theConnectionObject.prepareCall("findNearestBloggers( ?, ?, ?, ? )");
								theNearbyBloggersS.setLong( 1, theSiteRecno);
								theNearbyBloggersS.setDouble( 2, theLatValToUse);
								theNearbyBloggersS.setDouble( 3, theLongValToUse);
						theNearbyBloggersS.setInt( 4, 10);
						theNearbyBloggersS.execute();

						ResultSetList	theRSL = new ResultSetList( theNearbyBloggersS.getResultSet() );

								inLogger.info("Nearby bloggers = " + theRSL);

						theResult.setNearbyBloggers(theRSL);
					}
							catch (Exception e)
							{
								inLogger.error("", e);
							}
							finally
							{
								if ( theNearbyBloggersS != null)
								{
									try {
										theNearbyBloggersS.close();
									} catch (Exception e) {}
								}
					}
				}

						//////////////////////////////////////////////////////////////////

						theResult.setNewSiteRecno(theSiteRecno);
				theResult.setMessageAndCode( "Entry submitted successfully. It will be reviewed and, if suitable, will appear in the list soon.",
								SubmitBlogResultCode.OK);
						break;
					case -1:
						theResult.setMessageAndCode( "Blog URL \"" + lastBlogURL + "\" already in use!", SubmitBlogResultCode.DUPLICATE_BLOG_URL);
						break;
					case -2:
						theResult.setMessageAndCode( "There is already a blog named \"" + lastBlogName + "\"!", SubmitBlogResultCode.DUPLICATE_BLOG_NAME);
						break;
					case -7:
						theResult.setMessageAndCode( "Invalid Location code (" + r_siteLocVal + ") !", SubmitBlogResultCode.INVALID_LOCATION);
						break;
					case -8:
						theResult.setMessageAndCode( "Invalid Creator status code (" + r_statusVal + ") !", SubmitBlogResultCode.INVALID_CREATOR_STATUS);
						break;
					case -9:
						theResult.setMessageAndCode( "Invalid Site Category code (" + r_siteCatVal + ") !", SubmitBlogResultCode.INVALID_SITE_CATEGORY);
						break;
					default:
						theResult.setMessageAndCode( "An error occurred!", SubmitBlogResultCode.DB_ERROR_1);
						break;
				}
			}
			else
			{
				inLogger.warn("Cannot connect to Database!");

				theResult.setMessageAndCode( "Cannot connect to database", SubmitBlogResultCode.DB_CONN_ERROR);
			}
		}
		catch (Exception err)
		{
			inLogger.error("Database error", err);

			theResult.setMessageAndCode( "Database error", SubmitBlogResultCode.DB_ERROR_1);

			////////////////////////////////////////////////////////

			try {
				// m_Logger.info("ROLLBACK!");

				// (AGR) 28 April 2007. Sadly rollback() has no effect - not in MyISAM tables anyway, but I'm not switching
				// to "innoDB" across the board just for this!

				theConnectionObject.rollback();
			} catch (SQLException ee) {
				inLogger.error("Rollback error", ee);
			}
		}
		finally
		{
			USQL_Utils.closeStatementCatch(theStatement);

			////////////////////////////////////////////////////////

			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
			}
		}

		//////////////////////////////////////////////////////////////////

		theResult.setElapsedTime( System.currentTimeMillis() - theStartTimeMS);

		return theResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	protected static EnumSet<FeedType> _findFeedTypes( final String inStr)
	{
		if ( inStr.length() >= 1)
		{
			if ( inStr.startsWith("ALL_POSTS"))
			{
				return EnumSet.of( FeedType.RSS, FeedType.ATOM, FeedType.RSD);
			}
			else if ( inStr.startsWith("RSS"))
			{
				return EnumSet.of(FeedType.RSS);
			}
			else if (inStr.startsWith("ATOM"))
			{
				return EnumSet.of(FeedType.ATOM);
			}
			else if (inStr.startsWith("RSD"))
			{
				return EnumSet.of(FeedType.RSD);
			}
			else if (inStr.startsWith("FOAF"))
			{
				return EnumSet.of(FeedType.FOAF);
			}
		}

		return EnumSet.noneOf(FeedType.class);
	}
}