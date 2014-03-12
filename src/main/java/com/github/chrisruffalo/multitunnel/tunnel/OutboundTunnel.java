package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class OutboundTunnel extends ChannelHandlerAdapter {
	
	private final Logger logger;
	
	//private final int sourcePort;
	
	private final int destinationPort;
	
	private final String destinationHost;
	
	private final AtomicReference<Channel> channelRef;
	
	private final EventLoopGroup workerGroup;
	
	private final Semaphore remoteLock;
	
	private final Semaphore originLock;
	
	private Channel origin;
	
	public OutboundTunnel(EventLoopGroup workerGroup, int sourcePort, String destinationHost, int destinationPort) {
		// save values
		this.destinationPort = destinationPort;
		this.destinationHost = destinationHost;
		//this.sourcePort= sourcePort;
		
		// create descriptive logger
		this.logger = LoggerFactory.getLogger("outbound-" + UUID.randomUUID().toString() + "[" + sourcePort + "] => [" + destinationHost + ":" + destinationPort + "]");
		
		// same worker group
		this.workerGroup = workerGroup;
		
		// lock
		this.remoteLock = new Semaphore(1, true);
		this.originLock = new Semaphore(1, true);
		
		// init ref
		this.channelRef = new AtomicReference<>(null);
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		// get/create remote channel
		Channel remote = this.remote(true);
		
		// can't forward to a null channel
		if(remote == null) {
			this.logger.error("no remote channel available");
			ctx.close();
			return;
		}
			
		if(ctx.channel().id().equals(remote.id())) {
			this.originLock.acquireUninterruptibly();
			
			this.logger.trace("<------");
			
			// write back
			this.origin.writeAndFlush(msg);
			this.originLock.release();
		} else {
			// save where results need to return to
			this.originLock.acquireUninterruptibly();
			this.origin = ctx.channel();
			this.originLock.release();
			
			this.logger.trace("------>");
			
			// forward input to client pipeline
			remote.writeAndFlush(msg);
		}				
	}	
	
	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// if we are on the origin side kill and cleanup
		this.originLock.acquireUninterruptibly();
		if(this.origin != null && ctx.channel().id().equals(this.origin.id())) {
			this.logger.trace("origin disconnect");
			this.killRemote();
		}		
		this.originLock.release();
				
		// propagate
		super.disconnect(ctx, promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// if we are on the origin side kill and cleanup
		this.originLock.acquireUninterruptibly();
		if(this.origin != null && ctx.channel().id().equals(this.origin.id())) {
			this.logger.trace("origin close");
			this.killRemote();
		}		
		this.originLock.release();
				
		super.close(ctx, promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		// if we are on the origin side, handle inactivity normally
		boolean done = false;
		this.originLock.acquireUninterruptibly();
		if(this.origin != null && ctx.channel().id().equals(this.origin.id())) {
			this.logger.trace("origin inactive");
			done = true;
		}		
		this.originLock.release();
		
		// do the rest
		if(!done) {
			// get/create remote channel
			Channel remote = this.remote(false);
			
			// can't do anything with a null remote
			// (which may already be inactive?)
			if(remote == null) {
				return;
			}
				
			if(ctx.channel().id().equals(remote.id())) {
				this.logger.trace("remote inactive");
				this.killRemote();
				
				this.originLock.acquireUninterruptibly();
				if(this.origin != null) {
					this.origin.close();
					this.origin = null;
				}
				this.originLock.release();
			}
		}		
		
		super.channelInactive(ctx);
	}

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.logger.error("Error: {}", cause.getMessage(), cause);
        ctx.close();
    }

	private Channel remote(boolean create) {		
		// acquire
		this.remoteLock.acquireUninterruptibly();
		
		Channel ref = this.channelRef.get();
		if(ref != null) {
			this.logger.trace("using existing remote");
		} else if(create) {
			
			final ChannelHandlerAdapter _this = this;
			
			ChannelFuture future = null;
			
			// create remote connection details
			Bootstrap b = new Bootstrap()
	        	.group(this.workerGroup)
	        	.channel(NioSocketChannel.class)
		        .option(ChannelOption.TCP_NODELAY, true)
		        .option(ChannelOption.SO_KEEPALIVE, true)
		        .handler(new ChannelInitializer<SocketChannel>() {
		            @Override
		            public void initChannel(SocketChannel ch) throws Exception {
		            	ch.pipeline().addLast("return-channel", _this);
		            }
		        });
			
	        // start the client and keep the channel for writing to
	        try {
	        	future = b.connect(this.destinationHost, this.destinationPort).sync();
			} catch (InterruptedException e) {
				future = null;
				this.logger.error("Could not establish connection to remote: {}", e.getMessage(), e);
			}       
			
			// if no future was created/saved, return
			if(future == null) {
				return null;
			}
			
			// save and return ref
			ref = future.channel();
			this.channelRef.set(ref);
			this.logger.trace("created remote");
		}
		
		// release
		this.remoteLock.release();	
		
		return ref; 

	}
	
	private void killRemote() {
		// acquire
		this.remoteLock.acquireUninterruptibly();
		
		Channel remote = this.channelRef.get();
		if(remote != null) {
			if(remote.isOpen()) {
				remote.close();
			}
			this.channelRef.set(null);
			this.logger.trace("remote killed");
		}	
		
		// release
		this.remoteLock.release();
	}
}
