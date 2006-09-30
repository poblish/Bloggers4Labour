/*
 * LastPostTable.java
 *
 * Created on 09 September 2006, 18:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.activity;

import com.hiatus.UText;
import com.hiatus.htl.*;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.impl.basic.Channel;
import java.net.URL;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.htl.B4LHTLContext;
import org.bloggers4labour.Site;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class LastPostTable
{
	private Map<String,Long>		m_ChannelData = new TreeMap<String,Long>();
	private Map<Long,Date>			m_SiteData = new TreeMap<Long,Date>();
	private FeedList			m_FL;
//	private boolean				m_AddDefault;

/*	private int				m_LastHour;
	private int				m_Last2Hours;
	private int				m_Last12Hours;
	private int				m_LastDay;
	private int				m_Last2Days;
	private int				m_Last3Days;
	private int				m_LastWeek;
	private int				m_Last2Weeks;
	private int				m_LastMonth;
	private int				m_Last2Months;
	private int				m_Over2Months;
*/	private int				m_Unknown;
	private int[]				m_Stats = new int[11];
	private int				m_Total;

	private transient byte[]		m_CompletionLocker = new byte[0];

	private static Map<Long,Date>		s_LegacySiteData = new TreeMap<Long,Date>();
	private static Logger			s_Logger = Logger.getLogger("Main");

	private final static int		PERC_BAR_WIDTH = 200;

	/*******************************************************************************
		Yuk - these are presumed-archived blogs without proper feeds
	*******************************************************************************/
	static
	{
		GregorianCalendar	gc = new GregorianCalendar( TimeZone.getTimeZone("UTC"), Locale.UK);

		gc.set( 2006, Calendar.JANUARY, 21, 11, 23);
		s_LegacySiteData.put( 16L, gc.getTime());

		gc.set( 2006, Calendar.MAY, 1, 12, 0);
		s_LegacySiteData.put( 55L, gc.getTime());

		gc.set( 2006, Calendar.MAY, 2, 12, 0);
		s_LegacySiteData.put( 116L, gc.getTime());

		gc.set( 2006, Calendar.JANUARY, 5, 16, 0);
		s_LegacySiteData.put( 138L, gc.getTime());

		gc.set( 2005, Calendar.OCTOBER, 1, 16, 50);
		s_LegacySiteData.put( 184L, gc.getTime());

		gc.set( 2006, Calendar.MAY, 14, 12, 0);
		s_LegacySiteData.put( 261L, gc.getTime());

		gc.set( 2006, Calendar.FEBRUARY, 16, 12, 0);
		s_LegacySiteData.put( 369L, gc.getTime());

		gc.set( 2006, Calendar.APRIL, 4, 10, 32);
		s_LegacySiteData.put( 396L, gc.getTime());

		gc.set( 2006, Calendar.APRIL, 5, 18, 29);
		s_LegacySiteData.put( 397L, gc.getTime());
	}

	/*******************************************************************************
	*******************************************************************************/
	public LastPostTable( final FeedList inFL, boolean inAddDefault)
	{
		m_FL = inFL;
		// m_AddDefault = inAddDefault;

		if (inAddDefault)
		{
			m_SiteData.putAll( s_LegacySiteData );
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void store( final URL inChannel, long inTimeMSecs)
	{
		synchronized (m_CompletionLocker)
		{
			String	theKey = inChannel.toString();
			Long	currVal = m_ChannelData.get(theKey);

			// System.out.println("** store() #" + inChannel + " **** was " + currVal + ", now " + inTimeMSecs);

			if ( currVal == null || currVal.longValue() < inTimeMSecs)
			{
				m_ChannelData.put( theKey, inTimeMSecs);
			}

/*			if (  != null)
			{
				s_Logger.warn("Already got an entry for \"" + inChannel + "\"");
			}
			else	s_Logger.info("Storing " + inTimeMSecs + " for \"" + inChannel + "\", cd = " + m_ChannelData);
*/
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void complete()
	{
		synchronized (m_CompletionLocker)
		{
			// s_Logger.info("CD = " + m_ChannelData, new Throwable());

			if ( m_ChannelData.size() > 0)
			{
				Calendar	theCal = Calendar.getInstance();
				Calendar	thePrevMonthCal = (Calendar) theCal.clone();
				Calendar	thePrevPrevMonthCal = (Calendar) theCal.clone();
				long		currTimeMSecs = theCal.getTimeInMillis();

				thePrevMonthCal.roll( Calendar.MONTH, -1);
				thePrevPrevMonthCal.roll( Calendar.MONTH, -2);

				// System.out.println("  :: " + theCal.getTime());
				// System.out.println(" ::: " + thePrevMonthCal.getTime());
				// System.out.println(":::: " + thePrevPrevMonthCal.getTime());

				for ( int i = 0; i < 11; i++)
				{
					m_Stats[i] = 0;
				}

				/* m_Stats[0] = m_Last2Hours = m_Last12Hours = m_LastDay = m_Last2Days = m_Last3Days =
					m_LastWeek = m_Last2Weeks = m_LastMonth = m_Last2Months = m_Over2Months = */ m_Total = 0;

				int		x = m_FL.countURLs() - m_ChannelData.size();

				m_Unknown =  ( x > 0 ) ? x : 0;

				long		oneMonthDiffMSecs = currTimeMSecs - thePrevMonthCal.getTimeInMillis();
				long		twoMonthDiffMSecs = currTimeMSecs - thePrevPrevMonthCal.getTimeInMillis();

				// System.out.println("oneMonthDiffMSecs = " + oneMonthDiffMSecs);
				// System.out.println("twoMonthDiffMSecs = " + twoMonthDiffMSecs);

				/////////////////////////////////////////////////////////////////////////

				for ( String eachChannel : m_ChannelData.keySet())
				{
					Site	eachSite = m_FL.lookupFeedLocationURL(eachChannel);

					if ( eachSite != null)
					{
						Long	dateVal = m_ChannelData.get(eachChannel);
						long	dateMsecs = dateVal.longValue();
						Date	lastSiteDate = m_SiteData.get( eachSite.getRecno() );

						if ( lastSiteDate == null || lastSiteDate.getTime() < dateMsecs)
						{
							// System.out.println("@ Setting #" + eachSite.getRecno() + " to " + new Date(dateMsecs));

							m_SiteData.put( eachSite.getRecno(), new Date(dateMsecs));
						}
					}
					else	s_Logger.warn("Lookup failed for \"" + eachChannel + "\"");
				}

				m_ChannelData.clear();

				////////////////////////////////////////////////////////

				// s_Logger.info("keys = " + m_SiteData.keySet());

				for ( Date eachSitesLastDate : m_SiteData.values())
//				for ( Long eachSiteRecno : m_SiteData.keySet())
				{
//					long	diffMSecs = currTimeMSecs - m_SiteData.get(eachSiteRecno).getTime();
					long	diffMSecs = currTimeMSecs - eachSitesLastDate.getTime();

					if ( diffMSecs <= ONE_HOUR_MSECS)
					{
						m_Stats[0]++;
					}
					else if ( diffMSecs <= 2 * ONE_HOUR_MSECS)
					{
						m_Stats[1]++;
					}
					else if ( diffMSecs <= 12 * ONE_HOUR_MSECS)
					{
						m_Stats[2]++;
					}
					else if ( diffMSecs <= ONE_DAY_MSECS)
					{
						m_Stats[3]++;
					}
					else if ( diffMSecs <= 2 * ONE_DAY_MSECS)
					{
						m_Stats[4]++;
					}
					else if ( diffMSecs <= 3 * ONE_DAY_MSECS)
					{
						m_Stats[5]++;
					}
					else if ( diffMSecs <= ONE_WEEK_MSECS)
					{
						m_Stats[6]++;
					}
					else if ( diffMSecs <= 2 * ONE_WEEK_MSECS)
					{
						m_Stats[7]++;
					}
					else if ( diffMSecs <= oneMonthDiffMSecs)
					{
						m_Stats[8]++;
					}
					else if ( diffMSecs <= twoMonthDiffMSecs)
					{
						m_Stats[9]++;
					}
					else
					{
						m_Stats[10]++;
					}

					m_Total++;
				}
			}

			////////////////////////////////////////////////////////

/*			Site[] sArray = m_FL.getArrayToTraverse();
			for ( Site s : sArray)
			{
				if (!m_SiteData.containsKey( new Long( s.getRecno() ) ))
				{
					m_SiteData.put( s.getRecno(), null);
				}
			}
*/
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuffer getTableContent( Locale inLocale)
	{
		MessageFormat	theFormat = new MessageFormat("");
		theFormat.setLocale(inLocale);

		////////////////////////////////////////////////////////////////

		B4LHTLContext	theCtxt = new B4LHTLContext(inLocale); // HTL.createInstance(inLocale);
		HTLTemplate	eachStatTemplate = HTL.createTemplate( "activity_scores_each.vm", inLocale);
		NumberFormat	thePercFormat = NumberFormat.getPercentInstance(inLocale);
		StringBuilder	sb = new StringBuilder(1000);
		int		j = 0;
		int		cumulativeSitesCount = m_Total;
		int		maxIndividualStatValue = -1;

		// s_Logger.info("bundle = " + theCtxt.getBundle());

		for ( int i = 10; i >= 0; i--)
		{
			if ( m_Stats[i] > maxIndividualStatValue)
			{
				maxIndividualStatValue = m_Stats[i];
			}
		}

		for ( int i = 10; i >= 0; i--)
		{
			if ( m_Stats[i] == 0)
			{
				UText.applyMessagePattern( theFormat, theCtxt.loadString("activity.stats.0"));
			}
			else if ( m_Stats[i] == 1)
			{
				UText.applyMessagePattern( theFormat, theCtxt.loadString("activity.stats.1"));
			}
			else	UText.applyMessagePattern( theFormat, theCtxt.loadString("activity.stats.some"));

			////////////////////////////////////////////////////////

			double	thePercentage = (double) m_Stats[i] / (double) maxIndividualStatValue;
			double	temp = (int) Math.round( thePercentage * (double) PERC_BAR_WIDTH);

			theCtxt.put( "opt_name", theCtxt.loadString("activity.stat.name." + (j++) ));

			Object	theObjs[] = { new Integer( m_Stats[i] ) };

			theCtxt.put( "opt_votes", theFormat.format(theObjs));
			theCtxt.put( "opt_width_0", PERC_BAR_WIDTH - temp);
			theCtxt.put( "opt_width_1", temp);

			thePercentage = (double) m_Stats[i] / (double) m_Total;
			theCtxt.put( "opt_perc", thePercFormat.format(thePercentage));

			////////////////////////////////////////////////////////

			thePercentage = (double) cumulativeSitesCount / (double) m_Total;
			temp = (int) Math.round( thePercentage * (double) PERC_BAR_WIDTH);

			theCtxt.put( "opt_cum_votes", cumulativeSitesCount);
			theCtxt.put( "opt_cum_width_0", PERC_BAR_WIDTH - temp);
			theCtxt.put( "opt_cum_width_1", temp);

			////////////////////////////////////////////////////////

			sb.append( HTL.mergeTemplate( eachStatTemplate, theCtxt) );

			cumulativeSitesCount -= m_Stats[i];
		}

		theCtxt.put( "activity_stats_buf", sb);
		theCtxt.put( "total_blogs", m_Total);

		return HTL.mergeTemplate( "activity_scores.vm", theCtxt);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		StringBuilder	sb = new StringBuilder("Stats: ");

		synchronized (m_CompletionLocker)
		{
			sb.append( m_Stats[0] + " <= last hour, ")
			  .append( m_Stats[1] + " <= 2 hrs, ")
			  .append( m_Stats[2] + " <= 12 hrs, ")
			  .append( m_Stats[3] + " <= 1 day, ")
			  .append( m_Stats[4] + " <= 2 days, ")
			  .append( m_Stats[5] + " <= 3 days, ")
			  .append( m_Stats[6] + " <= 1 week, ")
			  .append( m_Stats[7] + " <= 2 weeks, ")
			  .append( m_Stats[8] + " <= 1 month, ")
			  .append( m_Stats[9] + " <= 2 months, ")
			  .append( m_Stats[10] + " > 2 months")
		//	  .append( ", " + m_Unknown + " unknown")
			  .append( ". Total: ")
			  .append( m_Total ); // .append(" (").append( m_FL.countURLs() ).append(")");
		}

		return sb.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getTotal()
	{
		return m_Total;
	}
}