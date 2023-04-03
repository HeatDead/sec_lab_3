package com.example.sec_lab_3;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class RsaApplication extends Application {
    private File file;
    private String fileContent;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private byte[] encryptedMessageBytes;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("RSA Encryption");
        Text fileLoad = new Text("Выберите файл: ");
        Button loadBtn = new Button("Обзор");
        Text fileLoaded = new Text("Файл не выбран!");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setMaxSize(300, 100);
        textArea.setWrapText(true);

        Button genKeyBtn = new Button("Сгенерировать ключи");
        Text keysGenerated = new Text(" - ");

        Button saveBtn = new Button("Сохранить в файлы");
        Text keysSaved = new Text(" - ");

        Button loadOpenKeyBtn = new Button("Загрузить открытый ключ");
        Text openKeyLoaded = new Text(" - ");

        Button loadCloseKeyBtn = new Button("Загрузить закрытый ключ");
        Text closeKeyLoaded = new Text(" - ");

        TextArea encryptedText = new TextArea();
        encryptedText.setEditable(false);
        encryptedText.setMaxSize(300, 180);
        encryptedText.setWrapText(true);

        TextArea decryptedText = new TextArea();
        decryptedText.setEditable(false);
        decryptedText.setMaxSize(300, 180);
        decryptedText.setWrapText(true);

        Button encryptBtn = new Button("Зашифровать");
        Button decryptBtn = new Button("Дешифровать");

        FlowPane loadPane = new FlowPane(fileLoad, loadBtn, fileLoaded);
        FlowPane genPane = new FlowPane(genKeyBtn, keysGenerated);
        FlowPane savePane = new FlowPane(saveBtn, keysSaved);
        FlowPane loadOpenPane = new FlowPane(loadOpenKeyBtn, openKeyLoaded);
        FlowPane loadClosePane = new FlowPane(loadCloseKeyBtn, closeKeyLoaded);
        FlowPane root = new FlowPane(loadPane, textArea, genPane, savePane,
                loadOpenPane, loadClosePane, encryptBtn, encryptedText, decryptBtn, decryptedText);
        root.setVgap(5);
        root.setOrientation(Orientation.VERTICAL);

        loadBtn.setOnAction(value -> {
            fileLoadBtnPressed();
            if(file != null) {
                try {
                    fileContent = new Scanner(file).useDelimiter("\\Z").next();
                    fileLoaded.setText("Файл загружен!");
                    textArea.setText(fileContent);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        genKeyBtn.setOnAction(value -> {
            KeyPairGenerator generator = null;
            try {
                generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                privateKey = pair.getPrivate();
                publicKey = pair.getPublic();
                keysGenerated.setText("Ключи сгенерированы!");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });

        saveBtn.setOnAction(value -> {
            if(privateKey == null || publicKey == null)
            {
                message("Ключи не сгенерированы!");
                return;
            }
            try {
                fileSaveBtnPressed();
                keysSaved.setText("Ключи сохранены!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        loadOpenKeyBtn.setOnAction(value -> {
            try {
                openKeyLoadBtnPressed();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            openKeyLoaded.setText("Ключ загружен!");
        });

        loadCloseKeyBtn.setOnAction(value -> {
            try {
                privateKeyLoadBtnPressed();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            closeKeyLoaded.setText("Ключ загружен!");
        });

        encryptBtn.setOnAction(value -> {
            if(publicKey == null)
            {
                message("Нет открытого ключа!");
                return;
            }
            if(fileContent == null)
            {
                message("Загрузите файл!");
                return;
            }
            Cipher encryptCipher = null;
            try {
                encryptCipher = Cipher.getInstance("RSA");
                encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
                byte[] secretMessageBytes = fileContent.getBytes(StandardCharsets.UTF_8);
                encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
                String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);

                encryptedText.setText(encodedMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        decryptBtn.setOnAction(value -> {
            if(privateKey == null)
            {
                message("Нет закрытого ключа!");
                return;
            }
            if(encryptedMessageBytes == null)
            {
                message("Сначала зашифруйте текст!");
                return;
            }
            Cipher decryptCipher = null;
            try {
                decryptCipher = Cipher.getInstance("RSA");
                decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
                String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
                decryptedText.setText(decryptedMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        stage.setScene(new Scene(root, 300, 690));
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public void fileLoadBtnPressed() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("Выберите файл (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(fileExtension);
        File file = fileChooser.showOpenDialog(new Stage());
        this.file = file;
    }

    public void fileSaveBtnPressed() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        String path;
        if (selectedDirectory != null) {
            path = selectedDirectory.getAbsolutePath();
            Files.write( Paths.get(path + "/openKey.ok"), publicKey.getEncoded());
            Files.write( Paths.get(path + "/privateKey.pk"), privateKey.getEncoded());
        }
    }

    public void openKeyLoadBtnPressed() throws Exception {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("Выберите файл (*.ok)", "*.ok");
        fileChooser.getExtensionFilters().add(fileExtension);
        File file = fileChooser.showOpenDialog(new Stage());
        byte[] publicKeyBytes = Files.readAllBytes(file.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = keyFactory.generatePublic(publicKeySpec);
    }

    public void privateKeyLoadBtnPressed() throws Exception {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileExtension = new FileChooser.ExtensionFilter("Выберите файл (*.pk)", "*.pk");
        fileChooser.getExtensionFilters().add(fileExtension);
        File file = fileChooser.showOpenDialog(new Stage());
        byte[] privateKeyBytes = Files.readAllBytes(file.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        privateKey = keyFactory.generatePrivate(privateKeySpec);
    }

    public void message(String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);

        VBox vbox = new VBox(new Text(message));
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(15));

        dialogStage.setScene(new Scene(vbox));
        dialogStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}