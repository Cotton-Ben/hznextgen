package com.hazelcast2.internal.cluster;

import com.hazelcast2.serialization.SerializationService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.SpiService;

import java.io.Serializable;
import java.util.concurrent.*;

/**
 * The cluster follows the active object pattern. There is a single thread which will do all operations. All interaction
 * with this object will go through a mailbox.
 */
public class Cluster implements SpiService {

    public static final String SERVICE_NAME = "hz:impl:clusterService";


    public final static ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(1);

    {
        TIMER.setRemoveOnCancelPolicy(true);
    }

    private final BlockingQueue mailbox = new LinkedBlockingQueue();
    private final short serviceId;
    private final SerializationService serializationService;
    private ScheduledFuture<?> future;

    public Cluster(ClusterSettings clusterSettings) {
        this.serviceId = clusterSettings.serviceId;
        this.serializationService = clusterSettings.serializationService;
    }

    public void start() {
        ClusterThread thread = new ClusterThread();
    //    thread.start();
     //   future = TIMER.scheduleWithFixedDelay(new TimerTask(), 0, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        mailbox.add(new StopEvent());
       // future.cancel(true);
    }

    @Override
    public short getServiceId() {
        return serviceId;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void dispatch(InvocationEndpoint source, byte[] invocationBytes) {
        mailbox.add(new InvocationEvent(source, invocationBytes));
    }

    private class TimerTask implements Runnable {
        @Override
        public void run() {
            TimeEvent timeUpdate = new TimeEvent(System.currentTimeMillis());
            mailbox.add(timeUpdate);
        }
    }

    private static class InvocationEvent {
        private final InvocationEndpoint source;
        private final byte[] invocationBytes;

        private InvocationEvent(InvocationEndpoint source, byte[] invocationBytes) {
            this.source = source;
            this.invocationBytes = invocationBytes;
        }
    }

    private static class TimeEvent {
        private final long timeMillis;

        private TimeEvent(long timeMillis) {
            this.timeMillis = timeMillis;
        }
    }

    private static class StopEvent {

    }

    private static class MemberJoinedEvent implements Serializable {

    }

    private static class MemberLeftEvent implements Serializable {

    }

    private static class PartitionEvent implements Serializable {
        private final int partitionId;
        private final boolean enable;

        private PartitionEvent(int partitionId, boolean enable) {
            this.partitionId = partitionId;
            this.enable = enable;
        }
    }

    private class ClusterThread extends Thread {
        public void run() {
            for (; ; ) {
                try {
                    if (!doRun()) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean doRun() throws Exception {
            Object item = mailbox.take();
            if (item instanceof StopEvent) {
                return false;
            } else if (item instanceof TimeEvent) {

            } else if (item instanceof InvocationEvent) {

            } else if (item instanceof PartitionEvent) {

            } else if (item instanceof MemberLeftEvent) {

            } else if (item instanceof MemberJoinedEvent) {

            }

            return true;
        }
    }
}
