package com.fastfriends.android.helper;

import android.util.Log;

import com.fastfriends.android.model.FitHistory;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jschnall on 2/17/15.
 */
public class FitHistoryHelper {
    public static final String LOGTAG = FitHistoryHelper.class.getSimpleName();
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    public static DataReadRequest readFitnessHistory(String timePeriod) {
        Calendar endCal = Calendar.getInstance();
        long endTime = endCal.getTimeInMillis();

        return readFitnessHistory(getStartTime(timePeriod), endTime);
    }

    public static long getStartTime(String timePeriod) {
        Calendar now = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        startCal.clear();

        if(FitHistory.WEEK.equals(timePeriod)) {
            // This week
            startCal.set(Calendar.WEEK_OF_YEAR, now.get(Calendar.WEEK_OF_YEAR));
            startCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
            // Within a week
            //endCal.add(Calendar.DAY_OF_WEEK, -7);
        } else if(FitHistory.MONTH.equals(timePeriod)) {
            // This month
            startCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
            startCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
            // Within a month
            //endCal.add(Calendar.MONTH, -1);
        } else if(FitHistory.YEAR.equals(timePeriod)) {
            // This year
            startCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
            // Within a year
            //endCal.add(Calendar.YEAR, -1);
        }

        return startCal.getTimeInMillis();
    }

    public static DataReadRequest readFitnessHistory(long startTime, long endTime) {
        // Setting a start and end date using a range of 1 week before this moment.
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(LOGTAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(LOGTAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                //.aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                //.aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                //.aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                        //.bucketByTime(1, TimeUnit.DAYS)
                .bucketByActivityType(1, TimeUnit.MINUTES) // only count activity segments lasting at least 1 minute
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        return readRequest;
    }

    public static List<FitHistory> buildHistoryList(DataReadResult dataReadResult, String timePeriod) {
        List<FitHistory> historyList = new ArrayList<FitHistory>();

        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                String activityType = bucket.getActivity();
                Log.d(LOGTAG, "ActivityType: " + activityType);

                if (!FitHistory.isTrackedActivity(activityType)) {
                    continue;
                }
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        for(Field field : dp.getDataType().getFields()) {
                            String fieldName = field.getName();
                            Value value = dp.getValue(field);
                            Log.d(LOGTAG, "  Field: " + fieldName);
                            Log.d(LOGTAG, "  Value: " + value);

                            // Currently only interested in duration (total time), steps, and distance
                            if (/*FitHistory.DISTANCE.equals(fieldName) ||*/ FitHistory.DURATION.equals(fieldName) ||
                                    FitHistory.STEPS.equals(fieldName)) {
                                FitHistory history = new FitHistory();
                                // ownerId will be set automatically by server when uploaded
                                history.setPeriod(timePeriod.toUpperCase());
                                history.setActivity(bucket.getActivity().toUpperCase());
                                history.setField(field.getName().toUpperCase());
                                history.setValue(dp.getValue(field).toString());
                                historyList.add(history);
                            }
                        }
                    }
                }
            }
        }

        return historyList;
    }

    public static void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(LOGTAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                Log.i(LOGTAG, "Bucket activity is: " + bucket.getActivity());
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(LOGTAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    public static void dumpDataSet(DataSet dataSet) {
        Log.i(LOGTAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        Log.i(LOGTAG, dataSet.getDataPoints().size() + " Data points");
        for (DataPoint dp : dataSet.getDataPoints()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Log.i(LOGTAG, "Data point:");
            Log.i(LOGTAG, "\tType: " + dp.getDataType().getName());
            Log.i(LOGTAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(LOGTAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(LOGTAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

}
