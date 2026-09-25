package com.gtolib.api.annotation.component_builder;

import com.gtolib.api.lang.CNEN;
import java.util.function.UnaryOperator;
import lombok.Generated;
import net.minecraft.network.chat.Component;

public class ComponentTemplate {
   private final String key;
   private final String cnTemplate;
   private final String enTemplate;
   private final UnaryOperator<StyleBuilder> styleOpFromRegister;
   private final Object defaultValue;
   private UnaryOperator<ComponentBuilder> templateCommentOp = UnaryOperator.identity();

   public ComponentTemplate(String key, String cnTemplate, String enTemplate, UnaryOperator<StyleBuilder> styleOpFromRegister) {
      this(key, cnTemplate, enTemplate, styleOpFromRegister, null);
   }

   public ComponentTemplate(String key, String cnTemplate, String enTemplate, UnaryOperator<StyleBuilder> styleOpFromRegister, Object defaultValue) {
      this.key = key;
      this.cnTemplate = cnTemplate;
      this.enTemplate = enTemplate;
      this.styleOpFromRegister = styleOpFromRegister;
      this.defaultValue = defaultValue;
   }

   public ComponentTemplate setComment(UnaryOperator<ComponentBuilder> commentOp) {
      this.templateCommentOp = commentOp;
      return this;
   }

   private ComponentBuilder createBuilder(ComponentBuilder valueBuilder, ComponentBuilder commentBuilder, UnaryOperator<StyleBuilder> templateStyleOpFromCreate) {
      ComponentBuilder templateCommentBuilder = ComponentBuilder.create();
      this.templateCommentOp.apply(templateCommentBuilder);
      return ComponentBuilder.create()
         .addElementFromTemplate(this, valueBuilder, templateStyleOpFromCreate)
         .addElementFromNested(templateCommentBuilder)
         .addElementFromNested(commentBuilder);
   }

   public ComponentBuilder createBuilder(UnaryOperator<ComponentBuilder> valueOp, UnaryOperator<ComponentBuilder> commentOp) {
      return this.createBuilder(applyOperator(valueOp), applyOperator(commentOp), null);
   }

   public ComponentBuilder createBuilder(
      UnaryOperator<ComponentBuilder> valueOp, UnaryOperator<ComponentBuilder> commentOp, UnaryOperator<StyleBuilder> templateStyleOpFromCreate
   ) {
      return this.createBuilder(applyOperator(valueOp), applyOperator(commentOp), templateStyleOpFromCreate);
   }

   private ComponentListSupplier create(ComponentBuilder valueBuilder, ComponentBuilder commentBuilder) {
      return new ComponentListSupplier(this.createBuilder(valueBuilder, commentBuilder, null).build());
   }

   public ComponentListSupplier create(UnaryOperator<ComponentBuilder> valueOp, UnaryOperator<ComponentBuilder> commentOp) {
      return this.create(applyOperator(valueOp), applyOperator(commentOp));
   }

   public ComponentListSupplier create(String value, UnaryOperator<ComponentBuilder> commentOp) {
      ComponentBuilder valueBuilder = ComponentBuilder.create();
      valueBuilder.addLines(value);
      return this.create(valueBuilder, applyOperator(commentOp));
   }

   public ComponentListSupplier create(Object object) {
      return object instanceof Component component
         ? this.create(builder -> builder.addLines(new Component[]{component}, c -> c), builder -> builder)
         : this.create(object.toString(), builder -> builder);
   }

   public ComponentListSupplier create(String cn, String en) {
      return this.create(builder -> builder.addLines(cn, en), builder -> builder);
   }

   public ComponentListSupplier create(CNEN cnen) {
      return this.create(cnen.cn(), cnen.en());
   }

   public ComponentListSupplier create(CNEN cnen, UnaryOperator<ComponentBuilder> commentOp) {
      return this.create(builder -> builder.addLines(cnen.cn(), cnen.en()), commentOp);
   }

   public ComponentListSupplier create(UnaryOperator<ComponentBuilder> valueOp) {
      return this.create(valueOp, builder -> builder);
   }

   public ComponentListSupplier create() {
      return this.create(builder -> builder, builder -> builder);
   }

   private static ComponentBuilder applyOperator(UnaryOperator<ComponentBuilder> operator) {
      ComponentBuilder builder = ComponentBuilder.create();
      operator.apply(builder);
      return builder;
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   public String getCnTemplate() {
      return this.cnTemplate;
   }

   @Generated
   public String getEnTemplate() {
      return this.enTemplate;
   }

   @Generated
   public UnaryOperator<StyleBuilder> getStyleOpFromRegister() {
      return this.styleOpFromRegister;
   }

   @Generated
   public Object getDefaultValue() {
      return this.defaultValue;
   }
}
