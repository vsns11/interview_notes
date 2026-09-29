package ch01.gc;

import java.util.ArrayList;
import java.util.List;

/** Allocates lots of short-lived objects and a few long-lived ones. Run with -Xlog:gc to watch the GC. */
public class GarbageMaker {
    public static void main(String[] args) {
        List<byte[]> keep = new ArrayList<>();          // long-lived: stays reachable
        long created = 0;
        for (int i = 0; i < 200_000; i++) {
            byte[] temp = new byte[1024];                // short-lived: garbage after this iteration
            temp[0] = (byte) i;
            created++;
            if (i % 20_000 == 0) keep.add(new byte[1024 * 1024]);
        }
        System.out.println("temporary arrays created: " + created);
        System.out.println("arrays still reachable  : " + keep.size());
    }
}
