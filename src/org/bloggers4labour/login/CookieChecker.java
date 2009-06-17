/*
 * CookieChecker.java
 *
 * Created on 21 April 2007, 16:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.login;

import com.hiatus.sql.USQL_Utils;
import com.hiatus.text.UText;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

	private static Logger		s_Logger = Logger.getLogger( CookieChecker.class );

	private final static String	LOGIN_COOK_NAME = "B4L.autologin";
	private final static String	PREFS_PREFIX = "prefs=";
	private final static String	STRING_ENCODING = "UTF-8";

	public final static String	SSN_USER_PREFERENCES = "b4l_user_prefs";

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public CookieChecker()
	{
	}

	/*******************************************************************************
		(AGR) 21 April 2007

		There are currently (29 May 2007) only three possible combinations of
		value types:

		  <user_recno>.<pass_hash_enc>                       (pre-29 May 2007)
		  prefs=<prefs_enc>|<user_recno>.<pass_hash_enc>     (logged-in, post-29 May 2007)
		  prefs=<prefs_enc>|                                 (not logged-in, post-29 May 2007)

		Where:

		  <user_recno> is a long integer
		  <pass_hash_enc> is a SHA-1-hashed, Base64-ed string without dots or | pipes, of unlimited length.
		  <prefs_enc> is a BigInteger bitmap, turned into a Base-36 string, without dots or | pipes, of unlimited length.
	*******************************************************************************/
	public LoginResult checkLogin( final HttpServletRequest inReq, HttpSession ioSession, HttpServletResponse ioResp)
	{
		BigInteger	currentPrefs = (BigInteger) ioSession.getAttribute(SSN_USER_PREFERENCES);
		Object		theObj = ioSession.getAttribute("b4l_user_recno");

		s_Logger.debug("... CURR PREFS: " + currentPrefs);

		if ( theObj != null && theObj instanceof Long)
		{
			m_LoggedInUserRecno = ((Long) theObj).longValue();
			m_LoggedInUsername = (String) ioSession.getAttribute("b4l_user_name");

			return LoginResult.VALID_SESSION_FOUND;
		}

		////////////////////////////////////////////////////////////////

		Cookie	ourCookie = _findOurCookie(inReq);

		if ( ourCookie != null)
		{
			String	theCookieVal = ourCookie.getValue();

			if (UText.isValidString(theCookieVal))
			{
				/////////////////////////////////////////////////////////////////  (AGR) 29 May 1007

				int	leftMargin = 0;
				String	thePrefsString = null;

				PrefsElementLocationIF	thePrefsLoc = findPrefsElement(theCookieVal);

				if ( thePrefsLoc != null)
				{
					leftMargin = thePrefsLoc.getEndLoc() + 1;

					if ( currentPrefs == null)
					{
						try
						{
							String	s = thePrefsLoc.getPrefsStr();

							// s_Logger.debug("=> prefs \"" + s + "\".");

							BigInteger	bi = new BigInteger(s);

							s_Logger.debug("=> parsed bi \"" + bi + "\".");

							ioSession.setAttribute( SSN_USER_PREFERENCES, bi);

							thePrefsString = s;
						}
						catch (RuntimeException e)
						{
						}
					}
				}

				/////////////////////////////////////////////////////////////////  (AGR) 29 May 1007

				if (UText.isNullOrBlank(thePrefsString))
				{
					s_Logger.debug("CHECK: initialising ssn Prefs");
					ioSession.setAttribute( SSN_USER_PREFERENCES, createDefaultPreferences());
				}

				/////////////////////////////////////////////////////////////////

				int	usernamePassSepDotPos = theCookieVal.indexOf('.', leftMargin);

				if ( usernamePassSepDotPos > 0)
				{
					String	userId = theCookieVal.substring( leftMargin, usernamePassSepDotPos);
					String	encodedHashedPass = theCookieVal.substring( usernamePassSepDotPos + 1);

					s_Logger.debug("user \"" + userId + "\", enc_h_p \"" + encodedHashedPass + "\".");

					try
					{
						byte[]	theHashedPassBytes = Base64.decodeBase64( encodedHashedPass.getBytes(STRING_ENCODING) );
						String	theHashedPassStr = new String( theHashedPassBytes, STRING_ENCODING);

						s_Logger.debug("=> h_p \"" + theHashedPassStr + "\".");

						if (_checkCookie( ourCookie, userId, theHashedPassStr, ioSession))
						{
							return LoginResult.VALID_COOKIE_FOUND;
						}
					}
					catch (UnsupportedEncodingException e)
					{
					}
					}
				}
			else	// (AGR) 30 May 2007
			{
				ourCookie = handleBasicCookie( inReq, ioResp);
			}

			// (AGR) 29 May 2007. No, we now allow non-logged-in cookies! ... ourCookie.setMaxAge(0);
		}
		else
		{
			/* ourCookie = */ handleBasicCookie( inReq, ioResp);	// (AGR) 30 May 2007
		}

		return LoginResult.NOT_LOGGED_IN;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	private Cookie _findOurCookie( final HttpServletRequest inReq)
	{
		Cookie[]	theLoadedCookies = inReq.getCookies();

		if ( theLoadedCookies != null)
		{
			s_Logger.debug("theLoadedCookies: " + Arrays.deepToString(theLoadedCookies));

			for ( Cookie eachCookie : theLoadedCookies)
			{
				s_Logger.debug("COOKIE \"" + eachCookie.getName() + "\" / value \"" + eachCookie.getValue() + "\"");

				if (eachCookie.getName().equals(LOGIN_COOK_NAME))
				{
					return eachCookie;
				}
			}
		}

		return null;
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
				catch (SQLException e)
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
		catch (RuntimeException err)
		{
			s_Logger.error("???", err);

		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
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

//		s_Logger.debug("theRS " + new ResultSetList(theRS) + ", x = " + x);

		if ( x && theRS.next())
		{
			Timestamp	thePrevLoginDate = theRS.getTimestamp("last_login");
			Long		userRecnoObj = Long.valueOf(inUserId);

			// s_Logger.debug("Populating Session...");

			m_LoggedInUserRecno = userRecnoObj.longValue();
			m_LoggedInUsername = theRS.getString("username");

			ioSession.setAttribute( "b4l_user_recno", userRecnoObj);
			ioSession.setAttribute( "b4l_user_name", m_LoggedInUsername);
			ioSession.setAttribute( "b4l_prev_login_date", thePrevLoginDate);

			theRS.close();
			USQL_Utils.closeStatementCatch(theS);

			////////////////////////////////////////////////////////  Update last login time each time the user auto-logs-in...

			theS = inConn.prepareCall("updateUsersLastLogin( ?, ? )");
			theS.setString( 1, inUserId);
			theS.setTimestamp( 2, thePrevLoginDate);
			theS.execute();
		}
		else
		{
			// s_Logger.debug("Clearing Session...");

			m_LoggedInUserRecno = -1L;
			m_LoggedInUsername = null;

			ioSession.removeAttribute( "b4l_user_recno");
			ioSession.removeAttribute( "b4l_user_name");
			ioSession.removeAttribute( "b4l_prev_login_date");
		}

		return theS;
	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public Cookie handleLoginCookie( final HttpServletRequest inReq, final HttpServletResponse ioResponse,
					 final long inUserRecno, final String inUsername, final String inHashedPassStr)
	{
		try
		{
			String	theEncodedHashedPassStr = new String( Base64.encodeBase64( inHashedPassStr.getBytes(STRING_ENCODING) ), STRING_ENCODING);

			s_Logger.debug("=> enc_h_p \"" + theEncodedHashedPassStr + "\".");

			/////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////

			HttpSession	theSession = inReq.getSession();
			BigInteger	currentPrefs = (BigInteger) theSession.getAttribute(SSN_USER_PREFERENCES);

			if ( currentPrefs == null)
			{
				s_Logger.debug("CREATE: initialising ssn Prefs.1");

				currentPrefs = createDefaultPreferences();
				theSession.setAttribute( SSN_USER_PREFERENCES, currentPrefs);
			}

			String	theBitsStr = currentPrefs.toString( Character.MAX_RADIX );

			// =====================================================

			StringBuilder	theNewValueStr = new StringBuilder(50);

			theNewValueStr.append(PREFS_PREFIX).append(theBitsStr).append("|")
					.append(inUserRecno).append(".").append(theEncodedHashedPassStr);

			s_Logger.debug("=> cookie value \"" + theNewValueStr + "\".");

			/////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////

			Cookie	theCookie =_findOurCookie(inReq);

			if ( theCookie == null)
			{
				theCookie = new Cookie( LOGIN_COOK_NAME, theNewValueStr.toString());
			}
			else
			{
				theCookie.setValue( theNewValueStr.toString() );
			}

//			Cookie	theCookie = new Cookie( LOGIN_COOK_NAME, theNewValueStr.toString()); // inUserRecno + "." + theEncodedHashedPassStr);
			theCookie.setMaxAge(31557600);    // approx number of secs in a year
			theCookie.setComment("This cookie lets you set preferences without having to keep logging-in.");

			s_Logger.debug("=> theCookie.1: " + theCookie);

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
	public Cookie handleBasicCookie( final HttpServletRequest inReq, final HttpServletResponse ioResponse)
	{
/*		try
		{
*/			s_Logger.debug("ENSURING THERE's A COOKIE!");

			/////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////

			HttpSession	theSession = inReq.getSession();
			BigInteger	currentPrefs = (BigInteger) theSession.getAttribute(SSN_USER_PREFERENCES);

			if ( currentPrefs == null)
			{
				s_Logger.debug("CREATE: initialising ssn Prefs.2");

				currentPrefs = createDefaultPreferences();
				theSession.setAttribute( SSN_USER_PREFERENCES, currentPrefs);
			}

			String	theBitsStr = currentPrefs.toString( Character.MAX_RADIX );

			// =====================================================

			StringBuilder	theNewValueStr = new StringBuilder(50);

			theNewValueStr.append(PREFS_PREFIX).append(theBitsStr).append("|");
		//			.append(inUserRecno).append(".").append(theEncodedHashedPassStr);

			s_Logger.debug("=> cookie value \"" + theNewValueStr + "\".");

			/////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////

			Cookie	theCookie; /* =_findOurCookie(inReq);

			if ( theCookie == null)
			{ */
				theCookie = new Cookie( LOGIN_COOK_NAME, theNewValueStr.toString());
/*			}
			else
			{
				theCookie.setValue( theNewValueStr.toString() );
			}

//			Cookie	theCookie = new Cookie( LOGIN_COOK_NAME, theNewValueStr.toString()); // inUserRecno + "." + theEncodedHashedPassStr);
*/			theCookie.setMaxAge(31557600);    // approx number of secs in a year
			theCookie.setComment("This cookie lets you set preferences without having to keep logging-in.");

//			s_Logger.debug("=> theCookie.2: " + theCookie);

			ioResponse.addCookie(theCookie);

			return theCookie;
/*		}
		catch (UnsupportedEncodingException e)
		{
			return null;	// Not going to happen
		}
*/	}

	/*******************************************************************************
		(AGR) 21 April 2007
	*******************************************************************************/
	public void requestLogout( final HttpServletRequest inReq, final HttpServletResponse ioResponse,
				   HttpSession ioSession, final String inLogoutURL) throws IOException
	{
		Cookie	ourCookie = _findOurCookie(inReq);

		s_Logger.debug("requestLogout() -> theCookie: " + ourCookie);

		if ( ourCookie != null)
		{
			// s_Logger.debug("GMA: " + ourCookie.getMaxAge());

			// (AGR) 29 May 2007. No, we now allow non-logged-in cookies! ... ourCookie.setMaxAge(0);

			PrefsElementLocationIF	thePrefsLoc = findPrefsElement( ourCookie.getValue() );

			if ( thePrefsLoc != null)	// Hmm. Strip out user stuff by setting value to be prefs section.
			{
				s_Logger.debug("LOGOUT: setting value to " + thePrefsLoc.getPrefsStr());

				StringBuilder	sb = new StringBuilder(30);

				sb.append( thePrefsLoc.getPrefsStr() ).append("|");

				ourCookie.setValue( sb.toString() );
			}
			else	// (AGR) 29 May 2007. For want of anything better...
			{
				ourCookie.setValue("");
			}

			////////////////////////////////////////////////////////

			ioResponse.addCookie(ourCookie);
		}

		////////////////////////////////////////////////////////////////

		ioSession.removeAttribute("b4l_user_recno");
		ioSession.removeAttribute("b4l_user_name");
		ioSession.removeAttribute("b4l_prev_login_date");

		ioSession.removeAttribute("want_mails");	// (AGR) 4 April 2005
		ioSession.removeAttribute("mail_summary");	//  "
		ioSession.removeAttribute("mail_hr");		//  "
		ioSession.removeAttribute("mail_min");		//  "
		ioSession.removeAttribute("b4l_mail_ud_str");	//  "
		ioSession.removeAttribute("mail_html");		// (AGR) 17 April 2005

		ioResponse.sendRedirect(inLogoutURL);
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
		(AGR) 29 May 2007
	*******************************************************************************/
	public static BigInteger createDefaultPreferences()
	{
		BigInteger	bi = BigInteger.ZERO;

		bi = bi.setBit( PreferenceBits.FRONTPAGE_DISPLAYPOSTS_DEFAULT.getBit() )
			.setBit( PreferenceBits.FRONTPAGE_DISPLAYPOSTS_STYLE_DEFAULT.getBit() );

		return bi;
	}

	/*******************************************************************************
		(AGR) 29 May 2007
	*******************************************************************************/
	public PrefsElementLocationIF findPrefsElement( final String inCookieValueStr)
	{
		int	prefsLabelPos = inCookieValueStr.indexOf(PREFS_PREFIX);

		if ( prefsLabelPos >= 0)
		{
			int	prefsEndPos = inCookieValueStr.indexOf("|", prefsLabelPos);

			if ( prefsEndPos > prefsLabelPos)
			{
				String	s = inCookieValueStr.substring( prefsLabelPos, prefsEndPos);

				return new PrefsElementLocation( prefsLabelPos, prefsEndPos, s);
			}
	}

		return null;
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

	/*******************************************************************************
	*******************************************************************************/
	public interface PrefsElementLocationIF
	{
		public int getStartLoc();
		public int getEndLoc();
		public String getPrefsStr();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static class PrefsElementLocation implements PrefsElementLocationIF
	{
		private int	m_StartLoc;
		private int	m_EndLoc;
		private String	m_PrefsStr;

		/*******************************************************************************
		*******************************************************************************/
		PrefsElementLocation( final int inStartLoc, final int inEndLoc, final String inStr)
		{
			m_StartLoc = inStartLoc;
			m_EndLoc = inEndLoc;
			m_PrefsStr = inStr;
		}

		/*******************************************************************************
		*******************************************************************************/
		public int getStartLoc()
		{
			return m_StartLoc;
		}

		/*******************************************************************************
		*******************************************************************************/
		public int getEndLoc()
		{
			return m_EndLoc;
		}

		/*******************************************************************************
		*******************************************************************************/
		public String getPrefsStr()
		{
			return m_PrefsStr;
		}
	}
}
