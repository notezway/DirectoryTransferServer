package ru.ntzw.com.dt.server.model;

public class SendFileResponse {

    private boolean allow;

    public SendFileResponse(boolean allow) {
        this.allow = allow;
    }

    public boolean isAllow() {
        return allow;
    }

    @Override
    public String toString() {
        return "SendFileResponse{" +
                "allow=" + allow +
                '}';
    }
}
