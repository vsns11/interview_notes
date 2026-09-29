package lld.ch04.dip.plugin;

public class Mp3Decoder implements AudioDecoder {
    @Override
    public String extension() {
        return "mp3";
    }

    @Override
    public void decode(String fileName) {
        System.out.println("Decoding MP3 file: " + fileName);
    }
}
