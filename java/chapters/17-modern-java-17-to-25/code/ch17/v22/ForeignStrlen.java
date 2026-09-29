package ch17.v22;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * JEP 454 (Java 22): the Foreign Function & Memory API calls C code without JNI.
 * Calls the C library's strlen(). Run with --enable-native-access=ALL-UNNAMED to silence the warning.
 */
public class ForeignStrlen {
    public static void main(String[] args) throws Throwable {
        Linker linker = Linker.nativeLinker();
        MethodHandle strlen = linker.downcallHandle(
                linker.defaultLookup().find("strlen").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));   // size_t strlen(const char*)

        try (Arena arena = Arena.ofConfined()) {                     // off-heap memory freed at the end of the block
            MemorySegment cString = arena.allocateFrom("Hello, native world!");     // UTF-8, NUL-terminated
            long len = (long) strlen.invokeExact(cString);
            System.out.println("strlen = " + len);

            MemorySegment ints = arena.allocate(ValueLayout.JAVA_INT, 5);            // like int[5] in C
            for (int i = 0; i < 5; i++) ints.setAtIndex(ValueLayout.JAVA_INT, i, i * i);
            System.out.println("off-heap ints: " + java.util.Arrays.toString(ints.toArray(ValueLayout.JAVA_INT))
                    + ", " + ints.byteSize() + " bytes");
        }
    }
}
