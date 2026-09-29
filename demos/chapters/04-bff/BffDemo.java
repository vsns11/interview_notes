import java.util.*;

/**
 * Backends for Frontends (BFF) — runnable demo.
 *
 *   javac BffDemo.java && java BffDemo
 *
 * Shows:
 *   1. Over-fetching: one shared API forces mobile to download desktop payloads
 *   2. Two BFFs projecting the SAME source object into different shapes
 *   3. Client-specific logic (TV HD entitlement) living where it belongs
 */
public class BffDemo {

    // ---------- The single source of truth ----------
    record Product(String id, String name, double price,
                   String longDescription, List<String> hiResImages,
                   Map<String, String> specs, List<String> reviews) {

        /** Rough payload size, to make over-fetching measurable. */
        int approxBytes() {
            int n = id.length() + name.length() + 8 + longDescription.length();
            for (String s : hiResImages) n += s.length();
            for (var e : specs.entrySet()) n += e.getKey().length() + e.getValue().length();
            for (String r : reviews) n += r.length();
            return n;
        }
    }

    static class ProductService {
        private int callCount = 0;

        Product getProductById(String id) {
            callCount++;
            return new Product(
                id,
                "Noise-Cancelling Headphones",
                299.99,
                "A".repeat(2000),                                   // long marketing copy
                List.of("https://cdn/img/hero-4k.jpg".repeat(3),
                        "https://cdn/img/side-4k.jpg".repeat(3),
                        "https://cdn/img/case-4k.jpg".repeat(3)),
                Map.of("driver", "40mm", "battery", "30h",
                       "weight", "254g", "codec", "LDAC/aptX"),
                List.of("R".repeat(400), "R".repeat(400), "R".repeat(400))
            );
        }

        int callCount() { return callCount; }
    }

    // ---------- What each client actually needs ----------
    record WebProductDetails(String id, String name, double price,
                             String description, List<String> images,
                             Map<String, String> specs, List<String> reviews) {
        int approxBytes() {
            int n = id.length() + name.length() + 8 + description.length();
            for (String s : images) n += s.length();
            for (var e : specs.entrySet()) n += e.getKey().length() + e.getValue().length();
            for (String r : reviews) n += r.length();
            return n;
        }
    }

    record MobileProductDetails(String id, String name, double price,
                                String shortDescription, String thumbnail) {
        int approxBytes() {
            return id.length() + name.length() + 8
                 + shortDescription.length() + thumbnail.length();
        }
    }

    record WatchProductDetails(String name, double price) {
        int approxBytes() { return name.length() + 8; }
    }

    // ---------- The BFFs: projection layers, not sources of truth ----------
    static class WebBff {
        private final ProductService service;
        WebBff(ProductService s) { this.service = s; }

        WebProductDetails getProductDetails(String id) {
            Product p = service.getProductById(id);
            return new WebProductDetails(p.id(), p.name(), p.price(),
                    p.longDescription(), p.hiResImages(), p.specs(), p.reviews());
        }
    }

    static class MobileBff {
        private final ProductService service;
        MobileBff(ProductService s) { this.service = s; }

        MobileProductDetails getProductDetails(String id) {
            Product p = service.getProductById(id);
            return new MobileProductDetails(
                    p.id(), p.name(), p.price(),
                    truncate(p.longDescription(), 120),          // trim the copy
                    p.hiResImages().get(0).replace("4k", "thumb") // smaller image
            );
        }
    }

    static class WatchBff {
        private final ProductService service;
        WatchBff(ProductService s) { this.service = s; }

        WatchProductDetails getProductDetails(String id) {
            Product p = service.getProductById(id);
            return new WatchProductDetails(truncate(p.name(), 20), p.price());
        }
    }

    /** Client-specific logic that would pollute a shared gateway. */
    static class TvBff {
        private final ProductService service;
        TvBff(ProductService s) { this.service = s; }

        String getProductDetails(String id, boolean deviceHdCertified) {
            Product p = service.getProductById(id);
            if (!deviceHdCertified) {
                return "SD-only payload for " + p.name();   // extra authorization check
            }
            return "Full HD payload for " + p.name()
                 + " with " + p.hiResImages().size() + " 4K images";
        }
    }

    static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    public static void main(String[] args) {
        ProductService service = new ProductService();
        WebBff web       = new WebBff(service);
        MobileBff mobile = new MobileBff(service);
        WatchBff watch   = new WatchBff(service);
        TvBff tv         = new TvBff(service);

        Product raw = service.getProductById("p-1");

        System.out.println("\n=== 1. The over-fetching problem ===");
        System.out.printf("  shared API payload:  %,6d bytes%n", raw.approxBytes());
        System.out.println("  >> a watch showing name + price downloads ALL of that");

        System.out.println("\n=== 2. Each BFF projects the same object differently ===");
        int webBytes    = web.getProductDetails("p-1").approxBytes();
        int mobileBytes = mobile.getProductDetails("p-1").approxBytes();
        int watchBytes  = watch.getProductDetails("p-1").approxBytes();

        System.out.printf("  Web BFF:     %,6d bytes  (%3d%% of shared)%n",
                webBytes, webBytes * 100 / raw.approxBytes());
        System.out.printf("  Mobile BFF:  %,6d bytes  (%3d%% of shared)%n",
                mobileBytes, mobileBytes * 100 / raw.approxBytes());
        System.out.printf("  Watch BFF:   %,6d bytes  (%3d%% of shared)%n",
                watchBytes, watchBytes * 100 / raw.approxBytes());
        System.out.println("  >> mobile saves ~" + (raw.approxBytes() - mobileBytes)
                + " bytes PER PRODUCT on a metered connection");

        System.out.println("\n=== 3. All BFFs call the SAME upstream service ===");
        System.out.println("  ProductService invocations so far: " + service.callCount());
        System.out.println("  >> BFFs are projection layers - one source of truth remains");

        System.out.println("\n=== 4. Client-specific logic belongs in its own BFF ===");
        System.out.println("  certified TV:     " + tv.getProductDetails("p-1", true));
        System.out.println("  uncertified TV:   " + tv.getProductDetails("p-1", false));
        System.out.println("  >> this HD check is meaningless to web/mobile/watch;");
        System.out.println("     in a shared gateway it would be a device-type conditional");
    }
}
