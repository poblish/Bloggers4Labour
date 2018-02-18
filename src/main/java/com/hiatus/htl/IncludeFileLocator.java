/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hiatus.htl;

import com.hiatus.file.File;
import java.util.Locale;

/**
 *
 * @author andrewregan
 */
public interface IncludeFileLocator 
{
	public String getId();
	public File getIncludeFileObject( final Locale inLocale, final String inKey);
}
