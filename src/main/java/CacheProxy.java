import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ivan on 05/10/16.
 */
public class CacheProxy implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 7526471155622776147L;

    private Object delegate;
    private Map<Method, Map<List<Object>, Object>> cache = new HashMap<>();

    public CacheProxy(Object delegate) {
        this.delegate = delegate;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result;

        if (method.isAnnotationPresent(Cache.class)) {

            Map<List<Object>, Object> mm = cache.get(method);
            List<Object> argList = Arrays.asList(args);

            if (mm == null) {
                mm = new HashMap<>();
                cache.put(method, mm);
            }

            if (!mm.containsKey(argList)) {
                result = method.invoke(delegate, args);
                mm.put(argList, result);
            } else
                result = mm.get(argList);
        } else
            result = method.invoke(delegate, args);

        return result;
    }


    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        Class<?> clazz = delegate.getClass();

        if (Arrays.stream(clazz.getInterfaces()).anyMatch(i -> i == Serializable.class)) {
            out.writeBoolean(true);
            out.writeObject(out);
        } else {
            out.writeBoolean(false);
            out.writeObject(clazz);
        }

        out.writeInt(cache.size());
        for (Map.Entry<Method, Map<List<Object>, Object>> entry : cache.entrySet()) {
            out.writeUTF(entry.getKey().getName());
            out.writeObject(entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object delegate;
        Class<?> clazz;

        if (in.readBoolean())
            delegate = in.readObject();
        else {
            try {
                delegate = ((Class<?>) in.readObject()).newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new IOException(e);
            }
        }

        this.delegate = delegate;
        clazz = delegate.getClass();

        int size = in.readInt();
        cache = new HashMap<>();

        for (int i = 0; i < size; ++i) {
            String methodString = in.readUTF();
            List<Method> found = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(methodString))
                    .collect(Collectors.toList());
            if (found.size() != 1)
                throw new ClassNotFoundException("method: '" + methodString + "' not found");
            cache.put(found.get(0), Map.class.cast(in.readObject()));
        }
    }
}
