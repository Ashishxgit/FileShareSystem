# FileShare System 

A multi-client TCP file sharing system built from scratch in Java — no external frameworks.

## Features
- Multi-client support via ThreadPool (10 concurrent clients)
- Token Authentication
- Upload / Download / List files
- MD5 integrity verification after every transfer
- Resume interrupted transfers using byte offset
- Java Swing GUI with real-time progress bar
- CLI client support

## Project Structure
<pre>
FileShareSystem/
├── src/
│   ├── FileServer.java       → Server, manages ThreadPool
│   ├── ClientHandler.java    → Handles each client on separate thread
│   ├── FileClient.java       → CLI client
│   ├── FileShareGUI.java     → Swing GUI client
│   └── FileUtils.java        → MD5, byte transfer utilities
└── shared_files/             → Auto-created on server startup
</pre>
## How to Run

### Compile
```bash
cd src
javac -encoding UTF-8 *.java
```

### Start Server
```bash
java FileServer
```

### Start GUI Client
```bash
java FileShareGUI
```

### Start CLI Client
```bash
java FileClient
```

## Auth Token
Default: `ashish123`

## Protocol Design
UPLOAD   → UPLOAD <filename> <filesize> <offset> <md5>
DOWNLOAD → DOWNLOAD <filename> <offset>
LIST     → LIST
EXIT     → EXIT

## Tech Stack
- Java (Sockets, Multithreading, IO, Swing)
- MD5 via Java MessageDigest
- ThreadPool via ExecutorService

## Architecture
Client connects → Server accepts → ThreadPool assigns thread → ClientHandler processes commands → Files stored in shared_files/