package com.gtolib.api;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import net.minecraft.ChatFormatting;

@DataGeneratorScanned
public final class GTOValues {
   public static final String GALAXY = "g";
   public static final String DYSON_LIST = "l";
   public static final String DYSON_COUNT = "c";
   public static final String DYSON_DAMAGE = "d";
   public static final String DYSON_USE = "u";
   public static final String BIG_CELL_LIST = "b";
   public static final String CELL_LIST = "l";
   public static final String CELL_UUID = "u";
   public static final String CELL_DATA = "d";
   public static final String CELL_KEYS = "k";
   public static final String CELL_AMOUNTS = "a";
   public static final String WIRELESS_ENERGY_UUID = "u";
   public static final String WIRELESS_ENERGY_STORAGE = "s";
   public static final String WIRELESS_ENERGY_CAPACITY = "c";
   public static final String WIRELESS_ENERGY_RATE = "r";
   public static final String WIRELESS_ENERGY_LOSS = "l";
   public static final String WIRELESS_ENERGY_DIMENSION = "d";
   public static final String WIRELESS_ENERGY_POS = "p";
   public static final String PLAYER_LIST = "l";
   public static final String PLAYER_UUID = "u";
   public static final String PLANET_LIST = "p";
   public static final String PLANET_NAME = "n";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Stellar Container Tier: %s", cn = "恒星热力容器等级：%s")
   public static final String STELLAR_CONTAINMENT_TIER = "stellar_container";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Power Module Tier: %s", cn = "动力模块等级：%s")
   public static final String POWER_MODULE_TIER = "power_module";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Casing Tier: %s", cn = "外壳等级：%s")
   public static final String COMPONENT_ASSEMBLY_CASING_TIER = "casing";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Glass Tier: %s", cn = "玻璃等级：%s")
   public static final String GLASS_TIER = "glass";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Machine Casing Tier: %s", cn = "机器外壳等级：%s")
   public static final String MACHINE_CASING_TIER = "machine_casing";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Graviton Flow Tier: %s", cn = "引力流等级：%s")
   public static final String GRAVITON_FLOW_TIER = "graviton_flow";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Integral Framework Tier: %s", cn = "整体框架等级：%s")
   public static final String INTEGRAL_FRAMEWORK_TIER = "integral_framework";
   public static final String COMPUTER_CASING_TIER = "computer_casing";
   public static final String COMPUTER_HEAT_TIER = "computer_heat";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Energy Control Module Tier: %s", cn = "能量控制模块等级：%s")
   public static final String ENERGY_CONTROL_MODULE_TIER = "energy_module";
   @RegisterLanguage(valuePrefix = "gtocore.tier", en = "Machining Control Module Tier: %s", cn = "运行控制模块等级：%s")
   public static final String MACHINING_CONTROL_MODULE_TIER = "machining_control_module";
   @RegisterLanguage(en = "Removing ash...", cn = "掏灰中...")
   public static final String REMOVING_ASH = "gtocore.drone.removing_ash";
   @RegisterLanguage(en = "Maintaining...", cn = "维护中...")
   public static final String MAINTAINING = "gtocore.drone.maintaining";
   public static final int[] MANA = new int[]{2, 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608, 33554432, 134217728};
   public static final long[] VAEX = new long[]{
      7L,
      30L,
      120L,
      480L,
      1920L,
      7680L,
      30720L,
      122880L,
      491520L,
      1966080L,
      7864320L,
      31457280L,
      125829120L,
      503316480L,
      2013265920L,
      8053063680L,
      32212254720L,
      128849018880L,
      515396075520L,
      2061584302080L,
      8246337208320L,
      32985348833280L,
      131941395333120L,
      527765581332480L,
      2111062325329920L,
      8444249301319680L,
      33776997205278720L,
      135107988821114880L,
      540431955284459520L,
      2161727821137838080L,
      8646911284551352319L
   };
   public static final String[] MANAN = new String[]{
      "Primitive",
      "Enlighten",
      "Apprentice",
      "Elemental",
      "Sorcerer",
      "Arcane",
      "Magisters",
      "Mysteries",
      "Philosopher",
      "Regards",
      "Awaken",
      "Prosperity",
      "End",
      "Perpetual"
   };
   public static final String[] MANACN = new String[]{"原始", "启蒙", "学徒", "精灵", "法师", "奥术", "导师", "秘仪", "贤者", "启示", "觉醒", "黄金", "终焉", "永恒"};
   public static final String[] VLVHCN = new String[]{
      "原始",
      "基础",
      ChatFormatting.AQUA + "进阶",
      ChatFormatting.GOLD + "进阶",
      ChatFormatting.DARK_PURPLE + "进阶",
      ChatFormatting.BLUE + "精英",
      ChatFormatting.LIGHT_PURPLE + "精英",
      ChatFormatting.RED + "精英",
      ChatFormatting.DARK_AQUA + "终极",
      ChatFormatting.DARK_RED + "史诗",
      ChatFormatting.GREEN + "史诗",
      ChatFormatting.DARK_GREEN + "史诗",
      ChatFormatting.YELLOW + "史诗",
      ChatFormatting.BLUE.toString() + ChatFormatting.BOLD + "传奇",
      ChatFormatting.RED.toString() + ChatFormatting.BOLD + "MAX"
   };
   public static final String[] VOLTAGE_NAMESCN = new String[]{
      "超低压", "低压", "中压", "高压", "超高压", "强导压", "剧差压", "零点压", "极限压", "极高压", "极超压", "极巨压", "超极限压", "过载压", "终压"
   };
   public static final String[] VNFR = new String[]{
      ChatFormatting.DARK_GRAY + "ULV" + ChatFormatting.RESET,
      ChatFormatting.GRAY + "LV" + ChatFormatting.RESET,
      ChatFormatting.AQUA + "MV" + ChatFormatting.RESET,
      ChatFormatting.GOLD + "HV" + ChatFormatting.RESET,
      ChatFormatting.DARK_PURPLE + "EV" + ChatFormatting.RESET,
      ChatFormatting.BLUE + "IV" + ChatFormatting.RESET,
      ChatFormatting.LIGHT_PURPLE + "LuV" + ChatFormatting.RESET,
      ChatFormatting.RED + "ZPM" + ChatFormatting.RESET,
      ChatFormatting.DARK_AQUA + "UV" + ChatFormatting.RESET,
      ChatFormatting.DARK_RED + "UHV" + ChatFormatting.RESET,
      ChatFormatting.GREEN + "UEV" + ChatFormatting.RESET,
      ChatFormatting.DARK_GREEN + "UIV" + ChatFormatting.RESET,
      ChatFormatting.YELLOW + "UXV" + ChatFormatting.RESET,
      ChatFormatting.BLUE.toString() + ChatFormatting.BOLD + "OpV" + ChatFormatting.RESET,
      ChatFormatting.RED.toString() + ChatFormatting.BOLD + "MAX" + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "1"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "2"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "3"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "4"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "5"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "6"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "7"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "8"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "9"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "10"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "11"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "12"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "13"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "14"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "15"
         + ChatFormatting.RESET,
      ChatFormatting.RED.toString()
         + ChatFormatting.BOLD
         + "M"
         + ChatFormatting.GREEN
         + ChatFormatting.BOLD
         + "A"
         + ChatFormatting.BLUE
         + ChatFormatting.BOLD
         + "X"
         + ChatFormatting.YELLOW
         + ChatFormatting.BOLD
         + "+"
         + ChatFormatting.RED
         + ChatFormatting.BOLD
         + "16"
         + ChatFormatting.RESET
   };
}
