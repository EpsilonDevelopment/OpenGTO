package com.gtolib.utils.register;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.api.lang.CNEN;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public final class RecipeTypeRegisterUtils {
   public static final String MAGIC = "magic";
   public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
   public static final Function<GTRecipeDefinition, String> TEMPERATURE = data -> LocalizationUtils.format(
      "gtceu.recipe.temperature", FormattingUtil.formatNumbers(data.data.getInt(GTRecipeDataKeys.EBF_TEMP))
   );
   public static final Function<GTRecipeDefinition, String> COIL = data -> {
      ICoilType requiredCoil = ICoilType.getMinRequiredType(data.data.getInt(GTRecipeDataKeys.EBF_TEMP));
      return requiredCoil != null && requiredCoil.getMaterial() != null
         ? LocalizationUtils.format("gtceu.recipe.coil.tier", I18n.get(requiredCoil.getMaterial().getUnlocalizedName()))
         : "";
   };
   public static final BiConsumer<GTRecipeDefinition, WidgetGroup> COIL_UI = (recipe, widgetGroup) -> {
      int temp = recipe.data.getInt(GTRecipeDataKeys.EBF_TEMP);
      List<List<ItemStack>> items = new ArrayList<>();
      items.add(
         GTCEuAPI.HEATING_COILS
            .entrySet()
            .stream()
            .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
            .map(coil -> new ItemStack(coil.getValue().get()))
            .toList()
      );
      widgetGroup.addWidget(
         new SlotWidget(new CycleItemStackHandler(items), 0, widgetGroup.getSize().width - 50, widgetGroup.getSize().height - 40, false, false)
      );
   };

   private RecipeTypeRegisterUtils() {
   }

   public static com.gtolib.api.recipe.RecipeType register(String name, String enLang, String cnLang, String type) {
      if (LANG != null) {
         LANG.put(name, new CNEN(cnLang, enLang));
      }

      return register(name, type);
   }

   public static com.gtolib.api.recipe.RecipeType register(String name, String cnLang, String type) {
      if (LANG != null) {
         LANG.put(name, new CNEN(cnLang, FormattingUtil.toEnglishName(name)));
      }

      return register(name, type);
   }

   public static com.gtolib.api.recipe.RecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
      com.gtolib.api.recipe.RecipeType recipeType = new com.gtolib.api.recipe.RecipeType(GTCEu.id(name), group, proxyRecipes);
      GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
      return recipeType;
   }
}
