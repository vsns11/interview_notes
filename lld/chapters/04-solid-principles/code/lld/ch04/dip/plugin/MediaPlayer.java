package lld.ch04.dip.plugin;

import java.util.HashMap;
import java.util.Map;

/** The core. New formats arrive as plug-ins; this class is never edited for them. */
public class MediaPlayer {
    private final Map<String, AudioDecoder> decoders = new HashMap<>();

    public void install(AudioDecoder decoder) {
        decoders.put(decoder.extension(), decoder);
    }

    public void play(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        AudioDecoder decoder = decoders.get(ext);
        if (decoder == null) {
            System.out.println("No plug-in installed for ." + ext);
            return;
        }
        decoder.decode(fileName);
        System.out.println("Playing audio...");
    }
}
