package com.gtolib.api.gui;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gtocore.api.gui.GTOGuiTextures;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class ParallelConfigurator implements IFancyConfigurator {
   private final IParallelMachine machine;

   public ParallelConfigurator(IParallelMachine machine) {
      this.machine = machine;
   }

   @Override
   public Component getTitle() {
      return Component.translatable("gtceu.machine.parallel_hatch.display");
   }

   @Override
   public IGuiTexture getIcon() {
      return GTOGuiTextures.PARALLEL_CONFIG;
   }

   @Override
   public Widget createConfigurator() {
      WidgetGroup group = new WidgetGroup(0, 0, 100, 20);
      var longInput = new LongInputWidget(this.machine::getParallel, this.machine::setParallel) {
         @Override
         public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            buffer.writeVarLong(ParallelConfigurator.this.machine.getMaxParallel());
            buffer.writeVarLong(ParallelConfigurator.this.machine.getMinParallel());
            this.setMax(ParallelConfigurator.this.machine.getMaxParallel());
            this.setMin(ParallelConfigurator.this.machine.getMinParallel());
         }

         @OnlyIn(Dist.CLIENT)
         @Override
         public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            this.setMax(buffer.readVarLong());
            this.setMin(buffer.readVarLong());
         }

         @Override
         public void detectAndSendChanges() {
            super.detectAndSendChanges();
            this.writeUpdateInfo(0, buf -> buf.writeVarLong(ParallelConfigurator.this.machine.getParallel()));
         }

         @OnlyIn(Dist.CLIENT)
         @Override
         public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            super.readUpdateInfo(id, buffer);
            if (id == 0) {
               ParallelConfigurator.this.machine.setParallel(buffer.readVarLong());
            }
         }
      };
      group.addWidget(longInput);
      return group;
   }
}
