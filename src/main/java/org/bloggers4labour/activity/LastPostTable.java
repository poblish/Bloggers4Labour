/*
 * LastPostTable.java
 *
 * Created on 09 September 2006, 18:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.activity;

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import com.hiatus.htl.HTL;
import com.hiatus.htl.HTLTemplate;
import com.hiatus.text.UText;
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
import org.bloggers4labour.site.SiteIF;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class LastPostTable implements LastPostTableIF
{
	private final Map<String,Long>		m_ChannelData = new TreeMap<String,Long>();
	private final Map<Long,Date>		m_SiteData = new Long2ObjectRBTreeMap<Date>();
	private FeedList			m_FL;

	private int[]				m_Stats = new int[11];
	private int				m_Total;

	private final byte[]			m_CompletionLocker = new byte[0];	// (AGR) 29 Jan 2007. Removed pointless 'transient'

	private final static Map<Long,Date>	s_LegacySiteData = new Long2ObjectRBTreeMap<Date>();
	private static Logger			s_Logger = Logger.getLogger( LastPostTable.class );

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
				if (thePrevMonthCal.after(theCal))		// (AGR) 22 Feb 2007. Careful we don't roll Jan 07 -> Dec 07 !
				{
					thePrevMonthCal.roll( Calendar.YEAR, -1);
				}

				thePrevPrevMonthCal.roll( Calendar.MONTH, -2);
				if (thePrevPrevMonthCal.after(thePrevMonthCal))	// (AGR) 22 Feb 2007. Careful we don't roll Jan 07 -> Nov 07 !
				{
					thePrevPrevMonthCal.roll( Calendar.YEAR, -1);
				}

				/////////////////////////////////////////////////////////////////////////

				for ( int i = 0; i < 11; i++)
				{
					m_Stats[i] = 0;
				}

				m_Total = 0;

				/////////////////////////////////////////////////////////////////////////

				long		oneMonthDiffMSecs = currTimeMSecs - thePrevMonthCal.getTimeInMillis();
				long		twoMonthDiffMSecs = currTimeMSecs - thePrevPrevMonthCal.getTimeInMillis();

				// System.out.println("oneMonthDiffMSecs = " + oneMonthDiffMSecs + " (" + thePrevMonthCal.getTimeInMillis() + ")");
				// System.out.println("twoMonthDiffMSecs = " + twoMonthDiffMSecs + " (" + thePrevPrevMonthCal.getTimeInMillis() + ")");

				/////////////////////////////////////////////////////////////////////////

				for ( String eachChannel : m_ChannelData.keySet())
				{
					SiteIF	eachSite = m_FL.lookupFeedLocationURL(eachChannel);

					if ( eachSite != null)
					{
						Long	dateVal = m_ChannelData.get(eachChannel);
						long	dateMsecs = dateVal.longValue();
						Date	lastSiteDate = m_SiteData.get( eachSite.getRecno() );

/*						if ( lastSiteDate == null)
						{
							s_Logger.info("*** " + eachSite.getRecno() + " *** c = " + eachChannel + ", dv = " + dateVal);
						}
*/
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

				// s_Logger.debug("Data: " + m_SiteData);
				// s_Logger.info("keys = " + m_SiteData.keySet());

				for ( Map.Entry<Long,Date> each : m_SiteData.entrySet())
			//	for ( Date eachSitesLastDate : m_SiteData.values())
				{
					Date	eachSitesLastDate = each.getValue();
					long	diffMSecs = currTimeMSecs - eachSitesLastDate.getTime();

					if ( diffMSecs <= -ONE_HOUR_MSECS)	// (AGR) 25 Feb 2007. Even further in the future than one hour!
					{
						// s_Logger.warn("Too far in the future - bogus entry " + each);

						continue;
					}
					else if ( diffMSecs <= ONE_HOUR_MSECS)	// (AGR) 25 Feb 2007. Now includes up yo 1 hr in the future!
					{
//						s_Logger.warn("-1 < x <= 1 hour for " + each + ", diff = " + diffMSecs);

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
		}

		s_Logger.debug("Results: " + toString());
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

			Object	theObjs[] = { Integer.valueOf( m_Stats[i] ) };	// (AGR) 29 Jan 2007. FindBugs: changed from new Integer

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
	@Override public String toString()
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

	/*******************************************************************************
		(AGR) 22 Feb 2007
	*******************************************************************************/
	public static void main( String[] args)
	{
		Calendar	theCal = Calendar.getInstance();
//		theCal.set(2007,10,30);
//		System.out.println("month: " + theCal.getTime());

		Calendar	thePrevMonthCal = (Calendar) theCal.clone();
		Calendar	thePrevPrevMonthCal = (Calendar) theCal.clone();
		long		currTimeMSecs = theCal.getTimeInMillis();

		thePrevMonthCal.roll( Calendar.MONTH, -1);
		if (thePrevMonthCal.after(theCal))
		{
			thePrevMonthCal.roll( Calendar.YEAR, -1);
		}

		thePrevPrevMonthCal.roll( Calendar.MONTH, -2);
		if (thePrevPrevMonthCal.after(thePrevMonthCal))
		{
			thePrevPrevMonthCal.roll( Calendar.YEAR, -1);
		}

		System.out.println("month -1: " + thePrevMonthCal.getTime());
		System.out.println("month -2: " + thePrevPrevMonthCal.getTime());

		long		oneMonthDiffMSecs = currTimeMSecs - thePrevMonthCal.getTimeInMillis();
		long		twoMonthDiffMSecs = currTimeMSecs - thePrevPrevMonthCal.getTimeInMillis();

		System.out.println("oneMonthDiffMSecs = " + oneMonthDiffMSecs + " (" + thePrevMonthCal.getTimeInMillis() + ")");
		System.out.println("twoMonthDiffMSecs = " + twoMonthDiffMSecs + " (" + thePrevPrevMonthCal.getTimeInMillis() + ")");
	}
}