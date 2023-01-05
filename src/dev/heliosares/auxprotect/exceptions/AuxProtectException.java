package dev.heliosares.auxprotect.exceptions;

import dev.heliosares.auxprotect.core.Language.L;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuxProtectException extends Exception {
    private static final long serialVersionUID = 8845413629243613163L;
    private final L l;
    private final List<Object> format;
    public AuxProtectException(L l, Object... format) {
        super(l.translate(format));
        this.l = l;
        if (format == null) {
            this.format = null;
        } else {
            this.format = Collections.unmodifiableList(Arrays.asList(format));
        }
    }

    public L getLang() {
        return l;
    }

    public List<Object> getFormat() {
        return format;
    }
}