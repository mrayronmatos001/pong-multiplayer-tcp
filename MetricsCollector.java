import java.util.ArrayDeque;
import java.util.Deque;

public class MetricsCollector {

    private static final int WINDOW = 60; // últimos 60 frames

    // Latência
    private long inputSentAt = -1;
    private final Deque<Double> latencySamples = new ArrayDeque<>();

    // Jitter
    private long lastStateArrival = -1;
    private final Deque<Double> intervalSamples = new ArrayDeque<>();

    // Input delay
    private long keyPressedAt = -1;
    private final Deque<Double> inputDelaySamples = new ArrayDeque<>();

    // FPS
    private int frameCount = 0;
    private long fpsWindowStart = System.currentTimeMillis();
    private int currentFps = 0;

    /** Chame quando enviar um input ao servidor */
    public void onInputSent() {
        inputSentAt = System.currentTimeMillis();
    }

    /** Chame quando o usuário pressionar uma tecla */
    public void onKeyPressed() {
        keyPressedAt = System.currentTimeMillis();
    }

    /** Chame quando um GameState chegar do servidor */
    public void onStateReceived() {
        long now = System.currentTimeMillis();

        // Latência: tempo desde o envio do input até o próximo estado
        if (inputSentAt > 0) {
            addSample(latencySamples, now - inputSentAt);
            inputSentAt = -1;
        }

        // Jitter: variação entre chegadas consecutivas
        if (lastStateArrival > 0) {
            addSample(intervalSamples, now - lastStateArrival);
        }
        lastStateArrival = now;

        // Input delay: tempo desde o keyPress até o estado refletir
        if (keyPressedAt > 0) {
            addSample(inputDelaySamples, now - keyPressedAt);
            keyPressedAt = -1;
        }

        // FPS
        frameCount++;
        if (now - fpsWindowStart >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            fpsWindowStart = now;
        }
    }

    public double getAvgLatency() {
        return average(latencySamples);
    }

    public double getJitter() {
        // Jitter = variação média entre intervalos consecutivos
        if (intervalSamples.size() < 2) return 0;
        double avg = average(intervalSamples);
        double sumDiff = 0;
        for (double v : intervalSamples) sumDiff += Math.abs(v - avg);
        return sumDiff / intervalSamples.size();
    }

    public double getAvgInputDelay() {
        return average(inputDelaySamples);
    }

    public int getFps() {
        return currentFps;
    }

    private void addSample(Deque<Double> deque, double value) {
        if (deque.size() >= WINDOW) deque.pollFirst();
        deque.addLast(value);
    }

    private double average(Deque<Double> deque) {
        if (deque.isEmpty()) return 0;
        double sum = 0;
        for (double v : deque) sum += v;
        return sum / deque.size();
    }
}