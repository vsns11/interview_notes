package lld.ch04.dip.plugin;

public class FlacDecoder implements AudioDecoder {
    @Override
    public String extension() {
        return "flac";
    }

    @Override
    public void decode(String fileName) {
        System.out.println("Decoding FLAC file: " + fileName);
    }
}
