package com.gtolib.utils;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class ChineseConverter {
   private static final Map<String, String> phraseFixes = new LinkedHashMap<>();

   private ChineseConverter() {
   }

   public static String convert(String text) {
      String convertedText = ZhConverterUtil.toTraditional(text);

      for (Entry<String, String> entry : phraseFixes.entrySet()) {
         convertedText = convertedText.replace(entry.getKey(), entry.getValue());
      }

      return convertedText;
   }

   static {
      phraseFixes.put("服務器", "伺服器");
      phraseFixes.put("鼠標", "滑鼠");
      phraseFixes.put("默認", "預設");
      phraseFixes.put("創建", "建立");
      phraseFixes.put("設置", "設定");
      phraseFixes.put("鏈接", "連結");
      phraseFixes.put("網絡", "網路");
      phraseFixes.put("信息", "資訊");
      phraseFixes.put("圖標", "圖示");
      phraseFixes.put("文件", "檔案");
      phraseFixes.put("激活", "啟用");
      phraseFixes.put("空閒", "閒置");
      phraseFixes.put("導出", "匯出");
      phraseFixes.put("導入", "匯入");
      phraseFixes.put("概率", "機率");
      phraseFixes.put("界面", "介面");
      phraseFixes.put("硅巖", "矽岩");
      phraseFixes.put("處理器集羣", "處理器叢集");
      phraseFixes.put("處理器超級計算機", "處理器超級電腦");
      phraseFixes.put("瞭", "了");
      phraseFixes.put("硅", "矽");
      phraseFixes.put("杆", "桿");
      phraseFixes.put("臺", "台");
      phraseFixes.put("巖", "岩");
      phraseFixes.put("併行", "並行");
      phraseFixes.put("併為", "並為");
      phraseFixes.put("超淨間", "無塵室");
      phraseFixes.put("超淨", "無塵");
      phraseFixes.put("纳米", "奈米");
      phraseFixes.put("末地", "終界");
      phraseFixes.put("下界合金", "獄髓");
      phraseFixes.put("下界", "地獄");
      phraseFixes.put("信標", "烽火台");
      phraseFixes.put("末影人", "終界使者");
      phraseFixes.put("末影", "終界");
      phraseFixes.put("烈焰人", "烈焰使者");
      phraseFixes.put("凋靈", "凋零");
      phraseFixes.put("納米", "奈米");
      phraseFixes.put("爲", "為");
      phraseFixes.put("啓", "啟");
      phraseFixes.put("着", "著");
      phraseFixes.put("羣", "群");
      phraseFixes.put("祕", "秘");
      phraseFixes.put("裏", "裡");
      phraseFixes.put("污", "汙");
      phraseFixes.put("纔", "才");
      phraseFixes.put("麪", "麵");
      phraseFixes.put("喫", "吃");
      phraseFixes.put("峯", "峰");
      phraseFixes.put("僞", "偽");
      phraseFixes.put("泄", "洩");
      phraseFixes.put("牀", "床");
      phraseFixes.put("皁", "皂");
      phraseFixes.put("衆", "眾");
   }
}
