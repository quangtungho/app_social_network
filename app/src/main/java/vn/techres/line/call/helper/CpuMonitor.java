package vn.techres.line.call.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class CpuMonitor {
    private static final int MOVING_AVERAGE_SAMPLES = 5;

    private static final int CPU_STAT_SAMPLE_PERIOD_MS = 2000;
    private static final int CPU_STAT_LOG_PERIOD_MS = 6000;

    private final Context appContext;
    // User CPU usage at current frequency.
    private final MovingAverage userCpuUsage;
    // System CPU usage at current frequency.
    private final MovingAverage systemCpuUsage;
    // Total CPU usage relative to maximum frequency.
    private final MovingAverage totalCpuUsage;
    // CPU frequency in percentage from maximum.
    private final MovingAverage frequencyScale;

    @Nullable
    private ScheduledExecutorService executor;
    private long lastStatLogTimeMs;
    private long[] cpuFreqMax;
    private int cpusPresent;
    private int actualCpusPresent;
    private boolean initialized;
    private boolean cpuOveruse;
    private String[] maxPath;
    private String[] curPath;
    private double[] curFreqScales;
    @Nullable
    private ProcStat lastProcStat;

    private static class ProcStat {
        final long userTime;
        final long systemTime;
        final long idleTime;

        ProcStat(long userTime, long systemTime, long idleTime) {
            this.userTime = userTime;
            this.systemTime = systemTime;
            this.idleTime = idleTime;
        }
    }

    private static class MovingAverage {
        private final int size;
        private double sum;
        private double currentValue;
        private final double[] circBuffer;
        private int circBufferIndex;

        public MovingAverage(int size) {
            if (size <= 0) {
                throw new AssertionError("Size value in MovingAverage ctor should be positive.");
            }
            this.size = size;
            circBuffer = new double[size];
        }

        public void reset() {
            Arrays.fill(circBuffer, 0);
            circBufferIndex = 0;
            sum = 0;
            currentValue = 0;
        }

        public void addValue(double value) {
            sum -= circBuffer[circBufferIndex];
            circBuffer[circBufferIndex++] = value;
            currentValue = value;
            sum += value;
            if (circBufferIndex >= size) {
                circBufferIndex = 0;
            }
        }

        public double getCurrent() {
            return currentValue;
        }

        public double getAverage() {
            return sum / (double) size;
        }
    }

    public static boolean isSupported() {
        return false;
    }

    public CpuMonitor(Context context) {
        if (!isSupported()) {
            throw new RuntimeException("CpuMonitor is not supported on this Android version.");
        }

        Timber.d("CpuMonitor ctor.");
        appContext = context.getApplicationContext();
        userCpuUsage = new MovingAverage(MOVING_AVERAGE_SAMPLES);
        systemCpuUsage = new MovingAverage(MOVING_AVERAGE_SAMPLES);
        totalCpuUsage = new MovingAverage(MOVING_AVERAGE_SAMPLES);
        frequencyScale = new MovingAverage(MOVING_AVERAGE_SAMPLES);
        lastStatLogTimeMs = SystemClock.elapsedRealtime();

        scheduleCpuUtilizationTask();
    }

    public void pause() {
        if (executor != null) {
            Timber.d("pause");
            executor.shutdownNow();
            executor = null;
        }
    }

    public void resume() {
        Timber.d("resume");
        resetStat();
        scheduleCpuUtilizationTask();
    }

    private void scheduleCpuUtilizationTask() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        executor = Executors.newSingleThreadScheduledExecutor();
        @SuppressWarnings("unused") // Prevent downstream linter warnings.
        Future<?> possiblyIgnoredError = executor.scheduleAtFixedRate(this::cpuUtilizationTask, 0, CPU_STAT_SAMPLE_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    private void cpuUtilizationTask() {
        boolean cpuMonitorAvailable = sampleCpuUtilization();
        if (cpuMonitorAvailable
                && SystemClock.elapsedRealtime() - lastStatLogTimeMs >= CPU_STAT_LOG_PERIOD_MS) {
            lastStatLogTimeMs = SystemClock.elapsedRealtime();
            String statString = getStatString();
            Timber.d(statString);
        }
    }

    private void init() {
        try (FileInputStream fin = new FileInputStream("/sys/devices/system/cpu/present");
             InputStreamReader streamReader = new InputStreamReader(fin, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader);
             Scanner scanner = new Scanner(reader).useDelimiter("[-\n]")) {
            scanner.nextInt(); // Skip leading number 0.
            cpusPresent = 1 + scanner.nextInt();
        } catch (FileNotFoundException e) {
            Timber.e("Cannot do CPU stats since /sys/devices/system/cpu/present is missing");
        } catch (IOException e) {
            Timber.e("Error closing file");
        } catch (Exception e) {
            Timber.e("Cannot do CPU stats due to /sys/devices/system/cpu/present parsing problem");
        }

        cpuFreqMax = new long[cpusPresent];
        maxPath = new String[cpusPresent];
        curPath = new String[cpusPresent];
        curFreqScales = new double[cpusPresent];
        for (int i = 0; i < cpusPresent; i++) {
            cpuFreqMax[i] = 0; // Frequency "not yet determined".
            curFreqScales[i] = 0;
            maxPath[i] = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
            curPath[i] = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq";
        }

        lastProcStat = new ProcStat(0, 0, 0);
        resetStat();

        initialized = true;
    }

    private synchronized void resetStat() {
        userCpuUsage.reset();
        systemCpuUsage.reset();
        totalCpuUsage.reset();
        frequencyScale.reset();
        lastStatLogTimeMs = SystemClock.elapsedRealtime();
    }

    private int getBatteryLevel() {
        // Use sticky broadcast with null receiver to read battery level once only.
        Intent intent = appContext.registerReceiver(
                null /* receiver */, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int batteryLevel = 0;
        int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        if (batteryScale > 0) {
            batteryLevel =
                    (int) (100f * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) / batteryScale);
        }
        return batteryLevel;
    }

    /**
     * Re-measure CPU use.  Call this method at an interval of around 1/s.
     * This method returns true on success.  The fields
     * cpuCurrent, cpuAvg3, and cpuAvgAll are updated on success, and represents:
     * cpuCurrent: The CPU use since the last sampleCpuUtilization call.
     * cpuAvg3: The average CPU over the last 3 calls.
     * cpuAvgAll: The average CPU over the last SAMPLE_SAVE_NUMBER calls.
     */
    private synchronized boolean sampleCpuUtilization() {
        long lastSeenMaxFreq = 0;
        long cpuFreqCurSum = 0;
        long cpuFreqMaxSum = 0;

        if (!initialized) {
            init();
        }
        if (cpusPresent == 0) {
            return false;
        }

        actualCpusPresent = 0;
        for (int i = 0; i < cpusPresent; i++) {
            /*
             * For each CPU, attempt to first read its max frequency, then its
             * current frequency.  Once as the max frequency for a CPU is found,
             * save it in cpuFreqMax[].
             */

            curFreqScales[i] = 0;
            if (cpuFreqMax[i] == 0) {
                // We have never found this CPU's max frequency.  Attempt to read it.
                long cpufreqMax = readFreqFromFile(maxPath[i]);
                if (cpufreqMax > 0) {
                    Timber.d("Core " + i + ". Max frequency: " + cpufreqMax);
                    lastSeenMaxFreq = cpufreqMax;
                    cpuFreqMax[i] = cpufreqMax;
                    maxPath[i] = null; // Kill path to free its memory.
                }
            } else {
                lastSeenMaxFreq = cpuFreqMax[i]; // A valid, previously read value.
            }

            long cpuFreqCur = readFreqFromFile(curPath[i]);
            if (cpuFreqCur == 0 && lastSeenMaxFreq == 0) {
                // No current frequency information for this CPU core - ignore it.
                continue;
            }
            if (cpuFreqCur > 0) {
                actualCpusPresent++;
            }
            cpuFreqCurSum += cpuFreqCur;

            /* Here, lastSeenMaxFreq might come from
             * 1. cpuFreq[i], or
             * 2. a previous iteration, or
             * 3. a newly read value, or
             * 4. hypothetically from the pre-loop dummy.
             */
            cpuFreqMaxSum += lastSeenMaxFreq;
            if (lastSeenMaxFreq > 0) {
                curFreqScales[i] = (double) cpuFreqCur / lastSeenMaxFreq;
            }
        }

        if (cpuFreqCurSum == 0 || cpuFreqMaxSum == 0) {
            Timber.e("Could not read max or current frequency for any CPU");
            return false;
        }

        /*
         * Since the cycle counts are for the period between the last invocation
         * and this present one, we average the percentual CPU frequencies between
         * now and the beginning of the measurement period.  This is significantly
         * incorrect only if the frequencies have peeked or dropped in between the
         * invocations.
         */
        double currentFrequencyScale = cpuFreqCurSum / (double) cpuFreqMaxSum;
        if (frequencyScale.getCurrent() > 0) {
            currentFrequencyScale = (frequencyScale.getCurrent() + currentFrequencyScale) * 0.5;
        }

        ProcStat procStat = readProcStat();
        if (procStat == null) {
            return false;
        }

        assert lastProcStat != null;
        long diffUserTime = procStat.userTime - lastProcStat.userTime;
        long diffSystemTime = procStat.systemTime - lastProcStat.systemTime;
        long diffIdleTime = procStat.idleTime - lastProcStat.idleTime;
        long allTime = diffUserTime + diffSystemTime + diffIdleTime;

        if (currentFrequencyScale == 0 || allTime == 0) {
            return false;
        }

        // Update statistics.
        frequencyScale.addValue(currentFrequencyScale);

        double currentUserCpuUsage = diffUserTime / (double) allTime;
        userCpuUsage.addValue(currentUserCpuUsage);

        double currentSystemCpuUsage = diffSystemTime / (double) allTime;
        systemCpuUsage.addValue(currentSystemCpuUsage);

        double currentTotalCpuUsage =
                (currentUserCpuUsage + currentSystemCpuUsage) * currentFrequencyScale;
        totalCpuUsage.addValue(currentTotalCpuUsage);

        // Save new measurements for next round's deltas.
        lastProcStat = procStat;

        return true;
    }

    private int doubleToPercent(double d) {
        return (int) (d * 100 + 0.5);
    }

    private synchronized String getStatString() {
        StringBuilder stat = new StringBuilder();
        stat.append("CPU User: ")
                .append(doubleToPercent(userCpuUsage.getCurrent()))
                .append("/")
                .append(doubleToPercent(userCpuUsage.getAverage()))
                .append(". System: ")
                .append(doubleToPercent(systemCpuUsage.getCurrent()))
                .append("/")
                .append(doubleToPercent(systemCpuUsage.getAverage()))
                .append(". Freq: ")
                .append(doubleToPercent(frequencyScale.getCurrent()))
                .append("/")
                .append(doubleToPercent(frequencyScale.getAverage()))
                .append(". Total usage: ")
                .append(doubleToPercent(totalCpuUsage.getCurrent()))
                .append("/")
                .append(doubleToPercent(totalCpuUsage.getAverage()))
                .append(". Cores: ")
                .append(actualCpusPresent);
        stat.append("( ");
        for (int i = 0; i < cpusPresent; i++) {
            stat.append(doubleToPercent(curFreqScales[i])).append(" ");
        }
        stat.append("). Battery: ").append(getBatteryLevel());
        if (cpuOveruse) {
            stat.append(". Overuse.");
        }
        return stat.toString();
    }

    /**
     * Read a single integer value from the named file.  Return the read value
     * or if an error occurs return 0.
     */
    private long readFreqFromFile(String fileName) {
        long number = 0;
        try (FileInputStream stream = new FileInputStream(fileName);
             InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String line = reader.readLine();
            number = parseLong(line);
        } catch (FileNotFoundException e) {
            // CPU core is off, so file with its scaling frequency .../cpufreq/scaling_cur_freq
            // is not present. This is not an error.
        } catch (IOException e) {
            // CPU core is off, so file with its scaling frequency .../cpufreq/scaling_cur_freq
            // is empty. This is not an error.
        }
        return number;
    }

    private static long parseLong(String value) {
        long number = 0;
        try {
            number = Long.parseLong(value);
        } catch (NumberFormatException e) {
            Timber.e(e, "parseLong error.");
        }
        return number;
    }

    /*
     * Read the current utilization of all CPUs using the cumulative first line
     * of /proc/stat.
     */
    @SuppressWarnings("StringSplitter")
    private @Nullable ProcStat readProcStat() {
        long userTime = 0;
        long systemTime = 0;
        long idleTime = 0;
        try (FileInputStream stream = new FileInputStream("/proc/stat");
             InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            // line should contain something like this:
            // cpu  5093818 271838 3512830 165934119 101374 447076 272086 0 0 0
            //       user    nice  system     idle   iowait  irq   softirq
            String line = reader.readLine();
            String[] lines = line.split("\\s+");
            int length = lines.length;
            if (length >= 5) {
                userTime = parseLong(lines[1]); // user
                userTime += parseLong(lines[2]); // nice
                systemTime = parseLong(lines[3]); // system
                idleTime = parseLong(lines[4]); // idle
            }
            if (length >= 8) {
                userTime += parseLong(lines[5]); // iowait
                systemTime += parseLong(lines[6]); // irq
                systemTime += parseLong(lines[7]); // softirq
            }
        } catch (FileNotFoundException e) {
            Timber.e(e, "Cannot open /proc/stat for reading");
            return null;
        } catch (Exception e) {
            Timber.e(e, "Problems parsing /proc/stat");
            return null;
        }
        return new ProcStat(userTime, systemTime, idleTime);
    }
}
