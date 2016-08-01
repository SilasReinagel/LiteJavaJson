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

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numTestOps; i++)
            operation.run();
        long stopTime = System.currentTimeMillis();

        long durationMillis = stopTime - startTime;
        double millisPerOp = (double)durationMillis / numTestOps;
        double opsPerSecond = (double)1000 / millisPerOp;
        return opsPerSecond;
    }
}
