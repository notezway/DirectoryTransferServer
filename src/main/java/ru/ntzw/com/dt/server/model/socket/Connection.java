package ru.ntzw.com.dt.server.model.socket;

import com.google.gson.Gson;
import ru.ntzw.com.dt.server.GsonProvider;

import java.io.*;
import java.net.Socket;

public class Connection implements AutoCloseable {

    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Gson gson;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        gson = GsonProvider.get();
    }

    public void writeJson(Object object) throws IOException {
        String s = gson.toJson(object);
        outputStream.writeUTF(s);
        outputStream.flush();
    }

    public <T> T readJson(Class<T> type) throws IOException {
        String s = inputStream.readUTF();
        return gson.fromJson(s, type);
    }

    public void readFile(File file, long length) throws IOException {
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            for(long i = 0; i < length; i++) {
                bos.write(inputStream.read());
            }
            bos.flush();
        }
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws Exception {
        flush();
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}
