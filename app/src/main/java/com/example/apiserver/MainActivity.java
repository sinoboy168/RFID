package com.example.apiserver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ApiServer apiServer;
    private TextView tvServerStatus;
    private TextView tvServerPort;
    private ViewPager viewPager;
    private LogFragment startupFragment;
    private LogFragment errorFragment;
    private LogFragment apiCallFragment;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvServerStatus = findViewById(R.id.tvServerStatus);
        tvServerPort = findViewById(R.id.tvServerPort);
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        handler = new Handler(Looper.getMainLooper());

        initViewPager();
        tabLayout.setupWithViewPager(viewPager);

        LogManager logManager = LogManager.getInstance();
        logManager.addStartupLog("Application started");
        logManager.addStartupLog("Initializing components...");

        startApiServer();
    }

    private void initViewPager() {
        startupFragment = LogFragment.newInstance(LogFragment.TYPE_STARTUP);
        errorFragment = LogFragment.newInstance(LogFragment.TYPE_ERRORS);
        apiCallFragment = LogFragment.newInstance(LogFragment.TYPE_API_CALLS);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(startupFragment);
        fragments.add(errorFragment);
        fragments.add(apiCallFragment);

        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.tab_startup));
        titles.add(getString(R.string.tab_errors));
        titles.add(getString(R.string.tab_api_calls));

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, titles);
        viewPager.setAdapter(adapter);
    }

    private void startApiServer() {
        new Thread(() -> {
            try {
                apiServer = new ApiServer(getApplicationContext());
                apiServer.startServer();
                
                handler.post(() -> {
                    tvServerStatus.setText("服务状态: 运行中");
                    tvServerPort.setText("端口: " + apiServer.getPort());
                    LogManager.getInstance().addStartupLog("API Server is running on port " + apiServer.getPort());
                });

                updateLogsPeriodically();
            } catch (IOException e) {
                handler.post(() -> {
                    tvServerStatus.setText("服务状态: 启动失败");
                    LogManager.getInstance().addErrorLog("Failed to start API server: " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateLogsPeriodically() {
        new Thread(() -> {
            while (apiServer != null && apiServer.isRunning()) {
                try {
                    Thread.sleep(1000);
                    handler.post(() -> {
                        if (startupFragment != null) startupFragment.updateLogContent();
                        if (errorFragment != null) errorFragment.updateLogContent();
                        if (apiCallFragment != null) apiCallFragment.updateLogContent();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiServer != null && apiServer.isRunning()) {
            apiServer.stopServer();
        }
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private List<String> titles;

        public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
