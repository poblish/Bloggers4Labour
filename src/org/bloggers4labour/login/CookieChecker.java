/*
 * CookieChecker.java
 *
 * Created on 21 April 2007, 16:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.login;

import com.hiatus.USQL_Utils;
import com.hiatus.UText;
import com.hiatus.sql.ResultSetList;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.http.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationManager;
import org.bloggers4labour.sql.DataSourceConnection;

/**
 *
 * @author andrewre
 */
public class CookieChecker
{
	private long			m_LoggedInUserRecno;
	private String			m_LoggedInUsername;

	private static Logger		s_Logger = Logger.getLogger("Main");	// Logger.getLogger( CookieChecker.class );

	private final static String	LOGIN_COOK_NAME = "B4L.autologin";
	private final static String	STRING_ENCODING = "UTF-8";

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public CookieChecker()
	{
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public LoginResult checkLogin( final HttpServletRequest inReq, HttpSession ioSession)
	{
		Cookie[]	theLoadedCookies = inReq.getCookies();

		if ( theLoadedCookies != null)
		{
			// s_Logger.info("theLoadedCookies: " + theLoadedCookies);

			for ( Cookie eachCookie : theLoadedCookies)
			{
				s_Logger.info("COOKIE \"" + eachCookie.getName() + "\" / value \"" + eachCookie.getValue() + "\"");

				if (eachCookie.getName().equals(LOGIN_COOK_NAME))
				{
					String	theCookieVal = eachCookie.getValue();

					if (UText.isValidString(theCookieVal))
					{
						int	idx = theCookieVal.indexOf('.');

						if ( idx > 0)
						{
							String	userId = theCookieVal.substring( 0, idx);
							String	encodedHashedPass = theCookieVal.substring( idx + 1);

							s_Logger.info("user \"" + userId + "\", enc_h_p \"" + encodedHashedPass + "\".");

							try
							{
								byte[]	theHashedPassBytes = Base64.decodeBase64( encodedHashedPass.getBytes(STRING_ENCODING) );
								String	theHashedPassStr = new String( theHashedPassBytes, STRING_ENCODING);

								s_Logger.info("=> h_p \"" + theHashedPassStr + "\".");

								if (_checkCookie( eachCookie, userId, theHashedPassStr, ioSession))
								{
									return LoginResult.VALID_COOKIE_FOUND;
								}
							}
							catch (UnsupportedEncodingException e)
							{
								;
							}
						}
					}

					// Remove this bad cookie!
				}
			}
		}

		////////////////////////////////////////////////////////////////

		Object	theObj = ioSession.getAttribute("b4l_user_recno");

		if ( theObj == null || !(theObj instanceof Long))
		{
			// response.sendRedirect("http://www.bloggers4labour.org/login.jsp");
			return LoginResult.NOT_LOGGED_IN;
		}

		m_LoggedInUserRecno = ((Long) theObj).longValue();
		m_LoggedInUsername = (String) ioSession.getAttribute("b4l_user_name");

		return LoginResult.VALID_SESSION_FOUND;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	private boolean _checkCookie( Cookie ioCookie, final String inUserId, final String inHashedPassStr, HttpSession ioSession)
	{
		DataSourceConnection	theConnectionObject = null;

		try
		{
			theConnectionObject = new DataSourceConnection( InstallationManager.getDefaultInstallation().getDataSource() );
			if (theConnectionObject.Connect())
			{
				Statement	theS = null;

				try
				{
					theS = _checkCookieInDatabase( theConnectionObject, inUserId, inHashedPassStr, ioSession);
					return true;
				}
				catch (Exception e)
				{
					s_Logger.error("creating Cookie statement", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theS);
				}
			}
			else
			{
				s_Logger.warn("Cannot connect!");
			}
		}
		catch (Exception err)
		{
			s_Logger.error("???", err);

		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
				theConnectionObject = null;
			}
		}

		return false;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	private CallableStatement _checkCookieInDatabase( final DataSourceConnection inConn,
							  final String inUserId, final String inHashedPassStr,
							  HttpSession ioSession) throws SQLException
	{
		CallableStatement	theS = inConn.prepareCall("checkCookieLogin( ?, ? )");

		theS.setString( 1, inUserId);
		theS.setString( 2, inHashedPassStr);

		boolean	x = theS.execute();

		ResultSet	theRS = theS.getResultSet();

//		s_Logger.info("theRS " + new ResultSetList(theRS) + ", x = " + x);

		if ( x && theRS.next())
		{
			s_Logger.info("Populating Session...");

			ioSession.setAttribute( "b4l_user_recno", Long.valueOf(inUserId));
			ioSession.setAttribute( "b4l_user_name", theRS.getString("username"));
			ioSession.setAttribute( "b4l_prev_login_date", theRS.getTimestamp("last_login"));
		}
		else
		{
			s_Logger.info("Clearing Session...");

			ioSession.removeAttribute( "b4l_user_recno");
			ioSession.removeAttribute( "b4l_user_name");
			ioSession.removeAttribute( "b4l_prev_login_date");
		}

//		m_ResultsCopy = new ResultSetList(theRS);

//		s_Logger.info("m_ResultsCopy " + m_ResultsCopy);

		theRS.close();

		return theS;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public Cookie createCookie( final HttpServletResponse ioResponse, final long inUserRecno,
				    final String inUsername, final String inHashedPassStr)
	{
		try
		{
			byte[]	theEncodedHashedPassBytes = Base64.encodeBase64( inHashedPassStr.getBytes(STRING_ENCODING) );
			String	theEncodedHashedPassStr = new String( theEncodedHashedPassBytes, STRING_ENCODING);

			s_Logger.info("=> enc_h_p \"" + theEncodedHashedPassStr + "\".");

			Cookie	theCookie = new Cookie( LOGIN_COOK_NAME, inUserRecno + "." + theEncodedHashedPassStr);
			theCookie.setMaxAge(31557600);    // approx number of secs in a year
			theCookie.setComment("This cookie lets you set preferences without having to keep logging-in.");

			s_Logger.info("=> theCookie: " + theCookie);

			ioResponse.addCookie(theCookie);

			return theCookie;
		}
		catch (UnsupportedEncodingException e)
		{
			return null;	// Not going to happen
		}
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public long getUserRecno()
	{
		return m_LoggedInUserRecno;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public String getUsername()
	{
		return m_LoggedInUsername;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public static void main( String[] args)
	{
		String	s = "159.AFC03462744589345D";
		int	idx = s.indexOf('.');

		if ( idx > 0)
		{
			System.out.println("id   = \"" + s.substring(0,idx) + "\"");
			System.out.println("pass = \"" + s.substring(idx + 1) + "\"");
		}
	}
}
