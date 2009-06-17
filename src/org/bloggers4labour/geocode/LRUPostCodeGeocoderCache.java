/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.geocode;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections.map.LRUMap;


/**
 *
 * @author andrewregan
 */
@SuppressWarnings("unchecked")
public class LRUPostCodeGeocoderCache implements PostCodeGeocoderCacheIF
{
	private Map<String,Location>	m_Cache = Collections.synchronizedMap((Map<String,Location>) new LRUMap(32) );

	/*******************************************************************************
	*******************************************************************************/
	private LRUPostCodeGeocoderCache()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public static PostCodeGeocoderCacheIF getInstance()
	{
		return LazyHolder.s_Inst;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void put( final String inPC, final Location inLoc)
	{
		m_Cache.put( inPC, inLoc);
	}

	/*******************************************************************************
	*******************************************************************************/
	public Location get( final String inPC)
	{
		return m_Cache.get(inPC);
	}

	/*******************************************************************************
	*******************************************************************************/
	public int size()
	{
		return m_Cache.size();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return m_Cache.toString();
	}

	/*******************************************************************************
		(AGR) 5 June 2005. See:
		    <http://www-106.ibm.com/developerworks/java/library/j-jtp03304/>
	*******************************************************************************/
	private static class LazyHolder
	{
		private static PostCodeGeocoderCacheIF	s_Inst = new LRUPostCodeGeocoderCache();
	}
}