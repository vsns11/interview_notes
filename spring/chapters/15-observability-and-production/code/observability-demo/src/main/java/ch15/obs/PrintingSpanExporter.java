package ch15.obs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * A tiny "logging exporter": instead of sending finished spans to a collector
 * (OTLP -> Jaeger/Tempo/Zipkin), keep them in memory so we can print them.
 * Boot adds every SpanExporter bean to its BatchSpanProcessor.
 */
@Component
public class PrintingSpanExporter implements SpanExporter {

    private final List<SpanData> finished = new CopyOnWriteArrayList<>();

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        finished.addAll(spans);
        return CompletableResultCode.ofSuccess();
    }

    public List<SpanData> finished() { return finished; }

    public void clear() { finished.clear(); }

    @Override
    public CompletableResultCode flush() { return CompletableResultCode.ofSuccess(); }

    @Override
    public CompletableResultCode shutdown() { return CompletableResultCode.ofSuccess(); }
}
