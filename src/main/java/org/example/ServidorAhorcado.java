package org.example;

import java.io.IOException;

public class ServidorAhorcado {
    public static void main(String[] args) throws IOException {
        // Crea una instancia del GameManager y la inicia
        GameManager gameManager = new GameManager();
        gameManager.start();
    }
}
