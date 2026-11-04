package com.atm;

import com.atm.config.DatabaseConfig;
import com.atm.ui.Async;
import com.atm.ui.LoginFrame;
import com.atm.ui.Theme;

import javax.swing.SwingUtilities;

/** Application entry point. */
public final class Main {

    private Main() { }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame(null);
            login.setVisible(true);

            // Friendly early warning if the database is not reachable.
            Async.run(login, () -> DatabaseConfig.query(con -> Boolean.TRUE), ok -> { });
        });
    }
}
