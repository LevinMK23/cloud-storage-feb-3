package com.geekbrains.cloud.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class CommandUtils {

    static String printHelp() {
        return "1. ls - выводит список файлов на экран\n" +
                "2. cd path - перемещается из текущей папки в папку из аргумента\n" +
                "3. cat file - печатает содержание текстового файла на экран\n" +
                "4. mkdir dir - создает папку в текущей директории\n" +
                "5. touch file - создает пустой файл в текущей директории\n";
    }

    static String getFiles(Path path) throws IOException {
        return Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.joining("\n")) + "\n\r";
    }

    static String catFile(Path path) throws IOException {
        return Files.lines(path)
                .collect(Collectors.joining("\n")) + "\n\r";
    }

}
