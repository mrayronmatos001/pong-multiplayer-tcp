import javax.swing.*;
import java.awt.*;

public class MetricsOverlay extends JPanel {

    private static final long serialVersionUID = 1L;

    private double avgLatency;
    private double jitter;
    private double inputDelay;
    private int fps;

    public MetricsOverlay() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 100));
    }

    public void update(double avgLatency, double jitter, double inputDelay, int fps) {
        this.avgLatency = avgLatency;
        this.jitter     = jitter;
        this.inputDelay = inputDelay;
        this.fps        = fps;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(5, 5, 195, 95, 10, 10);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString(String.format("Latência:    %.1f ms", avgLatency), 12, 25);
        g.drawString(String.format("Jitter:      %.1f ms", jitter),     12, 43);
        g.drawString(String.format("Input delay: %.1f ms", inputDelay), 12, 61);
        g.drawString(String.format("FPS:         %d",      fps),        12, 79);
    }
}