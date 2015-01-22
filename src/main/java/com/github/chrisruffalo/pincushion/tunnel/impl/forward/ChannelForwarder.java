package com.github.chrisruffalo.pincushion.tunnel.impl.forward;

import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public abstract class ChannelForwarder extends ChannelHandlerAdapter {

	protected abstract Channel target();
	
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		final Channel target = this.target();
		
		// do write on active target channel
        if (target != null && target.isActive()) {
        	target.write(msg);
        }
	}
		
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

	    final Channel target = this.target();
	    if (target != null && target.isActive()) {
	        // flush
	        target.flush();
	        
	        // signal new read
	        ctx.read();
	    }
	    
	    super.channelReadComplete(ctx);
    }

    @Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		final Channel target = this.target();
        if (target != null) {
        	ChannelForwarder.closeOnFlush(target);
        }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		// close target
		final Channel target = this.target();
		if(target != null && target.isOpen()) {
			target.close();
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
