/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.datagate.services;

import lombok.Data;
import lombok.Getter;
import org.dizitart.no2.datagate.models.AppDetails;
import org.dizitart.no2.datagate.models.Device;
import org.dizitart.no2.datagate.models.Statistics;
import org.dizitart.no2.datagate.models.SyncLog;
import org.dizitart.no2.meta.Attributes;
import org.jongo.Aggregate;
import org.jongo.Jongo;
import org.jongo.MongoCursor;
import org.jongo.marshall.jackson.oid.MongoId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.dizitart.no2.datagate.Constants.*;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee.
 */
@Service
public class AnalyticsService {
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    @Getter
    private final Statistics statistics = new Statistics();

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private Jongo jongo;

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void collectionStat() {
        statistics.setUserCount(getUserCount());
        statistics.setClientCount(getClientCount());
        statistics.setCollectionCount(getCollectionCount());
        statistics.setDocumentCount(getDocumentCount());
        statistics.setSyncGraphData(getSyncGraphData());
        statistics.setDeviceList(getDeviceList());
        statistics.setAppList(getAppList());
    }

    public int getUserCount() {
        return userAccountService.findUsersByAuthorities(AUTH_USER).size();
    }

    public int getClientCount() {
        return userAccountService.findUsersByAuthorities(AUTH_CLIENT).size();
    }

    public long getCollectionCount() {
        return jongo.getCollection(ATTRIBUTE_REPO).count();
    }

    public long getDocumentCount() {
        long sum = 0;
        Set<String> collectionNames = new HashSet<>();
        MongoCursor<Attributes> cursor = jongo.getCollection(ATTRIBUTE_REPO).find().as(Attributes.class);
        for (Attributes attributes : cursor) {
            collectionNames.add(attributes.getCollection());
        }

        for (String name : collectionNames) {
            sum = sum + jongo.getCollection(name).count();
        }
        return sum;
    }

    public Long[][] getSyncGraphData() {
        LocalDate start = LocalDate.now(ZoneId.systemDefault()).minusDays(30);
        Long[][] arrays = new Long[30][];
        for (int i = 0; i < 30; i++) {
            start = start.plusDays(1);
            long epochDay = start.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
            arrays[i] = new Long[]{
                epochDay,
                (long) jongo.getCollection(SYNC_LOG)
                    .find("{ epochDay: #}", epochDay)
                    .as(SyncLog.class)
                    .count()
            };
        }
        return arrays;
    }

    public List<Device> getDeviceList() {
        Aggregate.ResultsIterator<DeviceResult> deviceResults
            = jongo.getCollection(SYNC_LOG).aggregate(
            "{" +
                "  $group: {" +
                "    _id: '$userAgent.device'," +
                "    count: {" +
                "      $sum: 1" +
                "    }" +
                "  }" +
                "}, {" +
                "  $sort: {" +
                "    count: -1" +
                "  }" +
                "}")
            .as(DeviceResult.class);

        List<DeviceResult> deviceResultList
            = StreamSupport.stream(deviceResults.spliterator(), false)
            .collect(Collectors.toList());
        List<Device> deviceList = new ArrayList<>();
        int index = 0;
        long totalCount = deviceResultList.stream().mapToInt(device -> device.count).sum();

        for (DeviceResult deviceResult : deviceResultList) {
            if (isNullOrEmpty(deviceResult.device)) {
                deviceResult.device = "Other";
            }

            int percentage = (int) ((float) deviceResult.count * 100 / totalCount);
            switch (index) {
                case 0:
                    deviceList.add(new Device("blue", "#49A9EA", "#3498DB",
                        deviceResult.device, percentage));
                    break;
                case 1:
                    deviceList.add(new Device("green", "#36CAAB", "#26B99A",
                        deviceResult.device, percentage));
                    break;
                case 2:
                    deviceList.add(new Device("purple", "#B370CF", "#9B59B6",
                        deviceResult.device, percentage));
                    break;
                case 3:
                    deviceList.add(new Device("aero", "#CFD4D8", "#BDC3C7",
                        deviceResult.device, percentage));
                    break;
                case 4:
                    deviceList.add(new Device("red", "#E95E4F", "#E74C3C",
                        deviceResult.device, percentage));
                    break;
                default:
                    break;
            }
            index++;
        }

        return deviceList;
    }

    public List<AppDetails> getAppList() {
        Aggregate.ResultsIterator<AppResult> appResults
            = jongo.getCollection(SYNC_LOG).aggregate(
            "{" +
                "  $group: {" +
                "    _id: {" +
                "      appName: '$userAgent.appName'," +
                "      appVersion: '$userAgent.appVersion'" +
                "    }," +
                "    count: {" +
                "      $sum: 1" +
                "    }" +
                "  }" +
                "}, {" +
                "  $sort: {" +
                "    count: -1" +
                "  }" +
                "}")
            .as(AppResult.class);

        List<AppResult> appResultList
            = StreamSupport.stream(appResults.spliterator(), false)
            .collect(Collectors.toList());

        Map<String, Map<String, Integer>> appDetailsMap = new HashMap<>();
        for (AppResult appResult : appResultList) {
            App app = appResult.getApp();
            if (app != null) {
                Map<String, Integer> versionMap = appDetailsMap.get(app.getAppName());
                if (versionMap == null) {
                    versionMap = new HashMap<>();
                }
                versionMap.put(app.getAppVersion(), appResult.getCount());
                appDetailsMap.put(app.getAppName(), versionMap);
            }
        }

        List<AppDetails> appDetailsList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : appDetailsMap.entrySet()) {
            AppDetails appDetails = new AppDetails();
            appDetails.setName(entry.getKey());

            Map<String, Integer> versionMap = entry.getValue();
            int sum = versionMap.values().stream().mapToInt(i -> i).sum();

            List<AppDetails.AppVersion> versionList = new ArrayList<>();
            for (Map.Entry<String, Integer> versionEntry : versionMap.entrySet()) {
                versionList.add(new AppDetails.AppVersion(versionEntry.getKey(),
                    format(versionEntry.getValue()),
                    ((float) versionEntry.getValue() / sum) * 100));
            }
            appDetails.setVersionList(versionList);
            appDetailsList.add(appDetails);
        }

        return appDetailsList;
    }

    private static String format(long value) {
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    @Data
    private static class DeviceResult {
        @MongoId
        private String device;
        private int count;
    }

    @Data
    private static class AppResult {
        @MongoId
        private App app;
        private int count;
    }

    @Data
    private static class App {
        private String appName;
        private String appVersion;
    }

}
