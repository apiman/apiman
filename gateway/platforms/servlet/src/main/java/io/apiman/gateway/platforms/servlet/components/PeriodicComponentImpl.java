package io.apiman.gateway.platforms.servlet.components;

import java.util.Timer;
import java.util.TimerTask;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IPeriodicComponent;

public class PeriodicComponentImpl implements IPeriodicComponent {
    
    private Timer timer;

    public PeriodicComponentImpl() {
        timer = new Timer();
    }

    @Override
    public int setPeriodicTimer(long periodMillis, long initialDelayMillis,
            IAsyncHandler<Integer> periodicHandler) {
        
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                
            }
        };
        
        timer.schedule(null, initialDelayMillis, periodMillis);

    }

    @Override
    public int setOneshotTimer(long deltaMillis, IAsyncHandler<Integer> timerHandler) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void cancelTimer(int timerId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelAll() {
        // TODO Auto-generated method stub
        
    }

}
