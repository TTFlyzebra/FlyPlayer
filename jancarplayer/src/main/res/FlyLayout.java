import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by FlyZebra on 2018/6/23.
 * Descrip:
 */

public class FlyLayout {

    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String params = "1280x800";
        System.out.print("Will create layout-" + params + ".\n");
        File fileTo = new File("layout-" + params);
        fileTo.mkdirs();
        File fileSrc = new File("layout");
        File[] files = fileSrc.listFiles();
        for (File file : files) {
            System.out.print(file.getAbsolutePath() + "\n");
            FileInputStream fis = null;
            InputStreamReader isr = null;
            FileOutputStream fos = null;
            OutputStreamWriter osw = null;
            try {
                Pattern pattern = Pattern.compile("=\"[-]?\\d{1,4}px\"");
                File newFile = new File("layout-" + params +File.separator+ file.getName());
                fos = new FileOutputStream(newFile);
                osw = new OutputStreamWriter(fos,"UTF-8");
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String line = "";
                while ((line = br.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String str = matcher.group(0);
                        int num = 0;
                        if(       line.contains("layout_height")
                                ||line.contains("layout_marginBottom")
                                ||line.contains("layout_marginTop")
                                ||line.contains("paddingBottom")
                                ||line.contains("paddingTop")
                         ){
                            num = Integer.valueOf(str.substring(2,str.length()-3))*1280/1024;
                        }else{
                            num = Integer.valueOf(str.substring(2,str.length()-3))*1280/1024;
                        }
                        String newStr = "=\""+num+"px\"";
                        System.out.print(str+"->"+newStr+"\n");
                        line = line.replace(str,newStr);
                    }
                    osw.write(line+"\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (osw != null) try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fos != null) try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isr != null) try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (fis != null) try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
