package com.example.gatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GatewayServiceApplication.class)
public class EventGatewayServiceTest {

    @Autowired
    private EventGatewayService service;

    @MockBean
    private EventRepository eventRepo;

    @Test
    public void testIdempotency() {
        String eventId = "evt-1";
        Event existingEvent = new Event();
        existingEvent.setEventId(eventId);
        
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(existingEvent));
        
        Event newEvent = new Event();
        newEvent.setEventId(eventId);
        
        Event result = service.processEvent(newEvent);
        
        assertEquals(existingEvent, result);
        verify(eventRepo, never()).save(any());
    }

    @Test
    public void testValidation() {
        Event invalidEvent = new Event();
        invalidEvent.setEventId("evt-2");
        invalidEvent.setAmount(BigDecimal.valueOf(-10));
        
        assertThrows(IllegalArgumentException.class, () -> service.processEvent(invalidEvent));
    }
}