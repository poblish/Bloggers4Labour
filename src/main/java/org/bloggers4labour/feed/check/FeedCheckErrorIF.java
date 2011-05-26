/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check;

/**
 *
 * @author andrewregan
 */
public interface FeedCheckErrorIF extends FeedCheckResultIF
{
	String getMessage();
	Exception getException();
}