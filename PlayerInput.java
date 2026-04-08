import java.io.Serializable;

public class PlayerInput implements Serializable {

    private static final long serialVersionUID = 1L;

    public int playerId;
    public boolean up;
    public boolean down;

    public PlayerInput(int playerId, boolean up, boolean down) {
        this.playerId = playerId;
        this.up = up;
        this.down = down;
    }
}
