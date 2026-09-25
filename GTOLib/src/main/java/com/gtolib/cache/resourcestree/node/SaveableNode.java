package com.gtolib.cache.resourcestree.node;

import com.gtolib.utils.iostream.DataIOStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

public interface SaveableNode extends Node {
   void write(DataIOStream var1, boolean var2) throws IOException;

   static SaveableNode read(DataIOStream dis, Supplier<FileNode> fileNodeSupplier) {
      try {
         int size = dis.readVarInt();
         if (size == 0) {
            return fileNodeSupplier.get();
         }

         HashMap<String, Node> map = new HashMap<>(size);

         for (int i = 0; i < size; i++) {
            String key = dis.readUTF();
            SaveableNode value = read(dis, fileNodeSupplier);
            map.put(value instanceof DirectoryNode ? key.intern() : key, value);
         }

         SaveableDirectoryNode node = new SaveableDirectoryNode();
         node.path = map;
         return node;
      } catch (IOException e) {
         return null;
      }
   }

   static SaveableNode readSaveable(DataIOStream dis) {
      try {
         int size = dis.readVarInt();
         if (size <= 0) {
            SaveableFileNode node = new SaveableFileNode();
            if (size < 0) {
               byte[] data = new byte[-size];
               dis.readFully(data);
               node.data = data;
            }

            return node;
         } else {
            HashMap<String, Node> map = new HashMap<>(size);

            for (int i = 0; i < size; i++) {
               String key = dis.readUTF();
               SaveableNode value = readSaveable(dis);
               map.put(value instanceof DirectoryNode ? key.intern() : key, value);
            }

            SaveableDirectoryNode node = new SaveableDirectoryNode();
            node.path = map;
            return node;
         }
      } catch (IOException e) {
         return null;
      }
   }
}
