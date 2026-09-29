package lld.ch04.dip;

import java.math.BigDecimal;
import lld.ch04.dip.app.NotificationService;
import lld.ch04.dip.infra.EmailSender;
import lld.ch04.dip.infra.RecordingSender;
import lld.ch04.dip.infra.SmsSender;
import lld.ch04.dip.locator.CheckoutFlow;
import lld.ch04.dip.locator.PayPalService;
import lld.ch04.dip.locator.ServiceLocator;
import lld.ch04.dip.plugin.FlacDecoder;
import lld.ch04.dip.plugin.MediaPlayer;
import lld.ch04.dip.plugin.Mp3Decoder;

/** This class is the COMPOSITION ROOT: the one place that knows the concrete classes. */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Before: NotificationService builds EmailService itself ===");
        new lld.ch04.dip.before.NotificationService().notifyUser("Your order shipped");

        System.out.println("=== 2. After: inject any NotificationSender ===");
        new NotificationService(new EmailSender()).notifyUser("u-42", "Your order shipped");
        new NotificationService(new SmsSender()).notifyUser("u-42", "Your order shipped");
        var recorder = new RecordingSender();
        new NotificationService(recorder).notifyUser("u-7", "Test message");
        System.out.println("test double captured: " + recorder.sent());

        System.out.println("=== 3. Service locator ===");
        var locator = new ServiceLocator();
        locator.register("PayPal", new PayPalService());
        new CheckoutFlow(locator).pay("PayPal", new BigDecimal("100.00"));

        System.out.println("=== 4. Plug-in architecture ===");
        var player = new MediaPlayer();
        player.install(new Mp3Decoder());
        player.install(new FlacDecoder());
        player.play("song.mp3");
        player.play("concert.flac");
        player.play("voice.ogg");
    }
}
