package com.geekbrains.cloud.stream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Main {

    private static void printResult(int a, int b, Func foo) {
        System.out.println(foo.apply(a, b));
    }

    public static void main(String[] args) {
        Func foo = new Func() {
            @Override
            public int apply(int a, int b) {
                return a * b;
            }
        };

        System.out.println(foo.getClass());
        Func lam = (x, y) -> x * y; // lambda expression
        System.out.println(lam.getClass());

        System.out.println(lam.apply(1, 2));

        Func ref = Integer::sum; // method reference

        Processor processor = Main::printResult;
        processor.process(5, 5, ref);

        // Consumer, Predicate, Function, Supplier
        // forEach(void), peek(Stream)
        Consumer<String> printer = System.out::println;
        printer.accept("Hello world!");

        // filter(Stream),
        // dropWile(Stream),
        // allMatch(boolean), anyMatch(boolean), noneMatch(boolean)
        Predicate<String> predicate = s -> s.length() > 3;
        System.out.println(predicate.test("He"));

        // map(T) -> U Stream
        // flatMap(T) -> U Stream

        // Stream<T>
        // map(t -> Stream.of(t.toList()) -> Stream<Stream<T>>
        // flatMap(t -> Stream.of(t.toList()) -> Stream<T>

        Function<String, Integer> length = String::length;
        Integer len = length.apply("123");
        System.out.println(len);

        // collect
        Supplier<Integer> getter = () -> 1;
    }
}
