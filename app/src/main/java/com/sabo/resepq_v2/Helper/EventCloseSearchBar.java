package com.sabo.resepq_v2.Helper;

public class EventCloseSearchBar {
    private boolean closed;

    public EventCloseSearchBar(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
