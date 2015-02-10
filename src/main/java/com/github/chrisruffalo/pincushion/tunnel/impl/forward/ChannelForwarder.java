package com.github.chrisruffalo.pincushion.tunnel.impl.forward;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChannelForwarder extends ChannelHandlerAdapter {
    
    private static final Logger SHARED_LOGGER = LoggerFactory.getLogger("shared-forwarding-logger");

	protected abstract Channel target();
	
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		final Channel target = this.target();
		
		// do write on active target channel
        if (target != null && target.isActive()) {
        	target.write(msg);
        }

        this.logger().trace("writing");
	}
		
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

	    final Channel target = this.target();
	    if (target != null && target.isActive()) {
	        // flush
	        target.flush();

	        // restart read
	        ctx.read();
	    }

        this.logger().trace("done writing");
	    
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
	    // log error
	    this.logger().error("({}) closed on error: {}", this.getClass().getSimpleName(), cause.getMessage(), cause);
	    
		// close target
		final Channel target = this.target();
		if(target != null && target.isOpen()) {
			ChannelForwarder.closeOnFlush(target);
		}

		// close local
		ChannelForwarder.closeOnFlush(ctx.channel());
	}	
	
	/**
	 * Give children access to shared logger
	 * 
	 * @return
	 */
	protected Logger logger() {
	    return SHARED_LOGGER;
	}
	
    /**
     * Utility method for closing a channel and flushing it by sending an empty buffer out.
     * 
     * @param ch
     */
    protected static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        } else {
        	ch.close();
        }
    }
		
}
