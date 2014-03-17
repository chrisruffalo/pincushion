package com.github.chrisruffalo.multitunnel.tunnel.impl.forward;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public abstract class ChannelForwarder extends ChannelHandlerAdapter {

	protected abstract Channel target();
	
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		final Channel channel = this.target();
		
		// do write on active target channel
        if (channel != null && channel.isActive()) {
        	channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        // was able to flush out data, signal it to read
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = this.target();
        if (channel != null) {
        	ChannelForwarder.closeOnFlush(channel);
        }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		// close target
		Channel channel = this.target();
		if(channel != null && channel.isOpen()) {
			channel.close();
		}
		// close local
		ChannelForwarder.closeOnFlush(ctx.channel());		
	}	
	
    /**
     * Utility method for closing a channel and flushing it by sending an empty buffer out.
     * 
     * @param ch
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
        	ch.close();
        }
    }
		
}
