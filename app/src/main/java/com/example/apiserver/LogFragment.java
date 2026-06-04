package com.example.apiserver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LogFragment extends Fragment {
    public static final String ARG_LOG_TYPE = "log_type";
    public static final int TYPE_STARTUP = 0;
    public static final int TYPE_ERRORS = 1;
    public static final int TYPE_API_CALLS = 2;

    private int logType;
    private TextView tvLogContent;

    public static LogFragment newInstance(int logType) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LOG_TYPE, logType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            logType = getArguments().getInt(ARG_LOG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        tvLogContent = view.findViewById(R.id.tvLogContent);
        updateLogContent();
        return view;
    }

    public void updateLogContent() {
        if (tvLogContent == null) return;
        
        LogManager logManager = LogManager.getInstance();
        String content;
        
        switch (logType) {
            case TYPE_STARTUP:
                content = logManager.getStartupLogsString();
                break;
            case TYPE_ERRORS:
                content = logManager.getErrorLogsString();
                break;
            case TYPE_API_CALLS:
                content = logManager.getApiCallLogsString();
                break;
            default:
                content = "Unknown log type";
        }
        
        if (content.isEmpty()) {
            content = "暂无日志记录";
        }
        
        tvLogContent.setText(content);
    }
}
