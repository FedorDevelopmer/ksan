package com.course.filetransfer;
import java.io.*;
import java.nio.file.Path;

public class Serializer {
    public static void serialize(Object object, Path file){
        try(FileOutputStream os = new FileOutputStream(file.toFile());
            ObjectOutputStream objOS = new ObjectOutputStream(os)){
            objOS.writeObject(object);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static Object deserialize(Path file){
        Object object=null;
        try(FileInputStream os = new FileInputStream(file.toFile());
            ObjectInputStream objOS = new ObjectInputStream(os)){
            object = objOS.readObject();

        }catch (IOException  | ClassNotFoundException e){
            e.printStackTrace();
        }
        return object;
    }
}
