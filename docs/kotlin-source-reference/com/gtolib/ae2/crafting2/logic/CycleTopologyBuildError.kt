package com.gtolib.ae2.crafting2.logic

import com.gtolib.ae2.crafting2.model.ComputingComponent
import net.minecraft.network.chat.Component

public class CycleTopologyBuildError(message: Component, cycle: List<ComputingComponent>) : RuntimeException(message.getString())
