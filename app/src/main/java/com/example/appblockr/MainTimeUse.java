package com.example.appblockr;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appblockr.R;
import com.example.appblockr.testTimeUse.App;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class MainTimeUse extends AppCompatActivity {

    Button enableBtn, showBtn, backTime, nextTime;
    TextView permissionDescriptionTv, usageTv;
    ListView appsList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.time_use_app);
        enableBtn = findViewById(R.id.enable_btn);
        showBtn =  findViewById(R.id.show_btn);
        permissionDescriptionTv =findViewById(R.id.permission_description_tv);
        usageTv =  findViewById(R.id.usage_tv);
        appsList =  findViewById(R.id.apps_list);

        backTime = findViewById(R.id.BackTime);
        nextTime = findViewById(R.id.NextTime);

        this.loadStatistics();

        backTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainTimeUse.this, ShowAllApps.class);
                MainTimeUse.this.startActivity(myIntent);
            }
        });

        nextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainTimeUse.this, About.class);
                MainTimeUse.this.startActivity(myIntent);
            }
        });

//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.nav_locked_apps:
//                        startActivity(new Intent(getApplicationContext(),
//                                MainActivity.class));
//                        overridePendingTransition(0, 0);
//                        return true;
//                    case R.id.nav_all_apps:
//                        startActivity(new Intent(getApplicationContext(),
//                                ShowAllApps.class));
//                        overridePendingTransition(0, 0);
//                        return true;
//                    case R.id.nav_settings:
//                     startActivity(new Intent(getApplicationContext(),
//                                About.class));
//                        overridePendingTransition(0,0);
//                        return true;
//                    case R.id.nav_times:
//                        return true;
//                }
//                return false;
//            }
//        });
    }


    // m???i khi ???ng d???ng v??o foreground -> getGrantStatus v?? hi???n th??? c??c n??t t????ng ???ng
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        if (getGrantStatus()) {
            showHideWithPermission();
            showBtn.setOnClickListener(view -> loadStatistics()); //sau khi ???n btnEnable th?? hi???n ra btnShow
        } else {
            showHideNoPermission();
            enableBtn.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        }
    }


    /**
     * t???i s??? li???u th???ng k?? s??? d???ng trong 24 gi??? qua
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void loadStatistics() {
        //Cung c???p quy???n truy c???p v??o l???ch s??? v?? th???ng k?? s??? d???ng thi???t b???. D??? li???u s??? d???ng ???????c t???ng h???p th??nh c??c kho???ng th???i gian: ng??y, tu???n, th??ng v?? n??m.
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);

        //Nh???n s??? li???u th???ng k?? v??? m???c s??? d???ng ???ng d???ng trong kho???ng th???i gian nh???t ?????nh, ???????c t???ng h???p theo kho???ng th???i gian ???? ch??? ?????nh.
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  System.currentTimeMillis() - 1000*3600*24,  System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //l???y c???p API l???n h??n v???i y??u c??u
            appList = appList.stream().filter(app -> app.getTotalTimeInForeground() > 0).collect(Collectors.toList());
        }

        // Nh??m c??c usageStats theo ???ng d???ng v?? s???p x???p ch??ng theo t???ng th???i gian ??? n???n tr?????c
        if (appList.size() > 0) {
            Map<String, UsageStats> mySortedMap = new TreeMap<>(); //l??u tr??? c??c ph???n t??? d?????i d???ng key/values
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getPackageName(), usageStats);
            }
            showAppsUsage(mySortedMap);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showAppsUsage(Map<String, UsageStats> mySortedMap) {
        //public void showAppsUsage(List<UsageStats> usageStatsList) {
        ArrayList<App> appsList = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(mySortedMap.values());

        // s???p x???p c??c ???ng d???ng theo th???i gian ??? n???n tr?????c
        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z1.getTotalTimeInForeground(), z2.getTotalTimeInForeground()));

        // l???y t???ng th???i gian s??? d???ng ???ng d???ng ????? t??nh ph???n tr??m s??? d???ng cho t???ng ???ng d???ng
        long totalTime = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalTime = usageStatsList.stream().map(UsageStats::getTotalTimeInForeground).mapToLong(Long::longValue).sum();
        }

        //?????y ????? danh s??ch app
        for (UsageStats usageStats : usageStatsList) {
            try {
                String packageName = usageStats.getPackageName();
                Drawable icon = getDrawable(R.drawable.no_image);
                String[] packageNames = packageName.split("\\.");
                String appName = packageNames[packageNames.length-1].trim();


                if(isAppInfoAvailable(usageStats)){
                    ApplicationInfo ai = getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
                    icon = getApplicationContext().getPackageManager().getApplicationIcon(ai);
                    appName = getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();
                }

                //l???y ph???n tr??m
                String usageDuration = getDurationBreakdown(usageStats.getTotalTimeInForeground());
                int usagePercentage = (int) (usageStats.getTotalTimeInForeground() * 100 / totalTime);

                App usageStatDTO = new App(icon, appName, usagePercentage, usageDuration);
                appsList.add(usageStatDTO);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        // ?????o ng?????c danh s??ch ????? s??? d???ng nhi???u nh???t tr?????c
        Collections.reverse(appsList);
        // x??y d???ng b??? ??i???u h???p
        com.example.appblockr.testTimeUse.AppsAdapter adapter = new com.example.appblockr.testTimeUse.AppsAdapter(this, appsList);

        // g???n b??? ??i???u h???p v??o ListView
        ListView listView = findViewById(R.id.apps_list);
        listView.setAdapter(adapter);

        showHideItemsWhenShowApps();
    }

    /**
     * ki???m tra xem quy???n PACKAGE_USAGE_STATS c?? ???????c ph??p cho ???ng d???ng n??y kh??ng
     * @return true n???u ???????c c???p quy???n
     */
    private boolean getGrantStatus() {
        //ki???m so??t truy c???p v?? theo d??i ???ng d???ng
        AppOpsManager appOps = (AppOpsManager) getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);
        //Truy c???p v??o UsageStatsManager
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == MODE_ALLOWED);
        }
    }

    /**
     * ki???m tra xem th??ng tin ???ng d???ng c?? c??n trong thi???t b??? kh??ng/n???u kh??ng th?? kh??ng th??? hi???n th??? chi ti???t ???ng d???ng
     * @return true n???u c?? th??ng tin ???ng d???ng
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isAppInfoAvailable(UsageStats usageStats) {
        try {
            getApplicationContext().getPackageManager().getApplicationInfo(usageStats.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    /**
     * ph????ng th???c tr??? gi??p ????? nh???n chu???i ??? ?????nh d???ng hh:mm:ss t??? mili gi??y
     * @param millis (th???i gian ???ng d???ng ??? n???n tr?????c)
     * @return chu???i ??? ?????nh d???ng hh:mm:ss t??? mili gi??y
     */
    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        //????n v??? ??o l?????ng th???i gian timeunit
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours + " h " +  minutes + " m " + seconds + " s");
    }


    /**
     * ph????ng th???c tr??? gi??p ???????c s??? d???ng ????? hi???n th???/???n c??c m???c trong ch??? ????? xem khi kh??ng cho ph??p quy???n PACKAGE_USAGE_STATS
     */
    public void showHideNoPermission() {
        enableBtn.setVisibility(View.VISIBLE);
        permissionDescriptionTv.setVisibility(View.VISIBLE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.GONE);
        backTime.setVisibility(View.VISIBLE);
        nextTime.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.GONE);

    }

    /**
     * ph????ng th???c tr??? gi??p ???????c s??? d???ng ????? hi???n th???/???n c??c m???c trong ch??? ????? xem khi cho ph??p PACKAGE_USAGE_STATS
     */
    public void showHideWithPermission() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.VISIBLE);
        usageTv.setVisibility(View.GONE); //Ch??? ????? xem n??y l?? v?? h??nh v?? n?? kh??ng chi???m b???t k??? kho???ng tr???ng n??o cho m???c ????ch b??? c???c.
        backTime.setVisibility(View.VISIBLE);
        nextTime.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.GONE);


    }

    /**
     * ph????ng th???c tr??? gi??p ???????c s??? d???ng ????? hi???n th???/???n c??c m???c trong ch??? ????? xem khi hi???n th??? danh s??ch ???ng d???ng
     */
    public void showHideItemsWhenShowApps() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.VISIBLE);
        backTime.setVisibility(View.VISIBLE);
        nextTime.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.VISIBLE);


    }
    private void addIconToBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_zz);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_main);
    }
}