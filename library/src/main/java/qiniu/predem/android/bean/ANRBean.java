package qiniu.predem.android.bean;

import android.os.Looper;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Misty on 17/6/20.
 */

public class ANRBean extends Error {
    private static final long serialVersionUID = 1L;

    private ANRBean($._Thread st) {
        super("Application Not Responding", st);
    }

    public static ANRBean New(String prefix, boolean logThreadsWithoutStackTrace) {
        final Thread mainThread = Looper.getMainLooper().getThread();

        final Map<Thread, StackTraceElement[]> stackTraces = new TreeMap<Thread, StackTraceElement[]>(new Comparator<Thread>() {
            @Override
            public int compare(Thread lhs, Thread rhs) {
                if (lhs == rhs)
                    return 0;
                if (lhs == mainThread)
                    return 1;
                if (rhs == mainThread)
                    return -1;
                return rhs.getName().compareTo(lhs.getName());
            }
        });

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet())
            if (entry.getKey() == mainThread || (entry.getKey().getName().startsWith(prefix) && (logThreadsWithoutStackTrace || entry.getValue().length > 0)))
                stackTraces.put(entry.getKey(), entry.getValue());

        // Sometimes main is not returned in getAllStackTraces() - ensure that we list it
        if (!stackTraces.containsKey(mainThread)) {
            stackTraces.put(mainThread, mainThread.getStackTrace());
        }

        $._Thread tst = null;
        for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet())
            tst = new $(getThreadTitle(entry.getKey()), entry.getValue()).new _Thread(tst);

        return new ANRBean(tst);
    }

    public static ANRBean NewMainOnly() {
        final Thread mainThread = Looper.getMainLooper().getThread();
        final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();

        return new ANRBean(new $(getThreadTitle(mainThread), mainStackTrace).new _Thread(null));
    }

    private static String getThreadTitle(Thread thread) {
        return thread.getName() + " (state = " + thread.getState() + ")";
    }

    @Override
    public Throwable fillInStackTrace() {
        setStackTrace(new StackTraceElement[]{});
        return this;
    }

    private static class $ implements Serializable {
        private final String name;
        private final StackTraceElement[] stackTrace;

        private $(String name, StackTraceElement[] stackTrace) {
            this.name = name;
            this.stackTrace = stackTrace;
        }

        private class _Thread extends Throwable {
            private _Thread(_Thread other) {
                super(name, other);
            }

            @Override
            public Throwable fillInStackTrace() {
                setStackTrace(stackTrace);
                return this;
            }
        }
    }
}
