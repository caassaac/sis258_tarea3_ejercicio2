package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket; // Socket que representa la conexión del cliente con el servidor
    private final GameManager gameManager; // Referencia al GameManager para interactuar con la partida
    private PrintWriter out; // Flujo de salida para enviar mensajes al cliente

    // Constructor de la clase ClientHandler
    public ClientHandler(Socket socket, GameManager gameManager) {
        this.socket = socket; // Asigna el socket del cliente
        this.gameManager = gameManager; // Asigna la referencia al gestor del juego
    }


    @Override
    public void run() {
        try {
            // Configuración de los flujos de entrada y salida
            out = new PrintWriter(socket.getOutputStream(), true);
            // Flujo de entrada para recibir mensajes del cliente
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Mensaje de bienvenida para el jugador
            out.println("Bienvenido al Ahorcado Multiplayer!");

            String input;
            while ((input = in.readLine()) != null) {
                // Mostrar en el servidor lo que escribe el jugador
                System.out.println("Jugador " + socket.getInetAddress() + ": " + input);

                // Si el jugador escribe "salir", se desconecta
                if (input.equalsIgnoreCase("salir")) {
                    break;
                }

                // Si el jugador introduce una letra, se procesa el intento en la partida actual
                if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
                    GameSession currentGame = gameManager.getCurrentGame();
                    if (currentGame != null) {
                        currentGame.processGuess(input.charAt(0), this);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error con cliente: " + socket.getInetAddress());
        } finally {
            // El jugador se desconecta y se elimina de la lista de jugadores
            gameManager.removePlayer(this);
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error cerrando socket");
            }
        }
    }

    // Envía un mensaje al cliente
    public void sendMessage(String message) {
        out.println(message);
    }
}
