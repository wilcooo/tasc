package org.tagpro.tasc.extras;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tagpro.tasc.Command;
import org.tagpro.tasc.GameSubscriber;
import org.tagpro.tasc.data.GameState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MaxTime implements GameSubscriber {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Command command;
    private final int maxTime;
    private final TimeUnit timeUnit;
    private ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor();


    public MaxTime(Command command, int maxTime, TimeUnit timeUnit) {
        this.maxTime = maxTime;
        this.timeUnit = timeUnit;
        this.command = command;
    }

    @Override
    public void time(int time, GameState gameState) {
        if (gameState == GameState.ACTIVE) {
            e.schedule(() -> {
                log.info("Exit by timer:" + maxTime + " " + timeUnit);
                command.disconnect();
                System.exit(0);
            }, maxTime, timeUnit);

        }
    }
}
