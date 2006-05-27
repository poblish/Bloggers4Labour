/*
 * DataSourceConnTagTEI.java
 *
 * Created on 02 April 2006, 00:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 *
 * @author andrewre
 */
public class DataSourceConnTagTEI extends TagExtraInfo
{
	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnTagTEI()
	{
		super();
	}

	/*******************************************************************************
	*******************************************************************************/
	public VariableInfo[] getVariableInfo( TagData inData)
	{
		VariableInfo[]	varInfo = { new VariableInfo( "ds_conn", "org.bloggers4labour.sql.DataSourceConnection", true, VariableInfo.NESTED),
					    new VariableInfo( "ds_exception", "java.lang.Exception", true, VariableInfo.NESTED) };

		return varInfo;
	}
}