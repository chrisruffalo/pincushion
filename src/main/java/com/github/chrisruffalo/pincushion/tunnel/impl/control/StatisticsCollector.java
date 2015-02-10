package com.github.chrisruffalo.pincushion.tunnel.impl.control;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import com.github.chrisruffalo.pincushion.model.tunnel.TunnelStatistics;

@Sharable
public class StatisticsCollector extends ChannelHandlerAdapter {

    private static final AtomicIntegerFieldUpdater<StatisticsCollector> AC_UPDATER = AtomicIntegerFieldUpdater.newUpdater(StatisticsCollector.class, "activeConnections");
    private static final AtomicIntegerFieldUpdater<StatisticsCollector> TC_UPDATER = AtomicIntegerFieldUpdater.newUpdater(StatisticsCollector.class, "totalConnections");
    
    private static final AtomicLongFieldUpdater<StatisticsCollector> READ_UPDATER = AtomicLongFieldUpdater.newUpdater(StatisticsCollector.class, "bytesRead");
    private static final AtomicLongFieldUpdater<StatisticsCollector> RECD_UPDATER = AtomicLongFieldUpdater.newUpdater(StatisticsCollector.class, "bytesReturned");
    
	private volatile int activeConnections;
	
	private volatile int totalConnections;

	private volatile long bytesRead;
	
	private volatile long bytesReturned;
	
	public StatisticsCollector() {
		this.activeConnections = 0;
		this.totalConnections = 0;
		
		this.bytesRead = 0;
		this.bytesReturned = 0;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		AC_UPDATER.incrementAndGet(this);
		TC_UPDATER.incrementAndGet(this);
		
		super.channelActive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if(msg instanceof ByteBuf) {
		    final int localBytesRead = ((ByteBuf)msg).readableBytes();
			READ_UPDATER.addAndGet(this, localBytesRead);
		}
		
		super.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

		if(msg instanceof ByteBuf) {
		    final int localBytesReturned = ((ByteBuf)msg).readableBytes();
			RECD_UPDATER.addAndGet(this, localBytesReturned);
		}
		
		super.write(ctx, msg, promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		AC_UPDATER.decrementAndGet(this);

		super.channelInactive(ctx);
	}

	public TunnelStatistics collect() {
		final TunnelStatistics stats = new TunnelStatistics();
		
		// get connections
		stats.setActiveConnections(activeConnections);
		stats.setTotalConnections(totalConnections);
		
		// get bytes
		stats.setRead(bytesRead);
		stats.setReturned(bytesReturned);
		
		return stats;
	}

    public int getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(int totalConnections) {
        this.totalConnections = totalConnections;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getBytesReturned() {
        return bytesReturned;
    }

    public void setBytesReturned(long bytesReturned) {
        this.bytesReturned = bytesReturned;
    }
}
