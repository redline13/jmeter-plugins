package com.googlecode.jmeter.plugins.webdriver.config;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.logging.LogType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.jmeter.util.JMeterUtils;

public class ChromeDriverConfig extends WebDriverConfig<ChromeDriver> {

    private static final String REDLINE = "RedLine13";
    private static final long serialVersionUID = 100L;
    private static final Logger LOGGER = LoggingManager.getLoggerForClass();
    private static final String CHROME_SERVICE_PATH = "ChromeDriverConfig.chromedriver_path";
    private static final String ANDROID_ENABLED = "ChromeDriverConfig.android_enabled";
    private static final Map<String, ChromeDriverService> services = new ConcurrentHashMap<String, ChromeDriverService>();

    public void setChromeDriverPath(String path) {
        setProperty(CHROME_SERVICE_PATH, path);
    }

    public String getChromeDriverPath() {
        boolean isRedline = JMeterUtils.getPropDefault(REDLINE,false);
        if (!isRedline) {
            return getPropertyAsString(CHROME_SERVICE_PATH);
        } else {
            return "chromedriver";
        }
    }

    Capabilities createCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, createProxy());
        LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(LogType.BROWSER, Level.ALL);
		capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);


        if(isAndroidEnabled()) {
            Map<String, String> chromeOptions = new HashMap<String, String>();
            chromeOptions.put("androidPackage", "com.android.chrome");
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        }

        return capabilities;
    }

    Map<String, ChromeDriverService> getServices() {
        return services;
    }

    @Override
    protected ChromeDriver createBrowser() {
        final ChromeDriverService service = getThreadService();
        return service != null ? new ChromeDriver(service, createCapabilities()) : null;
    }

    @Override
    public void quitBrowser(final ChromeDriver browser) {
        super.quitBrowser(browser);
        final ChromeDriverService service = services.remove(currentThreadName());
        if (service != null && service.isRunning()) {
            service.stop();
        }
    }

    private ChromeDriverService getThreadService() {
        ChromeDriverService service = services.get(currentThreadName());
        if (service != null) {
            return service;
        }
        try {
            service = new ChromeDriverService.Builder().usingDriverExecutable(new File(getChromeDriverPath())).build();
            service.start();
            services.put(currentThreadName(), service);
        } catch (IOException e) {
            LOGGER.error("Failed to start chrome service");
            service = null;
        }
        return service;
    }

    public boolean isAndroidEnabled() {
        return getPropertyAsBoolean(ANDROID_ENABLED);
    }

    public void setAndroidEnabled(boolean enabled) {
        setProperty(ANDROID_ENABLED, enabled);
    }
}
