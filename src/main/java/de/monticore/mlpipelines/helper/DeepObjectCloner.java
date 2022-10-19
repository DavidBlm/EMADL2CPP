package de.monticore.mlpipelines.helper;

import java.io.*;

public class DeepObjectCloner<T> {
    public T clone(T object) {
        try {
            byte[] bytes = serialize(object);
            T clone = deserialize(bytes);
            return clone;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("Object could not be cloned");
    }

    private byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteData = bos.toByteArray();
        return byteData;
    }

    private T deserialize(byte[] byteData) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
        T object = (T) new ObjectInputStream(bais).readObject();

        return object;
    }
}
