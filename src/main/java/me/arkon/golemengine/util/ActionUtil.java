package me.arkon.golemengine.util;

import me.arkon.golemengine.action.GolemAction;
import me.arkon.golemengine.action.WaitAction;
import me.arkon.golemengine.component.PlayerMonitorComponent;

import java.util.ArrayList;

public class ActionUtil {

    public static void flushWaitAction(PlayerMonitorComponent monitor) {
        int ticks = monitor.ticksSinceLastAction;
        if (ticks <= 0) return;

        ArrayList<GolemAction> actions = monitor.actions;

        if (!actions.isEmpty()) {
            GolemAction last = actions.getLast();
            if (last instanceof WaitAction waitAction) {
                WaitAction updatedWait = new WaitAction(waitAction.getTicks() + ticks);
                actions.set(actions.lastIndexOf(last), updatedWait);
                monitor.ticksSinceLastAction = 0;
                return;
            }
        }

        actions.add(new WaitAction(ticks));
        monitor.ticksSinceLastAction = 0;
    }
}
