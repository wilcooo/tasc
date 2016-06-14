package org.tagpro.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tagpro.tasc.GameSubscriber;
import org.tagpro.tasc.box2d.TagProWorld;
import org.tagpro.tasc.data.*;
import org.tagpro.tasc.extras.ServerStepEstimator;

import java.util.Map;

public class Precision implements GameSubscriber, ServerStepEstimator.ServerStepObserver {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Controller controller;
    private final ServerStepEstimator stepEstimator;

    private int id;
    private volatile BallUpdate lastUpdate;
    private volatile int lastUpdateStep;

    private int c = 0;

    public Precision(Controller controller, ServerStepEstimator stepEstimator) {
        this.controller = controller;
        this.stepEstimator = stepEstimator;
    }

    @Override
    public void onEstimateStep(int step) {
        if (lastUpdate == null) {
            log.warn("Lastupdate is null");
            return;
        }
        int diffSteps = step - lastUpdateStep;
        float verticalAc = getVerticalAc();
        float currentVelocity = TagProWorld.calculateSpeed(lastUpdate.getLx(), verticalAc, diffSteps);
        float currentPosition = lastUpdate.getRx() + TagProWorld.calculatePosition(lastUpdate.getLx(), verticalAc, diffSteps);


        int stepsUntilStandStill = TagProWorld.calculateStepsUntilStandStill(currentVelocity, verticalAc);
        float positionIfReverse = currentPosition + TagProWorld.calculatePosition(currentVelocity, -verticalAc, stepsUntilStandStill);

//        log.info("Ac:" + verticalAc + " LastV:" + lastUpdate.getLx() + " CurrentV:" + currentVelocity + " SUSS:" + stepsUntilStandStill);
        log.info("Ac:" + verticalAc + " CurrentP:" + currentPosition + " SUSS:" + stepsUntilStandStill + " positionIfReverse:" + positionIfReverse + " c" + c++);
        if (lastUpdate.getLx() < -1.4f && positionIfReverse < 0.33f && controller.isPushed(Key.LEFT)) {
            controller.key(Key.RIGHT, KeyAction.KEYDOWN);
            c = 0;
        } else if (lastUpdate.getLx() > 1.4f && positionIfReverse > 5 && controller.isPushed(Key.RIGHT)) {
            controller.key(Key.LEFT, KeyAction.KEYDOWN);
            c = 0;
        }
    }

    private float getVerticalAc() {
        if (controller.isPushed(Key.LEFT)) {
            return -0.025f;
        } else if (controller.isPushed(Key.RIGHT)) {
            return 0.025f;
        } else {
            return 0;
        }
    }

    @Override
    public void onUpdate(int step, Map<Integer, Update> updates) {
        Update self = updates.get(id);
        if (self != null && self.getBallUpdate() != null) {
            this.lastUpdate = self.getBallUpdate();
            this.lastUpdateStep = step;
            log.info("Update rx:" + lastUpdate.getRx() + " step:" + step);
        }
    }

    @Override
    public void time(int time, GameState gameState) {
        if (gameState == GameState.ACTIVE) {
            controller.key(Key.LEFT, KeyAction.KEYDOWN);
            stepEstimator.addListener(this);
        }
    }

    @Override
    public void onId(int id) {
        this.id = id;
    }

}
