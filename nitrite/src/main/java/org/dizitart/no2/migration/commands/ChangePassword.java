package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.util.SecureString;

/**
 * @author Anindya Chatterjee
 */
public class ChangePassword implements Command {
    private String userName;
    private SecureString oldPassword;
    private SecureString newPassword;

    public ChangePassword(String userName, SecureString oldPassword, SecureString newPassword) {

    }

    @Override
    public void execute(Nitrite nitrite) {

    }
}
