/** The application module: declares what it needs. */
module com.example.app {
    requires com.example.greet;             // reads the library module (checked at compile AND launch time)
    requires java.logging;                  // a JDK module; java.base is always required implicitly
}
