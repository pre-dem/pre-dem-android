package qiniu.predem.android.anr;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import qiniu.predem.android.bean.ANRBean;

/**
 * Created by Misty on 17/6/20.
 */

public class ANRWatchDog extends Thread {

    private static final int DEFAULT_ANR_TIMEOUT = 5000;
    private static final ANRListener DEFAULT_ANR_LISTENER = new ANRListener() {
        @Override
        public void onAppNotResponding(ANRBean error) {
            throw error;
        }
    };
    private static final InterruptionListener DEFAULT_INTERRUPTION_LISTENER = new InterruptionListener() {
        @Override
        public void onInterrupted(InterruptedException exception) {
            Log.w("ANRWatchdog", "Interrupted: " + exception.getMessage());
        }
    };
    private final Handler _uiHandler = new Handler(Looper.getMainLooper());
    private final int _timeoutInterval;
    private ANRListener _anrListener = DEFAULT_ANR_LISTENER;
    private InterruptionListener _interruptionListener = DEFAULT_INTERRUPTION_LISTENER;
    private String _namePrefix = "";
    private boolean _logThreadsWithoutStackTrace = false;
    private boolean _ignoreDebugger = false;
    private volatile int _tick = 0;
    private final Runnable _ticker = new Runnable() {
        @Override
        public void run() {
            _tick = (_tick + 1) % Integer.MAX_VALUE;
        }
    };

    /**
     * Constructs a watchdog that checks the ui thread every {@value #DEFAULT_ANR_TIMEOUT} milliseconds
     */
    public ANRWatchDog() {
        this(DEFAULT_ANR_TIMEOUT);
    }

    /**
     * Constructs a watchdog that checks the ui thread every given interval
     *
     * @param timeoutInterval The interval, in milliseconds, between to checks of the UI thread.
     *                        It is therefore the maximum time the UI may freeze before being reported as ANR.
     */
    public ANRWatchDog(int timeoutInterval) {
        super();
        _timeoutInterval = timeoutInterval;
    }

    /**
     * Sets an interface for when an ANR is detected.
     * If not set, the default behavior is to throw an error and crash the application.
     *
     * @param listener The new listener or null
     * @return itself for chaining.
     */
    public ANRWatchDog setANRListener(ANRListener listener) {
        if (listener == null) {
            _anrListener = DEFAULT_ANR_LISTENER;
        } else {
            _anrListener = listener;
        }
        return this;
    }

    /**
     * Sets an interface for when the watchdog thread is interrupted.
     * If not set, the default behavior is to just log the interruption message.
     *
     * @param listener The new listener or null.
     * @return itself for chaining.
     */
    public ANRWatchDog setInterruptionListener(InterruptionListener listener) {
        if (listener == null) {
            _interruptionListener = DEFAULT_INTERRUPTION_LISTENER;
        } else {
            _interruptionListener = listener;
        }
        return this;
    }

    /**
     * Set the prefix that a thread's name must have for the thread to be reported.
     * Note that the main thread is always reported.
     * Default "".
     *
     * @param prefix The thread name's prefix for a thread to be reported.
     * @return itself for chaining.
     */
    public ANRWatchDog setReportThreadNamePrefix(String prefix) {
        if (prefix == null)
            prefix = "";
        _namePrefix = prefix;
        return this;
    }

    /**
     * Set that only the main thread will be reported.
     *
     * @return itself for chaining.
     */
    public ANRWatchDog setReportMainThreadOnly() {
        _namePrefix = null;
        return this;
    }

    /**
     * Set that all running threads will be reported,
     * even those from which no stack trace could be extracted.
     * Default false.
     *
     * @param logThreadsWithoutStackTrace Whether or not all running threads should be reported
     * @return itself for chaining.
     */
    public ANRWatchDog setLogThreadsWithoutStackTrace(boolean logThreadsWithoutStackTrace) {
        _logThreadsWithoutStackTrace = logThreadsWithoutStackTrace;
        return this;
    }

    /**
     * Set whether to ignore the debugger when detecting ANRs.
     * When ignoring the debugger, ANRWatchdog will detect ANRs even if the debugger is connected.
     * By default, it does not, to avoid interpreting debugging pauses as ANRs.
     * Default false.
     *
     * @param ignoreDebugger Whether to ignore the debugger.
     * @return itself for chaining.
     */
    public ANRWatchDog setIgnoreDebugger(boolean ignoreDebugger) {
        _ignoreDebugger = ignoreDebugger;
        return this;
    }

    @Override
    public void run() {
        setName("|ANR-WatchDog|");

        int lastTick;
        int lastIgnored = -1;
        while (!isInterrupted()) {
            lastTick = _tick;
            _uiHandler.post(_ticker);
            try {
                Thread.sleep(_timeoutInterval);
            } catch (InterruptedException e) {
                _interruptionListener.onInterrupted(e);
                return;
            }

            // If the main thread has not handled _ticker, it is blocked. ANR.
            if (_tick == lastTick) {
                if (!_ignoreDebugger && Debug.isDebuggerConnected()) {
                    if (_tick != lastIgnored) {
                        Log.w("ANRWatchdog", "An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))");
                    }
                    lastIgnored = _tick;
                    continue;
                }

                ANRBean error;
                if (_namePrefix != null) {
                    error = ANRBean.New(_namePrefix, _logThreadsWithoutStackTrace);
                } else {
                    error = ANRBean.NewMainOnly();
                }
                _anrListener.onAppNotResponding(error);
                return;
            }
        }
    }

    public interface ANRListener {
        public void onAppNotResponding(ANRBean error);
    }

    public interface InterruptionListener {
        public void onInterrupted(InterruptedException exception);
    }
}
