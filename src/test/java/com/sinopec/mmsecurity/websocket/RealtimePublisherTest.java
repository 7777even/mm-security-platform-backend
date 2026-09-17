package com.sinopec.mmsecurity.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RealtimePublisherTest {

    @Mock
    private RealtimeBroadcastService broadcastService;

    @InjectMocks
    private RealtimePublisher publisher;

    @Test
    void publish_buildsChangedTopicAndPayload() {
        publisher.publish("device", EntityChangedEvent.Action.UPDATED, "D1", null);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(broadcastService).broadcast(topicCaptor.capture(), payloadCaptor.capture());

        assertEquals("device.changed", topicCaptor.getValue());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals("device", payload.get("domain"));
        assertEquals("updated", payload.get("action"));
        assertEquals("D1", payload.get("id"));
    }
}
