package com.gtolib.cache.resourcestree.node;

import com.gtolib.utils.iostream.DataIOStream;
import java.io.IOException;

public class SaveableFileNode extends CachedFileNode {
   @Override
   public void write(DataIOStream dos, boolean cached) throws IOException {
      if (cached && this.data != null) {
         int len = this.data.length;
         if (len > 0) {
            dos.writeVarInt(-len);
            dos.write(this.data);
            return;
         }
      }

      dos.writeByte(0);
   }
}
