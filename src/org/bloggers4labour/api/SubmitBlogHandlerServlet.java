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
import java.sql.Connection;
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
	private final static String	EVIL_APPEAL_STATEMENT = "If you disagree with this decision, please contact us.";	// (AGR) 8 December 2006

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
		CharSequence	theOutputBuffer = null;
		boolean		keepAlive = true;

		try
		{
			MainServlet.ensureNotCached(inResponse);

			inResponse.setLocale( MainServlet.GetClientLocale(inRequest) );

			////////////////////////////////////////////////////////

			String			outputFormatStr = inRequest.getParameter("outputFormat");
			SubmissionResult	theResult = _handleSubmission( m_DataSource, inRequest, inResponse);

			// s_Servlet_Logger.info("returned: " + theResult);

			StringBuilder		ourBuilder = new StringBuilder(1000);

			if (UText.isNullOrBlank(outputFormatStr) || outputFormatStr.equalsIgnoreCase("XML"))
			{
				inResponse.setContentType("text/xml");

				ourBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				ourBuilder.append("<B4L>");

				MainServlet._addXMLElement( ourBuilder, "code", theResult.m_Code);
				MainServlet._addXMLCDataElement( ourBuilder, "message", theResult.m_Message);
				MainServlet._addXMLElement( ourBuilder, "new_blog_id", theResult.m_SiteRecno);
				MainServlet._addXMLElement( ourBuilder, "geocoded_OK", theResult.m_GeocodingSucceeded);

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

				MainServlet._addXMLElement( ourBuilder, "nearby_blogs", bloggersBuf);	// can be empty

				ourBuilder.append("</B4L>");
			}
			else if (outputFormatStr.equalsIgnoreCase("JS"))
			{
				inResponse.setContentType("text/javascript");

				ourBuilder.append("{ ");
				ourBuilder.append("code: \"").append( theResult.m_Code ).append("\",");
				ourBuilder.append("message: \"").append( theResult.m_Message ).append("\",");
				ourBuilder.append("new_blog_id: \"").append( theResult.m_SiteRecno ).append("\",");
				ourBuilder.append("geocoded_OK: \"").append( theResult.m_GeocodingSucceeded ).append("\",");
				ourBuilder.append("nearby_blogs: [");	// nearby_bloggers STARTS

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

			// s_Servlet_Logger.info("theOutputBuffer = " + theOutputBuffer);
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
			return new SubmissionResult( "Your Blog's name is unacceptable. Please try again. " + EVIL_APPEAL_STATEMENT, SubmitBlogResultCode.UNACCEPTABLE_BLOG_NAME);
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
			return new SubmissionResult( "Your Post Code doesn't seem to be valid. Please try again. " + EVIL_APPEAL_STATEMENT, SubmitBlogResultCode.UNACCEPTABLE_POSTCODE);
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
		CallableStatement	theNearbyBloggersS = null;
		Statement		theStatement = null;
		SubmissionResult	theResult = new SubmissionResult();

		try
		{
			theConnectionObject = new DataSourceConnection(inDS);
			if (theConnectionObject.Connect())
			{
				theConnectionObject.setAutoCommit(false);	// (AGR) 28 April 2007

/*				s_Servlet_Logger.info("Commit?: " + theConnectionObject.getConnection().getAutoCommit());
				s_Servlet_Logger.info("Curr TL: " + theConnectionObject.getConnection().getTransactionIsolation());
				theConnectionObject.getConnection().setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE );
				s_Servlet_Logger.info("New TL:  " + theConnectionObject.getConnection().getTransactionIsolation());
*/
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

					int	glInsertCount = theStatement.executeUpdate( theGLocQueryBuf.toString(), Statement.RETURN_GENERATED_KEYS);

					if ( glInsertCount != 1)
					{
						throw new SQLException("glInsertCount is " + glInsertCount);
					}

					//////////////////////////////////////////////////////////////////

					ResultSet	theGLocRS = theStatement.getGeneratedKeys();

					if (theGLocRS.next())
					{
						theGLocRecno = theGLocRS.getLong(1);
						s_Servlet_Logger.debug("UNIQUE_ID for geoLocation = " + theGLocRecno);
					}
					else
					{
						throw new SQLException("Can't get ID of created geoLocation");
					}

					theGLocRS.close();
					theGLocRS = null;
				}
				else
				{
					adjustedPostCodeStr = null;
				}

				//////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////

				StringBuilder	theCreatorQueryBuf = new StringBuilder("INSERT INTO creator VALUES (NULL,");

				theCreatorQueryBuf.append( USQL_Utils.getQuoted(lastFNames) ).append(',')
						  .append( USQL_Utils.getQuoted(lastSurname) ).append(',')
						  .append( USQL_Utils.getQuoted(lastEmail) ).append(',')
						  .append( String.valueOf(r_statusVal) + ',')
						  .append( "0" )
						  .append(")");

				int	crInsertCount = theStatement.executeUpdate( theCreatorQueryBuf.toString(), Statement.RETURN_GENERATED_KEYS);

				if ( crInsertCount != 1)
				{
					throw new SQLException("crInsertCount is " + crInsertCount);
				}

				//////////////////////////////////////////////////////////////////

				ResultSet	theLastRS = theStatement.getGeneratedKeys();
				long		theCreatorID;

				if (theLastRS.next())
				{
					theCreatorID = theLastRS.getLong(1);
					s_Servlet_Logger.debug("UNIQUE_ID for creator = " + theCreatorID);
				}
				else
				{
					throw new SQLException("Can't get ID of created Creator");
				}

				theLastRS.close();	// (AGR) 15 Feb 2007

				//////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////

				StringBuilder	theQueryBuf = new StringBuilder("INSERT INTO site VALUES (NULL,");
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

				theStatement.executeUpdate( theQueryBuf.toString(), Statement.RETURN_GENERATED_KEYS);

				//////////////////////////////////////////////////////////////////  (AGR) 28 April 2007

				long	theNewSiteID;

				theLastRS = theStatement.getGeneratedKeys();
				if (theLastRS.next())
				{
					theNewSiteID = theLastRS.getLong(1);

					theResult.setNewSiteRecno(theNewSiteID);	// (AGR) 29 April 2007

					s_Servlet_Logger.debug("UNIQUE_ID for site = " + theNewSiteID);
				}
				else
				{
					throw new SQLException("Can't get ID of created Site");
				}

				theLastRS.close();
				theLastRS = null;

				//////////////////////////////////////////////////////////////////

				int	scCount = theStatement.executeUpdate("INSERT INTO siteCreators VALUES (NULL," + theNewSiteID + "," + theCreatorID + ")");

				if ( scCount != 1)
				{
					throw new SQLException("scCount is " + scCount);
				}

				//////////////////////////////////////////////////////////////////

				USQL_Utils.closeStatementCatch(theStatement);
				theStatement = null;

				//////////////////////////////////////////////////////////////////  (AGR) 16 Feb 2007

				if ( theDiscoveredLocation != null && !theDiscoveredLocation.isBlank())
				{
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
					catch (SQLException e)
					{
						throw e;	// Ensure rollback
					}
					catch (Exception e2)
					{
						s_Servlet_Logger.error("", e2);    // Presumably not serious enough to demand rollback
					}
				}

				//////////////////////////////////  (AGR) 28 April 2007. Success!

				// s_Servlet_Logger.info("COMMIT!!");

				theConnectionObject.commit();

				theResult.setMessageAndCode( "Entry submitted successfully. It will be reviewed and, if suitable, will appear in the list soon.",
								SubmitBlogResultCode.OK);
			}
			else
			{
				s_Servlet_Logger.warn("Cannot connect!");

				theResult.setMessageAndCode( "Cannot connect to database", SubmitBlogResultCode.DB_CONN_ERROR);
			}
		}
		catch (Exception err)
		{
			s_Servlet_Logger.error("Database error", err);

			theResult.setMessageAndCode( "Database error", SubmitBlogResultCode.DB_ERROR_1);

			////////////////////////////////////////////////////////

			try {
				// s_Servlet_Logger.info("ROLLBACK!");

				// (AGR) 28 April 2007. Sadly rollback() has no effect - not in MyISAM tables anyway, but I'm not switching
				// to "innoDB" across the board just for this!

				theConnectionObject.rollback();
			} catch (SQLException ee) {
				s_Servlet_Logger.error("Rollback error", ee);
			}
		}
		finally
		{
			USQL_Utils.closeStatementCatch(theStatement);

			////////////////////////////////////////////////////////

			if ( theNearbyBloggersS != null)
			{
				try {
					// s_Servlet_Logger.info("... closing " + theNearbyBloggersS);

					theNearbyBloggersS.close();
					theNearbyBloggersS = null;
				} catch (Exception e) {}
			}

			////////////////////////////////////////////////////////

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
		protected Long			m_SiteRecno;

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

		/*******************************************************************************
		*******************************************************************************/
		public void setNewSiteRecno( final Long x)
		{
			m_SiteRecno = x;
		}
	}
}