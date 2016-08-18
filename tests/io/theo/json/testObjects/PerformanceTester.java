package io.theo.json.testObjects;

public final class PerformanceTester
{
    private PerformanceTester()
    {
    }

    public static double getOpsPerSecond(final int numTestOps, final int numWarmUps, final Runnable operation)
    {
        for (int i = 0; i < numWarmUps; i++)
            operation.run();

        long startTimeNano = System.nanoTime();
        for (int i = 0; i < numTestOps; i++)
            operation.run();
        long stopTimeNano = System.nanoTime();

        long durationNano = stopTimeNano - startTimeNano;
        double nanosPerOp = (double)durationNano / (double)numTestOps;
        double opsPerSecond = (double)1_000_000_000 / nanosPerOp;
        return opsPerSecond;
    }
}
