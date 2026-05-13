// ClientHandler.java
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private String sharedFolder;

    // valid auth token — in production this would come from a database
    private static final String AUTH_TOKEN = "ashish123";
    private boolean authenticated = false;

    public ClientHandler(Socket socket, String sharedFolder) {
        this.clientSocket = socket;
        this.sharedFolder = sharedFolder;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);
            InputStream rawIn   = clientSocket.getInputStream();
            OutputStream rawOut = clientSocket.getOutputStream();
        ) {
            System.out.println("[+] Client connected: "
                + clientSocket.getInetAddress());

            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("[CMD] " + command);

                if (command.startsWith("AUTH")) {
                    handleAuth(command, out);

                } else if (!authenticated) {
                    out.println("ERROR Not authenticated. Send: AUTH <token>");

                } else if (command.startsWith("UPLOAD")) {
                    handleUpload(command, in, out, rawIn);

                } else if (command.startsWith("DOWNLOAD")) {
                    handleDownload(command, out, rawOut);

                } else if (command.equals("LIST")) {
                    handleList(out);

                } else if (command.equals("EXIT")) {
                    System.out.println("[-] Client disconnected.");
                    break;

                } else {
                    out.println("ERROR Unknown command");
                }
            }

        } catch (IOException e) {
            System.out.println("[ERROR] ClientHandler: " + e.getMessage());
        }
    }

    // ─── AUTH ─────────────────────────────────────────────────
    private void handleAuth(String command, PrintWriter out) {
        // command format: AUTH <token>
        String[] parts = command.split(" ");
        if (parts.length < 2) {
            out.println("ERROR Invalid AUTH format");
            return;
        }
        if (parts[1].equals(AUTH_TOKEN)) {
            authenticated = true;
            out.println("AUTH_SUCCESS");
            System.out.println("[+] Client authenticated.");
        } else {
            out.println("AUTH_FAIL");
            System.out.println("[-] Auth failed — wrong token.");
        }
    }

    // ─── UPLOAD ───────────────────────────────────────────────
    private void handleUpload(String command, BufferedReader in,
                               PrintWriter out, InputStream rawIn)
                               throws IOException {
        // command format: UPLOAD <filename> <filesize> <offset> <md5>
        String[] parts = command.split(" ");
        String filename = parts[1];
        long fileSize   = Long.parseLong(parts[2]);
        long offset     = Long.parseLong(parts[3]);
        String clientMD5 = parts[4];

        out.println("READY");

        File destFile = new File(sharedFolder + "/" + filename);

        // append mode if offset > 0 (resuming), else fresh write
        try (FileOutputStream fos = new FileOutputStream(destFile, offset > 0)) {
            FileUtils.transferBytes(rawIn, fos, fileSize - offset);
        }

        // verify integrity
        String serverMD5 = FileUtils.computeMD5(destFile);
        if (serverMD5.equals(clientMD5)) {
            System.out.println("[+] Uploaded: " + filename + " | MD5 OK");
            out.println("SUCCESS");
        } else {
            System.out.println("[!] MD5 mismatch: " + filename);
            out.println("ERROR MD5 mismatch — file corrupted");
            destFile.delete(); // discard corrupted file
        }
    }

    // ─── DOWNLOAD ─────────────────────────────────────────────
    private void handleDownload(String command, PrintWriter out,
                                 OutputStream rawOut) throws IOException {
        // command format: DOWNLOAD <filename> <offset>
        String[] parts  = command.split(" ");
        String filename = parts[1];
        long offset     = Long.parseLong(parts[2]);

        File file = new File(sharedFolder + "/" + filename);
        if (!file.exists()) {
            out.println("ERROR File not found");
            return;
        }

        // send filesize + MD5 so client can verify after receiving
        String md5 = FileUtils.computeMD5(file);
        out.println("FILESIZE " + file.length() + " " + md5);

        try (FileInputStream fis = new FileInputStream(file)) {
            // skip already-downloaded bytes
            if (offset > 0) FileUtils.skipBytes(fis, offset);
            FileUtils.transferBytes(fis, rawOut, file.length() - offset);
        }

        System.out.println("[+] Downloaded: " + filename + " | MD5: " + md5);
    }

    // ─── LIST ─────────────────────────────────────────────────
    private void handleList(PrintWriter out) {
        File folder = new File(sharedFolder);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            out.println("(no files)");
        } else {
            for (File f : files) {
                out.println(f.getName() + " (" + f.length() + " bytes)");
            }
        }
        out.println("END");
    }
}