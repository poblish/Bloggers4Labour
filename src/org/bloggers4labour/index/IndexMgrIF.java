/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.index;

import java.util.List;
import org.apache.lucene.search.Query;

/**
 *
 * @author andrewregan
 */
public interface IndexMgrIF 
{
	List<SearchMatch> runQuery( final String inQueryStr);
	List<SearchMatch> runQuery( final Query inQuery);

	void optimise();
}