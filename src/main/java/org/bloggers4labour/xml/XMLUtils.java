/*
 * XMLUtils.java
 *
 * Created on July 26, 2005, 3:57 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.xml;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author andrewre
 */
public class XMLUtils
{
	/*******************************************************************************
	*******************************************************************************/
	private XMLUtils()
	{
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public static String getIDValue( Node inParent, String inNodeName) throws TransformerException
	{
		try
		{
			final XPath	theXPathObj = XPathFactory.newInstance().newXPath();
			Node		matchedNode = (Node) theXPathObj.evaluate( inNodeName, inParent, XPathConstants.NODE);

			if ( matchedNode != null)
			{
				return getNodeIDValue(matchedNode);
			}
		}
		catch (XPathExpressionException ex) {}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getNodeIDValue( Node inOurNode)
	{
		return getNodeAttrValue( inOurNode, "id");
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String getNodeAttrValue( Node inOurNode, String inAttrName)
	{
		NamedNodeMap	nnm = inOurNode.getAttributes();

		if ( nnm != null)
		{
			Node	attrNode = nnm.getNamedItem(inAttrName);

			if ( attrNode != null)
			{
				return attrNode.getNodeValue();
			}
		}

		return null;
	}
}
