package org.tagpro.tasc;

import org.tagpro.tasc.box2d.Box2DClientSidePredictor;
import org.tagpro.tasc.box2d.ClientSidePredictionLogger;
import org.tagpro.tasc.data.Key;
import org.tagpro.tasc.data.KeyAction;
import org.tagpro.tasc.data.KeyChange;
import org.tagpro.tasc.extras.*;
import org.tagpro.tasc.starter.Starter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class ExampleBots {
    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        if (args.length != 1) {
            throw new IllegalArgumentException("No bot to start");
        } else if (args[0].equals("PredictionBot")) {
            startPredictionBot();
        } else if (args[0].equals("GoRightBotLeaveWhenDead")) {
            startGoRightBotLeaveWhenDead();
        }
    }

    private static void startGoRightBotLeaveWhenDead() throws InterruptedException, IOException, URISyntaxException {
        Starter s = new Starter("GoRightBotLeaveWhenDead");
        s.addListener(new ExitWhenDead());
        s.addListener(new MaxTime(7, TimeUnit.SECONDS));
        s.addListener(new CommandFix());
        s.addListener(new FixedMovement(new KeyChange(Key.RIGHT, KeyAction.KEYDOWN, 0)));
        s.start();
    }

    public static void startPredictionBot() throws IOException, URISyntaxException, InterruptedException {
        Starter s = new Starter("PredictionBot");

        ServerStepEstimator stepEstimator = new ServerStepEstimator();
        Box2DClientSidePredictor clientSidePredictor = new Box2DClientSidePredictor();
        ClientSidePredictionLogger clientSidePredictionLogger = new ClientSidePredictionLogger();

        stepEstimator.addListener(clientSidePredictor);
        clientSidePredictor.addListener(clientSidePredictionLogger);

        s.addListener(clientSidePredictor);
        s.addListener(clientSidePredictionLogger);
        s.addListener(stepEstimator);
        s.addListener(new MaxTime(4, TimeUnit.SECONDS));
        s.addListener(FixedMovement.createRightThenLeftMovement());

        s.start();
    }


}
