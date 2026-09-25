package com.gtolib.api;

import com.gregtechceu.gtceu.utils.Event;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiPluginContainer;
import java.util.Set;
import java.util.function.Consumer;
import mezz.jei.api.IModPlugin;
import net.minecraft.world.item.Item;

public final class GTOApi {
   public static final Event<Consumer<EmiPluginContainer>> EMI_PLUGIN_EVENT = Event.create();
   public static final Event<Consumer<IModPlugin>> JEI_PLUGIN_EVENT = Event.create();
   public static final Event<Set<Item>> EMI_HIDE_ITEM_EVENT = Event.createRegister();
   public static final Event<Set<EmiStack>> EMI_ADD_STACK_EVENT = Event.createRegister();

   private GTOApi() {
   }
}
