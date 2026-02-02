package me.arkon.golemengine.util;

import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.action.GolemAction;
import me.arkon.golemengine.action.WaitAction;
import me.arkon.golemengine.component.AnchorMonitorComponent;
import me.arkon.golemengine.component.GolemActionComponent;

import java.util.ArrayList;

public class ActionUtil {

    public static void flushWaitAction(AnchorMonitorComponent monitor) {
        int ticks = monitor.ticksSinceLastAction;
        if (ticks <= 0) return;

        ArrayList<GolemAction> actions = monitor.actions;

        if (!actions.isEmpty()) {
            GolemAction last = actions.getLast();
            if (last instanceof WaitAction(int waitTicks)) {
                WaitAction updatedWait = new WaitAction(waitTicks + ticks);
                actions.set(actions.lastIndexOf(last), updatedWait);
                monitor.ticksSinceLastAction = 0;
                return;
            }
        }

        actions.add(new WaitAction(ticks));
        monitor.ticksSinceLastAction = 0;
    }
}
