package com.huawei.scrcpy_ohos;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RuntimeHelper {
    //    private static final Runtime instance = Runtime.getRuntime();
    private static RuntimeHelper instance;

    public static RuntimeHelper getInstance() {
        if (instance == null) {
            instance = new RuntimeHelper();
        }
        return instance;
    }

    private Runtime runtime;

    public RuntimeHelper() {
        runtime = Runtime.getRuntime();
    }

    public boolean isSuccess(Map<String, Object> execRet) {
        if (((int) execRet.get("code")) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> getData(Map<String, Object> execRet) {
        if (((int) execRet.get("code")) == 0) {
            return ((List<String>) execRet.get("data"));
        } else {
            return null;
        }
    }

    public List<String> getError(Map<String, Object> execRet) {
        if (((int) execRet.get("code")) == 0) {
            return null;
        } else {
            return ((List<String>) execRet.get("data"));
        }
    }

    public void printSuccessMessage(Map<String, Object> execRet) {
        if (((int) execRet.get("code")) == 0) {
            List<String> data = (List<String>) execRet.get("data");
            for (String msg : data) {
                System.out.println(msg);
            }
        } else {
        }
    }

    public void printErrorMessage(Map<String, Object> execRet) {
        if (((int) execRet.get("code")) == 0) {
        } else {
            List<String> data = (List<String>) execRet.get("data");
            for (String msg : data) {
                System.out.println(msg);
            }
        }
    }

    public Map<String, Object> exec(String command) {
        CountDownLatch latch = new CountDownLatch(2);
        Map<String, Object> map = new HashMap<>();
        try {
            Process process = runtime.exec(command);
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        List<String> log = new ArrayList<>();
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            if (line.length() != 0) {
                                log.add(line);
                            }
                        }
                        reader.close();
                        map.put("code", 0);
                        map.put("data", log);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                        List<String> log = new ArrayList<>();
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            if (line.length() != 0) {
                                log.add(line);
                            }
                        }
                        reader.close();
                        if (!log.isEmpty()) {
                            map.put("code", -1);
                            map.put("data", log);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }
            }).start();

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
