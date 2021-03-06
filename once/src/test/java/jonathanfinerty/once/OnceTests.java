package jonathanfinerty.once;

import android.content.pm.PackageInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(TestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class OnceTests {

    private static final String tagUnderTest = "testTag";

    @Before
    public void Setup() {
        Once.initialise(RuntimeEnvironment.application);
    }

    @Test
    public void unseenTags() {
        boolean seenThisInstall = Once.beenDone(Once.THIS_APP_INSTALL, tagUnderTest);
        Assert.assertFalse(seenThisInstall);

        boolean seenThisAppVersion = Once.beenDone(Once.THIS_APP_VERSION, tagUnderTest);
        Assert.assertFalse(seenThisAppVersion);

        boolean seenInTheLastDay = Once.beenDone(TimeUnit.DAYS, 1, tagUnderTest);
        Assert.assertFalse(seenInTheLastDay);
    }

    @Test
    public void seenTagImmediately() {
        Once.markDone(tagUnderTest);

        boolean seenThisInstall = Once.beenDone(Once.THIS_APP_INSTALL, tagUnderTest);
        Assert.assertTrue(seenThisInstall);

        boolean seenThisAppVersion = Once.beenDone(Once.THIS_APP_VERSION, tagUnderTest);
        Assert.assertTrue(seenThisAppVersion);

        boolean seenThisMinute = Once.beenDone(TimeUnit.MINUTES, 1, tagUnderTest);
        Assert.assertTrue(seenThisMinute);
    }

    @Test
    public void removeFromDone() {
        Once.markDone(tagUnderTest);

        Once.clearDone(tagUnderTest);

        boolean seenThisInstall = Once.beenDone(Once.THIS_APP_INSTALL, tagUnderTest);
        Assert.assertFalse(seenThisInstall);

        boolean seenThisAppVersion = Once.beenDone(Once.THIS_APP_VERSION, tagUnderTest);
        Assert.assertFalse(seenThisAppVersion);

        boolean seenInTheLastDay = Once.beenDone(TimeUnit.DAYS, 1, tagUnderTest);
        Assert.assertFalse(seenInTheLastDay);
    }

    @Test
    public void seenTagAfterAppUpdate() {
        Once.markDone(tagUnderTest);

        simulateAppUpdate();

        boolean seenThisInstall = Once.beenDone(Once.THIS_APP_INSTALL, tagUnderTest);
        Assert.assertTrue(seenThisInstall);

        boolean seenThisAppVersion = Once.beenDone(Once.THIS_APP_VERSION, tagUnderTest);
        Assert.assertFalse(seenThisAppVersion);

        boolean seenThisMinute = Once.beenDone(TimeUnit.MINUTES, 1, tagUnderTest);
        Assert.assertTrue(seenThisMinute);
    }

    @Test
    public void seenTagAfterSecond() throws InterruptedException {
        Once.markDone(tagUnderTest);

        boolean seenThisInstall = Once.beenDone(Once.THIS_APP_INSTALL, tagUnderTest);
        Assert.assertTrue(seenThisInstall);

        boolean seenThisAppVersion = Once.beenDone(Once.THIS_APP_VERSION, tagUnderTest);
        Assert.assertTrue(seenThisAppVersion);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1) + 1);
        boolean seenThisSecond = Once.beenDone(TimeUnit.SECONDS, 1, tagUnderTest);
        Assert.assertFalse(seenThisSecond);

        long secondInMillis = 1000;
        boolean seenThisSecondInMillis = Once.beenDone(secondInMillis, tagUnderTest);
        Assert.assertFalse(seenThisSecondInMillis);
    }

    @Test
    public void reset() {
        String tag1 = "tag1";
        String tag2 = "tag2";
        Once.markDone(tag1);
        Once.markDone(tag2);

        Once.clearAll();

        Assert.assertFalse(Once.beenDone(Once.THIS_APP_INSTALL, tag1));
        Assert.assertFalse(Once.beenDone(Once.THIS_APP_VERSION, tag1));
        Assert.assertFalse(Once.beenDone(1000L, tag1));

        Assert.assertFalse(Once.beenDone(Once.THIS_APP_INSTALL, tag2));
        Assert.assertFalse(Once.beenDone(Once.THIS_APP_VERSION, tag2));
        Assert.assertFalse(Once.beenDone(1000L, tag2));
    }

    private void simulateAppUpdate() {
        RobolectricPackageManager rpm = RuntimeEnvironment.getRobolectricPackageManager();
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = RuntimeEnvironment.application.getPackageName();
        packageInfo.lastUpdateTime = new Date().getTime();
        rpm.addPackage(packageInfo);
        Once.initialise(RuntimeEnvironment.application);
    }

}
