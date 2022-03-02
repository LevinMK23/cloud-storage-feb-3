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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static com.geekbrains.cloud.nio.CommandUtils.*;

public class EchoServerNio {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf;
    private Path currentDir;

    public EchoServerNio() throws Exception {
        buf = ByteBuffer.allocate(50);
        currentDir = Paths.get("server").normalize().toAbsolutePath();
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

        System.out.println("Received: " + reader.toString());
        String msg = reader.toString().trim();
        if (msg.startsWith("--help")) {
            channel.write(ByteBuffer.wrap(printHelp().getBytes(StandardCharsets.UTF_8)));
        }
        if (msg.startsWith("ls")) {
            channel.write(ByteBuffer.wrap(getFiles(currentDir).getBytes(StandardCharsets.UTF_8)));
        }
        if (msg.startsWith("cd")) {
            String destination = msg.substring(3);
            if ("..".equals(destination)) {
                currentDir = currentDir.getParent().normalize().toAbsolutePath();
            } else if (currentDir.equals(Paths.get(destination).normalize().toAbsolutePath().getParent())) {
                currentDir = Paths.get(destination).normalize().toAbsolutePath();
            }
        }
        if (msg.startsWith("cat")) {
            String fileName = msg.substring(4);
            channel.write(ByteBuffer.wrap(catFile(Paths.get(fileName)).getBytes(StandardCharsets.UTF_8)));
        }
        if (msg.startsWith("mkdir")) {
            String newDir = msg.substring(6);
            if (!Files.exists(Paths.get(newDir))) {
                Files.createDirectory(Paths.get(currentDir.toString(), newDir));
            }
        }
        if (msg.startsWith("touch")) {
            String newFile = msg.substring(6);
            Files.createFile(Paths.get(newFile));
        }

        channel.write(ByteBuffer.wrap(("..\\" + currentDir.getFileName() + "-> ").getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
        socketChannel.write(ByteBuffer.wrap("Welcome in Mike terminal\nPlease input --help to print command list\n-> ".getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws Exception {
        new EchoServerNio();
    }

}