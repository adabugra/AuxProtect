package dev.heliosares.auxprotect.exceptions;

import dev.heliosares.auxprotect.core.Language.L;

public class PlatformException extends CommandException {
    private static final long serialVersionUID = 259350605635468408L;

    public PlatformException() {
        super(L.UNKNOWN_SUBCOMMAND);
    }
}