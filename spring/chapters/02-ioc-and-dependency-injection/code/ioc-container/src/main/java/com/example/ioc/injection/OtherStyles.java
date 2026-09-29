package com.example.ioc.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Setter and field injection — they work, but prefer the constructor (see 2.5). */
@Component
public class OtherStyles {
    @Autowired @Qualifier("push")
    Notifier fieldInjected;                   // field injection: needs reflection, can't be final

    Notifier setterInjected;

    @Autowired
    void setNotifier(@Qualifier("email") Notifier n) {   // setter/method injection
        this.setterInjected = n;
    }
}
