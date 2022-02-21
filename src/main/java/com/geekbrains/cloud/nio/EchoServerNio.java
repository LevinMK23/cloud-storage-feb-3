package com.geekbrains.cloud.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class EchoServerNio {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf;

    public EchoServerNio() throws Exception {
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

        String msg = "From server: " + reader.toString();
        System.out.println("Received: " + msg);
        channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
    }

    public static void main(String[] args) throws Exception {
        new EchoServerNio();
    }

}
