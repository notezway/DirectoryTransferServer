package ru.ntzw.com.dt.server.model.socket;

import ru.ntzw.com.dt.server.Main;
import ru.ntzw.com.dt.server.model.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SocketService implements Initializable, Disposable {

    private final InetSocketAddress address;
    private ServerSocket serverSocket;
    private Function<SendFileRequest, SendFileResponse> onRequestFunction;
    private Supplier<File> fileSupplier;
    private BiFunction<SendFileRequest, File, Boolean> onFileWritten;

    public SocketService(String bindAddress, int bindPort, Function<SendFileRequest, SendFileResponse> onRequestFunction,
                         Supplier<File> fileSupplier,
                         BiFunction<SendFileRequest, File, Boolean> onFileWritten) {
        this.address = new InetSocketAddress(bindAddress, bindPort);
        this.onRequestFunction = onRequestFunction;
        this.fileSupplier = fileSupplier;
        this.onFileWritten = onFileWritten;
    }

    @Override
    public void init() throws Exception {
        serverSocket = new ServerSocket();
        Main.log.debug("ServerSocket created");
        serverSocket.bind(address);
        Main.log.debug("ServerSocket bound to " + address);
        Thread listenThread = new Thread(() -> {
            while(!serverSocket.isClosed()) {
                try {
                    Main.log.debug("Listening for new connection...");
                    Socket socket = serverSocket.accept();
                    Main.log.debug("New connection accepted: " + socket.getInetAddress());
                    Thread socketThread = new Thread(() -> {
                        try (Connection connection = new Connection(socket)) {
                            Main.log.debug("Input and Output streams opened");
                            SendFileRequest request = connection.readJson(SendFileRequest.class);
                            Main.log.info("Received SendFileRequest: " + request);
                            SendFileResponse response = onRequestFunction.apply(request);
                            Main.log.debug("Response for '" + request + "' : " + response);
                            connection.writeJson(response);
                            Main.log.info("Sent SendFileResponse: " + response);
                            if (response.isAllow()) {
                                Main.log.debug("Response allowed file sending, listening for");
                                File file = fileSupplier.get();
                                Main.log.debug("Path to saving: " + file.getAbsolutePath());
                                SendFileResult result = new SendFileResult(false, "Internal server error");
                                try {
                                    connection.readFile(file, request.getLength());
                                    Main.log.debug("File successfully ridden from socket");
                                    if(onFileWritten.apply(request, file)) {
                                        Main.log.debug("Info about file '" + file.getName() + "' written to DB");
                                        result = new SendFileResult(true, "");
                                    }
                                    Main.log.debug("Sending SendFileResult: " + result);
                                    connection.writeJson(result);
                                    Main.log.info("Communication complete, closing connection...");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Main.log.error(e.getLocalizedMessage(), e);
                                    Main.log.info("File " + file + " has been deleted: " + file.delete());
                                    Main.log.debug("Sending message about error: " + result);
                                    connection.writeJson(result);
                                }
                            } else {
                                SendFileResult result = new SendFileResult(false, "Server disallowed file transmission");
                                Main.log.debug("Sending SendFileResult: " + result);
                                connection.writeJson(result);
                                Main.log.debug("Response disallowed file sending, closing connection...");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Main.log.error(e.getLocalizedMessage(), e);
                        }
                    });
                    socketThread.setName("Thread-" + socket.getRemoteSocketAddress());
                    Main.log.debug("Starting new connection thread: " + socketThread.getName());
                    socketThread.start();
                } catch (IOException e) {
                    if (e.getClass() != SocketException.class) {
                        e.printStackTrace();
                        Main.log.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        });
        listenThread.setName("Thread-ServerSocket");
        Main.log.debug("Starting ServerSocket thread");
        listenThread.start();
    }

    @Override
    public void dispose() throws Exception {
        if(serverSocket != null)
            serverSocket.close();
    }
}
