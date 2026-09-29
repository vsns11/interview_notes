package ch14.async;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SignupService {
    private final ApplicationEventPublisher events;
    private final Trace trace;

    public SignupService(ApplicationEventPublisher events, Trace trace) {
        this.events = events;
        this.trace = trace;
    }

    public void signUp(String email) {
        trace.add("publisher: publishing on " + Trace.thread());
        events.publishEvent(new UserSignedUp(email));
        trace.add("publisher: publishEvent returned on " + Trace.thread());
    }
}
