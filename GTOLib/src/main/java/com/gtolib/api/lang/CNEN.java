package com.gtolib.api.lang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

@DataGeneratorScanned
public record CNEN(String cn, String en) {
   @RegisterLanguage(cn = "拆除模式", en = "Demolition Mode")
   public static final String DEMOLITION = "gtocore.auto_build.demolition_mode";
   @RegisterLanguage(cn = "主结构搭建", en = "Main Structure Build")
   public static final String MAIN = "gtocore.auto_build.main";
   @RegisterLanguage(cn = "模块搭建", en = "Module Build")
   public static final String MODULE = "gtocore.auto_build.module";
   @RegisterLanguage(cn = "镜像搭建", en = "Mirror Build")
   public static final String FLIP = "gtocore.auto_build.flip";
   @RegisterLanguage(cn = "替换模式", en = "Replace Mode")
   public static final String REPLACE = "gtocore.auto_build.replace";
   @RegisterLanguage(cn = "替换等级方块为设置的等级方块", en = "Replace Tier Block with the set block")
   public static final String REPLACE_A = "gtocore.auto_build.replace.a";
   @RegisterLanguage(cn = "等级方块", en = "Tiered Block")
   public static final String TIER = "gtocore.auto_build.tier";

   public static CNEN create(String cn, String en) {
      return new CNEN(cn, en);
   }
}
