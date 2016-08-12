package io.opentracing.io.opentracing.mocktracer;

import io.opentracing.Span;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MockTracerTest {
    @Test
    public void testRootSpan() {
        // Create and finish a root Span.
        MockTracer tracer = new MockTracer();
        {
            Span span = tracer.buildSpan("tester").withStartTimestamp(1000).start();
            span.setTag("string", "foo");
            span.setTag("int", 7);
            span.log(1001, "event name", tracer);
            span.finish(2000);
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        // Check that the Span looks right.
        assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertEquals("tester", finishedSpan.operationName());
        assertEquals(0, finishedSpan.parentId());
        assertNotEquals(0, finishedSpan.context().traceId());
        assertNotEquals(0, finishedSpan.context().spanId());
        assertEquals(1000, finishedSpan.startMicros());
        assertEquals(2000, finishedSpan.finishMicros());
        Map<String, Object> tags = finishedSpan.tags();
        assertEquals(2, tags.size());
        assertEquals(7, tags.get("int"));
        assertEquals("foo", tags.get("string"));
        List<MockSpan.LogEntry> logs = finishedSpan.logEntries();
        assertEquals(1, logs.size());
        MockSpan.LogEntry log = logs.get(0);
        assertEquals(1001, log.timestampMicros());
        assertEquals("event name", log.eventName());
        assertEquals(tracer, log.payload());
    }

    @Test
    public void testChildSpan() {
        // Create and finish a root Span.
        MockTracer tracer = new MockTracer();
        {
            Span parent = tracer.buildSpan("parent").withStartTimestamp(1000).start();
            Span child = tracer.buildSpan("child").withStartTimestamp(1100).asChildOf(parent).start();
            child.finish(1900);
            parent.finish(2000);
        }
        List<MockSpan> finishedSpans = tracer.finishedSpans();

        // Check that the Spans look right.
        assertEquals(2, finishedSpans.size());
        MockSpan child = finishedSpans.get(0);
        MockSpan parent = finishedSpans.get(1);
        assertEquals("child", child.operationName());
        assertEquals("parent", parent.operationName());
        assertEquals(parent.context().spanId(), child.parentId());
        assertEquals(parent.context().traceId(), child.context().traceId());
    }
}
