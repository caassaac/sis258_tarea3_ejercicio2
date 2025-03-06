package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteAhorcado {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Dirección del servidor
        int port = 5555; // Puerto del servidor

        try (
                Socket socket = new Socket(serverAddress, port); // Crea un socket para conectarse al servidor en la dirección y puerto especificados
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Flujo de entrada para recibir mensajes del servidor
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Flujo de salida para enviar mensajes al servidor
                Scanner scanner = new Scanner(System.in) // Scanner para leer la entrada del usuario desde la consola
        ) {


            System.out.println("Conectado al servidor! Escribe 'salir' para salir");

            // Hilo para escuchar mensajes del servidor
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado del servidor");
                }
            }).start();

            // Captura la entrada del usuario y la envía al servidor
            while (true) {
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("salir")) {
                    out.println("salir");
                    break;
                }
                if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
                    out.println(input);
                }
            }
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}
