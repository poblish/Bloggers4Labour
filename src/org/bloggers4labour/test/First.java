/*
 * First.java
 *
 * Created on June 21, 2005, 11:33 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;

/**
 *
 * @author andrewre
 */
public class First implements Observer
{
//	static FeedList		s_FL = null; // InstallationManager.getDefaultInstallation().getFeedList();

	public static void main( String[] args)
	{
		Calendar		theStartCal = Calendar.getInstance( Locale.UK );
		Calendar		theEndCal = Calendar.getInstance( Locale.UK );
		DateFormat		dtf = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG, Locale.UK);
		SimpleDateFormat	sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.US);

		sdf.setTimeZone( TimeZone.getTimeZone("America/New_York") );

		int	numWeeksback = 0;
		int	curWeekDay = theStartCal.get( Calendar.DAY_OF_WEEK );
		int	diff = curWeekDay - theStartCal.getFirstDayOfWeek();

		theStartCal.set( Calendar.HOUR, 0);
		theStartCal.set( Calendar.MINUTE, 0);
		theStartCal.set( Calendar.SECOND, 0);
		theStartCal.set( Calendar.MILLISECOND, 0);

		long	theNewMSecs = theStartCal.getTimeInMillis() - (( numWeeksback * 7L + (long) diff) * 86400L * 1000L);
		theStartCal.setTimeInMillis(theNewMSecs);
		theEndCal.setTimeInMillis( theNewMSecs + ( 7L * 86400L * 1000L));

		System.out.println(">= " + sdf.format( theStartCal.getTime() ) + " or " + dtf.format( theStartCal.getTime() ));
		System.out.println(" < " + sdf.format( theEndCal.getTime() ) + " or " + dtf.format( theEndCal.getTime() ));

/*		java.math.BigInteger	bi = new java.math.BigInteger("0");

		bi = bi.setBit(58).setBit(49).setBit(150);

		System.out.println("==> " + bi.toString( Character.MAX_RADIX ));
*/
		// s_FL.addObserver( new First() );
	}

	public First()
	{
	}

	public void update(Observable o, Object arg)
	{
		for ( int i = 0; i < 5; i++)
			new Thread( new Runner() ).start();
	}

	private static class Runner implements Runnable
	{
//		private ChannelIF	m_Channel;

		public Runner()
		{
//			m_Channel = new ChannelBuilder().createChannel("foo " + this);
		}

		public void run()
		{
/*
			Site	s = s_FL.lookupPostsChannel(m_Channel);
			System.out.println( this + " got " + s);
*/
		}
	}
}
