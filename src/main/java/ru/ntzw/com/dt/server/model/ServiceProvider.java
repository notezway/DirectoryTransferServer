package ru.ntzw.com.dt.server.model;

import ru.ntzw.com.dt.server.model.logging.LoggingService;
import ru.ntzw.com.dt.server.model.properties.PropertiesService;
import ru.ntzw.com.dt.server.model.random.RandomStringService;
import ru.ntzw.com.dt.server.model.socket.SocketService;
import ru.ntzw.com.dt.server.model.sql.SqlService;

public class ServiceProvider {

    private PropertiesService propertiesService;
    private SqlService sqlService;
    private SocketService socketService;
    private RandomStringService randomStringService;
    private LoggingService loggingService;

    public PropertiesService getPropertiesService() {
        return propertiesService;
    }

    public void setPropertiesService(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    public SqlService getSqlService() {
        return sqlService;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public SocketService getSocketService() {
        return socketService;
    }

    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    public RandomStringService getRandomStringService() {
        return randomStringService;
    }

    public void setRandomStringService(RandomStringService randomStringService) {
        this.randomStringService = randomStringService;
    }

    public LoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }
}
