/*
 * StatsTest.java
 *
 * Created on 20 May 2006, 23:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.test;

import java.text.*;
import java.util.*;
import org.bloggers4labour.*;
import org.bloggers4labour.jmx.*;

/**
 *
 * @author andrewre
 */
public class StatsTest
{
	private Locale	thePageLocale = Locale.UK;

	/********************************************************************
	********************************************************************/
	public StatsTest()
	{
		InstallationIF	theInstall = InstallationManager.getInstallation("b4l");
		Headlines	theRecentHeads = theInstall.getHeadlinesMgr().getRecentPostsInstance();
		Headlines	the24HourHeads = theInstall.getHeadlinesMgr().get24HourInstance();
		Stats		theStats = theInstall.getManagement().getStats();

		Calendar	theLastCheckTime = theStats.getLastFeedCheckTime();
		int		theURLCount = theStats.getFeedCount();
		int		theSuccessfulFeedCount = theStats.getSuccessfulFeedCount();     // (AGR) 7 October 2005
		List            theFailedFeedsList = theStats.getFailedFeedsList();             // (AGR) 7 October 2005
		int		blogsUsed24 = theStats.getBlogsUsedInLast24Hours();
		int		blogsUsed48 = theStats.getRecentBlogsUsed();

		//////////////////////////////////////////////////////////////////////////////////////////

		NumberFormat	thePercFormat = NumberFormat.getPercentInstance(thePageLocale);

		thePercFormat.setMaximumFractionDigits(1);

		//////////////////////////////////////////////////////////////////////////////////////////

		String		blogActivity24Str;
		String		blogActivity48Str;

		if ( theURLCount < 1)
		{
			blogActivity24Str = blogActivity48Str = null;
		}
		else
		{
			blogActivity24Str = " (" + thePercFormat.format((double) blogsUsed24 / theURLCount) + " of the total)";
			blogActivity48Str = " (" + thePercFormat.format((double) blogsUsed48 / theURLCount) + " of the total)";
		}
	}
	
}
