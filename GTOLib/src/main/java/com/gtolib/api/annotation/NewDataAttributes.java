package com.gtolib.api.annotation;

import com.gtolib.api.annotation.component_builder.ComponentBuilder;
import com.gtolib.api.annotation.component_builder.ComponentTemplate;
import com.gtolib.api.annotation.component_builder.StyleBuilder;
import net.minecraft.network.chat.Component;

public final class NewDataAttributes {
   public static final String PREFIX = "gtocore.lang";
   public static final String PREFIX_TEMPLATE = "gtocore.lang.template";
   private static final Component PREFIX_BAR = ComponentBuilder.create().addLines("- ", StyleBuilder::setGold).buildSingle();
   private static final Component PREFIX_POINT = ComponentBuilder.create().addLines("৹ ", StyleBuilder::setGold).buildSingle();
   public static final Component PREFIX_STAR = ComponentBuilder.create().addLines("☆", StyleBuilder::setGold).buildSingle();
   public static final Component PREFIX_TAB_POINT = ComponentBuilder.create().addLines("  ৹ ", StyleBuilder::setGold).buildSingle();
   public static final Component PREFIX_TAB_STAR = ComponentBuilder.create().addLines("  ☆", StyleBuilder::setGold).buildSingle();
   public static final Component PREFIX_TAB = ComponentBuilder.create().addLines("  ", StyleBuilder::setGold).buildSingle();
   public static final ComponentTemplate ALLOW_PARALLEL = new ComponentTemplate(
         "allow_parallel", "可并行 : %s", "Parallel : %s", styleOp -> styleOp.setColor(2003199).setPrefix(PREFIX_BAR), "✔"
      )
      .setComment(
         componentBuilder -> componentBuilder.addCommentLines(
            "允许安装并行控制仓实现并行处理", "Allows the installation of parallel control hatches to achieve parallel processing"
         )
      );
   public static final ComponentTemplate EMPTY_WITH_BAR = new ComponentTemplate(
      "empty_with_bar", "%s", "%s", styleBuilder -> styleBuilder.setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate EMPTY_WITH_TAB = new ComponentTemplate(
      "empty_with_tab", "%s", "%s", styleBuilder -> styleBuilder.setPrefix(PREFIX_TAB)
   );
   public static final ComponentTemplate TIME_COST_MULTIPLY = new ComponentTemplate(
      "time_cost_multiply", "时间乘数 : %s", "Time Cost Multiply : %s", styleBuilder -> styleBuilder.setPrefix(PREFIX_BAR).setColor(6591981)
   );
   public static final ComponentTemplate ENERGY_COST_MULTIPLY = new ComponentTemplate(
      "energy_cost_multiply", "能量乘数 : %s", "Energy Cost Multiply : %s", styleBuilder -> styleBuilder.setPrefix(PREFIX_BAR).setColor(8179068)
   );
   public static final ComponentTemplate EMPTY_WITH_POINT = new ComponentTemplate(
      "empty_with_point", "%s", "%s", styleBuilder -> styleBuilder.setPrefix(PREFIX_POINT)
   );
   public static final ComponentTemplate EMPTY_WITH_NON = new ComponentTemplate("empty_with_non", "%s", "%s", styleBuilder -> styleBuilder);
   public static final ComponentTemplate MULTIPLY = new ComponentTemplate(
      "multiply", "乘数 : %s", "Multiply : %s", style -> style.setColor(14315734).setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate MAIN_FUNCTION = new ComponentTemplate(
      "main_function", "主功能 : %s", "Main Function : %s", styleBuilder -> styleBuilder.setGold().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate CAPACITY = new ComponentTemplate(
      "capacity", "容量 : %s", "Capacity : %s", styleBuilder -> styleBuilder.setLightPurple().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate CURRENT = new ComponentTemplate(
      "current", "电流 : %s A", "Current : %s A", style -> style.setColor(50637).setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate VOLTAGE = new ComponentTemplate(
      "voltage", "电压 : %s", "Voltage : %s", style -> style.setColor(13789470).setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate RUNTIME_REQUIREMENT = new ComponentTemplate(
      "runtime_requirement", "运行要求 : %s", "Runtime Requirement : %s", styleBuilder -> styleBuilder.setYellow().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate MIRACULOUS_TOOLS = new ComponentTemplate(
      "miraculous_tools", "妙妙工具 : %s", "Wonderful Tools : %s", styleBuilder -> styleBuilder.setGold().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate NOT_ALLOW_SHARED = new ComponentTemplate(
         "not_allow_shared", "可共享 : %s", "Shared : %s", style -> style.setColor(16753920).setPrefix(PREFIX_BAR), "✘"
      )
      .setComment(
         componentBuilder -> componentBuilder.addCommentLines(
            "代表此仓室能否被多台多方块结构共享", "Represents whether this hatch can be shared by multiple multi-block structures"
         )
      );
   public static final ComponentTemplate ALLOW_PARALLEL_SPECIAL = new ComponentTemplate(
         "allow_parallel_special", "可特殊并行 : %s", "Special Parallel : %s", style -> style.setColor(2003199).setPrefix(PREFIX_BAR), "✔"
      )
      .setComment(
         componentBuilder -> componentBuilder.addCommentLines("机器自带并行机制，无需安装并行控制仓", "Built-in parallel processing, no parallel control hatch required")
      );
   public static final ComponentTemplate ALLOW_PARALLEL_NUMBER = new ComponentTemplate(
      "allow_parallel_number", "并行数 : %s", "Parallel Number : %s", style -> style.setColor(6749952).setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate ALLOW_MULTI_RECIPE_PARALLEL = new ComponentTemplate(
         "allow_multi_recipe_parallel", "多线程 : %s", "Multi-threading : %s", styleBuilder -> styleBuilder.setBlinkingCyan().setPrefix(PREFIX_BAR), "✔"
      )
      .setComment(
         componentBuilder -> componentBuilder.addCommentLines(
            "允许安装线程仓实现多线程配方并行处理。\n难以想象的工作效率，尽管去做吧！",
            "Allows the installation of thread hatches to achieve multi-threaded recipe parallel processing.\nImagine the unimaginable work efficiency, go ahead and do it!"
         )
      );
   public static final ComponentTemplate PREFECT_OVERCLOCK = new ComponentTemplate(
      "perfect_overclock", "无损超频 : %s", "Perfect Overclock : %s", styleBuilder -> styleBuilder.setBlinkingRed().setPrefix(PREFIX_BAR), "✔"
   );
   public static final ComponentTemplate LASER_LOSS_OVERCLOCKING = new ComponentTemplate(
         "lossy_overclock", "超损超频！", "Lossy Overclock!", styleBuilder -> styleBuilder.setRed().setPrefix(PREFIX_BAR)
      )
      .setComment(b -> b.addCommentLines("机器功率每乘以4，耗时×65%", "For every 4 times increase in power, the time cost is multiplied by 65%"));
   public static final ComponentTemplate LASER_ENERGY_HATCH = new ComponentTemplate(
         "laser_energy_hatch", "激光仓 : %s", "Laser Energy Hatch : %s", styleBuilder -> styleBuilder.setBlinkingOrange().setPrefix(PREFIX_BAR), "✔"
      )
      .setComment(b -> b.addCommentLines("激光仓一般可以提供巨大的能量", "Laser Energy Hatches generally provide massive amounts of energy"));
   public static final ComponentTemplate RECIPES_TYPE = new ComponentTemplate(
      "recipes_type", "配方类型 : %s", "Recipes Type : %s", styleBuilder -> styleBuilder.setYellow().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate LEVEL = new ComponentTemplate(
      "level", "等级 : %s", "Tier : %s", styleBuilder -> styleBuilder.setGreen().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate RECIPE_LEVEL = new ComponentTemplate(
      "recipe_level", "配方等级 : %s", "Recipe Tier : %s", styleBuilder -> styleBuilder.setAqua().setPrefix(PREFIX_BAR)
   );
   public static final ComponentTemplate ALLOW_MODULE = new ComponentTemplate(
      "allow_module", "附属模块 : %s", "Auxiliary Module : %s", styleBuilder -> styleBuilder.setOrange().setPrefix(PREFIX_BAR), "✔"
   );
   public static final ComponentTemplate WORKABLE_IN_SPACE = new ComponentTemplate(
         "workable_in_space", "可以在太空工作 : %s", "Workable in Space : %s", styleBuilder -> styleBuilder.setColor(2003199).setPrefix(PREFIX_BAR), "✔"
      )
      .setComment(
         componentBuilder -> componentBuilder.addCommentLines(
            "该机器可以在含有全真空、射线暴露、失重等的复杂太空环境下工作。\n无需空间站等提供的额外环境支持",
            "This machine can work in complex space environments such as vacuum, radiation exposure, and weightlessness.\nNo additional environmental support is required, such as space stations"
         )
      );
}
