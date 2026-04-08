import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class PongClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("0.tcp.sa.ngrok.io", 12944);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream());

        int playerId = (int) in.readObject();
        System.out.println("Você é o jogador: " + playerId);

        MetricsCollector metrics = new MetricsCollector();
        GamePanel        panel   = new GamePanel(new GameState());
        MetricsOverlay   overlay = new MetricsOverlay();

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(800, 400));
        panel.setBounds(0, 0, 800, 400);
        overlay.setBounds(5, 5, 205, 100);
        layered.add(panel,   JLayeredPane.DEFAULT_LAYER);
        layered.add(overlay, JLayeredPane.PALETTE_LAYER);

        JFrame frame = new JFrame("Pong TCP - Jogador " + playerId);
        frame.add(layered);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { frame.requestFocusInWindow(); }
        });

        final boolean[] upPressed   = {false};
        final boolean[] downPressed = {false};

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean changed = false;
                if (e.getKeyCode() == KeyEvent.VK_UP   && !upPressed[0])   { upPressed[0]   = true; changed = true; }
                if (e.getKeyCode() == KeyEvent.VK_DOWN && !downPressed[0]) { downPressed[0] = true; changed = true; }
                if (changed) {
                    try {
                        metrics.onKeyPressed();
                        metrics.onInputSent();
                        out.writeObject(new PlayerInput(playerId, upPressed[0], downPressed[0]));
                        out.flush();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                boolean changed = false;
                if (e.getKeyCode() == KeyEvent.VK_UP)   { upPressed[0]   = false; changed = true; }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) { downPressed[0] = false; changed = true; }
                if (changed) {
                    try {
                        metrics.onInputSent();
                        out.writeObject(new PlayerInput(playerId, upPressed[0], downPressed[0]));
                        out.flush();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        // Atualiza overlay a cada 500ms
        new Timer(500, e -> overlay.update(
            metrics.getAvgLatency(),
            metrics.getJitter(),
            metrics.getAvgInputDelay(),
            metrics.getFps()
        )).start();

        new Thread(() -> {
            try {
                while (true) {
                    GameState state = (GameState) in.readObject();
                    metrics.onStateReceived();
                    SwingUtilities.invokeLater(() -> panel.setGameState(state));
                }
            } catch (Exception e) {
                System.out.println("Desconectado do servidor");
            }
        }).start();
    }
}