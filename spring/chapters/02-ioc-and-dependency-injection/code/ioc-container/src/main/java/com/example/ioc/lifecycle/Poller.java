package com.example.ioc.lifecycle;

import com.example.ioc.Trace;
import org.springframework.context.SmartLifecycle;

/** Something that must run only while the whole context is up (e.g. a queue poller). */
public class Poller implements SmartLifecycle {
    private volatile boolean running;
    @Override public void start()  { running = true;  Trace.add("SmartLifecycle.start (Poller)"); }
    @Override public void stop()   { running = false; Trace.add("SmartLifecycle.stop (Poller)"); }
    @Override public boolean isRunning() { return running; }
    @Override public int getPhase() { return 0; }   // lower phase starts earlier, stops later
}
