/*
 * MoneySpent.java
 *
 * Created on 26 March 2007, 21:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.funding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author andrewre
 */
public class MoneySpent
{
	private long			m_StartTimeMsecs;
	private BigDecimal		m_BaselineTotal;
	private BigDecimal		m_PoundsPerSecond;

	private static TimeZone		s_UTCZone = TimeZone.getTimeZone("GMT");

	/********************************************************************
	********************************************************************/
	public MoneySpent()
	{
		Calendar	startCal = Calendar.getInstance();

		startCal.setTimeZone(s_UTCZone);
		startCal.set( 2007, 2, 22, 0, 0, 0);
//		System.out.println(": Then = " + startCal.getTime());

		m_StartTimeMsecs = startCal.getTimeInMillis();

		// m_PencePerSecond = new BigDecimal("325.1").divide( new BigDecimal("86400.0") );
		m_PoundsPerSecond = new BigDecimal("0.000037627315");
		m_BaselineTotal = new BigDecimal("372.07");
	}

	/********************************************************************
	********************************************************************/
	public BigDecimal getRawTotal()
	{
		Calendar	currCal = Calendar.getInstance();

		currCal.setTimeZone(s_UTCZone);
//		System.out.println(":  Now = " + currCal.getTime());

		long		theDiff = currCal.getTimeInMillis() - m_StartTimeMsecs;
//		BigDecimal	bd = BigDecimal.valueOf( Long.toString(theDiff) );
		BigDecimal	bd = BigDecimal.valueOf(theDiff);
//		System.out.println(":   bd = " + bd);
//		System.out.println(":   m_PoundsPerSecond = " + m_PoundsPerSecond);

		bd = bd.movePointLeft(3).multiply(m_PoundsPerSecond);
//		System.out.println("Adding... " + bd);
		bd = bd.add(m_BaselineTotal);
//		System.out.println(" Raw value... " + bd);

		return bd;
	}

	/********************************************************************
	********************************************************************/
	public BigDecimal getTotal()
	{
		return getRawTotal().setScale( 2, RoundingMode.HALF_DOWN);
	}

	/********************************************************************
	********************************************************************/
	public BigDecimal getAmountPerBlog( int inNumBloggers)
	{
//		return getRawTotal().divide( new BigDecimal("454.00000000000") ).setScale( 2, RoundingMode.HALF_DOWN);
//		return getRawTotal().divide( new BigDecimal("454"), 2, RoundingMode.HALF_DOWN);
		return getRawTotal().divide( BigDecimal.valueOf(inNumBloggers), 2, RoundingMode.HALF_DOWN);
	}

	/********************************************************************
	********************************************************************/
	public static void main( String[] args)
	{
		MoneySpent	ms = new MoneySpent();
		NumberFormat	theCF = NumberFormat.getCurrencyInstance( Locale.UK );

		System.out.println("Total = " + theCF.format( ms.getTotal() ));
		System.out.println("Total per blogger = " + theCF.format( ms.getAmountPerBlog(454) ));
	}
}
