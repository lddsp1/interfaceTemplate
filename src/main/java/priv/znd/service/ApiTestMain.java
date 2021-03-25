package priv.znd.service;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author lddsp
 * @date 2021/3/23 5:52
 */
public class ApiTestMain {
    private static final Logger logger = LoggerFactory.getLogger(ApiTestMain.class);
    public static void main(String[] args) {
        logger.info("开始执行测试案例...................");
        LauncherDiscoveryRequest launcherReques = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(
                        selectClass(CreateTest.class)
                )
                .filters(
                        includeClassNamePatterns(".*Test")
                )
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(launcherReques);
        SummaryGeneratingListener listener =new SummaryGeneratingListener();;
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(launcherReques);
        TestExecutionSummary summary = listener.getSummary();
        logger.info("案例总数:"+summary.getTestsFoundCount());
        logger.info("案例执行失败数有:"+summary.getTestsFailedCount());
        logger.info("案例执行成功数有:"+summary.getTestsSucceededCount());
        logger.info("案例执行结束.......................");

    }
}
