package io.theo.json;

import io.theo.json.testObjects.PerformanceTester;
import io.theo.json.testObjects.SimpleStringListValueObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

public class JsonPerformanceIntegrationTests
{
    @Test
    public void JsonSerializer_PerformanceTest_BetterThanTenThousandsOpsPerSecond()
    {
        SimpleStringListValueObject obj = new SimpleStringListValueObject(Arrays.asList("JC Denton", "Adam Jensen", "Paul Denton", "David Sarif"));

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> Json.toJsonString(obj));

        System.out.println("Serialize Simple POJO: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 10000);
    }

    @Test
    public void JsonSerializer_PerformanceTestLargeList_BetterThanFiveThousandOpsPerSecond()
    {
        List<String> items = new ArrayList<>();
        IntStream.range(0, 100).forEach(x -> items.add(UUID.randomUUID().toString()));

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> Json.toJsonString(items));

        System.out.println("Serialize 100 Item List: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 5000);
    }

    @Test
    public void JsonSerializer_PerformanceTestLargeStringMap_BetterThanFiveThousandOpsPerSecond()
    {
        Map<String, String> items = new HashMap<>();
        IntStream.range(0, 100).forEach(x -> items.put(Integer.toString(x), UUID.randomUUID().toString()));

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> Json.toJsonString(items));

        System.out.println("Serialize 100 Item Map: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 5000);
    }

    @Test
    public void JsonDeserializer_PerformanceTest_FasterThanTenThousandOpsPerSecond()
    {
        String json = "{ \"Value\": [ \"JC Denton\", \"Adam Jensen\", \"Paul Denton\", \"David Sarif\" ] }";

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> Json.toObj(SimpleStringListValueObject.class, json));

        System.out.println("Deserialize Simple POJO: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 10000);
    }
}
