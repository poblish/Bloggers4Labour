/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.activity;

import java.net.URL;
import java.util.Locale;

/**
 *
 * @author andrewregan
 */
public interface LastPostTableIF 
{
	StringBuffer getTableContent( Locale inLocale);
	void store( final URL inChannel, long inTimeMSecs);
	void complete();
}