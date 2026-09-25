package com.gtolib.api.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import com.google.common.collect.SetMultimap;
import java.util.WeakHashMap;

public interface IExpandedGrid extends IGrid {
   WeakHashMap<IExpandedGrid, Long> PERFORMANCE_MAP = new WeakHashMap<>();

   SetMultimap<Class<?>, IGridNode> getMachines();

   long getLatency();

   void observe();
}
