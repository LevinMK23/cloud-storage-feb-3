package com.geekbrains.cloud.client;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import sun.font.FontRunIterator;

public class MainController implements Initializable {

    private static final int BUFFER_SIZE = 8192;

    public TextField clientPath;
    public TextField serverPath;
    public ListView<String> clientView;
    public ListView<String> serverView;
    private File currentDirectory;
    private File serverDirectory;


    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;

    private boolean delete = false;

    public Button buttonDuwn;
    public Button buttonUp;
    public RadioButton buttonRadio;

    private Boolean chekSelectedView(ListView<String> view) { // если файл не выделен возвращает false
        return !view.getSelectionModel().isEmpty();
    }

    private void alert(String title, String meseg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(meseg);
        alert.showAndWait();
    }

    private void buttonOnOff(Boolean bool) {
        buttonDuwn.setDisable(!bool);
        buttonUp.setDisable(!bool);
        buttonRadio.setDisable(!bool);
    }

    // Platform.runLater(() -> {})
    private void updateClientView() {
        Platform.runLater(() -> {
            clientPath.setText(currentDirectory.getAbsolutePath());
            clientView.getItems().clear();
            clientView.getItems().add("...");
            clientView.getItems()
                    .addAll(currentDirectory.list());
        });
    }

    // Platform.runLater(() -> {})
    private void updateServerView() {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            try {
                os.writeUTF("#serverDirectory_message#");
                int size = is.readInt();
                serverPath.setText(is.readUTF());
                for (int i = 0; i < size; i++) {
                    serverView.getItems().add(is.readUTF());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // download file to client
    public void download(ActionEvent actionEvent) throws IOException {
        if (chekSelectedView(serverView)) {
            buttonOnOff(false);
            String name = serverView.getSelectionModel().getSelectedItem();
            os.writeUTF("#fileDown_message#");
            os.writeBoolean(delete);
            os.writeUTF(name);
            os.flush();
            long size = is.readLong();
            File newFile = currentDirectory.toPath().resolve(name).toFile();
            try (OutputStream fos = new FileOutputStream(newFile)) {
                for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                    int readCount = is.read(buf);
                    fos.write(buf, 0, readCount);
                }
            }
            updateServerView();
            updateClientView();
            buttonOnOff(true);
            Platform.runLater(() -> {
            clientView.getSelectionModel().select(name);
            });
        } else {
            alert("Файл не выбран", "Выбери файл в списке справа!");
        }
    }

    // upload file to server
    public void upload(ActionEvent actionEvent) throws IOException {
        if (chekSelectedView(clientView)) {
            buttonOnOff(false);
            String item = clientView.getSelectionModel().getSelectedItem();
            File selected = currentDirectory.toPath().resolve(item).toFile();
            if (selected.isFile()) {
                os.writeUTF("#file_message#");
                os.writeUTF(selected.getName());
                os.writeLong(selected.length());
                try (InputStream fis = new FileInputStream(selected)) {
                    while (fis.available() > 0) {
                        int readBytes = fis.read(buf);
                        os.write(buf, 0, readBytes);
                    }
                }
                os.flush();
                deleteFile(selected, item);
            }
            updateServerView();
            updateClientView();
            buttonOnOff(true);
            Platform.runLater(() -> {
                serverView.getSelectionModel().select(item);
            });
        } else {
            alert("Файл не выбран", "Выбери файл в списке слева!");
        }
    }

    public void radioButtonDeleteFile(ActionEvent actionEvent) throws IOException {
        if (!delete) {
            delete = true;
        } else {
            delete = false;
        }
    }

    public void deleteFile(File file, String name) {
        if (delete) {
            if (file.delete()) {
                System.out.println("File: " + name + " delete");
            }
        }
    }

    private void initNetwork() {
        try {
            buf = new byte[BUFFER_SIZE];
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDirectory = new File(System.getProperty("user.home"));
        // run in FX Thread
        // :: - method reference
        updateClientView();
        initNetwork();
        updateServerView();
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    currentDirectory = currentDirectory.getParentFile();
                    updateClientView();
                } else {
                    File selected = currentDirectory.toPath().resolve(item).toFile();
                    if (selected.isDirectory()) {
                        currentDirectory = selected;
                        updateClientView();
                    }
                }
            }
        });
    }
}
