import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private final int WIDTH = 800;
    private final int HEIGHT = 400;
    private final int PADDLE_WIDTH = 15;
    private final int PADDLE_HEIGHT = 100;
    private final int BALL_SIZE = 20;

    private GameState gameState;

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == null) return;

        g.setColor(Color.WHITE);

        g.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);

        g.fillRect(30, gameState.leftPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(WIDTH - 45, gameState.rightPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

        g.fillOval(gameState.ballX, gameState.ballY, BALL_SIZE, BALL_SIZE);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString(String.valueOf(gameState.scoreLeft), WIDTH / 2 - 60, 40);
        g.drawString(String.valueOf(gameState.scoreRight), WIDTH / 2 + 40, 40);
    }
}