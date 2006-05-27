/*
 * FeedUtilsTest.java
 * JUnit based test
 *
 * Created on 22 March 2005, 15:29
 */

package org.bloggers4labour;

import junit.framework.*;
import com.hiatus.UText;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.utils.ParserUtils;
import java.util.Date;
import java.util.regex.Pattern;

/**
 *
 * @author andrewre
 */
public class FeedUtilsTest extends TestCase
{
	
	public FeedUtilsTest(String testName)
	{
		super(testName);
	}

    public static Test suite()
    {
        TestSuite suite = new TestSuite(FeedUtilsTest.class);
        
        return suite;
    }

    public void testAdjustDescription()
    {
        System.out.println("testAdjustDescription");
 
	String	input = "Cardinal declares, \"The book is everywhere. There is a very real risk that many people who read it will believe that the fables it contains are true\", says, \"It astonishes and worries me that so many people believe these lies\".\n\nScientist comments, \"It amuses and disappoints me that the cardinal is talking about The Da Vinci [...]";
	System.out.println( FeedUtils.adjustDescription(input) );
        // TODO add your test code below by replacing the default call to fail.
       //  fail("The test case is empty.");
    }

    public void testGetItemDate()
    {
        System.out.println("testGetItemDate");
        
        // TODO add your test code below by replacing the default call to fail.
        // fail("The test case is empty.");
    }
	
}
