// FileUtils.java
import java.io.*;
import java.security.*;

public class FileUtils {

    // Sends bytes from InputStream to OutputStream
    public static void transferBytes(InputStream in,
                                     OutputStream out,
                                     long fileSize) throws IOException {
        byte[] buffer = new byte[4096];
        long remaining = fileSize;
        int bytesRead;

        while (remaining > 0 &&
               (bytesRead = in.read(buffer, 0,
                           (int) Math.min(buffer.length, remaining))) != -1) {
            out.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
        out.flush();
    }

    // Prints progress to console
    public static void printProgress(long transferred, long total) {
        int percent = (int) ((transferred * 100) / total);
        System.out.print("\rProgress: " + percent + "% ["
                + "=".repeat(percent / 2)
                + " ".repeat(50 - percent / 2) + "]");
        if (percent == 100) System.out.println();
    }

    // Computes MD5 hash of a file — returns hex string
    public static String computeMD5(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            // convert byte array to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5 algorithm not found", e);
        }
    }

    // Skips N bytes from InputStream (for resume)
    public static void skipBytes(InputStream in, long offset) throws IOException {
        long skipped = 0;
        byte[] buffer = new byte[4096];
        while (skipped < offset) {
            int toRead = (int) Math.min(buffer.length, offset - skipped);
            int bytesRead = in.read(buffer, 0, toRead);
            if (bytesRead == -1) break;
            skipped += bytesRead;
        }
    }
}