@file:SourceDebugExtension(["SMAP\nFlexibleContainerDsl.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FlexibleContainerDsl.kt\ncom/gtolib/api/gui/ktflexible/FlexibleContainerDslKt\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,93:1\n1#2:94\n*E\n"])

package com.gtolib.api.gui.ktflexible

import com.lowdragmc.lowdraglib.LDLib
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import kotlin.jvm.internal.SourceDebugExtension
import net.minecraft.network.FriendlyByteBuf

public fun root(width: Int, height: Int, init: (RootBuilder) -> Unit): WidgetGroup {
   val builder: RootBuilder = RootBuilder(width, height)
   builder.buildAndInit(init)
   val var10000: Widget = builder.getBuiltWidget()
   return var10000 as WidgetGroup
}

public fun rootFresh(width: Int, height: Int, init: (RootBuilder) -> Unit): FreshWidgetGroupAbstract {
   @Metadata(
      mv = {2, 3, 0},
      k = 1,
      xi = 48,
      d1 = "\u0000#\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002*\u0001\u0000\b\n\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\r\u0010\u0004\u001a\u00020\u0005H\u0010¢\u0006\u0002\b\u0006J\b\u0010\u0007\u001a\u00020\u0005H\u0016J\b\u0010\b\u001a\u00020\u0005H\u0016J\u001a\u0010\t\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016J\u001a\u0010\u000e\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016¨\u0006\u000f",
      d2 = {
            "com/gtolib/api/gui/ktflexible/FlexibleContainerDslKt$rootFresh$FreshWidgetGroup",
            "Lcom/gtolib/api/gui/ktflexible/FreshWidgetGroupAbstract;",
            "<init>",
            "(IILkotlin/jvm/functions/Function1;)V",
            "fresh",
            "",
            "fresh$gtocore_forge_1_20_1",
            "requireFresh",
            "serverFresh",
            "readUpdateInfo",
            "id",
            "",
            "buffer",
            "Lnet/minecraft/network/FriendlyByteBuf;",
            "handleClientAction",
            "gtocore-forge-1.20.1"
      }
   )
   final class FreshWidgetGroup extends FreshWidgetGroupAbstract {
      public FreshWidgetGroup() {
         val var10001: Position = Position.ORIGIN
         super(var10001, Size(width, height))
      }

      @Override
      public void fresh$gtocore_forge_1_20_1() {
         this.clearAllWidgets()
         val builder: RootBuilder = RootBuilder(width, height)
         builder.buildAndInit(init)
         val var10000: Widget = builder.getBuiltWidget()
         this.addWidget((var10000 as WidgetGroup) as Widget)
         this.initWidget()
      }

      @Override
      public void requireFresh() {
         if (LDLib.isRemote()) {
            this.writeClientAction(947, { buf: FriendlyByteBuf ->
               buf.writeBoolean(true)
            })
         }
      }

      @Override
      public void serverFresh() {
         this.writeUpdateInfo(947, { buf: FriendlyByteBuf ->
            buf.writeBoolean(true)
         })
         this.fresh$gtocore_forge_1_20_1()
      }

      public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
         if (id == 947) {
            this.fresh$gtocore_forge_1_20_1()
         } else {
            super.readUpdateInfo(id, buffer)
         }
      }

      public void handleClientAction(int id, FriendlyByteBuf buffer) {
         if (id == 947) {
            this.serverFresh()
         } else {
            super.handleClientAction(id, buffer)
         }
      }

      private static final void requireFresh$lambda$0(FriendlyByteBuf buf) {
         buf.writeBoolean(true)
      }

      private static final void serverFresh$lambda$1(FriendlyByteBuf buf) {
         buf.writeBoolean(true)
      }
   }

   val rootContainer: FreshWidgetGroup = object : FreshWidgetGroupAbstract {
      init {
         val var10001: Position = Position.ORIGIN
         super(var10001, Size(width, height))
      }

      internal override fun fresh() {
         this.clearAllWidgets()
         val builder: RootBuilder = RootBuilder(width, height)
         builder.buildAndInit(init)
         val var10000: Widget = builder.getBuiltWidget()
         this.addWidget((var10000 as WidgetGroup) as Widget)
         this.initWidget()
      }

      public override fun requireFresh() {
         if (LDLib.isRemote()) {
            this.writeClientAction(947, { buf: FriendlyByteBuf ->
               buf.writeBoolean(true)
            })
         }
      }

      public override fun serverFresh() {
         this.writeUpdateInfo(947, { buf: FriendlyByteBuf ->
            buf.writeBoolean(true)
         })
         this.fresh$gtocore_forge_1_20_1()
      }

      public open fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf?) {
         if (id == 947) {
            this.fresh$gtocore_forge_1_20_1()
         } else {
            super.readUpdateInfo(id, buffer)
         }
      }

      public open fun handleClientAction(id: Int, buffer: FriendlyByteBuf?) {
         if (id == 947) {
            this.serverFresh()
         } else {
            super.handleClientAction(id, buffer)
         }
      }
   }
   rootContainer.fresh$gtocore_forge_1_20_1()
   return rootContainer
}

public fun vBox(width: Int, style: (Style) -> Unit = { var0: Style ->
      Unit.INSTANCE
   }, init: (VBoxBuilder) -> Unit): WidgetGroup {
   val var4: Style = Style(0, 0, 0, 0, 0, 31, null)
   style(var4)
   val builder: VBoxBuilder = VBoxBuilder(width, var4, false, 4, null)
   builder.buildAndInit(init)
   val var10000: Widget = builder.getBuiltWidget()
   return var10000 as WidgetGroup
}

public fun hBox(height: Int, style: (Style) -> Unit = { var0: Style ->
      Unit.INSTANCE
   }, init: (HBoxBuilder) -> Unit): WidgetGroup {
   val var4: Style = Style(0, 0, 0, 0, 0, 31, null)
   style(var4)
   val builder: HBoxBuilder = HBoxBuilder(height, var4, false, 4, null)
   builder.buildAndInit(init)
   val var10000: Widget = builder.getBuiltWidget()
   return var10000 as WidgetGroup
}

public fun vScroll(width: Int, height: Int, init: (VScrollBuilder) -> Unit): WidgetGroup {
   val builder: VScrollBuilder = VScrollBuilder(width, height, null, 4, null)
   builder.buildAndInit(init)
   val var10000: Widget = builder.getBuiltWidget()
   return var10000 as WidgetGroup
}

public fun hScroll(width: Int, height: Int, init: (HScrollBuilder) -> Unit): WidgetGroup {
   val builder: HScrollBuilder = HScrollBuilder(width, height, null, 4, null)
   builder.buildAndInit(init)
   val var10000: Widget = builder.getBuiltWidget()
   return var10000 as WidgetGroup
}
