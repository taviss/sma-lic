package com.sma.core.camera.rtmp;

import com.sma.core.camera.api.Camera;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.net.rtmp.RTMPConnection;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.stream.AbstractClientStream;
import org.red5.server.stream.IStreamData;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RTMPCamera implements Camera {
    private String server;
    private int port;
    private String id;
    private String path;
    
    private final BlockingQueue<ByteBuffer> imageQueue = new LinkedBlockingQueue();
    
    public RTMPCamera(String id, String server, String path, int port) {
        this.id = id;
       this.server = server;
       this.path = path;
       this.port = port;
    }
    
    @Override
    public byte[] getSnapshot() {
        final SMRTMPClient rtmpClient = new SMRTMPClient();
        final int duration = 10000;
        final String name = "snap.jpg";

        IPendingServiceCallback callback = new IPendingServiceCallback() {
            public void resultReceived(IPendingServiceCall call) {
                System.out.println("call result: " + call);
                //logger.debug("service call result: " + call);
                if ("connect".equals(call.getServiceMethodName())) {
                    rtmpClient.createStream(this);
                } else if ("createStream".equals(call.getServiceMethodName())) {
                    Double streamId = (Double) call.getResult();
                    //logger.debug("createStream result stream id: " + streamId);
                    //logger.debug("playing video by name: " + name);
                    rtmpClient.play(streamId, name, 0, duration);
                }
            }
        };
        
        rtmpClient.connect(server, port, path, callback);
        
        try {
            ByteBuffer data = imageQueue.take();

            return data.array();
        } catch(InterruptedException e) {
            return null;
        }
    }

    @Override
    public String getId() {
        return this.id;
    }
    
    private class SMRTMPClient extends RTMPClient {
        private RTMPConnection conn;

        @Override
        public void connectionOpened(RTMPConnection conn) {
            super.connectionOpened(conn);
            this.conn = conn;
        }

        @Override
        public void connectionClosed(RTMPConnection conn) {
            super.connectionClosed(conn);
        }
        
        @Override
        public void createStream(IPendingServiceCallback callback) {
            IPendingServiceCallback wrapper = new CreateStreamCallBack(callback);
            invoke("createStream", null, wrapper);
        }

        private class CreateStreamCallBack implements IPendingServiceCallback {

            private IPendingServiceCallback wrapped;

            public CreateStreamCallBack(IPendingServiceCallback wrapped) {
                this.wrapped = wrapped;
            }

            public void resultReceived(IPendingServiceCall call) {
                Double streamIdInt = (Double) call.getResult();
                if (conn != null && streamIdInt != null) {
                    Stream stream = new Stream();
                    stream.setConnection(conn);
                    stream.setStreamId(streamIdInt.intValue());
                    conn.addClientStream(stream);
                }
                wrapped.resultReceived(call);
            }

        }
    }

    private class Stream extends AbstractClientStream implements IEventDispatcher {

        public void close() { }

        public void start() { }

        public void stop() { }

        public void dispatchEvent(IEvent event) {
            if (!(event instanceof IRTMPEvent)) {
                //logger.debug("skipping non rtmp event: " + event);
                return;
            }
            IRTMPEvent rtmpEvent = (IRTMPEvent) event;
            /*
            if (logger.isDebugEnabled()) {
                logger.debug("rtmp event: " + rtmpEvent.getHeader() + ", "
                        + rtmpEvent.getClass().getSimpleName());
            }*/
            if (!(rtmpEvent instanceof IStreamData)) {
                //logger.debug("skipping non stream data");
                return;
            }
            if (rtmpEvent.getHeader().getSize() == 0) {
                //logger.debug("skipping event where size == 0");
                return;
            }
            if (rtmpEvent instanceof VideoData) {
                ByteBuffer data = ((IStreamData) rtmpEvent).getData().asReadOnlyBuffer().buf();
            } else if (rtmpEvent instanceof AudioData) {
            }
            
        }
    }
}
