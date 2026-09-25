package com.gtolib.api.beam;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Beam {
   private static final int MAX_NODES = 256;
   protected final BeamNode initialNode;
   protected final ObjectArrayList<BeamSegment> segments = new ObjectArrayList<>();
   private final List<BeamSegment> segmentsView = Collections.unmodifiableList(this.segments);
   protected final ObjectArrayList<BeamNode> nodes = new ObjectArrayList<>();
   private final List<BeamNode> nodesView = Collections.unmodifiableList(this.nodes);
   private transient Object clientRenderData;

   public Beam(BeamNode initialNode) {
      this.initialNode = initialNode;
   }

   public void rebuildPath(Level level) {
      this.rebuildPath(level, -1);
   }

   void rebuildPath(Level level, int beamId) {
      this.segments.clear();
      this.nodes.clear();
      this.clientRenderData = null;
      BeamNode currentNode = this.initialNode;
      int passIndex = 0;

      while (currentNode != null && currentNode.propertiesSnapshot().intensity > 0L && this.nodes.size() < 256) {
         this.nodes.add(currentNode);
         BeamSegment segment = new BeamSegment(currentNode, level, beamId, passIndex);
         this.segments.add(segment);
         if (segment.passContext != null) {
            passIndex++;
         }

         currentNode = segment.end;
      }
   }

   public BeamNode initialNode() {
      return this.initialNode;
   }

   public List<BeamSegment> segments() {
      return this.segmentsView;
   }

   public List<BeamNode> nodes() {
      return this.nodesView;
   }

   Object clientRenderData() {
      return this.clientRenderData;
   }

   void clientRenderData(Object clientRenderData) {
      this.clientRenderData = clientRenderData;
   }

   void writeToNetwork(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.nodes.size());
      if (this.nodes.isEmpty()) {
         writeNode(buffer, this.initialNode);
      } else {
         this.nodes.forEach(node -> writeNode(buffer, node));
         writeVec3(buffer, this.segments.get(this.segments.size() - 1).endPosition);
      }
   }

   static Beam readFromNetwork(FriendlyByteBuf buffer) {
      int nodeCount = readSize(buffer);
      if (nodeCount == 0) {
         return new Beam(readNode(buffer));
      }

      BeamNode firstNode = readNode(buffer);
      Beam beam = new Beam(firstNode);
      beam.nodes.add(firstNode);

      for (int i = 1; i < nodeCount; i++) {
         beam.nodes.add(readNode(buffer));
      }

      Vec3 terminalEnd = readVec3(buffer);

      for (int i = 0; i < nodeCount; i++) {
         BeamNode end = i + 1 < nodeCount ? beam.nodes.get(i + 1) : null;
         Vec3 endPosition = end == null ? terminalEnd : end.position();
         beam.segments.add(new BeamSegment(beam.nodes.get(i), end, endPosition));
      }

      return beam;
   }

   private static int readSize(FriendlyByteBuf buffer) {
      int size = buffer.readVarInt();
      if (size >= 0 && size <= 256) {
         return size;
      } else {
         throw new IllegalArgumentException("Invalid ray beam path size: " + size);
      }
   }

   private static void writeNode(FriendlyByteBuf buffer, BeamNode node) {
      buffer.writeFloat((float)node.x);
      buffer.writeFloat((float)node.y);
      buffer.writeFloat((float)node.z);
      BeamProperties properties = node.propertiesSnapshot();
      buffer.writeFloat((float)properties.vx);
      buffer.writeFloat((float)properties.vy);
      buffer.writeFloat((float)properties.vz);
      buffer.writeVarLong(properties.intensity);
      buffer.writeVarInt(properties.waveLength);
      buffer.writeFloat(properties.polarization);
   }

   private static BeamNode readNode(FriendlyByteBuf buffer) {
      double x = buffer.readFloat();
      double y = buffer.readFloat();
      double z = buffer.readFloat();
      BeamProperties properties = new BeamProperties();
      properties.vx = buffer.readFloat();
      properties.vy = buffer.readFloat();
      properties.vz = buffer.readFloat();
      properties.intensity = buffer.readVarLong();
      properties.waveLength = buffer.readVarInt();
      properties.polarization = buffer.readFloat();
      return new BeamNode(x, y, z, properties);
   }

   private static void writeVec3(FriendlyByteBuf buffer, Vec3 vec3) {
      buffer.writeFloat((float)vec3.x);
      buffer.writeFloat((float)vec3.y);
      buffer.writeFloat((float)vec3.z);
   }

   private static Vec3 readVec3(FriendlyByteBuf buffer) {
      return new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
   }
}
