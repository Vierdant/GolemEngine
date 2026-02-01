package me.arkon.golemengine.util;

import me.arkon.golemengine.action.WaitAction;
import me.arkon.golemengine.component.AnchorMonitorComponent;

public class ActionUtil {

    public static void flushWaitAction(AnchorMonitorComponent monitor) {
        if (monitor.ticksSinceLastAction > 0) {
            monitor.actions.add(new WaitAction(monitor.ticksSinceLastAction));
            monitor.ticksSinceLastAction = 0;
        }
    }

}
