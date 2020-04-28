package ru.ntzw.com.dt.server;

import ru.ntzw.com.dt.server.model.*;
import ru.ntzw.com.dt.server.model.logging.DailyRotatingLoggingService;
import ru.ntzw.com.dt.server.model.logging.LogLevel;
import ru.ntzw.com.dt.server.model.logging.LoggingService;
import ru.ntzw.com.dt.server.model.properties.SimplePropertiesService;
import ru.ntzw.com.dt.server.model.random.RandomStringService;
import ru.ntzw.com.dt.server.model.socket.SocketService;
import ru.ntzw.com.dt.server.model.sql.SimpleSqlService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class Main {

    private static ServiceProvider serviceProvider;
    public static LoggingService log;

    public static void main(String[] args) {
        Thread.currentThread().setName("Thread-Main");
        try {
            createServices();
            log.debug("Services created");
            addShutdownHook();
            log.debug("Shutdown hook added");
            initSingleSocket(args);
            log.debug("SingleSocket initialized");
            initServices();
            log.debug("Services initialized");
            log.info("Initialization complete with no errors");
        } catch (Exception e) {
            try {
                log.error(e.getLocalizedMessage(), e);
            } catch (Exception e2) {
                System.err.println("Logging service unavailable, writing to stderr");
                e.printStackTrace();
            }
        }
    }

    private static void initSingleSocket(String[] args) throws IOException {
        int port = serviceProvider.getPropertiesService().getInteger("singleInstancePort", 49234);
        log.debug("Starting SingleSocket on port " + port);
        SingleSocket singleSocket = new SingleSocket(Main::onCommand, port);
        if(!singleSocket.isFirst()) {
            log.info("Found already running instance of program");
            if(args != null && args.length > 0 && args[0] != null) {
                log.info("Current instance is not first, sending command to exist one: " + args[0]);
                singleSocket.sendCommand(args[0]);
            }
            log.info("Exiting due to no reason to have more than one instance running at time");
            System.exit(0);
        }
    }

    private static void onCommand(String command) {
        if("stop".equalsIgnoreCase(command)) {
            log.info("Received 'stop' command from terminal, exiting");
            System.exit(0);
        }
    }

    private static SendFileResponse onSendFileRequest(SendFileRequest request) {
        return new SendFileResponse(true);
    }

    private static File getFileForSaving() {
        String basePath;
        int fileNameLength;
        synchronized (serviceProvider.getPropertiesService()) {
            basePath = serviceProvider.getPropertiesService().getString("fileSavingPath", "receivedFiles");
            fileNameLength = serviceProvider.getPropertiesService().getInteger("fileRandomNameLength", 10);
        }
        File baseDir = new File(basePath);
        if (!baseDir.isDirectory())
            throw new RuntimeException("Invalid location for file saving: " + basePath);
        Path path;
        while(Files.exists(path = Paths.get(basePath, serviceProvider.getRandomStringService().generate(fileNameLength))));
        return path.toFile();
    }

    private static boolean onFileWritten(SendFileRequest request, File file) {
        log.info(String.format("File from request %s written to %s", request.toString(), file.getAbsolutePath()));
        String tableName;
        synchronized (serviceProvider.getPropertiesService()) {
            tableName = serviceProvider.getPropertiesService().getString("sqlTableName", "received_files (fullname, email, filename)");
        }
        for(String email : request.getEmails()) {
            try {
                serviceProvider.getSqlService().insert(tableName, request.getFullname(), email, file.getName());
            } catch (SQLException e) {
                log.error("Error occurred while inserting data in table", e);
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static void createServices() {
        serviceProvider = new ServiceProvider();
        serviceProvider.setPropertiesService(new SimplePropertiesService(Paths.get("properties.cfg")));
        try {
            ((Initializable)serviceProvider.getPropertiesService()).init();
        } catch (Exception e) {
            System.err.println("Unable to initialize PropertiesService");
            e.printStackTrace();
        }
        serviceProvider.setLoggingService(new DailyRotatingLoggingService(
                serviceProvider.getPropertiesService().getString("logDirectory", "logs"),
                serviceProvider.getPropertiesService().getString("logFileNameBase", "log"),
                serviceProvider.getPropertiesService().getString("logFileNameSuffix", ".txt"),
                LogLevel.valueOf(serviceProvider.getPropertiesService().getString("logLevel", "INFO").toUpperCase()),
                serviceProvider.getPropertiesService().getInteger("logFilesToKeep", 14)
        ));
        try {
            ((Initializable)serviceProvider.getLoggingService()).init();
            Main.log = serviceProvider.getLoggingService();
        } catch (Exception e) {
            System.err.println("Unable to initialize LoggingService");
            e.printStackTrace();
        }
        serviceProvider.setSqlService(new SimpleSqlService(
                serviceProvider.getPropertiesService().getString("sqlUrl", "jdbc:mysql://localhost:3306/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"),
                serviceProvider.getPropertiesService().getString("sqlUsername", "root"),
                serviceProvider.getPropertiesService().getString("sqlPassword", "root")
        ));
        serviceProvider.setSocketService(new SocketService(
                serviceProvider.getPropertiesService().getString("socketAddress", "0.0.0.0"),
                serviceProvider.getPropertiesService().getInteger("socketPort", 37512),
                Main::onSendFileRequest,
                Main::getFileForSaving,
                Main::onFileWritten
        ));
    }

    private static void initServices() throws Exception {
        try {
            ((Initializable) serviceProvider.getSqlService()).init();
        } catch(Exception e) {
            throw new RuntimeException("Failed to initialize SqlService", e);
        }
        try {
            ((Initializable) serviceProvider.getSocketService()).init();
        } catch(Exception e) {
            throw new RuntimeException("Failed to initialize SocketService", e);
        }
        serviceProvider.setRandomStringService(new RandomStringService());
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ((Disposable) serviceProvider.getPropertiesService()).dispose();
            } catch(Exception e) {
                System.err.println("Failed to dispose PropertiesService");
                e.printStackTrace();
            }
            try {
                ((Disposable) serviceProvider.getSqlService()).dispose();
            } catch(Exception e) {
                System.err.println("Failed to dispose SqlService");
                e.printStackTrace();
            }
            try {
                ((Disposable) serviceProvider.getSocketService()).dispose();
            } catch(Exception e) {
                System.err.println("Failed to dispose SocketService");
                e.printStackTrace();
            }
            try {
                ((Disposable) serviceProvider.getLoggingService()).dispose();
            } catch(Exception e) {
                System.err.println("Failed to dispose LoggingService");
                e.printStackTrace();
            }
        }));
    }
}
