package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import io.github.dmitrybdev.checkstyle.AbstractModifiersCheck.Modifier;
import io.github.dmitrybdev.checkstyle.AbstractModifiersCheck.ModifierOrder;
import io.github.dmitrybdev.checkstyle.AbstractModifiersCheck.Order;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractModifiersCheckTest extends CheckstyleTest {

    @Override
    protected Collection<AbstractCheck> getChecks() {
        return List.of(new AbstractModifiersCheck());
    }

    @Override
    protected void createTestCases(Map<String, Collection<String>> testCases) {
        testCases.put(
                // language=Java
                """
                @Getter() @Setter
                @spring.Component
                public class TestClass {
                    @Lazy
                    private @Nullable final
                    String field;
                
                    @Getter() private String method() {}
                    public String getField() {}
                    public void getField() {}
                    public boolean isField() {}
                    public void setField(String field) {}
                    public void setField() {}
                
                    enum Enum {}
                    public abstract class AbstractClass {
                        public abstract void method();
                    }
                    public static class Class {
                        Class(String arg) {}
                    }
                    interface Interface {
                        @Lazy void method();
                    }
                    @interface Interface {
                        String method() default "";
                    }
                    record Record(@Nullable String field) {}
                }
                """,
                List.of(
                        "1:1 1:0 @Getter(), 1:10 @Setter, 2:0 @spring.Component, 3:0 public, 1:0 class",
                        "4:5 4:4 @Lazy, 5:4 private, 5:12 @Nullable, 5:22 final, 6:4 type, 4:4 field",
                        "8:5 8:4 @Getter(), 8:14 private, 8:22 type, 8:4 method",
                        "9:5 9:4 public, 9:11 type, 9:4 method, 9:18 getter",
                        "10:5 10:4 public, 10:11 type, 10:4 method",
                        "11:5 11:4 public, 11:11 type, 11:4 method, 11:19 getter",
                        "12:5 12:4 public, 12:11 type, 12:4 method, 12:16 setter",
                        "12:26 12:25 type, 12:25 param",
                        "13:5 13:4 public, 13:11 type, 13:4 method",
                        "15:5 15:4 enum",
                        "16:5 16:4 public, 16:11 abstract, 16:4 class",
                        "17:9 17:8 public, 17:15 abstract, 17:24 type, 17:8 method",
                        "19:5 19:4 public, 19:11 static, 19:4 class",
                        "20:9 20:8 constructor",
                        "20:15 20:14 type, 20:14 param",
                        "22:5 22:4 interface",
                        "23:9 23:8 @Lazy, 23:14 type, 23:8 method",
                        "25:5 25:4 @interface",
                        "26:9 26:8 type",
                        "28:5 28:4 record",
                        "28:19 28:18 @Nullable, 28:28 type, 28:18 param"
                )
        );
    }

    @Test
    public void parseTemplate() {
        Order order = new AbstractModifiersCheck().parseTemplate("""
            @Annotation   @Annotation() private, type
        """);

        ModifierOrder annotationOrder = order.getOrder(new Modifier("@Annotation", false, -1, -1));
        assertThat(annotationOrder).isNotNull();
        assertThat(annotationOrder.groupOrder()).isEqualTo(0);
        assertThat(annotationOrder.order()).isEqualTo(0);

        ModifierOrder annotationWithArgsOrder = order.getOrder(new Modifier("@Annotation", true, -1, -1));
        assertThat(annotationWithArgsOrder).isNotNull();
        assertThat(annotationWithArgsOrder.groupOrder()).isEqualTo(1);
        assertThat(annotationWithArgsOrder.order()).isEqualTo(1);

        AbstractModifiersCheck.GroupOrder annotatedOrder = order.getOrder(List.of(
                new Modifier("@Annotation", false, -1, -1),
                new Modifier("private", false, -1, -1)
        ));
        assertThat(annotatedOrder).isNotNull();
        assertThat(annotatedOrder.order()).isEqualTo(0);
    }
}