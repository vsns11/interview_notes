package ch13.binary;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Binary data: DataOutput/DataInput streams, then the NIO channel + buffer model. */
public class DataAndChannels {
    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("java1315_bin");
        Path f = dir.resolve("record.bin");
        try {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(f)))) {
                out.writeInt(2025);            // 4 bytes, big-endian
                out.writeDouble(3.5);          // 8 bytes
                out.writeBoolean(true);        // 1 byte
                out.writeUTF("ok");            // 2-byte length + modified UTF-8
            }
            System.out.println("file size = " + Files.size(f) + " bytes (4 + 8 + 1 + 2 + 2)");
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(f)))) {
                // must read in exactly the same order and types
                System.out.println("read back: " + in.readInt() + ", " + in.readDouble() + ", "
                        + in.readBoolean() + ", " + in.readUTF());
            }

            // NIO: a channel moves bytes to/from a ByteBuffer
            Path g = dir.resolve("channel.txt");
            try (FileChannel ch = FileChannel.open(g, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                ByteBuffer buf = ByteBuffer.wrap("channels and buffers".getBytes(StandardCharsets.UTF_8));
                int written = ch.write(buf);
                System.out.println("channel wrote " + written + " bytes, position now " + ch.position());
            }
            try (FileChannel ch = FileChannel.open(g, StandardOpenOption.READ)) {
                ByteBuffer buf = ByteBuffer.allocate(8);                 // capacity 8
                StringBuilder sb = new StringBuilder();
                while (ch.read(buf) != -1) {                             // fill the buffer
                    buf.flip();                                          // switch to draining: limit=position, position=0
                    sb.append(StandardCharsets.UTF_8.decode(buf));
                    buf.clear();                                         // ready to fill again
                }
                System.out.println("channel read: " + sb);
            }

            // Random access: jump to a byte offset
            try (FileChannel ch = FileChannel.open(g, StandardOpenOption.READ)) {
                ByteBuffer word = ByteBuffer.allocate(3);
                ch.read(word, 13);                                       // absolute position 13
                System.out.println("bytes 13..15 = " + new String(word.array(), StandardCharsets.UTF_8));
            }
        } finally {
            Files.deleteIfExists(f); Files.deleteIfExists(dir.resolve("channel.txt")); Files.delete(dir);
        }
    }
}
