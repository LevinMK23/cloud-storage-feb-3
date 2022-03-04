package com.geekbrains.cloud.nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class EchoServerNio {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf;
    private Path currentDirectory;

    public EchoServerNio() throws Exception {
        currentDirectory = Paths.get("server");
        buf = ByteBuffer.allocate(5);
        serverSocketChannel = ServerSocketChannel.open();
        selector = Selector.open();

        serverSocketChannel.bind(new InetSocketAddress(8189));
        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocketChannel.isOpen()) {

            selector.select();

            Set<SelectionKey> keys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey currentKey = iterator.next();
                if (currentKey.isAcceptable()) {
                    handleAccept();
                }
                if (currentKey.isReadable()) {
                    handleRead(currentKey);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey currentKey) throws IOException {

        SocketChannel channel = (SocketChannel) currentKey.channel();

        StringBuilder reader = new StringBuilder();

        while (true) {
            int count = channel.read(buf);

            if (count == 0) {
                break;
            }

            if (count == -1) {
                channel.close();
                return;
            }

            buf.flip();

            while (buf.hasRemaining()) {
                reader.append((char) buf.get());
            }

            buf.clear();
        }

        String msg = reader.toString().trim();
        //added
        if ("ls".equals(msg)) {
            channel.write(ByteBuffer.wrap(getDirFiles(currentDirectory).getBytes(StandardCharsets.UTF_8)));
            printArrow(channel);
        } else if (msg.startsWith("cd ")) {
            Path newPath = Paths.get(msg.substring(3, msg.length()));
            if (Files.exists(newPath)) {
                currentDirectory = newPath;
                System.out.println("Current directory changed!");
                channel.write(ByteBuffer.wrap("Current directory changed!\n\r".getBytes(StandardCharsets.UTF_8)));
                printArrow(channel);
            }
        } else if(msg.startsWith("cat ") && msg.endsWith(".txt")){
            if (Files.exists(Paths.get(currentDirectory.toString() + "\\" + msg.substring(4, msg.length())))) {
                Path fileToOpen = currentDirectory.resolve(msg.substring(4, msg.length()));
                byte[] bytes = Files.readAllBytes(fileToOpen);
                channel.write(ByteBuffer.wrap(bytes));
                channel.write(ByteBuffer.wrap("\n\r".getBytes(StandardCharsets.UTF_8)));
                printArrow(channel);
            } else {
                channel.write(ByteBuffer.wrap("No such file in current directory!!!\n\r".getBytes(StandardCharsets.UTF_8)));
                printArrow(channel);
            }
        } else if(msg.startsWith("mkdir ") && (msg.length() > 6)) {
            Files.createDirectory(Paths.get(currentDirectory + "\\" + msg.substring(6, msg.length())));
            printArrow(channel);
        } else if(msg.startsWith("touch ") && (msg.length() > 6)) {
            Files.createFile(Paths.get(currentDirectory + "\\" + msg.substring(6, msg.length())));
            printArrow(channel);
        }
        else {
            printArrow(channel);
        }
        System.out.println("Received: " + msg);
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
        //added
        socketChannel.write(ByteBuffer.wrap("Welcome to terminal\n\r".getBytes(StandardCharsets.UTF_8)));
        printArrow(socketChannel);
    }

    private String getDirFiles(Path path) throws IOException {
        return Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.joining("\n\r")) + "\n\r";
    }

    private void printArrow(SocketChannel socketChannel) throws IOException {
        socketChannel.write(ByteBuffer.wrap("-> ".getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws Exception {
        new EchoServerNio();
    }

}
