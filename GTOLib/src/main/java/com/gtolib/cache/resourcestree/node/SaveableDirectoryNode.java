package com.gtolib.cache.resourcestree.node;

import com.gtolib.ae2.crafting2.utils.PerfLogger;
import com.gtolib.utils.iostream.DataIOStream;
import java.io.IOException;
import java.util.Map.Entry;

public class SaveableDirectoryNode extends DirectoryNode implements SaveableNode {
   public static int paths;

   @Override
   public void write(DataIOStream dos, boolean cached) throws IOException {
      dos.writeVarInt(this.path.size());

      for (Entry<String, Node> entry : this.path.entrySet()) {
         dos.writeUTF(entry.getKey());
         SaveableNode node = (SaveableNode)entry.getValue();
         node.write(dos, cached);
      }
   }

   static {
      new PerfLogger();
   }
}
