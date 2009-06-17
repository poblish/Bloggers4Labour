/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.dates;

import de.nava.informa.utils.ParserUtils;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author andrewregan
 */
public class DateParsingTest // extends AbstractB4LTest
{
	/*******************************************************************************
	*******************************************************************************/
	@Test public void testDates()
	{
		_test1Date("2008-08-18T10:44:00.002+02:00");
		_test1Date("2008-08-18T10:47:14.328+02:00");
		_test1Date("2008-08-07T22:23:00.001+02:00");
		_test1Date("2008-08-08T11:09:14.081+02:00");
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _test1Date( final String inStr)
	{
		Date	theDate = ParserUtils.getDate(inStr);
		Assert.assertNotNull(theDate);

		System.out.println( inStr + " --> " + theDate);
	}
}