package com.geekbrains.cloud.nio;

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
import java.util.Iterator;
import java.util.Set;

public class EchoServerNio {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf;
    private Path path;

    public EchoServerNio() throws Exception {
        path = Paths.get("server","dir1").toAbsolutePath().normalize();
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
        if (reader.toString().startsWith("--help")){
            String msg = "Commands:" +System.lineSeparator()+
                    "1. ls - выводит список файлов на экран" +System.lineSeparator()+
                    "2. cd path - перемещается из текущей папки в папку из аргумента" +System.lineSeparator()+
                    "3. cat file - печатает содержание текстового файла на экран" +System.lineSeparator()+
                    "4. mkdir dir - создает папку в текущей директории" +System.lineSeparator()+
                    "5. touch file - создает пустой файл в текущей директории"+System.lineSeparator();
            channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        }
        String mainCommand = reader.toString();

        if (mainCommand.startsWith("ls")){
            StringBuilder msg = new StringBuilder();
            msg.append("Current directory: "+path+System.lineSeparator());
            Object[] fileList = Files.list(path).toArray();
            for (Object s: fileList) {
                Path m = Paths.get(s.toString());
                msg.append(m.getFileName()+System.lineSeparator());
            }

            channel.write(ByteBuffer.wrap(msg.toString().getBytes(StandardCharsets.UTF_8)));
        }

        if (mainCommand.startsWith("cd")){
            String msg ="";
            
            String commands = reader.delete(0,3).delete(reader.length()-2,reader.length()).toString();

            if (commands.equals("...")){
                path = path.getParent().normalize().toAbsolutePath();
            }
            else if(Files.isDirectory(Paths.get(commands))){
                path = Paths.get(commands).normalize().toAbsolutePath();
            }
            else if (!commands.contains("\\") &&
                    Files.isDirectory(Paths.get(path.toString(),commands))
            ){
                if (path.toString()=="\\"){
                    path = Paths.get("\\"+commands).normalize().toAbsolutePath();
                }
                else{
                    path = Paths.get(path.toString(), commands).normalize().toAbsolutePath();
                }
            }
            else {
                msg = "Path \""+commands+"\" is not directory!"+System.lineSeparator();
            }
            channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        }

        if (mainCommand.startsWith("cat")){
            String msg;
            String commands = reader.delete(0,4).delete(reader.length()-2,reader.length()).toString();
            if (Files.isDirectory(Paths.get(commands))){
                msg = "it\'s not\'t file!";
                channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
            }
            else {
                Path file = path.resolve(commands);
                byte[] bytes = Files.readAllBytes(file);
                String string = new String(bytes, StandardCharsets.UTF_8);
                String[] text = string.split("\n");
                StringBuilder finishText = new StringBuilder();
                for (String t: text) {
                    finishText.append(t+System.lineSeparator());
                }
                msg = finishText.toString();
            }
            channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        }

        if (mainCommand.startsWith("mkdir")){
            String msg;
            String commands = reader.delete(0,6).delete(reader.length()-2,reader.length()).toString();
            Path dir = Paths.get(path.toString(),commands);
            Files.createDirectories(dir);
        }
        if (mainCommand.startsWith("touch")){
            String msg;
            String commands = reader.delete(0,6).delete(reader.length()-2,reader.length()).toString();
            Path file = Paths.get(path.toString(),commands);
            Files.createFile(file);

        }
        if(mainCommand.contains("�")){
            System.out.println("putty client connect");
        }
        else if(!mainCommand.contains("�")&&
                !mainCommand.startsWith("ls")&&
                !mainCommand.startsWith("cd")&&
                !mainCommand.startsWith("cat")&&
                !mainCommand.startsWith("mkdir")&&
                !mainCommand.startsWith("touch")
        ){
            String msg = "invalid command: "+reader.toString()+System.lineSeparator();
            channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        }
        String msg = "-> ";
        channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
        String msg = "Welcome in Mike terminal"+
                System.lineSeparator()+
                System.lineSeparator()+
                "input --help to show command list"+System.lineSeparator();
        socketChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws Exception {
        new EchoServerNio();
    }

}
