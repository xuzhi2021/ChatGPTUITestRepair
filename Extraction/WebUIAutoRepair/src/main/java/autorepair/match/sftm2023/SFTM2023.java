package autorepair.match.sftm2023;

import config.Settings;
//import jdk.internal.org.objectweb.asm.util.Printer;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;

public class SFTM2023 {

    public static void main(String[] args) {
        String path = Settings.sftmPath + "net6" + File.separator + "SFTM.exe";
        String oldHTML = "D:\\data\\test\\old\\old.html";
        String newHTML = "D:\\data\\test\\new\\new.html";
        String output = "D:\\result2.json";
        SFTM2023 sftm2023 = new SFTM2023();
        sftm2023.match(oldHTML, newHTML);
        System.out.println(sftm2023.getTime());
//        System.out.println("res" + new SFTM2023().getNewXpath("/html/body[1]/div[1]/div[1]/div[3]/a[1]"));
//        System.out.println(new SFTM2023().normalizeXpath("/BODY/DIV[0]/DIV[0]/DIV[2]/A[0]".toLowerCase(Locale.ROOT)));
    }

    /**
     * execute the .exe
     */
    public Process match(String param1, String param2) {
        String path = Settings.sftmPath + "net6" + File.separator + "SFTM.exe";
        String outputPath = Settings.sftmPath + "tempResult.json";
        String outputPath2 = Settings.sftmPath + "time.txt";
        Runtime rn = Runtime.getRuntime();
        Process p = null;
        System.out.println("start executing .exe\n" + param1 + "\n" + param2);
        String[] parm = {path, param1, param2, outputPath,outputPath2};
        try {
            p = rn.exec(parm);//执行exe
            p.waitFor();
        } catch (Exception e) {
            System.out.println("Error exec!" + e.toString());
        }
        System.out.println("execution succeeded." + outputPath);
        return p;
    }

    public long getTime(){
        String outputPath2 = Settings.sftmPath + "time.txt";
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(outputPath2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(fileInputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        long time = 0;
        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
                String[] split = line.split(":");
                time += Integer.parseInt(split[0]) * 3600;
                time += Integer.parseInt(split[1]) * 60;
                time *= 1000;
                time += Double.parseDouble(split[2]) * 1000;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return time;
    }

    public String getNewXpath(String oldXpath) {
        String outputPath = Settings.sftmPath + "tempResult.json";
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(fileInputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        int count = 0;
        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;//1 empty line 2 old 3 new
            if (count % 3 == 1) {
                continue;
            } else if (count % 3 == 2) {
                String sourceXpath = line;
                if (sourceXpath.trim().equals("null") ) {
                    continue;
                }
                sourceXpath = normalizeXpath(sourceXpath);
                if (sourceXpath.equals(oldXpath)) {
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String targetXpath = line;
                    if (targetXpath.equals("null")){
                        return "";
                    }
                    return normalizeXpath(line);
                }
            }
        }

        return "";
    }

    public String getOldXpath(String newXpath) {
        String outputPath = Settings.sftmPath + "tempResult.json";
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(fileInputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        int count = 0;
        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;//1 empty line 2 old 3 new
            String sourceXpath = "";
            if (count % 3 == 1) {
                continue;
            } else if (count % 3 == 2) {
                sourceXpath = line;
                if (sourceXpath.trim().equals("null") ) {
                    sourceXpath = "";
                }else{
                    sourceXpath = normalizeXpath(sourceXpath);
                }
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                count++;
                String targetXpath = line;
                if (!targetXpath.equals("null")){
                    targetXpath = normalizeXpath(targetXpath);
                    if (targetXpath.equals(newXpath.toLowerCase(Locale.ROOT).trim())) {
                        return sourceXpath;
                    }
                }
            }
        }

        return "";
    }

    public static Process runExe(String path, String param1, String param2, String param3) {
        Runtime rn = Runtime.getRuntime();
        Process p = null;
        String[] parm = {path, param1, param2, param3};
        try {
            p = rn.exec(parm);//execute .exe
        } catch (Exception e) {
            System.out.println("Error exec!" + e.toString());
        }
        return p;
    }

    public String normalizeXpath(String erratumXpath) {
        if (erratumXpath == null) return null;
       erratumXpath = erratumXpath.toLowerCase(Locale.ROOT).trim();
        String headXpath = "/html[1]/body[1]";
        if (erratumXpath.equals("/body")) return headXpath;
        StringBuilder xpath = new StringBuilder(erratumXpath.substring(5));
        String[] partXpaths = xpath.toString().split("/");
        String postXpath = "";
        for (String partXpath : partXpaths) {
            if (partXpath.trim().length() == 0) continue;
            postXpath += "/";
            String temp = partXpath.substring(partXpath.indexOf("[") + 1, partXpath.lastIndexOf("]"));
            int index = Integer.parseInt(temp);
            index++;
            if (!partXpath.substring(0, partXpath.indexOf("[")).equals("svg")) {
                postXpath = postXpath + partXpath.substring(0, partXpath.indexOf("[")) + "[" + index + "]";
            } else {
                postXpath = postXpath + "/*[name()=\"svg\"][" + index + "]";
            }
        }
        return headXpath + postXpath;
    }

}
