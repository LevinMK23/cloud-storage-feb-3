package com.geekbrains.cloud.stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamApiExamples {

    public static void main(String[] args) throws IOException {

        Path text = Paths.get("server", "1.txt");

        List<String> words = Files.lines(text)
                .filter(line -> !line.isEmpty())
                .flatMap(line -> Stream.of(line.split(" +")))
                .map(String::toLowerCase)
                .map(word -> word.replaceAll("[^a-zA-Z]+", ""))
                .filter(line -> !line.isEmpty())
                .sorted()
                //.distinct() // оставить только уникальные
                .collect(Collectors.toList());

        System.out.println(words);

        Map<String, Integer> wordsMap = words.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        value -> 1,
                        Integer::sum
                ));
        System.out.println(wordsMap);

        List<WordNode> nodesList = wordsMap.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .map(entry -> new WordNode(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // nodesList.forEach(System.out::println);

        // reduce - вычисление на всем стриме
        // 1 2 3 4 5 - просуммировать
        // acc, keyMapper, valueMapper

        IntStream.range(1, 1)
                .boxed()
                .reduce(Integer::sum)
                .ifPresent(System.out::println);

        Integer sum = IntStream.rangeClosed(1, 10)
                .boxed()
                .reduce(0, Integer::sum);
        System.out.println(sum);

        Integer multiply = IntStream.rangeClosed(1, 10)
                .boxed()
                .reduce(1, (x, y) -> x * y);
        System.out.println(multiply);

        List<Integer> digits = IntStream.rangeClosed(1, 10)
                .boxed()
                .reduce(
                        new ArrayList<>(),
                        (identity, value) -> {
                            identity.add(value);
                            return identity;
                        },
                        (left, right) -> left
                );

        System.out.println(digits);
    }
}
