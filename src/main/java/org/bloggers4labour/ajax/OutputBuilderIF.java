/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.ajax;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author andrewregan
 */
public interface OutputBuilderIF extends OutputIF
{
	public OutputElementIF getRoot();

	public OutputElementIF newElement( final String inName, final Map<String,Object> inAttrs);
	public OutputElementIF newWrapperElement( final String inName);

	public void setContentType( final HttpServletResponse inResponse);
}
