/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.req;

/**
 *
 * @author andrewregan
 */
public interface ParametersIF 
{
	String getParameter( final String inKey);

	String getTrimmedRequestString( final String inKey);
	String getTrimmedRequestString( final String inKey, final String inDefaultVal);

	int parseRequestInt( final String inKey, final int inDefaultVal);
}
