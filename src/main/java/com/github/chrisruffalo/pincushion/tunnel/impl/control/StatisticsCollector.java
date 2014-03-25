package com.github.chrisruffalo.pincushion.tunnel.impl.control;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.github.chrisruffalo.pincushion.model.tunnel.TunnelStatistics;

@Sharable
public class StatisticsCollector extends ChannelHandlerAdapter {

	private AtomicInteger activeConnections;
	
	private AtomicInteger totalConnections;

	private AtomicLong bytesRead;
	
	private AtomicLong bytesReturned;
	
	public StatisticsCollector() {
		this.activeConnections = new AtomicInteger(0);
		this.totalConnections = new AtomicInteger(0);
		
		this.bytesRead = new AtomicLong(0);
		this.bytesReturned = new AtomicLong(0);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.activeConnections.incrementAndGet();
		this.totalConnections.incrementAndGet();
		
		super.channelActive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if(msg instanceof ByteBuf) {
			this.bytesRead.addAndGet(((ByteBuf)msg).readableBytes());
		}
		
		super.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

		if(msg instanceof ByteBuf) {
			this.bytesReturned.addAndGet(((ByteBuf)msg).readableBytes());
		}
		
		super.write(ctx, msg, promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.activeConnections.decrementAndGet();
		
		super.channelInactive(ctx);
	}

	public TunnelStatistics collect() {
		TunnelStatistics stats = new TunnelStatistics();
		
		// get connections
		stats.setActiveConnections(this.activeConnections.intValue());
		stats.setTotalConnections(this.totalConnections.intValue());
		
		// get bytes
		stats.setRead(this.bytesRead.longValue());
		stats.setReturned(this.bytesReturned.longValue());
		
		return stats;
	}
}
