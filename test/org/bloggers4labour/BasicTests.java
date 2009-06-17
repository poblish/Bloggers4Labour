/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author andrewregan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	org.bloggers4labour.HelloWorldTest.class,
	org.bloggers4labour.FeedUtilsTest.class,
	org.bloggers4labour.mail.DigestSenderTest.class
})
public class BasicTests
{
}