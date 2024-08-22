package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class BoundaryCheckTest extends CheckstyleTest {

    @Override
    protected Collection<AbstractCheck> getChecks() {
        BoundaryCheck singleLineStatementsBoundary = new BoundaryCheck();
        singleLineStatementsBoundary.setMinLength(3);
        singleLineStatementsBoundary.setMinNewLines(1);

        BoundaryCheck privatePublicBoundary = new BoundaryCheck();
        privatePublicBoundary.setAfter("private method, field");
        privatePublicBoundary.setBefore("public method, public getter");
        privatePublicBoundary.setMinNewLines(2);

        BoundaryCheck classBoundary = new BoundaryCheck();
        classBoundary.setAfter("method, field");
        classBoundary.setBefore("class");
        classBoundary.setMinNewLines(2);

        BoundaryCheck getterSetterBoundary = new BoundaryCheck();
        getterSetterBoundary.setAfter("getter");
        getterSetterBoundary.setBefore("setter");
        getterSetterBoundary.setMinNewLines(1);
        getterSetterBoundary.setCommentsAsNewLines(false);

        return List.of(singleLineStatementsBoundary, privatePublicBoundary, classBoundary, getterSetterBoundary);
    }

    @Override
    protected void createTestCases(Map<String, Collection<String>> testCases) {
        testCases.put(
                // language=Java
                """
                public class TestClass {
                    private int field1;
                    private int field2;
                    @Lazy
                    private int field3;
                    // Comment
                    @Lazy
                    private int field4;
                
                
                    public void method1() {}
                    public void method2() {
                    }
                
                
                    @Retention
                    interface TestInterface {}
                    interface TestInterface2 {}
                    interface TestInterface3 {}
                }
                """,
                List.of(
                        "4:5 Current interval (0 lines) is less than required: 1",
                        "12:5 Current interval (0 lines) is less than required: 1",
                        "18:5 Current interval (0 lines) is less than required: 1"
                )
        );

        testCases.put(
                // language=Java
                """
                public class TestClass {
                    private int field3;
                
                    public void method1() {}
                    private void method2() {}
                    public void method3() {}
                
                    interface TestInterface {
                        String method() {}
                    }
                }
                """,
                List.of(
                        "4:5 Current interval (1 lines) is less than required: 2",
                        "6:5 Current interval (0 lines) is less than required: 2",
                        "8:5 Current interval (1 lines) is less than required: 2"
                )
        );

        testCases.put(
                // language=Java
                """
                public abstract class TestClass {
                    private int field3;
                
                    public String getMethod() {}
                    // comment
                    public abstract void setMethod(String value);
                    public String getMethod() {}
                }
                """,
                List.of(
                        "4:5 Current interval (1 lines) is less than required: 2",
                        "6:5 Current interval (0 lines) is less than required: 1"
                )
        );

        testCases.put(
                // language=Java
                """
                import java.lang.annotation.Retention;
                
                @Retention()
                public @interface Annotation {
                    private int value();
                    private int value2() default 0;
                }
                """,
                List.of()
        );

        testCases.put(
                // language=Java
                """
                public class TestClass {
    
                    private int value() {
                        int value = 3;
                        int value2 = 4;
                    }
                }
                """,
                List.of()
        );
    }
}