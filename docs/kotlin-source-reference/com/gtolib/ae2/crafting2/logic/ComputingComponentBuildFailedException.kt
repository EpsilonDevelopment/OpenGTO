package com.gtolib.ae2.crafting2.logic

import appeng.api.crafting.IPatternDetails
import com.gtolib.ae2.crafting2.model.ComputingComponent
import net.minecraft.network.chat.Component

public class ComputingComponentBuildFailedException(message: Component,
   detail: IPatternDetails? = null,
   cycle: List<ComputingComponent>? = null,
   cause: Throwable? = null
) : RuntimeException(message.getString(), cause)
