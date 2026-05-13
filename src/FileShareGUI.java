// FileShareGUI.java
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class FileShareGUI extends JFrame {

    // ── Connection ─────────────────────────────────────────────
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private InputStream rawIn;
    private OutputStream rawOut;
    private boolean connected = false;

    // ── UI Components ──────────────────────────────────────────
    private JTextField tokenField;
    private JButton connectBtn;
    private JTextArea logArea;
    private JButton uploadBtn;
    private JButton downloadBtn;
    private JButton listBtn;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private DefaultListModel<String> fileListModel;
    private JList<String> fileList;

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public FileShareGUI() {
        setTitle("FileShare v2.0");
        setSize(750, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));

        buildTopPanel();
        buildCenterPanel();
        buildBottomPanel();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── TOP PANEL — connection ─────────────────────────────────
    private void buildTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setBackground(new Color(40, 40, 40));
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
            new Color(70, 70, 70)));

        JLabel title = new JLabel("⚡ FileShare");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(100, 200, 255));

        JLabel tokenLabel = new JLabel("Token:");
        tokenLabel.setForeground(Color.LIGHT_GRAY);
        tokenLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tokenField = new JTextField("ashish123", 12);
        tokenField.setBackground(new Color(55, 55, 55));
        tokenField.setForeground(Color.WHITE);
        tokenField.setCaretColor(Color.WHITE);
        tokenField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tokenField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        connectBtn = makeButton("Connect", new Color(50, 150, 255));
        connectBtn.addActionListener(e -> toggleConnection());

        statusLabel = new JLabel("● Disconnected");
        statusLabel.setForeground(new Color(255, 80, 80));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        top.add(title);
        top.add(Box.createHorizontalStrut(20));
        top.add(tokenLabel);
        top.add(tokenField);
        top.add(connectBtn);
        top.add(Box.createHorizontalStrut(10));
        top.add(statusLabel);

        add(top, BorderLayout.NORTH);
    }

    // ── CENTER PANEL — file list + log ─────────────────────────
    private void buildCenterPanel() {
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setBackground(new Color(30, 30, 30));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // left — server file list
        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setBackground(new Color(30, 30, 30));

        JLabel filesLabel = new JLabel("  Server Files");
        filesLabel.setForeground(new Color(180, 180, 180));
        filesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filesLabel.setOpaque(true);
        filesLabel.setBackground(new Color(45, 45, 45));
        filesLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setBackground(new Color(38, 38, 38));
        fileList.setForeground(new Color(200, 230, 200));
        fileList.setFont(new Font("Consolas", Font.PLAIN, 13));
        fileList.setSelectionBackground(new Color(60, 100, 160));
        fileList.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setBorder(BorderFactory.createLineBorder(
            new Color(60, 60, 60)));
        fileScroll.getViewport().setBackground(new Color(38, 38, 38));

        leftPanel.add(filesLabel, BorderLayout.NORTH);
        leftPanel.add(fileScroll, BorderLayout.CENTER);

        // right — activity log
        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setBackground(new Color(30, 30, 30));

        JLabel logLabel = new JLabel("  Activity Log");
        logLabel.setForeground(new Color(180, 180, 180));
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logLabel.setOpaque(true);
        logLabel.setBackground(new Color(45, 45, 45));
        logLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(180, 230, 180));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(
            new Color(60, 60, 60)));
        logScroll.getViewport().setBackground(new Color(20, 20, 20));

        rightPanel.add(logLabel, BorderLayout.NORTH);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        center.add(leftPanel);
        center.add(rightPanel);
        add(center, BorderLayout.CENTER);
    }

    // ── BOTTOM PANEL — actions + progress ─────────────────────
    private void buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(10, 5));
        bottom.setBackground(new Color(40, 40, 40));
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(new Color(40, 40, 40));

        uploadBtn   = makeButton("⬆ Upload File",   new Color(50, 180, 100));
        downloadBtn = makeButton("⬇ Download",       new Color(50, 150, 255));
        listBtn     = makeButton("↺ Refresh List",   new Color(150, 100, 220));

        uploadBtn.addActionListener(e -> uploadFile());
        downloadBtn.addActionListener(e -> downloadFile());
        listBtn.addActionListener(e -> listFiles());

        // disable until connected
        uploadBtn.setEnabled(false);
        downloadBtn.setEnabled(false);
        listBtn.setEnabled(false);

        btnRow.add(uploadBtn);
        btnRow.add(downloadBtn);
        btnRow.add(listBtn);

        // progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Idle");
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setForeground(new Color(50, 200, 120));
        progressBar.setBorderPainted(false);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        progressBar.setPreferredSize(new Dimension(0, 22));

        bottom.add(btnRow, BorderLayout.CENTER);
        bottom.add(progressBar, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    // ── CONNECT / DISCONNECT ───────────────────────────────────
    private void toggleConnection() {
        if (!connected) {
            try {
                socket  = new Socket(HOST, PORT);
                in      = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                out     = new PrintWriter(socket.getOutputStream(), true);
                rawIn   = socket.getInputStream();
                rawOut  = socket.getOutputStream();

                // authenticate
                out.println("AUTH " + tokenField.getText().trim());
                String response = in.readLine();

                if (!"AUTH_SUCCESS".equals(response)) {
                    log("[ERROR] Authentication failed.");
                    socket.close();
                    return;
                }

                connected = true;
                statusLabel.setText("● Connected");
                statusLabel.setForeground(new Color(80, 220, 80));
                connectBtn.setText("Disconnect");
                tokenField.setEnabled(false);
                uploadBtn.setEnabled(true);
                downloadBtn.setEnabled(true);
                listBtn.setEnabled(true);

                log("[+] Connected & authenticated.");
                listFiles(); // auto-refresh on connect

            } catch (IOException e) {
                log("[ERROR] Cannot connect: " + e.getMessage());
            }
        } else {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (out != null) out.println("EXIT");
            if (socket != null) socket.close();
        } catch (IOException ignored) {}

        connected = false;
        statusLabel.setText("● Disconnected");
        statusLabel.setForeground(new Color(255, 80, 80));
        connectBtn.setText("Connect");
        tokenField.setEnabled(true);
        uploadBtn.setEnabled(false);
        downloadBtn.setEnabled(false);
        listBtn.setEnabled(false);
        log("[-] Disconnected.");
    }

    // ── LIST FILES ─────────────────────────────────────────────
    private void listFiles() {
        new Thread(() -> {
            try {
                out.println("LIST");
                fileListModel.clear();
                String line;
                while (!(line = in.readLine()).equals("END")) {
                    final String entry = line;
                    SwingUtilities.invokeLater(
                        () -> fileListModel.addElement(entry));
                }
                log("[+] File list refreshed.");
            } catch (IOException e) {
                log("[ERROR] List failed: " + e.getMessage());
            }
        }).start();
    }

    // ── UPLOAD FILE ────────────────────────────────────────────
    private void uploadFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        new Thread(() -> {
            try {
                String md5 = FileUtils.computeMD5(file);
                log("[i] Uploading: " + file.getName()
                    + " (" + file.length() + " bytes)");
                log("[i] MD5: " + md5);

                out.println("UPLOAD " + file.getName()
                    + " " + file.length() + " 0 " + md5);

                if (!"READY".equals(in.readLine())) {
                    log("[ERROR] Server not ready."); return;
                }

                // transfer with progress
                transferWithProgress(
                    new FileInputStream(file), rawOut, file.length());

                String result = in.readLine();
                log(result.equals("SUCCESS")
                    ? "[+] Upload complete. MD5 verified ✓"
                    : "[ERROR] " + result);

                SwingUtilities.invokeLater(this::listFiles);

            } catch (IOException e) {
                log("[ERROR] Upload failed: " + e.getMessage());
            }
        }).start();
    }

    // ── DOWNLOAD FILE ──────────────────────────────────────────
    private void downloadFile() {
        String selected = fileList.getSelectedValue();
        if (selected == null) {
            log("[!] Select a file from the list first.");
            return;
        }

        // extract filename from "filename.txt (123 bytes)"
        String filename = selected.split(" ")[0];

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(filename));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File saveFile = chooser.getSelectedFile();
        new Thread(() -> {
            try {
                long offset = saveFile.exists() ? saveFile.length() : 0;
                if (offset > 0) log("[~] Resuming from byte: " + offset);

                out.println("DOWNLOAD " + filename + " " + offset);
                String response = in.readLine();

                if (response.startsWith("ERROR")) {
                    log("[ERROR] " + response); return;
                }

                String[] parts  = response.split(" ");
                long fileSize   = Long.parseLong(parts[1]);
                String serverMD5 = parts[2];

                log("[i] Downloading: " + filename
                    + " (" + fileSize + " bytes)");

                try (FileOutputStream fos =
                        new FileOutputStream(saveFile, offset > 0)) {
                    transferWithProgress(rawIn, fos, fileSize - offset);
                }

                String localMD5 = FileUtils.computeMD5(saveFile);
                if (localMD5.equals(serverMD5)) {
                    log("[+] Download complete. MD5 verified ✓");
                    log("[+] Saved: " + saveFile.getAbsolutePath());
                } else {
                    log("[!] MD5 mismatch — file may be corrupted.");
                }

            } catch (IOException e) {
                log("[ERROR] Download failed: " + e.getMessage());
            }
        }).start();
    }

    // ── TRANSFER WITH PROGRESS BAR ─────────────────────────────
    private void transferWithProgress(InputStream in,
                                       OutputStream out,
                                       long totalSize) throws IOException {
        byte[] buffer = new byte[4096];
        long transferred = 0;
        int bytesRead;

        SwingUtilities.invokeLater(() -> progressBar.setValue(0));

        while (transferred < totalSize &&
               (bytesRead = in.read(buffer, 0,
                   (int) Math.min(buffer.length,
                                  totalSize - transferred))) != -1) {
            out.write(buffer, 0, bytesRead);
            transferred += bytesRead;

            final int percent = (int) ((transferred * 100) / totalSize);
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(percent);
                progressBar.setString(percent + "%");
            });
        }
        out.flush();
        SwingUtilities.invokeLater(() -> progressBar.setString("Done"));
    }

    // ── HELPERS ────────────────────────────────────────────────
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileShareGUI::new);
    }
}