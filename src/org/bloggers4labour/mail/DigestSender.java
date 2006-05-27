/*
 * DigestSender.java
 *
 * Created on 01 April 2005, 01:09
 */

package org.bloggers4labour.mail;

import com.hiatus.CoreMail;
import com.hiatus.ULocale2;
import com.hiatus.UDates;
import com.hiatus.UHTML;
import com.hiatus.USQL_Utils;
import com.hiatus.UText;
import com.hiatus.htl.*;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.Item;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.sql.DataSourceConnection;
import org.bloggers4labour.sql.QueryBuilder;
import static org.bloggers4labour.Constants.*;
import org.bloggers4labour.*;

/**
 *
 * @author andrewre
 */
public class DigestSender
{
	private Timer			m_CheckerTimer;
	private CheckerTask		m_CheckerTask;
	private DateFormat		m_DF;
	private DateFormat		m_DMYFormat;
	private CoreMail		m_MailObj;
	private Session			m_MailSession;

	private Locale			m_Locale = Locale.UK;
	private InstallationIF		m_Install;

	private static DigestSender	s_List;
	private static Logger		s_DS_Logger = Logger.getLogger("Main");

	private final static long	IDEAL_INTERVAL = 15;
	private final static long	INTERVAL = IDEAL_INTERVAL;

	private final static String	OUR_EMAIL_STR = "us@bloggers4labour.org";

	/*******************************************************************************
		(AGR) 4 July 2005. For testing!
	*******************************************************************************/
	public static void main( String[] args)
	{
		Properties	p = new Properties();
		p.setProperty( "bm.docs_directory_path", "/Users/andrewre/www/htdocs/bloggers4labour/htl/");
		p.setProperty( "bm.locales_dir_name", "locales/");
		p.setProperty( "bm.default_dir_name", "default");

		com.hiatus.WebApp.setProperties(p);

		HTL.initHTL(p);
		HTLCache.init();

		InstallationManager.getDefaultInstallation();

		new DigestSender( InstallationManager.getDefaultInstallation() ).test();
	}

	/*******************************************************************************
	*******************************************************************************/
	public DigestSender( final InstallationIF inInstall)
	{
		m_Install = inInstall;
		m_CheckerTimer = new Timer("DigestSender: sender Timer");
		m_CheckerTask = new CheckerTask();

		//////////////////////////////////////////////////////

		m_DF = ULocale2.getClientDateTimeFormat( m_Locale, DateFormat.MEDIUM);

		try
		{
			m_DMYFormat = DateFormat.getDateInstance( DateFormat.MEDIUM, m_Locale);
		}
		catch (Exception e)
		{
			m_DMYFormat = m_DF;
		}

		m_DMYFormat.setTimeZone( ULocale2.getBestTimeZone(m_Locale) );

		//////////////////////////////////////////////////////
		
		Calendar	timeToStart = FeedUtils.getNextDigestTime();
		String		prefix = m_Install.getLogPrefix();

		s_DS_Logger.info( prefix + "m_CheckerTimer " + m_CheckerTimer + " starting at " + m_DF.format( timeToStart.getTime() ));

		m_CheckerTimer.scheduleAtFixedRate( m_CheckerTask, timeToStart.getTime(), INTERVAL * ONE_MINUTE_MSECS);

		s_DS_Logger.info( prefix + "created " + this);

		//////////////////////////////////////////////////////

		Properties	theProps = System.getProperties();

		// theProps.put( "mail.smtp.dsn.notify", "SUCCESS,FAILURE ORCPT=rfc822;" + m_CSEmailAddress);
		// theProps.put( "mail.smtp.dsn.ret", "FULL");

		m_MailSession = Session.getDefaultInstance( theProps, null);
		s_DS_Logger.info( prefix + "created Mail session: " + m_MailSession);

		m_MailObj = new CoreMail();
		s_DS_Logger.info( prefix + "created Mail object: " + m_MailObj);
	}
	
	/*******************************************************************************
	*******************************************************************************
	private synchronized static DigestSender getInstance( boolean inCreateIfNeeded)
	{
		if ( s_List == null && inCreateIfNeeded)
		{
			s_List = new DigestSender();
		}
		
		return s_List;
	}/

	/*******************************************************************************
	*******************************************************************************
	public synchronized static DigestSender getInstance()
	{
		return getInstance(true);
	}/

	/*******************************************************************************
	*******************************************************************************/
	public static void cancelTimer()
	{
		if ( s_List != null)
		{
			if ( s_List.m_CheckerTimer != null)
			{
				s_DS_Logger.info("DigestSender: cancelling Timer: " + s_List.m_CheckerTimer);

				s_List.m_CheckerTimer.cancel();
				s_List.m_CheckerTimer = null;
			}

			s_List.m_CheckerTask = null;

			s_List = null;	// (AGR) 27 May 2005
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void test()
	{
		// FeedList.getInstance().addObserver( new MailTester() );
	}

	/*******************************************************************************
	*******************************************************************************/
	class MailTester implements java.util.Observer
	{
		/*******************************************************************************
		*******************************************************************************/
		public void update( java.util.Observable o, Object obj)
		{
//			runIt( new TextMessageBuilder( InstallationManager.getDefaultInstallation() ) );
			runIt( new HTMLMessageBuilder( InstallationManager.getDefaultInstallation() ) );
		}

		/*******************************************************************************
		*******************************************************************************/
		private synchronized void runIt( MessageBuilder ioBuilder)
		{
			ItemIF[]	theItemsA = m_Install.getHeadlinesMgr().getEmailPostsInstance().toArray();
			long		theCurrMSecs = System.currentTimeMillis();
			long		theItemAgeMSecs;
			int		theEligibleItemsCount = 0;

			for ( int z = 0; z < theItemsA.length; z++)
			{
				theItemAgeMSecs = theCurrMSecs - FeedUtils.getItemDate( theItemsA[z] ).getTime();
				if ( theItemAgeMSecs < ONE_DAY_MSECS)
				{
					theEligibleItemsCount++;
				}
			}

			StringBuilder	sb = new StringBuilder();

			ioBuilder.setCount(theEligibleItemsCount);

			for ( int z = 0; z < theItemsA.length; z++)
			{
				Item	theItem = (Item) theItemsA[z];
				Date	theItemDate = FeedUtils.getItemDate(theItem);

				theItemAgeMSecs = theCurrMSecs - theItemDate.getTime();

				if ( theItemAgeMSecs >= ONE_DAY_MSECS)
				{
					continue;
				}

				////////////////////////////////////////////////////////////////////

				ioBuilder.startNewItem(theItem);
				ioBuilder.buildMail( theItemAgeMSecs, "hellO", z, theItemDate, 1);
				ioBuilder.handleCategories();

				///////////////////////////////////////////////////////////////////////////

				sb.append( ioBuilder.generateMessageBody() );
			}

			ioBuilder.setMessageBody(sb);

			System.out.println("text = " + ioBuilder.generateMessageBodyText());
			System.out.println("HTML = " + ioBuilder.generateMessageBodyHTML());
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	class CheckerTask extends TimerTask
	{
		/*******************************************************************************
		*******************************************************************************/
		public CheckerTask()
		{
			;
		}

		/*******************************************************************************
		*******************************************************************************/
		private void _handleStatement( Statement inS) throws SQLException
		{
			Locale			theLocale = Locale.UK;
			ResourceBundle		theBundle = m_Install.getBundle(theLocale);
			GregorianCalendar	currLocalTimeCal = ULocale2.getGregorianCalendar(theLocale);
			int			currLocalHour = currLocalTimeCal.get( Calendar.HOUR_OF_DAY );
			int			currLocalMinute = currLocalTimeCal.get( Calendar.MINUTE );
			long			adjustedMins = (long)( currLocalMinute / IDEAL_INTERVAL) * IDEAL_INTERVAL;

			// s_DS_Logger.info("CURRENT hr=" + currLocalHour + ", currLocalMinute=" + currLocalMinute);

			////////////////////////////////////////////////////////////////////////////////////

			String		theQuery = QueryBuilder.getDigestEmailQuery((long) currLocalHour, adjustedMins);
			ResultSet	theRS = inS.executeQuery(theQuery);

			if (theRS.next())
			{
				Headlines	h = m_Install.getHeadlinesMgr().getEmailPostsInstance();

				if ( h == null)		// (AGR) 2 April 2006
				{
					USQL_Utils.closeResultSetCatch(theRS);
					return;
				}

				ItemIF[]	theItemsA = h.toArray();

				if ( theItemsA.length < 1)
				{
					USQL_Utils.closeResultSetCatch(theRS);
					return;
				}

				////////////////////////////////////////////////////////////////////////////  Check items are within past 24 hours

				Date		theCurrDate = new Date();
				long		theCurrMSecs = theCurrDate.getTime();
				long		theItemAgeMSecs;
				int		theEligibleItemsCount = 0;

				for ( int z = 0; z < theItemsA.length; z++)
				{
					theItemAgeMSecs = theCurrMSecs - FeedUtils.getItemDate( theItemsA[z] ).getTime();
					if ( theItemAgeMSecs < ONE_DAY_MSECS)
					{
						theEligibleItemsCount++;
					}
				}

				if ( theEligibleItemsCount < 1)
				{
					USQL_Utils.closeResultSetCatch(theRS);
					return;
				}

				////////////////////////////////////////////////////////////////////////////

				StringBuilder	recipientsMsgBuf = new StringBuilder(10000);	// (AGR) 28 May 2005. Was a 5000 byte StringBuffer
				List<String>	theMsgRecipients = new ArrayList<String>(1);
				Date		theItemDate;
				Item		theItem;

				StringBuffer	theEmailSubject = new StringBuffer();
				Formatter	theEmailSubjectFormatter = new Formatter( theEmailSubject, theLocale);

				theEmailSubjectFormatter.format( theLocale, theBundle.getString("mail.subject"), m_DMYFormat.format(theCurrDate));

				////////////////////////////////////////////////

				theMsgRecipients.add("");

				do	// loop through the Users
				{
					InternetAddress	theSenderAddress;

					try	// (AGR) 14 April 2005
					{
						theSenderAddress = new InternetAddress( OUR_EMAIL_STR, "Bloggers4Labour");
					}
					catch (UnsupportedEncodingException e)
					{
						try
						{
							theSenderAddress = new InternetAddress(OUR_EMAIL_STR);
						}
						catch (AddressException e2)
						{
							return;		// FIXME - error message!
						}
					}

					////////////////////////////////////////////////////////////////////

					String		prefix = m_Install.getLogPrefix();
					MimeMessage	theMailMsg;
					String		emailAddr = theRS.getString(2);
					int		wantsSummary = theRS.getInt("digest_include_summary");
					boolean		wantsHTML = ( theRS.getInt("digest_HTML") == 1);	// (AGR) 14 April 2005

					MessageBuilder	mb = wantsHTML ? new HTMLMessageBuilder( m_Install, m_DF) :  new TextMessageBuilder( m_Install, m_DF);

					s_DS_Logger.info( prefix + "created: " + mb);

					mb.setCount(theEligibleItemsCount);

					for ( int z = 0; z < theItemsA.length; z++)
					{
						theItem = (Item) theItemsA[z];
						theItemDate = FeedUtils.getItemDate(theItem);
						theItemAgeMSecs = theCurrMSecs - theItemDate.getTime();
						if ( theItemAgeMSecs >= ONE_DAY_MSECS)
						{
							continue;
						}

						////////////////////////////////////////////////////////////////////

						mb.startNewItem(theItem);
						mb.buildMail( theItemAgeMSecs, theEmailSubject, z, theItemDate, wantsSummary);
						mb.handleCategories();

						///////////////////////////////////////////////////////////////////////////

						recipientsMsgBuf.append( mb.generateMessageBody() );
					}

					mb.setMessageBody(recipientsMsgBuf);

					///////////////////////////////////////////////////////////////////////////

					// String	stringToSend = mb.generateMessageBodyText().toString();

					// theHTMLMailContent = mb.generateMessageBodyHTML();

					// s_DS_Logger.info("ctxt: " + mb.getContext());

					///////////////////////////////////////////////////////////////////////////

					// s_DS_Logger.info("... trying <" + emailAddr + ">, desiredHr=" + desiredHr + ", digest_min=" + desiredMin + ", summ? " + wantsSummary);

					theMsgRecipients.set( 0, emailAddr);

					theMailMsg = m_MailObj.create1Message( m_MailSession,
										theSenderAddress,
										theMsgRecipients,
										theEmailSubject.toString(),
										mb.generateMessageBodyText().toString(),
										mb.generateMessageBodyHTML(),
										theLocale);

					if ( theMailMsg != null)
					{
						try
						{
							Transport.send(theMailMsg);

							s_DS_Logger.info( prefix + "Message sent to <" + emailAddr + ">");
						}
						catch (MessagingException e)
						{
							s_DS_Logger.error( prefix + "Mailing failed", e);
						}
					}

					mb.clear();

					////////////////////////////////////////////////////////////////////////////

					recipientsMsgBuf.setLength(0);
				}
				while (theRS.next());
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		public void run()
		{
			DataSourceConnection	theConnectionObject = null;
			StringBuffer		theBuf;
			boolean			isGood = false;

			// s_DS_Logger.info("CheckerTask.run() called at " + m_DF.format( new Date() ));

			try
			{
				theConnectionObject = new DataSourceConnection( m_Install.getDataSource() );

				if (theConnectionObject.Connect())
				{
					Statement	theS = null;

					try
					{
						theS = theConnectionObject.createStatement();
						_handleStatement(theS);
					}
					catch (Exception e)
					{
						s_DS_Logger.error("creating statement", e);
					}
					finally
					{
						USQL_Utils.closeStatementCatch(theS);
					}
				}
				else
				{
					s_DS_Logger.warn("Cannot connect!");
				}
			}
			catch (Exception err)
			{
				s_DS_Logger.error("???", err);
			}
			finally
			{
				if ( theConnectionObject != null)
				{
					theConnectionObject.CloseDown();
					theConnectionObject = null;
				}
			}
		}
	}

}
