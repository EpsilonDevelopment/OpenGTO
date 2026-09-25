package com.gtolib.ae2.crafting2.utils

import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import com.gtolib.api.annotation.DataGeneratorScanned
import com.gtolib.api.annotation.language.RegisterLanguage
import com.gtolib.utils.FluidUtils
import com.gtolib.utils.ItemUtils
import java.util.ArrayList
import java.util.Optional
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.ClickEvent.Action
import net.minecraft.world.entity.player.Player
import org.jetbrains.annotations.NotNull

@DataGeneratorScanned
public class AE2CraftingTranslation {
   @DataGeneratorScanned
   public companion object {
      @RegisterLanguage(en = "Unbuildable: crafting cycle detected with %s nodes", cn = "无法构建：检测到样板循环，共 %s 个节点")
      @NotNull
      public final val TR_CYCLE_MAIN: String

      @RegisterLanguage(en = " -> ", cn = " -> ")
      @NotNull
      public final val TR_CYCLE_INDENT: String

      @RegisterLanguage(en = "[", cn = "[")
      @NotNull
      public final val TR_BRACKET_OPEN: String

      @RegisterLanguage(en = "]", cn = "]")
      @NotNull
      public final val TR_BRACKET_CLOSE: String

      @RegisterLanguage(en = "Please review related patterns; try disabling tool-using or fuzzy patterns.", cn = "请检查相关样板，并尝试关闭使用工具或模糊模式的样板。")
      @NotNull
      public final val TR_CYCLE_FOOTER: String

      @RegisterLanguage(en = "Click to copy: %s", cn = "点击复制：%s")
      @NotNull
      public final val TR_CLICK_TO_COPY: String

      @RegisterLanguage(en = "Calculation too large: max %s, current %s. Forced to loop-iteration mode.", cn = "已强制切换至循环迭代模式。计算复杂度过高：最大 %s，当前 %s。")
      @NotNull
      public final val TR_TOO_LARGE_MAIN: String

      @RegisterLanguage(en = "Input requirement: please remove tools or containers.", cn = "输入检查：请删除工具，容器。")
      @NotNull
      public final val TR_INPUT_REQUIREMENT_MAIN: String

      @RegisterLanguage(
         en = "Self-increase operator detected in final output's crafting path, preventing further crafting.",
         cn = "最终输出的合成路径中存在合成目标为自身的自增算子，无法继续后续合成。"
      )
      @NotNull
      public final val TR_SELF_INCREASE_OPERATOR_MAIN: String

      @RegisterLanguage(en = "Please use the order item, or use this operator (pattern) when requesting other items.", cn = "请使用订单功能，或在请求他物时使用此算子(样板)")
      @NotNull
      public final val TR_SELF_INCREASE_OPERATOR_HELP: String

      public fun ERR_CRAFTING_CYCLE_DETECTED(cycle: List<AEKey>): List<Component> {
         val lines: java.util.List = ArrayList()
         val var11: MutableComponent = Component.translatable(this.TR_CYCLE_MAIN, arrayOf(cycle.size()))
            .withStyle(arrayOf(ChatFormatting.RED, ChatFormatting.BOLD))
            lines.add(var11)

         for (var9 in cycle) {
            val line: MutableComponent = Component.translatable(this.TR_CYCLE_INDENT)
               .withStyle(ChatFormatting.GRAY)
               .append(this.buildItemComponent(var9) as Component)
               lines.add(line)
         }

         if (!cycle.isEmpty()) {
            val var7: MutableComponent = Component.translatable(this.TR_CYCLE_INDENT)
               .withStyle(ChatFormatting.GRAY)
               .append(this.buildItemComponent(CollectionsKt.first(cycle)) as Component)
               lines.add(var7)
         }

         return lines
      }

      public fun ERR_CRAFTING_TOO_LARGE(maxCalculation: Long, nowCalculation: Long, todoAEKey: AEKey): List<Component> {
         val lines: java.util.List = ArrayList()
         val var11: MutableComponent = Component.translatable(this.TR_TOO_LARGE_MAIN, arrayOf(maxCalculation, nowCalculation))
            .withStyle(arrayOf(ChatFormatting.RED, ChatFormatting.BOLD))
            lines.add(var11)
         var var10001: MutableComponent = Component.translatable(this.TR_CYCLE_INDENT)
            .withStyle(ChatFormatting.GRAY)
            .append(this.buildItemComponent(todoAEKey) as Component)
            lines.add(var10001)
         var10001 = Component.translatable(this.TR_CYCLE_FOOTER).withStyle(ChatFormatting.AQUA)
         lines.add(var10001)
         return lines
      }

      public fun ERR_CRAFTING_INPUT_REQUIREMENTS(inputKey: AEKey): List<Component> {
         val lines: java.util.List = ArrayList()
         val var5: MutableComponent = Component.translatable(this.TR_INPUT_REQUIREMENT_MAIN).withStyle(arrayOf(ChatFormatting.RED, ChatFormatting.BOLD))
         lines.add(var5)
         val var10001: MutableComponent = Component.translatable(this.TR_CYCLE_INDENT)
            .withStyle(ChatFormatting.GRAY)
            .append(this.buildItemComponent(inputKey) as Component)
            lines.add(var10001)
         return lines
      }

      public fun ERR_CRAFTING_SELF_INCREASE_OPERATOR(todoAEKey: AEKey): List<Component> {
         val lines: java.util.List = ArrayList()
         var var5: MutableComponent = Component.translatable(this.TR_SELF_INCREASE_OPERATOR_MAIN).withStyle(arrayOf(ChatFormatting.RED, ChatFormatting.BOLD))
         lines.add(var5)
         var5 = Component.translatable(this.TR_SELF_INCREASE_OPERATOR_HELP).withStyle(arrayOf(ChatFormatting.GREEN, ChatFormatting.BOLD))
         lines.add(var5)
         var5 = Component.translatable(this.TR_CYCLE_INDENT).withStyle(ChatFormatting.GRAY).append(this.buildItemComponent(todoAEKey) as Component)
         lines.add(var5)
         return lines
      }

      public fun sendErrorMessage(source: IActionSource?, message: Component) {
         if (source != null) {
            val var10000: Optional = source.player()
            if (var10000.isPresent()) {
               (var10000.get() as Player).sendSystemMessage(message)
            }
         }
      }

      private fun getItemIdString(item: AEKey): String {
         val var10000: java.lang.String
         if (item is AEItemKey) {
            val var3: java.lang.String = ItemUtils.getId((item as AEItemKey).item)
            var10000 = var3
         } else if (item is AEFluidKey) {
            val var4: java.lang.String = FluidUtils.getId((item as AEFluidKey).fluid)
            var10000 = var4
         } else {
            var10000 = item.toString()
         }

         return var10000
      }

      private fun getItemDisplayName(item: AEKey): String {
         var var2: java.lang.String
         try {
            var2 = if (item is AEItemKey)
               (item as AEItemKey).item.getName((item as AEItemKey).toStack()).getString()
               else
               (if (item is AEFluidKey) (item as AEFluidKey).fluid.getFluidType().getDescription().getString() else item.getDisplayName().getString())
               var2 = var2
         } catch (var4: Exception) {
            var2 = this.getItemIdString(item)
         }

         return var2
      }

      private fun buildItemComponent(item: AEKey): MutableComponent {
         val var10000: MutableComponent = this.createClickableItemId(this.getItemIdString(item))
            .append(Component.translatable(this.TR_BRACKET_OPEN).withStyle(ChatFormatting.GRAY) as Component)
            .append(this.createClickableDisplayName(this.getItemDisplayName(item)) as Component)
            .append(Component.translatable(this.TR_BRACKET_CLOSE).withStyle(ChatFormatting.GRAY) as Component)
            return var10000
      }

      private fun createClickableItemId(itemId: String): MutableComponent {
         val var3: MutableComponent = Component.literal(itemId)
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(
               Style.EMPTY
                  .withClickEvent(ClickEvent(Action.COPY_TO_CLIPBOARD, itemId))
                  .withHoverEvent(
                     HoverEvent(
                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(this.TR_CLICK_TO_COPY, arrayOf(itemId)).withStyle(ChatFormatting.GREEN)
                     )
                  )
            )
            return var3
      }

      private fun createClickableDisplayName(displayName: String): MutableComponent {
         val var3: MutableComponent = Component.literal(displayName)
            .withStyle(ChatFormatting.WHITE)
            .withStyle(
               Style.EMPTY
                  .withClickEvent(ClickEvent(Action.COPY_TO_CLIPBOARD, displayName))
                  .withHoverEvent(
                     HoverEvent(
                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(this.TR_CLICK_TO_COPY, arrayOf(displayName)).withStyle(ChatFormatting.GREEN)
                     )
                  )
            )
            return var3
      }
   }
}
