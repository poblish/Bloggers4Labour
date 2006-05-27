/*
 * AddHandler.java
 *
 * Created on June 25, 2005, 9:39 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.headlines;

import de.nava.informa.core.ItemIF;
import org.bloggers4labour.Headlines;
import org.bloggers4labour.Installation;
import org.bloggers4labour.ItemContext;

/**
 *
 * @author andrewre
 */
public interface AddHandler extends Handler
{
	public void onAdd( final Installation inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt);
}
