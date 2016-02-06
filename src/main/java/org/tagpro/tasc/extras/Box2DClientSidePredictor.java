package org.tagpro.tasc.extras;

import org.tagpro.tasc.GameSubscriber;
import org.tagpro.tasc.TagProWorld;
import org.tagpro.tasc.data.*;

import java.util.Map;

public class Box2DClientSidePredictor implements ClientSidePredictor, GameSubscriber {

    private final TagProWorld world = new TagProWorld(1);
    private int id;

    @Override
    public PlayerState predict(int step) {
        synchronized (this) {
            world.proceedToStep(step);
            return world.getPlayer(1).getPlayerState();
        }
    }

    @Override
    public void keyPressed(Key key, KeyAction keyAction, int count) {
        world.getPlayer(1).setKey(key, keyAction);
    }

    @Override
    public void onUpdate(int step, Map<Integer, Update> updates) {
        Update update = updates.get(id);
        if (update == null) {
            //Not self player
            return;
        }

        final BallUpdate ballUpdate = update.getBallUpdate();
        if (ballUpdate != null) {
            synchronized (this) {
                world.getPlayer(1).setBodyPositionAndVelocity(ballUpdate);
                world.setStep(step);
            }
        }
    }

    @Override
    public void onId(int id) {
        this.id = id;
    }
}
