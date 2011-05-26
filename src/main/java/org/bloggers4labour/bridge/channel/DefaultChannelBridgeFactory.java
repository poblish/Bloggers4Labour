/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel;

/**
 *
 * @author andrewregan
 */
public class DefaultChannelBridgeFactory implements ChannelBridgeFactoryIF
{
	/*******************************************************************************
	*******************************************************************************/
	public ChannelBridgeIF getInstance()
	{
		return new ChannelBridgeIF()
		{
			/*******************************************************************************
			*******************************************************************************/
			@SuppressWarnings("unchecked")
			public ChannelIF bridge( final de.nava.informa.core.ChannelIF inOriginal)
			{
				try
				{
					Class<? extends ChannelIF>	thePClazz = (Class<? extends ChannelIF>) Class.forName("com.hiatus.poblish2.b4l.PoblishChannelImpl");

					return thePClazz.getConstructor( de.nava.informa.core.ChannelIF.class).newInstance(inOriginal);
				}
				catch (Throwable ex)
				{
					;
				}

				return new DefaultChannelImpl(inOriginal);
			}

			/*******************************************************************************
			*******************************************************************************/
			public de.nava.informa.core.ChannelIF bridge( final ChannelIF inOriginal)
			{
				return inOriginal;
			}
		};
	}
}
