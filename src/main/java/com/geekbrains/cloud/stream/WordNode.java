package com.geekbrains.cloud.stream;

import lombok.Data;

@Data
public class WordNode {

    private String word;
    private int count;

    public WordNode(String word, int count) {
        this.word = word;
        this.count = count;
    }
}
