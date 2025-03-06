package org.example;

import java.util.*;

public class GameSession implements Runnable {
    private final String word; // Palabra a adivinar
    private final List<ClientHandler> players; // Lista de jugadores
    private final GameManager gameManager;
    private char[] progress; // Progreso actual de la palabra adivinada
    private final Set<Character> attemptedLetters = new HashSet<>(); // Letras ya intentadas
    private int errors = 0; // Número de errores cometidos
    private int currentTurn = 0; // Índice del jugador en turno

    public GameSession(String word, List<ClientHandler> players, GameManager gameManager) {
        this.word = word.toUpperCase();
        this.players = new ArrayList<>(players);
        this.gameManager = gameManager;
        initializeGame();
    }

    private void initializeGame() {
        // Inicializar el progreso con guiones bajos
        progress = new char[word.length()];
        Arrays.fill(progress, '_');
        attemptedLetters.clear();
        errors = 0;
        currentTurn = 0;

        // Anunciar el inicio del juego
        broadcastToPlayers("\nNUEVO JUEGO INICIADO!");
        updateGameState();
        announceTurn();
    }

    @Override
    public void run() {
        synchronized (gameManager) { // Sincronizar con el GameManager para asegurar el acceso correcto
            // Mantener el juego en ejecución hasta que se gane o se pierda
            while (errors < 7 && !isWordComplete()) {
                try {
                    gameManager.wait(100); // Espera de manera eficiente, liberando el hilo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        endGame();
    }


    public synchronized void processGuess(char letter, ClientHandler player) {
        // Verifica si es el turno del jugador
        if (players.get(currentTurn) != player) {
            player.sendMessage("No es tu turno, espera a que te toque.");
            return;
        }

        // Si el juego ya terminó, no se aceptan más intentos
        if (errors >= 7 || isWordComplete()) return;

        letter = Character.toUpperCase(letter);

        // Si la letra ya fue intentada, se informa al jugador
        if (attemptedLetters.contains(letter)) {
            player.sendMessage("Letra ya intentada: " + letter);
            return;
        }

        attemptedLetters.add(letter);
        boolean correct = false;

        // Verifica si la letra está en la palabra
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                progress[i] = letter;
                correct = true;
            }
        }

        // Si la letra no está en la palabra, se incrementa el contador de errores
        if (!correct) {
            errors++;
            broadcastToPlayers("Letra incorrecta! Errores: " + errors + "/7");
        }

        updateGameState();
        checkGameEnd();

        // Cambiar de turno si el juego no ha terminado
        if (errors < 7 && !isWordComplete()) {
            nextTurn();
        }
    }

    private void updateGameState() {
        // Enviar el estado actual del juego a todos los jugadores
        broadcastToPlayers("Palabra: " + new String(progress));
        broadcastToPlayers("Letras intentadas: " + attemptedLetters);
    }

    private void checkGameEnd() {
        // Si se completa la palabra o se alcanzan los errores máximos, termina el juego
        if (isWordComplete() || errors >= 7) {
            endGame();
        }
    }

    private void endGame() {
        // Mensaje de victoria o derrota
        if (isWordComplete()) {
            broadcastToPlayers("¡GANARON! Palabra correcta: " + word);
        } else {
            broadcastToPlayers("¡PERDIERON! La palabra era: " + word);
        }
        gameManager.endGame();
    }

    private boolean isWordComplete() {
        return new String(progress).equals(word);
    }

    private void broadcastToPlayers(String message) {
        // Enviar un mensaje a todos los jugadores
        players.forEach(p -> p.sendMessage(message));
    }

    private void announceTurn() {
        // Anunciar de quién es el turno
        broadcastToPlayers("Turno de Jugador " + (currentTurn + 1));
        players.get(currentTurn).sendMessage("Es tu turno, ingresa una letra.");
    }

    private void nextTurn() {
        // Cambia al siguiente jugador en la lista
        currentTurn = (currentTurn + 1) % players.size();
        announceTurn();
    }
}
