package com.geekbrains.cloud.server;

import javafx.application.Platform;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Arrays;

public class CloudFileHandler implements Runnable {

    private static final int BUFFER_SIZE = 8192;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final byte[] buf;
    private File serverDirectory;

    private boolean delete = false;

    public CloudFileHandler(Socket socket) throws IOException {
        System.out.println("Client connected!");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[BUFFER_SIZE];
        serverDirectory = new File("server");
    }

    public void deleteFile(File file, String name) {
        if (delete) {
            if(file.delete()){
            System.out.println("File: " + name + " delete");}
        }
    }

    @Override
    public void run() {
        try {
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
                } else if ("#fileDown_message#".equals(command)) {
                    delete = is.readBoolean();
                    String name = is.readUTF();
                    File selected = serverDirectory.toPath().resolve(name).toFile();
                    if (selected.isFile()) {
                        os.writeLong(selected.length());
                        try (InputStream fis = new FileInputStream(selected)) {
                            while (fis.available() > 0) {
                                int readBytes = fis.read(buf);
                                os.write(buf, 0, readBytes);
                            }
                        }
                        os.flush();
                        deleteFile(selected, name);
                    }
                    System.out.println("File: " + name + " is download");
                } else if ("#serverDirectory_message#".equals(command)) {
                    os.writeInt(serverDirectory.list().length);
                    os.writeUTF(serverDirectory.getAbsolutePath());
                    for (int i = 0; i < serverDirectory.list().length; i++) {
                        os.writeUTF(serverDirectory.list()[i]);
                    }
                    os.flush();
                } else {
                    System.err.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
