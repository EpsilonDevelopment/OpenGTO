package com.gtolib.api.annotation.component_builder;

import com.gtolib.api.annotation.NewDataAttributes;
import com.gtolib.api.annotation.component_builder.element.ComponentElement;
import com.gtolib.api.annotation.component_builder.element.ContainerNestedElement;
import com.gtolib.api.annotation.component_builder.element.ContainerTemplateElement;
import com.gtolib.api.annotation.component_builder.element.ContainerTextBlockElement;
import com.gtolib.api.annotation.component_builder.element.LeafComponentElement;
import com.gtolib.api.annotation.component_builder.element.LeafLiteralElement;
import com.gtolib.api.annotation.component_builder.element.LeafTranslatableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ComponentBuilder {
   private final List<ComponentElement> elements = new ArrayList<>();

   private ComponentBuilder() {
   }

   public boolean isEmpty() {
      return this.elements.isEmpty();
   }

   public ComponentBuilder addInLineBuilder(UnaryOperator<ComponentBuilder> lineBuilderOp, UnaryOperator<StyleBuilder> styleOp) {
      ComponentBuilder lineBuilder = create();
      lineBuilderOp.apply(lineBuilder);
      this.elements.add(LeafComponentElement.create(lineBuilder.buildSingleSupplier(), styleOp));
      return this;
   }

   public ComponentBuilder addLiteralLine(String s, UnaryOperator<StyleBuilder> styleOp) {
      this.elements.add(new LeafLiteralElement(s, styleOp));
      return this;
   }

   public ComponentBuilder addLines(ComponentBuilder... builders) {
      for (ComponentBuilder builder : builders) {
         this.elements.add(new ContainerNestedElement(builder));
      }

      return this;
   }

   public ComponentBuilder addLines(String symbol) {
      return this.addLines(symbol, symbol, UnaryOperator.identity());
   }

   public ComponentBuilder addLines(String symbol, UnaryOperator<StyleBuilder> styleOp) {
      return this.addLines(symbol, symbol, styleOp);
   }

   public ComponentBuilder addLines(Component[] components, UnaryOperator<StyleBuilder> styleOp) {
      for (Component component : components) {
         this.elements.add(new LeafComponentElement(component, styleOp));
      }

      return this;
   }

   public ComponentBuilder addLines(Supplier<List<Component>> supplier, UnaryOperator<StyleBuilder> styleOp) {
      this.elements.add(new LeafComponentElement(supplier, styleOp));
      return this;
   }

   public ComponentBuilder addLines(Component component, UnaryOperator<StyleBuilder> styleOp) {
      this.elements.add(new LeafComponentElement(component, styleOp));
      return this;
   }

   public ComponentBuilder addLines(String cn, String en) {
      return this.addLines(cn, en, null);
   }

   public ComponentBuilder addCommentLines(String cn, String en) {
      return this.addLines(cn, en, styleBuilder -> styleBuilder.setGray().setPrefix(NewDataAttributes.PREFIX_TAB));
   }

   public ComponentBuilder addCommentLines(String cn, String en, UnaryOperator<StyleBuilder> styleOp) {
      return this.addLines(cn, en, styleBuilder -> styleOp.apply(styleBuilder.setGray().setPrefix(NewDataAttributes.PREFIX_TAB)));
   }

   public ComponentBuilder addLines(String cn, String en, UnaryOperator<StyleBuilder> styleOp) {
      return this.addLines(cn, en, styleOp);
   }

   private ComponentBuilder addLines(String cn, String en, UnaryOperator<StyleBuilder> styleOp, Object... args) {
      String[] cnLines = cn.lines().toArray(String[]::new);
      String[] enLines = en.lines().toArray(String[]::new);
      if (cnLines.length != enLines.length) {
         throw new IllegalArgumentException(String.format("中英文行数不匹配: cn=%d行, en=%d行", cnLines.length, enLines.length));
      }

      if (cnLines.length == 1) {
         this.elements.add(new LeafTranslatableElement(cn, en, styleOp, args));
      } else {
         this.elements.add(new ContainerTextBlockElement(cnLines, enLines, styleOp, args));
      }

      return this;
   }

   public ComponentBuilder addElementFromTemplate(ComponentTemplate template, Object value, UnaryOperator<StyleBuilder> styleOpFromCreate) {
      this.elements.add(new ContainerTemplateElement(template, value, styleOpFromCreate));
      return this;
   }

   public ComponentBuilder addElementFromNested(ComponentBuilder nestedBuilder) {
      this.elements.add(new ContainerNestedElement(nestedBuilder));
      return this;
   }

   public ComponentBuilder addElement(ComponentElement element) {
      this.elements.add(element);
      return this;
   }

   public ComponentListSupplier build() {
      return new ComponentListSupplier(() -> {
         List<Component> components = new ArrayList<>();

         for (ComponentElement element : this.elements) {
            List<Component> built = element.build();
            components.addAll(built);
         }

         return components;
      });
   }

   public List<Component> buildComponents() {
      return this.build().get();
   }

   public Component[] buildComponentsArray() {
      return this.buildComponents().toArray(new Component[0]);
   }

   public Component buildSingle() {
      return this.buildSingleSupplier().get();
   }

   private Supplier<Component> buildSingleSupplier() {
      return () -> {
         MutableComponent result = Component.empty();

         for (Component component : this.buildComponents()) {
            result.append(component);
         }

         return result;
      };
   }

   public static ComponentBuilder create() {
      return new ComponentBuilder();
   }

   public static ComponentBuilder create(String cn, String en, UnaryOperator<StyleBuilder> styleOp) {
      return create().addLines(cn, en, styleOp);
   }
}
