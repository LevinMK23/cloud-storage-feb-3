package com.geekbrains.cloud.server;

import java.io.*;
import java.net.Socket;

public class CloudFileHandler implements Runnable {

    private static final int BUFFER_SIZE = 8192;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final byte[] buf;
    private File serverDirectory;

    public CloudFileHandler(Socket socket) throws IOException {
        System.out.println("Client connected!");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[BUFFER_SIZE];
        serverDirectory = new File("server");
    }

    @Override
    public void run() {
        try {
                sendListServerFiles();
            while (true) {
                String command = is.readUTF();
                if ("#file_message#".equals(command)) {
                    String name = is.readUTF();
                    long size = is.readLong();
                    File newFile = serverDirectory.toPath()
                            .resolve(name)
                            .toFile();
                    try (OutputStream fos = new FileOutputStream(newFile)) {
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int readCount = is.read(buf);
                            fos.write(buf, 0, readCount);
                        }
                    }
                    System.out.println("File: " + name + " is uploaded");
                    sendListServerFiles();
                }
                if ("downloadToClient".equals(command)){
                    String item = is.readUTF();
                    File fileToClient = serverDirectory.toPath().resolve(item).toFile();
                    os.writeLong(fileToClient.length());
                    try (InputStream fis = new FileInputStream(fileToClient)) {
                        while (fis.available() > 0) {
                            int readBytes = fis.read(buf);
                            os.write(buf, 0, readBytes);
                        }
                    }
                    os.flush();
                }
                    else {
                    System.err.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendListServerFiles() throws IOException {
        String[] listServerFiles = serverDirectory.list();
        os.writeInt(listServerFiles.length);
        for (int i = 0; i < listServerFiles.length; i++) {
            os.writeUTF(listServerFiles[i]);
        }
    }

}
