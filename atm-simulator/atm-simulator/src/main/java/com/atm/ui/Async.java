package com.atm.ui;

import com.atm.exception.ATMException;

import javax.swing.AbstractButton;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Runs database work off the Swing event thread and reports errors in a dialog. */
public final class Async {

    private Async() { }

    public static <T> void run(Component parent, Callable<T> work, Consumer<T> onSuccess) {
        run(parent, null, work, onSuccess, null);
    }

    public static <T> void run(Component parent, AbstractButton button, Callable<T> work,
                               Consumer<T> onSuccess, Runnable onFailure) {
        if (button != null) {
            button.setEnabled(false);
        }
        if (parent != null) {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                if (button != null) {
                    button.setEnabled(true);
                }
                if (parent != null) {
                    parent.setCursor(Cursor.getDefaultCursor());
                }
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (!(cause instanceof ATMException)) {
                        cause.printStackTrace();
                    }
                    Theme.showError(parent, cause instanceof ATMException
                            ? cause.getMessage()
                            : "Unexpected error: " + cause.getMessage());
                    if (onFailure != null) {
                        onFailure.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
