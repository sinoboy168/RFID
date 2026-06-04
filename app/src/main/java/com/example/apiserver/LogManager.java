package com.example.apiserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogManager {
    private static LogManager instance;
    private List<String> startupLogs;
    private List<String> errorLogs;
    private List<String> apiCallLogs;

    private LogManager() {
        startupLogs = new ArrayList<>();
        errorLogs = new ArrayList<>();
        apiCallLogs = new ArrayList<>();
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public void addStartupLog(String message) {
        String log = getTimestamp() + " - " + message;
        startupLogs.add(log);
    }

    public void addErrorLog(String message) {
        String log = getTimestamp() + " - ERROR: " + message;
        errorLogs.add(log);
    }

    public void addApiCallLog(String method, String uri, String remoteAddr, int statusCode, long duration, String responseBody) {
        String log = getTimestamp() + " - " + method + " " + uri + " from " + remoteAddr + " -> " + statusCode + " (" + duration + "ms)\n  Response: " + responseBody;
        apiCallLogs.add(log);
    }

    public List<String> getStartupLogs() {
        return new ArrayList<>(startupLogs);
    }

    public List<String> getErrorLogs() {
        return new ArrayList<>(errorLogs);
    }

    public List<String> getApiCallLogs() {
        return new ArrayList<>(apiCallLogs);
    }

    public String getStartupLogsString() {
        return String.join("\n", startupLogs);
    }

    public String getErrorLogsString() {
        return String.join("\n", errorLogs);
    }

    public String getApiCallLogsString() {
        return String.join("\n", apiCallLogs);
    }

    public void clearAllLogs() {
        startupLogs.clear();
        errorLogs.clear();
        apiCallLogs.clear();
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
