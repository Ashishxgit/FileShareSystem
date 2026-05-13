// FileServer.java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.time.*;
import java.time.format.*;

public class FileServer {

    private static final int PORT = 5000;
    private static final String SHARED_FOLDER = "../shared_files";

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        new File(SHARED_FOLDER).mkdirs();

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║     File Share Server v2.0       ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.println("  Port       : " + PORT);
        System.out.println("  Shared Dir : " + SHARED_FOLDER);
        System.out.println("  Max Clients: 10");
        System.out.println("  Auth Token : ashish123");
        System.out.println("  Started at : " + LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("──────────────────────────────────");
        System.out.println("Waiting for clients...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[" + LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    + "] New connection from: "
                    + clientSocket.getInetAddress());

                threadPool.execute(
                    new ClientHandler(clientSocket, SHARED_FOLDER)
                );
            }

        } catch (IOException e) {
            System.out.println("[ERROR] Server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}