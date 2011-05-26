/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.geocode;

/**
 *
 * @author andrewregan
 */
public interface PostCodeGeocoderCacheIF 
{
	public int size();

	public Location get( final String inPC);
	public void put( final String inPC, final Location inLoc);
}