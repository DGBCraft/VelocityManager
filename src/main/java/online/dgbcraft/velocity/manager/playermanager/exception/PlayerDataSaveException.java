package online.dgbcraft.velocity.manager.playermanager.exception;

import java.io.IOException;

/**
 * @author Sanluli36li
 */
public class PlayerDataSaveException extends IOException {
    public PlayerDataSaveException(IOException e) {
        super(e);
    }
}