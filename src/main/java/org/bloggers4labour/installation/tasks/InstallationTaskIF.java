/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.installation.tasks;

import org.bloggers4labour.InstallationIF;

/**
 *
 * @author andrewregan
 */
public interface InstallationTaskIF extends Runnable
{
	InstallationIF getInstallation();

	long getDelayMS();
	long getFrequencyMS();
}