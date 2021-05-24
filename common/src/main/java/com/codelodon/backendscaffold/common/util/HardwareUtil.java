package com.codelodon.backendscaffold.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HardwareUtil {
    public static String getMachineUUID() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("cat /sys/devices/virtual/dmi/id/product_uuid");
        InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        p.waitFor();
        if (p.exitValue() != 0) {
            return null;
        }

        return reader.readLine();
    }
}
