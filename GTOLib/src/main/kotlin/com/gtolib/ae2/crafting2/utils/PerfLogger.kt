package com.gtolib.ae2.crafting2.utils

import java.util.ArrayList
import java.util.HashMap

enum class DisplayLevel {
    MAIN,
    SECONDARY,
    DETAIL,
    NONE;

    fun zhLabel(): String = when (this) {
        MAIN -> "主要"
        SECONDARY -> "次要"
        DETAIL -> "详情"
        NONE -> "不打印输出"
    }
}

class PerfLogger {

    private val roots = ArrayList<Node>()
    private val stack = ArrayList<Node>()

    @Synchronized
    fun push(name: String, displayLevel: DisplayLevel = DisplayLevel.MAIN, vars: Map<String, Any?> = emptyMap()) {
        val node = Node(name, System.nanoTime(), null, stack.size, displayLevel, vars)
        if (stack.isEmpty()) {
            roots.add(node)
        } else {
            stack[stack.size - 1].children.add(node)
        }
        stack.add(node)
    }

    @Synchronized
    fun pop() {
        if (stack.isNotEmpty()) {
            stack.removeAt(stack.size - 1).endNs = System.nanoTime()
        }
    }

    @Synchronized
    fun mark(name: String, displayLevel: DisplayLevel = DisplayLevel.MAIN, vars: Map<String, Any?> = emptyMap()) {
        push(name, displayLevel, vars)
        pop()
    }

    @Synchronized
    fun reset() {
        roots.clear()
        stack.clear()
    }

    @Synchronized
    fun report(maxLevel: DisplayLevel = DisplayLevel.MAIN): String {
        if (maxLevel == DisplayLevel.NONE) {
            return ""
        }

        val sb = StringBuilder()
        val now = System.nanoTime()
        val maxLenByDepth = HashMap<Int, Int>()
        var rootTotalNs = 0L

        fun collectMax(n: Node) {
            val durationNs = (n.endNs ?: now) - n.startNs
            val durationUs = durationNs / 1000.0
            if (n.depth == 0) {
                rootTotalNs += durationNs
            }
            if (n.displayLevel.ordinal <= maxLevel.ordinal) {
                val durStrLen = "%.3f".format(durationUs).length
                if (durStrLen > (maxLenByDepth[n.depth] ?: 0)) {
                    maxLenByDepth[n.depth] = durStrLen
                }
            }
            for (child in n.children) {
                collectMax(child)
            }
        }

        fun appendNode(n: Node, parentDurationNs: Long) {
            val durationNs = (n.endNs ?: System.nanoTime()) - n.startNs
            val durationUs = durationNs / 1000.0
            val indent = "  ".repeat(n.depth)
            val parentPercent = if (n.depth == 0) {
                100.0
            } else if (parentDurationNs > 0L) {
                durationNs.toDouble() / parentDurationNs * 100.0
            } else {
                100.0
            }
            val totalPercent = if (rootTotalNs > 0L) durationNs.toDouble() / rootTotalNs * 100.0 else 100.0
            val parentPctStr = "%.2f%%".format(parentPercent).padStart(6)
            val totalPctStr = "%.2f%%".format(totalPercent).padStart(6)
            val durStr = "%.3f".format(durationUs)
            val durPadded = durStr.padStart(maxLenByDepth[n.depth] ?: durStr.length)
            val varsStr = if (n.vars.isNotEmpty()) {
                n.vars.entries.joinToString(", ", " {", "}") { (k, v) ->
                    "$k=${(v?.toString() ?: "null").replace("\n", "\\n")}"
                }
            } else {
                ""
            }
            val levelLabel = "(${n.displayLevel.zhLabel()})"
            if (n.displayLevel.ordinal <= maxLevel.ordinal) {
                sb.append(
                    "$indent- $parentPctStr $totalPctStr ${n.name.replace("\n", "\\n")} $levelLabel$varsStr — $durPadded µs\n"
                )
            }
            for (child in n.children) {
                appendNode(child, durationNs)
            }
        }

        for (root in roots) {
            collectMax(root)
        }
        for (root in roots) {
            appendNode(root, (root.endNs ?: now) - root.startNs)
        }

        return sb.toString()
    }

    private data class Node(
        val name: String,
        val startNs: Long,
        var endNs: Long? = null,
        val depth: Int,
        val displayLevel: DisplayLevel,
        val vars: Map<String, Any?> = emptyMap(),
        val children: ArrayList<Node> = ArrayList()
    )

    companion object {
        var destroy = false
    }
}
