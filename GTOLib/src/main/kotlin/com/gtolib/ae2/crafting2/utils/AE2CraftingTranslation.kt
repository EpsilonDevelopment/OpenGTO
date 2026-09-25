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
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.ClickEvent.Action
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

@DataGeneratorScanned
class AE2CraftingTranslation {

    @DataGeneratorScanned
    companion object {

        @RegisterLanguage(en = "Unbuildable: crafting cycle detected with %s nodes", cn = "无法构建：检测到样板循环，共 %s 个节点")
        val TR_CYCLE_MAIN = "gtocore.crafting2.translation.cycle.main"

        @RegisterLanguage(en = " -> ", cn = " -> ")
        val TR_CYCLE_INDENT = "gtocore.crafting2.translation.cycle.indent"

        @RegisterLanguage(en = "[", cn = "[")
        val TR_BRACKET_OPEN = "gtocore.crafting2.translation.common.bracket_open"

        @RegisterLanguage(en = "]", cn = "]")
        val TR_BRACKET_CLOSE = "gtocore.crafting2.translation.common.bracket_close"

        @RegisterLanguage(en = "Please review related patterns; try disabling tool-using or fuzzy patterns.", cn = "请检查相关样板，并尝试关闭使用工具或模糊模式的样板。")
        val TR_CYCLE_FOOTER = "gtocore.crafting2.translation.cycle.footer"

        @RegisterLanguage(en = "Click to copy: %s", cn = "点击复制：%s")
        val TR_CLICK_TO_COPY = "gtocore.crafting2.translation.common.click_to_copy"

        @RegisterLanguage(en = "Calculation too large: max %s, current %s. Forced to loop-iteration mode.", cn = "已强制切换至循环迭代模式。计算复杂度过高：最大 %s，当前 %s。")
        val TR_TOO_LARGE_MAIN = "gtocore.crafting2.translation.too_large.main"

        @RegisterLanguage(en = "Input requirement: please remove tools or containers.", cn = "输入检查：请删除工具，容器。")
        val TR_INPUT_REQUIREMENT_MAIN = "gtocore.crafting2.translation.input.requirement.main"

        @RegisterLanguage(
            en = "Self-increase operator detected in final output's crafting path, preventing further crafting.",
            cn = "最终输出的合成路径中存在合成目标为自身的自增算子，无法继续后续合成。"
        )
        val TR_SELF_INCREASE_OPERATOR_MAIN = "gtocore.crafting2.translation.self_increase_operator.main"

        @RegisterLanguage(en = "Please use the order item, or use this operator (pattern) when requesting other items.", cn = "请使用订单功能，或在请求他物时使用此算子(样板)")
        val TR_SELF_INCREASE_OPERATOR_HELP = "gtocore.crafting2.translation.self_increase_operator.help"

        fun ERR_CRAFTING_CYCLE_DETECTED(cycle: List<AEKey>): List<Component> {
            val lines = ArrayList<Component>()
            lines.add(
                Component.translatable(TR_CYCLE_MAIN, cycle.size)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            )
            for (key in cycle) {
                lines.add(
                    Component.translatable(TR_CYCLE_INDENT)
                        .withStyle(ChatFormatting.GRAY)
                        .append(buildItemComponent(key))
                )
            }
            if (cycle.isNotEmpty()) {
                lines.add(
                    Component.translatable(TR_CYCLE_INDENT)
                        .withStyle(ChatFormatting.GRAY)
                        .append(buildItemComponent(cycle.first()))
                )
            }
            return lines
        }

        fun ERR_CRAFTING_TOO_LARGE(maxCalculation: Long, nowCalculation: Long, todoAEKey: AEKey): List<Component> {
            val lines = ArrayList<Component>()
            lines.add(
                Component.translatable(TR_TOO_LARGE_MAIN, maxCalculation, nowCalculation)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            )
            lines.add(
                Component.translatable(TR_CYCLE_INDENT)
                    .withStyle(ChatFormatting.GRAY)
                    .append(buildItemComponent(todoAEKey))
            )
            lines.add(Component.translatable(TR_CYCLE_FOOTER).withStyle(ChatFormatting.AQUA))
            return lines
        }

        fun ERR_CRAFTING_INPUT_REQUIREMENTS(inputKey: AEKey): List<Component> {
            val lines = ArrayList<Component>()
            lines.add(
                Component.translatable(TR_INPUT_REQUIREMENT_MAIN)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            )
            lines.add(
                Component.translatable(TR_CYCLE_INDENT)
                    .withStyle(ChatFormatting.GRAY)
                    .append(buildItemComponent(inputKey))
            )
            return lines
        }

        fun ERR_CRAFTING_SELF_INCREASE_OPERATOR(todoAEKey: AEKey): List<Component> {
            val lines = ArrayList<Component>()
            lines.add(
                Component.translatable(TR_SELF_INCREASE_OPERATOR_MAIN)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            )
            lines.add(
                Component.translatable(TR_SELF_INCREASE_OPERATOR_HELP)
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
            )
            lines.add(
                Component.translatable(TR_CYCLE_INDENT)
                    .withStyle(ChatFormatting.GRAY)
                    .append(buildItemComponent(todoAEKey))
            )
            return lines
        }

        fun sendErrorMessage(source: IActionSource?, message: Component) {
            if (source != null) {
                val player = source.player()
                if (player.isPresent) {
                    player.get().sendSystemMessage(message)
                }
            }
        }

        private fun getItemIdString(item: AEKey): String = when (item) {
            is AEItemKey -> ItemUtils.getId(item.item)
            is AEFluidKey -> FluidUtils.getId(item.fluid)
            else -> item.toString()
        }

        private fun getItemDisplayName(item: AEKey): String = try {
            when (item) {
                is AEItemKey -> item.item.getName(item.toStack()).string
                is AEFluidKey -> item.fluid.fluidType.description.string
                else -> item.displayName.string
            }
        } catch (e: Exception) {
            getItemIdString(item)
        }

        private fun buildItemComponent(item: AEKey): MutableComponent = createClickableItemId(getItemIdString(item))
            .append(Component.translatable(TR_BRACKET_OPEN).withStyle(ChatFormatting.GRAY))
            .append(createClickableDisplayName(getItemDisplayName(item)))
            .append(Component.translatable(TR_BRACKET_CLOSE).withStyle(ChatFormatting.GRAY))

        private fun createClickableItemId(itemId: String): MutableComponent = Component.literal(itemId)
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent(Action.COPY_TO_CLIPBOARD, itemId))
                    .withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.translatable(TR_CLICK_TO_COPY, itemId).withStyle(ChatFormatting.GREEN)
                        )
                    )
            )

        private fun createClickableDisplayName(displayName: String): MutableComponent = Component.literal(displayName)
            .withStyle(ChatFormatting.WHITE)
            .withStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent(Action.COPY_TO_CLIPBOARD, displayName))
                    .withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.translatable(TR_CLICK_TO_COPY, displayName).withStyle(ChatFormatting.GREEN)
                        )
                    )
            )
    }
}
