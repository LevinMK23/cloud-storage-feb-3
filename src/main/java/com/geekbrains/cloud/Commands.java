package com.geekbrains.cloud;

public enum Commands {

    SERVER_FILES("#file_list#");

    private String command;

    Commands(String command){
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
