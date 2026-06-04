package com.example.apiserver;

import android.content.Context;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.util.Map;

public class ApiServer extends NanoHTTPD {
    private static final int PORT = 8080;
    private Context context;
    private LogManager logManager;

    public ApiServer(Context context) throws IOException {
        super(PORT);
        this.context = context;
        this.logManager = LogManager.getInstance();
    }

    @Override
    public Response serve(IHTTPSession session) {
        long startTime = System.currentTimeMillis();
        
        String method = session.getMethod().name();
        String uri = session.getUri();
        String remoteAddr = session.getRemoteIpAddress();
        
        logManager.addStartupLog("Received request: " + method + " " + uri + " from " + remoteAddr);
        
        String responseBody;
        int statusCode;
        String contentType = "application/json";
        
        try {
            if (!"GET".equals(method)) {
                responseBody = "{\"error\": \"Only GET requests are supported\", \"status\": \"error\"}";
                statusCode = 405;
                logManager.addErrorLog("Unsupported method: " + method);
            } else {
                Map<String, String> params = session.getParms();
                
                switch (uri) {
                    case "/api/v1/server/info":
                        responseBody = "{\"service\": \"Android Local API Server\", \"version\": \"1.0.0\", \"port\": " + PORT + ", \"status\": \"running\", \"status\": \"success\"}";
                        statusCode = 200;
                        break;
                    case "/api/v1/system/time":
                        responseBody = "{\"timestamp\": " + System.currentTimeMillis() + ", \"datetime\": \"" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()) + "\", \"status\": \"success\"}";
                        statusCode = 200;
                        break;
                    case "/api/v1/system/echo":
                        String echoMsg = params.getOrDefault("message", "No message provided");
                        responseBody = echoMsg;
                        statusCode = 200;
                        contentType = "text/plain";
                        break;
                    case "/api/v1/server/status":
                        responseBody = "{\"server\": \"active\", \"requests_handled\": 0, \"uptime\": \"N/A\", \"status\": \"success\"}";
                        statusCode = 200;
                        break;
                    case "/api/v1/tools/random":
                        int randomNumber = (int) (Math.random() * 1000);
                        responseBody = "{\"random\": " + randomNumber + ", \"min\": 0, \"max\": 999, \"status\": \"success\"}";
                        statusCode = 200;
                        break;
                    case "/api/v1/tools/ping":
                        responseBody = "{\"response\": \"pong\", \"latency_ms\": " + (System.currentTimeMillis() - startTime) + ", \"status\": \"success\"}";
                        statusCode = 200;
                        break;
                    default:
                        responseBody = "{\"error\": \"Endpoint not found\", \"available_endpoints\": [\"/api/v1/server/info\", \"/api/v1/system/time\", \"/api/v1/system/echo?message=xxx\", \"/api/v1/server/status\", \"/api/v1/tools/random\", \"/api/v1/tools/ping\"], \"status\": \"error\"}";
                        statusCode = 404;
                }
            }
        } catch (Exception e) {
            responseBody = "{\"error\": \"" + e.getMessage() + "\", \"status\": \"error\"}";
            statusCode = 500;
            logManager.addErrorLog("API Error: " + e.getMessage());
        }
        
        long duration = System.currentTimeMillis() - startTime;
        logManager.addApiCallLog(method, uri, remoteAddr, statusCode, duration, responseBody);
        
        return Response.newFixedLengthResponse(Status.lookup(statusCode), contentType, responseBody);
    }

    public void startServer() {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logManager.addStartupLog("API Server started on port " + PORT);
        } catch (IOException e) {
            logManager.addErrorLog("Failed to start server: " + e.getMessage());
            throw new RuntimeException("Failed to start server", e);
        }
    }

    public void stopServer() {
        stop();
        logManager.addStartupLog("API Server stopped");
    }

    public boolean isRunning() {
        return super.isAlive();
    }

    public int getPort() {
        return PORT;
    }
}
