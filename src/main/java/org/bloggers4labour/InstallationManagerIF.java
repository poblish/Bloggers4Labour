/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour;

import com.hiatus.htl.IncludeFileLocator;
import java.util.Set;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author andrewregan
 */
public interface InstallationManagerIF 
{
	public InstallationStatus getStatus();
	public InstallationIF get( String inName);
	public Set<String> getInstallationNames();

	public Iterable<IncludeFileLocator> getFileLocators();

	public DataSource lookupDataSource( String inName) throws NamingException;

	public void restart();
	public void startIfStopped();
	public void stop();
}