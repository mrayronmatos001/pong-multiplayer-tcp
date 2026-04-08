    import java.io.Serializable;

    public class GameState implements Serializable {
        
        private static final long serialVersionUID = 1L;

        public int ballX;
        public int ballY;
        public int ballSpeedX;
        public int ballSpeedY;

        public int leftPaddleY;
        public int rightPaddleY;

        public int scoreLeft;
        public int scoreRight;

        public GameState() {
            ballX = 390;
            ballY = 190;
            ballSpeedX = 4;
            ballSpeedY = 3;

            leftPaddleY = 150;
            rightPaddleY = 150;

            scoreLeft = 0;
            scoreRight = 0;
        }
    }