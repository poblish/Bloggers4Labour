/*
 * FormatUtils.java
 *
 * Created on May 15, 2005, 1:42 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

import java.util.EnumSet;

/**
 *
 * @author andrewre
 */
public class FormatUtils
{
	private static EnumSet<FormatOption>	s_Defaults = EnumSet.of(FormatOption.BASIC);

	/*******************************************************************************		
	*******************************************************************************/
	public static EnumSet<FormatOption> defaultOptions()
	{
		return s_Defaults; // EnumSet.of(FormatOption.BASIC);
	}

	/*******************************************************************************		
	*******************************************************************************/
	public static boolean allowingImages( EnumSet<FormatOption> inOptions)
	{
		return inOptions.contains( FormatOption.ALLOW_IMAGES ); // (( inOptions & ALLOW_IMAGES) != 0);
	}

	/*******************************************************************************		
	*******************************************************************************/
	public static boolean allowingBreaks( EnumSet<FormatOption> inOptions)
	{
		return inOptions.contains( FormatOption.ALLOW_BREAKS ); // (( inOptions & ALLOW_BREAKS) != 0);
	}
}
