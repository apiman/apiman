package io.apiman.gateway.platforms.servlet.components;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IPeriodicComponent;

public class PeriodicComponentImpl implements IPeriodicComponent {
    
    private Timer timer = new Timer();
    private Set<Long> idSet = new LinkedHashSet<>();
    private AtomicInteger id;
 
    public PeriodicComponentImpl() {
    }

    @Override
    public long setPeriodicTimer(long periodMillis, long initialDelayMillis,
            final IAsyncHandler<Long> periodicHandler) {
        
        AtomicInteger id = new AtomicInteger();
        
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                periodicHandler.handle(result);
            }
        };
        
        task.
        
        timer.schedule(null, initialDelayMillis, periodMillis);

    }

    @Override
    public long setOneshotTimer(long deltaMillis, IAsyncHandler<Long> timerHandler) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void cancelTimer(long timerId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelAll() {
        // TODO Auto-generated method stub
        
    }

}
