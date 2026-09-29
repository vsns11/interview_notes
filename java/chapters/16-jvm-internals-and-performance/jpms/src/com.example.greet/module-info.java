/** A library module: exports its API package, keeps "internal" hidden. */
module com.example.greet {
    exports com.example.greet;              // public types here are usable by other modules
    // com.example.greet.internal is NOT exported: public classes there are still inaccessible outside
}
