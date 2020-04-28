package ru.ntzw.com.dt.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

class SingleSocket {

    private final Consumer<String> commandConsumer;

    private final InetSocketAddress address;
    private boolean isFirst = true;

    SingleSocket(Consumer<String> commandConsumer, int port) throws IOException {
        this.commandConsumer = commandConsumer;
        this.address = new InetSocketAddress("127.0.0.1", port);
        ServerSocket serverSocket = new ServerSocket();
        try {
            serverSocket.bind(address);
            listenForOtherInstances(serverSocket);
        } catch(IOException e) {
            //сокет скорее всего уже занят
            isFirst = false;
        }
    }

    private void listenForOtherInstances(ServerSocket serverSocket) {
        Thread listenThread = new Thread(() -> {
            while(serverSocket.isBound() && !serverSocket.isClosed()) {
                try(Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String command = reader.readLine();
                    synchronized(commandConsumer) {
                        commandConsumer.accept(command);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listenThread.setDaemon(true);
        listenThread.start();
    }

    boolean isFirst() {
        return isFirst;
    }

    void sendCommand(String path) {
        try(Socket socket = new Socket(address.getAddress(), address.getPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            writer.write(path);
            writer.newLine();
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
