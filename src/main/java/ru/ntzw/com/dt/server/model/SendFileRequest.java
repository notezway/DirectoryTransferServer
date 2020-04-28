package ru.ntzw.com.dt.server.model;

import java.util.List;

public class SendFileRequest {

    private long length;
    private String fullname;
    private List<String> emails;

    public SendFileRequest(long length, String fullname, List<String> emails) {
        this.length = length;
        this.fullname = fullname;
        this.emails = emails;
    }

    public long getLength() {
        return length;
    }

    public String getFullname() {
        return fullname;
    }

    public List<String> getEmails() {
        return emails;
    }

    @Override
    public String toString() {
        return "SendFileRequest{" +
                "length=" + length +
                ", fullname='" + fullname + '\'' +
                ", emails=" + emails +
                '}';
    }
}
