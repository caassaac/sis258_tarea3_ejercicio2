package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameManager {
    private static final int PORT = 5555; // Puerto en el que el servidor escuchará conexiones
    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>());
    private String currentWord; // Palabra actual que se usará en la partida
    private boolean gameInProgress = false; // Indica si hay una partida en curso
    private ServerSocket serverSocket;
    private GameSession activeGame; // Sesión de juego actualmente activa

    // Mét.odo para iniciar el servidor
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en el puerto " + PORT);

        // Inicia un hilo para aceptar nuevas conexiones de clientes
        new Thread(this::acceptPlayers).start();

        // Inicia el mét.odo que solicita al administrador una nueva palabra cuando sea posible
        promptForNewWord();
    }

    // Mét.odo que solicita al administrador ingresar una palabra para iniciar la partida
    private void promptForNewWord() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Se pide la palabra solo si no hay una partida en curso y hay al menos un jugador conectado
                if (!gameInProgress && !players.isEmpty()) {
                    System.out.print("\n[ADMIN] Ingrese nueva palabra: ");

                    if (!scanner.hasNextLine()) { // Verifica si hay entrada disponible
                        break; // Sale del bucle si no hay entrada
                    }

                    String newWord = scanner.nextLine().trim().toUpperCase();

                    if (!newWord.isEmpty()) {
                        currentWord = newWord;
                        System.out.println("Palabra establecida: " + currentWord);
                        startNewGame();
                    }
                }
            } catch (NoSuchElementException | IllegalStateException e) {
                System.out.println("Error al leer entrada del administrador. Terminando...");
                break; // Sale del bucle si ocurre un error
            }
        }
    }

    // Mét.odo que acepta conexiones entrantes de clientes
    private void acceptPlayers() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                synchronized (players) {
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    players.add(handler);
                    new Thread(handler).start();
                    System.out.println("Nuevo jugador conectado desde "
                            + clientSocket.getInetAddress() + ". Total conectados: " + players.size());
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.out.println("Error al aceptar jugador: " + e.getMessage());
                }
            }
        }
    }

    // Mét.odo para iniciar una nueva partida utilizando la palabra actual y la lista de jugadores
    private void startNewGame() {
        if (players.isEmpty() || currentWord == null) return;

        gameInProgress = true;
        activeGame = new GameSession(currentWord, players, this);
        new Thread(activeGame).start();
    }

    // Mét.odo llamado cuando la partida ha finalizado
    public void endGame() {
        gameInProgress = false;
        currentWord = null;
        broadcastToAll("\nJUEGO TERMINADO! Esperando nueva palabra...");
    }

    // Envía un mensaje a todos los jugadores conectados
    public void broadcastToAll(String message) {
        synchronized (players) {
            players.forEach(p -> p.sendMessage(message));
        }
    }

    // Remueve a un jugador de la lista cuando se desconecta
    public void removePlayer(ClientHandler player) {
        synchronized (players) {
            players.remove(player);
            System.out.println("Jugador desconectado. Restantes: " + players.size());
        }
    }

    // Retorna la sesión de juego actual
    public GameSession getCurrentGame() {
        return activeGame;
    }
}
