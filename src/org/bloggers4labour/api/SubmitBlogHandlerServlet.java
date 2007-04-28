/*
 * SubmitBlogHandlerServlet.java
 *
 * Created on 27 April 2007, 23:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.api;

import com.hiatus.USQL_Utils;
import com.hiatus.UText;
import com.hiatus.sql.ResultSetList;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import javax.servlet.http.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationManager;
import org.bloggers4labour.MainServlet;
import org.bloggers4labour.geocode.Location;
import org.bloggers4labour.geocode.PostCodeGeocoder;
import org.bloggers4labour.sql.DataSourceConnection;
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
	private final static String	evilAppealStatementStr = "If you disagree with this decision, please <a class=\"col\" target=\"_blank\" href=\"http://www.bloggers4labour.org/contact.jsp\">get in touch</a> with us.";	// (AGR) 8 December 2006

	/*******************************************************************************
	*******************************************************************************
	public SubmitBlogHandlerServlet()
	{
		super();
	} */

	/*******************************************************************************
	*******************************************************************************/
	public void doGet( HttpServletRequest inRequest, HttpServletResponse inResponse)
	{
		StringBuilder	theOutputBuffer = null;
		boolean		keepAlive = true;

		try
		{
			SubmissionResult	theResult = _handleSubmission( m_DataSource, inRequest, inResponse);

			// s_Servlet_Logger.info("returned: " + theResult);

			theOutputBuffer = new StringBuilder();
			theOutputBuffer.append("{ ");
			theOutputBuffer.append("code: \"").append( theResult.m_Code ).append("\",");
			theOutputBuffer.append("message: \"").append( theResult.m_Message ).append("\",");
			theOutputBuffer.append("geocoded_OK: \"").append( theResult.m_GeocodingSucceeded ).append("\",");
			theOutputBuffer.append("nearby_bloggers: [");	// nearby_bloggers STARTS

			ResultSetList	rsl = theResult.m_Results;

			if ( rsl != null)
			{
				List	theRows = rsl.getRowsList();

				if ( theRows.size() > 0)
				{
					for ( Object eachRowObj : theRows)
					{
						Map	m = (Map) eachRowObj;

						theOutputBuffer.append("{ ");
						theOutputBuffer.append("name: \"").append( m.get("name") ).append("\"");
						theOutputBuffer.append(", url: \"").append( m.get("url") ).append("\"");
						theOutputBuffer.append("}");
					}
				}
			}

			theOutputBuffer.append("]");	// nearby_bloggers ENDS

			theOutputBuffer.append("}");

			s_Servlet_Logger.info("theOutputBuffer = " + theOutputBuffer);
		}
		catch (Exception e)
		{
			s_Servlet_Logger.error("SubmitBlogHandlerServlet.doGet", e);
		}
		finally
		{
			MainServlet.myOutputBuffer( inResponse, theOutputBuffer, keepAlive);
			MainServlet.Finish(inResponse);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private SubmissionResult _handleSubmission( final DataSource inDS, final HttpServletRequest request, final HttpServletResponse response)
	{
		PostCodeValidator	postCodeValidator = new PostCodeValidator();
		PostCodeGeocoder	postCodeGeocoder = new PostCodeGeocoder();

		String			theResultMessage;

		///////////////////////////////////////////////////

		String		HTTP_STR = "http://";
		String		lastFNames = request.getParameter("fnames");
		String		lastSurname = request.getParameter("sname");
		String		lastBlogURL = request.getParameter("blog_url");
		String		lastFeedURL = request.getParameter("feed_url");
		String		lastBlogName = request.getParameter("blog_name");
		String		lastDescr = request.getParameter("blog_descr");
		String		lastEmail = request.getParameter("email");
		String		lastPostCode = request.getParameter("postCode");
		int		r_isBlogVal = Integer.parseInt( request.getParameter("is_blog") );
		int		r_siteCatVal = Integer.parseInt( request.getParameter("site_category") );
		int		r_siteLocVal = Integer.parseInt( request.getParameter("location") );
		int		r_statusVal = Integer.parseInt( request.getParameter("status") );

		if ( lastDescr != null)
		{
			lastDescr = lastDescr.trim();
		}

		if ( lastFeedURL != null && lastFeedURL.startsWith(HTTP_STR))
		{
			lastFeedURL = lastFeedURL.trim();
		}

		if (UText.isValidString(lastFNames))
		{
			lastFNames = lastFNames.trim();
		}

		if (UText.isValidString(lastSurname))
		{
			lastSurname = lastSurname.trim();
		}

		if ( UText.isValidString(lastBlogURL) && lastBlogURL.startsWith(HTTP_STR))
		{
			lastBlogURL = lastBlogURL.trim();
		}

		if (UText.isValidString(lastEmail))
		{
			lastEmail = lastEmail.trim();
		}

		//////////////////////////////////////////////////////////////////

		if (evilNamesPattern.matcher(lastBlogName).find())	// (AGR) 18 June 2006
		{
			return new SubmissionResult( "Your Blog's name is unacceptable. Please try again. " + evilAppealStatementStr, SubmitBlogResultCode.UNACCEPTABLE_BLOG_NAME);
		}

		if (UText.isNullOrBlank(lastBlogName))
		{
			return new SubmissionResult( "Please specify a Blog name", SubmitBlogResultCode.MISSING_BLOG_NAME);
		}

		lastBlogName = lastBlogName.trim();

		//////////////////////////////////////////////////////////////////

		PostCodeResult	thePostCodeResult = postCodeValidator.validate(lastPostCode);

		if ( thePostCodeResult == PostCodeResult.INVALID)
		{
			return new SubmissionResult( "Your Post Code doesn't seem to be valid. Please try again. " + evilAppealStatementStr, SubmitBlogResultCode.UNACCEPTABLE_POSTCODE);
		}

		//////////////////////////////////////////////////////////////////  (AGR) 5 July 2006

		if ( evilNamesPattern.matcher(lastDescr).find() ||
		     noLinksAllowedPattern.matcher(lastDescr).find())
		{
			return new SubmissionResult( "Your Blog's description is unacceptable. Please try again. " + evilAppealStatementStr, SubmitBlogResultCode.UNACCEPTABLE_DESCRIPTION);
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
			return new SubmissionResult( "Your Blog's URL is unacceptable. Please try again. " + evilAppealStatementStr, SubmitBlogResultCode.UNACCEPTABLE_BLOG_URL);
		}

		if (!lastBlogURL.startsWith(HTTP_STR))
		{
			return new SubmissionResult( "The Blog URL must start with: \"http://\"", SubmitBlogResultCode.UNACCEPTABLE_BLOG_URL_PREFIX);
		}

		//////////////////////////////////////////////////////////////////

		if ( UText.isValidString(lastFeedURL) && !lastFeedURL.startsWith(HTTP_STR))
		{
			return new SubmissionResult( "The Feed URL must start with: \"http://\"", SubmitBlogResultCode.UNACCEPTABLE_FEED_URL_PREFIX);
		}

		//////////////////////////////////////////////////////////////////

		if (UText.isNullOrBlank(lastEmail))
		{
			return new SubmissionResult( "Please provide a contact email address", SubmitBlogResultCode.MISSING_EMAIL);
		}

		//////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////

		DataSourceConnection	theConnectionObject = null;
		Statement		theStatement = null;
		SubmissionResult	theResult = new SubmissionResult();

		try
		{
			theConnectionObject = new DataSourceConnection(inDS);
			if (theConnectionObject.Connect())
			{
				try
				{
					theStatement = theConnectionObject.createStatement();

					//////////////////////////////////////////////////////////////////

					String	theURLToLookup;

					if (lastBlogURL.startsWith("http://www."))
					{
						theURLToLookup = lastBlogURL.substring(11);
					}
					else if (lastBlogURL.startsWith(HTTP_STR))
					{
						theURLToLookup = lastBlogURL.substring(7);
					}
					else	theURLToLookup = lastBlogURL;

					if (theURLToLookup.endsWith("/"))
					{
						theURLToLookup = theURLToLookup.substring( 0, theURLToLookup.length() - 1);
					}

					//////////////////////////////////////////////////////////////////

					String		theURLBit = "IF( LOCATE(\"http://www\",url)=1, SUBSTRING(url, LOCATE(\"http://www\",url)+11, LENGTH(url)-11), IF( LOCATE(\"http://\",url)=1, SUBSTRING(url, LOCATE(\"http://\",url)+7, LENGTH(url)-7), url))";
					String		theCheckQuery = "SELECT " + theURLBit + " AS 'got_url',name FROM site WHERE " + theURLBit + "=" + USQL_Utils.getQuoted(theURLToLookup) + " OR name=" + USQL_Utils.getQuoted(lastBlogName);
					ResultSet	theIDCheckRS = null;

					// System.out.println("theCheckQuery = " + theCheckQuery);

					try
					{
						theIDCheckRS = theStatement.executeQuery(theCheckQuery);
						if (theIDCheckRS.next())
						{
							boolean	x = ( UText.isValidString(lastBlogURL) && theIDCheckRS.getString("got_url").equals(theURLToLookup));

							if (x)
							{
								theResult.setMessageAndCode( "Blog URL \"" + lastBlogURL + "\" already in use!", SubmitBlogResultCode.DUPLICATE_BLOG_URL);
							}
							else
							{
								theResult.setMessageAndCode( "There is already a blog named \"" + lastBlogName + "\"!", SubmitBlogResultCode.DUPLICATE_BLOG_NAME);
							}

							return theResult;
						}
					}
					catch (SQLException e)
					{
						s_Servlet_Logger.error("Look in site table", e);

						theResult.setMessageAndCode( "Database error", SubmitBlogResultCode.DB_ERROR_3);

						return theResult;
					}
					finally
					{
						USQL_Utils.closeResultSetCatch(theIDCheckRS);
					}

					//////////////////////////////////////////////////////////////////  (AGR) 11 Feb 2007

					long		theGLocRecno = 1;	// default "n/a" record
					String		adjustedPostCodeStr;
					Location	theDiscoveredLocation = null;

					if ( thePostCodeResult == PostCodeResult.VALID)
					{
						StringBuilder	theGLocQueryBuf = null;

						adjustedPostCodeStr = lastPostCode.trim().toUpperCase();

						//////////////////////////////////////////////////////////////////  (AGR) 16 Feb 2007

						boolean	gotGoodResult = false;

						try
						{
							theDiscoveredLocation = postCodeGeocoder.lookupLocation(adjustedPostCodeStr);

							s_Servlet_Logger.info("Location: " + theDiscoveredLocation);

							if (!theDiscoveredLocation.isBlank())
							{
								gotGoodResult = true;

								theGLocQueryBuf = new StringBuilder("INSERT INTO geoLocation (gloc_exact,gloc_latitude,gloc_longitude,gloc_description) VALUES (1,");
								theGLocQueryBuf.append( theDiscoveredLocation.getLatitude() ).append(",")
										.append( theDiscoveredLocation.getLongitude() ).append(",")
										.append( USQL_Utils.getQuoted(adjustedPostCodeStr) ).append(")");

								theResult.setGeocodingResult( Boolean.TRUE );	// (AGR) 18 Feb 2007
							}
						}
						catch (Exception e)
						{
							s_Servlet_Logger.error("Create/PC", e);

							theResult.setGeocodingResult( Boolean.FALSE );		// (AGR) 18 Feb 2007
						}

						if (!gotGoodResult)
						{
							theGLocQueryBuf = new StringBuilder("INSERT INTO geoLocation (gloc_exact,gloc_description) VALUES (1,");
							theGLocQueryBuf.append( USQL_Utils.getQuoted(adjustedPostCodeStr) ).append(")");
						}

						//////////////////////////////////////////////////////////////////

						theStatement.executeUpdate( theGLocQueryBuf.toString() );

						ResultSet	theGLocRS = theStatement.executeQuery("SELECT LAST_INSERT_ID() FROM geoLocation");

						if (theGLocRS.next())
						{
							theGLocRecno = theGLocRS.getLong(1);
						}

						theGLocRS.close();
					}
					else
					{
						adjustedPostCodeStr = null;
					}

					//////////////////////////////////////////////////////////////////

					StringBuilder	theCreatorQueryBuf = new StringBuilder("INSERT INTO creator VALUES (NULL,");

					theCreatorQueryBuf.append( USQL_Utils.getQuoted(lastFNames) ).append(',')
							  .append( USQL_Utils.getQuoted(lastSurname) ).append(',')
							  .append( USQL_Utils.getQuoted(lastEmail) ).append(',')
							  .append( String.valueOf(r_statusVal) + ',')
							  .append( "0" )
							  .append(")");

					theStatement.executeUpdate( theCreatorQueryBuf.toString() );

					ResultSet	theLastRS = theStatement.executeQuery("SELECT LAST_INSERT_ID() FROM creator");
					long		theCreatorID;

					if (theLastRS.next())
					{
						theCreatorID = theLastRS.getLong(1);
					}
					else
					{
						throw new SQLException("Can't get ID of created Creator");
					}

					theLastRS.close();	// (AGR) 15 Feb 2007

					//////////////////////////////////////////////////////////////////

					StringBuffer	theQueryBuf = new StringBuffer("INSERT INTO site VALUES (NULL,");
					int		r_isApproved = 0;
					int		r_isDead = 0;
					int		r_showFeedOnPage = 1;

					theQueryBuf.append( USQL_Utils.getQuoted(lastBlogURL) ).append(',')
						   .append( USQL_Utils.getQuoted(lastFeedURL) ).append(",NULL,NULL,")
						   .append( USQL_Utils.getQuoted(lastBlogName) ).append(',')
						   .append( USQL_Utils.getQuoted(lastDescr) ).append(",NOW(),")
						   .append( String.valueOf(r_isBlogVal) + ',')
						   .append( String.valueOf(r_showFeedOnPage) + ',')
						   .append( String.valueOf(r_isApproved) + ',')
						   .append( String.valueOf(r_isDead) + ',')
						   .append( String.valueOf(r_siteCatVal) + ',')
						   .append( String.valueOf(r_siteLocVal) + ',')
						   .append( String.valueOf(theGLocRecno) )	// (AGR) 11 Feb 2007
						   .append(")");

					try
					{
						theStatement.executeUpdate( theQueryBuf.toString() );
						theResultMessage = theQueryBuf.toString();
					}
					catch (SQLException e)
					{
						theResultMessage = theQueryBuf + "<br/>" + e.toString();
					}

					//////////////////////////////////////////////////////////////////  (AGR) 15 Feb 2007

					long	theNewSiteID;

					theLastRS = theStatement.executeQuery("SELECT LAST_INSERT_ID() FROM site");

					if (theLastRS.next())
					{
						theNewSiteID = theLastRS.getLong(1);
					}
					else
					{
						throw new SQLException("Can't get ID of created Site");
					}

					theLastRS.close();

					//////////////////////////////////////////////////////////////////

					String	scInsertStr = "INSERT INTO siteCreators VALUES (NULL," + theNewSiteID + "," + theCreatorID + ")";

					try
					{
						int	scCount = theStatement.executeUpdate(scInsertStr);

						if ( scCount != 1)
						{
							throw new SQLException("scCount is " + scCount);
						}
					}
					catch (SQLException e)
					{
						theResultMessage = scInsertStr + "<br/>" + e.toString();
					}

					//////////////////////////////////////////////////////////////////

					USQL_Utils.closeStatementCatch(theStatement);
					theStatement = null;

					//////////////////////////////////////////////////////////////////  (AGR) 16 Feb 2007

					if ( theDiscoveredLocation != null && !theDiscoveredLocation.isBlank())
					{
						CallableStatement	theNearbyBloggersS = null;

						try
						{
							theNearbyBloggersS = theConnectionObject.prepareCall("findNearestBloggers( ?, ?, ?, ? )");
							theNearbyBloggersS.setLong( 1, theNewSiteID);
							theNearbyBloggersS.setDouble( 2, theDiscoveredLocation.getLatitude());
							theNearbyBloggersS.setDouble( 3, theDiscoveredLocation.getLongitude());
							theNearbyBloggersS.setInt( 4, 10);
							theNearbyBloggersS.execute();

							ResultSetList	theRSL = new ResultSetList( theNearbyBloggersS.getResultSet() );

							s_Servlet_Logger.info("theRSL = " + theRSL);

							theResult.setNearbyBloggers(theRSL);
						}
						catch (Exception e)
						{
							s_Servlet_Logger.error("", e);
						}
						finally
						{
							if ( theNearbyBloggersS != null)
							{
								try {
									theNearbyBloggersS.close();
									theNearbyBloggersS = null;
								} catch (Exception e) {}
							}
						}
					}

					//////////////////////////////////  Success!

					theResult.setMessageAndCode( "Entry submitted successfully. It will be reviewed and, if suitable, will appear in the list soon.",
									SubmitBlogResultCode.OK);
				}
				catch (Exception e)
				{
					s_Servlet_Logger.error("creating statement", e);

					theResult.setMessageAndCode( "Database error", SubmitBlogResultCode.DB_ERROR_2);
				}
			}
			else
			{
				s_Servlet_Logger.warn("Cannot connect!");

				theResult.setMessageAndCode( "Cannot connect to database", SubmitBlogResultCode.DB_CONN_ERROR);
			}
		}
		catch (Exception err)
		{
			s_Servlet_Logger.error("???", err);

			theResult.setMessageAndCode( "Database error", SubmitBlogResultCode.DB_ERROR_1);
		}
		finally
		{
			USQL_Utils.closeStatementCatch(theStatement);

			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
				theConnectionObject = null;
			}
		}

		//////////////////////////////////////////////////////////////////

		return theResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	class SubmissionResult
	{
		protected String		m_Message;
		protected SubmitBlogResultCode	m_Code;
		protected Boolean		m_GeocodingSucceeded;
		protected ResultSetList		m_Results;

		/*******************************************************************************
		*******************************************************************************/
		SubmissionResult()
		{
			m_Code = SubmitBlogResultCode.UNKNOWN;
		}

		/*******************************************************************************
		*******************************************************************************/
		SubmissionResult( final String inS, final SubmitBlogResultCode inCode)
		{
			m_Message = inS;
			m_Code = inCode;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setMessageAndCode( final String inS, final SubmitBlogResultCode inCode)
		{
			m_Message = inS;
			m_Code = inCode;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setGeocodingResult( final Boolean x)
		{
			m_GeocodingSucceeded = x;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setNearbyBloggers( final ResultSetList x)
		{
			m_Results = x;
		}
	}
}