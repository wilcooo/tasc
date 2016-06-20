package org.tagpro.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tagpro.tasc.Command;
import org.tagpro.tasc.ConcurrentSetKeyState;
import org.tagpro.tasc.KeyState;
import org.tagpro.tasc.data.BallUpdate;
import org.tagpro.tasc.data.Key;
import org.tagpro.tasc.data.KeyAction;
import org.tagpro.tasc.data.Tile;
import org.tagpro.tasc.extras.ServerStepEstimator;
import org.tagpro.tasc.starter.Starter;

import java.util.HashMap;
import java.util.Map;

public class Controller implements Command.KeyObserver, ServerStepEstimator.ServerStepObserver {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Command command;
    private final ServerStepEstimator serverStepEstimator;

    private final ConcurrentSetKeyState keyState = new ConcurrentSetKeyState();

    public Controller(Command command, ServerStepEstimator serverStepEstimator) {
        this.command = command;
        this.serverStepEstimator = serverStepEstimator;
    }

    public static Controller create(Starter starter) {
        final Command command = starter.getCommand();
        Controller ret = new Controller(command, ServerStepEstimator.create(starter));
        ret.serverStepEstimator.addListener(ret);
        command.addObserver(ret);
        return ret;
    }


    @Override
    public void keyChanged(Key key, KeyAction keyAction, int count) {
        log.info("Key pressed:" + key + " " + keyAction);
        keyState.setKey(key, keyAction);
    }

    public void key(Key key, KeyAction keyAction) {
        if (keyState.getStateFor(key) == keyAction) {
            log.debug("Key already at state. " + key + "=" + keyAction);
        } else {
            if (keyAction == KeyAction.KEYDOWN && keyState.getStateFor(key.getOpposite()) == KeyAction.KEYDOWN) {
                //release opposite key
                command.key(key.getOpposite(), KeyAction.KEYUP);
            }
            command.key(key, keyAction);
        }
    }

    public boolean isPushed(Key key) {
        return keyState.isPushed(key);
    }

    public KeyState getKeyState() {
        return keyState;
    }

    public void goTo(Tile tile, BallUpdate update) {
        if (tile == null) {
            stop();
            return;
        }
        goTo(tile.getX(), tile.getY(), update);
    }

    public void stop() {
        log.info("Stopping");
        for (Key key : Key.values()) {
            key(key, KeyAction.KEYUP);
        }
    }

    public void goTo(float destinationX, float destinationY, BallUpdate update) {
        Map<Key, KeyAction> keysForDirection = getKeysForDirection(destinationX, destinationY, update);
        setKeys(keysForDirection);
    }

    private void setKeys(Map<Key, KeyAction> keysForDirection) {
        for (Map.Entry<Key, KeyAction> entry : keysForDirection.entrySet()) {
            key(entry.getKey(), entry.getValue());
        }
    }

    protected Map<Key, KeyAction> getKeysForDirection(float destinationX, float destinationY, BallUpdate update) {
        Map<Key, KeyAction> ret = new HashMap<>();
        float distanceX = destinationX - update.getRx();
        float distanceY = destinationY - update.getRy();

        log.info("Setting distance X:" + distanceX + " Y:" + distanceY + " " + update);

        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            //horizontal primary
            Key primary = distanceX > 0 ? Key.RIGHT : Key.LEFT;
            ret.put(primary, KeyAction.KEYDOWN);

            //vertical secondary
            float desiredLy = (distanceY / Math.abs(distanceX)) * Math.abs(update.getLx());
            float velocityDiffY = update.getLy() - desiredLy;
            boolean shouldGoDown = distanceY > 0f;
            boolean tooFastDown = velocityDiffY >= 0f;
            boolean tooFastUp = velocityDiffY <= 0f;
            boolean tooFast = shouldGoDown ? tooFastDown : tooFastUp;

            Key secondary = shouldGoDown ? Key.DOWN : Key.UP;
            KeyAction action = tooFast ? KeyAction.KEYUP : KeyAction.KEYDOWN;
            ret.put(secondary, action);
        } else {
            //vertical primary
            Key primary = distanceY > 0 ? Key.DOWN : Key.UP;
            ret.put(primary, KeyAction.KEYDOWN);

            //horizontal secondary
            float desiredLx = (distanceX / Math.abs(distanceY)) * Math.abs(update.getLy());

            float velocityDiffX = update.getLx() - desiredLx;
            boolean shouldGoRight = distanceX > 0f;
            boolean toFastRight = velocityDiffX >= 0f;
            boolean tooFastLeft = velocityDiffX <= 0f;
            boolean tooFast = shouldGoRight ? toFastRight : tooFastLeft;

            Key secondary = shouldGoRight ? Key.RIGHT : Key.LEFT;
            KeyAction action = tooFast ? KeyAction.KEYUP : KeyAction.KEYDOWN;
            ret.put(secondary, action);
        }
        return ret;
    }

    @Override
    public void onEstimateStep(int step) {

    }
}
