import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Rectangle;

public class PongServer {
    private static final int PORT = 5000;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int PADDLE_HEIGHT = 100;
    private static final int BALL_SIZE = 20;
    private static final int PADDLE_SPEED = 6;

    private GameState gameState = new GameState();
    private final List<ObjectOutputStream> clients = new ArrayList<>();

    private volatile boolean player1Up = false;
    private volatile boolean player1Down = false;
    private volatile boolean player2Up = false;
    private volatile boolean player2Down = false;

    public static void main(String[] args) throws Exception {
        new PongServer().start();
    }

    public void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor rodando na porta " + PORT);

        new Thread(this::gameLoop).start();

        int playerId = 1;

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado: " + socket.getInetAddress());

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            synchronized (clients) {
                clients.add(out);
            }

            int assignedPlayerId = playerId;
            playerId++;
            if (playerId > 2) playerId = 1;

            out.writeObject(assignedPlayerId);
            out.flush();

            new Thread(() -> handleClient(in, socket, assignedPlayerId)).start();
        }
    }

    private void handleClient(ObjectInputStream in, Socket socket, int playerId) {
        try {
            while (true) {
                PlayerInput input = (PlayerInput) in.readObject();

                if (playerId == 1) {
                    player1Up = input.up;
                    player1Down = input.down;
                } else if (playerId == 2) {
                    player2Up = input.up;
                    player2Down = input.down;
                }
            }
        } catch (Exception e) {

            if (playerId == 1) {
                player1Up = false;
                player1Down = false;
            } else if (playerId == 2) {
                player2Up = false;
                player2Down = false;
            }

            try {
                socket.close();
                System.out.println("Cliente desconectado: " + socket.getInetAddress());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void gameLoop() {
        while (true) {
            updateGame();
            sendStateToClients();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        if (player1Up && gameState.leftPaddleY > 0) {
            gameState.leftPaddleY -= PADDLE_SPEED;
        }
        if (player1Down && gameState.leftPaddleY + PADDLE_HEIGHT < HEIGHT) {
            gameState.leftPaddleY += PADDLE_SPEED;
        }

        if (player2Up && gameState.rightPaddleY > 0) {
            gameState.rightPaddleY -= PADDLE_SPEED;
        }
        if (player2Down && gameState.rightPaddleY + PADDLE_HEIGHT < HEIGHT) {
            gameState.rightPaddleY += PADDLE_SPEED;
        }

        gameState.ballX += gameState.ballSpeedX;
        gameState.ballY += gameState.ballSpeedY;

        if (gameState.ballY <= 0 || gameState.ballY + BALL_SIZE >= HEIGHT) {
            gameState.ballSpeedY = -gameState.ballSpeedY;
        }

        Rectangle leftPaddle = new Rectangle(30, gameState.leftPaddleY, 15, PADDLE_HEIGHT);
        Rectangle rightPaddle = new Rectangle(WIDTH - 45, gameState.rightPaddleY, 15, PADDLE_HEIGHT);
        Rectangle ballRect = new Rectangle(gameState.ballX, gameState.ballY, BALL_SIZE, BALL_SIZE);

        if (ballRect.intersects(leftPaddle)) {
            gameState.ballSpeedX = Math.abs(gameState.ballSpeedX);
        }

        if (ballRect.intersects(rightPaddle)) {
            gameState.ballSpeedX = -Math.abs(gameState.ballSpeedX);
        }

        if (gameState.ballX < 0) {
            gameState.scoreRight++;
            resetBall();
        }

        if (gameState.ballX > WIDTH) {
            gameState.scoreLeft++;
            resetBall();
        }
    }

    private void resetBall() {
        gameState.ballX = WIDTH / 2 - BALL_SIZE / 2;
        gameState.ballY = HEIGHT / 2 - BALL_SIZE / 2;
        gameState.ballSpeedX = (Math.random() < 0.5) ? 4 : -4;
        gameState.ballSpeedY = (Math.random() < 0.5) ? 3 : -3;
    }

    private void sendStateToClients() {
        synchronized (clients) {
            Iterator<ObjectOutputStream> iterator = clients.iterator();

            while (iterator.hasNext()) {
                ObjectOutputStream out = iterator.next();
                try {
                    out.reset();
                    out.writeObject(gameState);
                    out.flush();
                } catch (IOException e) {
                    iterator.remove();
                }
            }
        }
    }
}