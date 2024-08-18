package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class MemberGroupCheckTest extends CheckstyleTest {

    @Override
    protected Class<? extends AbstractCheck> getCheckClass() {
        return MemberGroupCheck.class;
    }

    @Override
    protected void configure(DefaultConfiguration configuration) {
        configuration.addProperty("groups", """
            private static <field>
            private <field>
            @Override public <method>, private <method>
            public <method>, private <method>
            private <method>
            <class>
        """);
    }

    @Override
    protected void createTestCases(Map<String, Collection<String>> testCases) {
        // Single line vs multiline test
        testCases.put(
                // language=Java - Fields order with comments
                """
                public class TestClass {
    
                    private final String field;
                
                    private final String field2;
    
                    /**
                     * Comment
                     */
                    private final String field2;
                    private final String field3 =
                        "";
                }
                """,
                List.of(
                        "3:5 Single-line members must be separated by 0 line(s). Current interval: 1 line(s)",
                        "5:5 Single-line members must be separated by 0 line(s). Current interval: 1 line(s)",
                        "10:5 Single-line members must be separated by 0 line(s). Current interval: 1 line(s)",
                        "11:5 Members must be separated by 1 line(s). Current interval: 0 line(s)"
                )
        );

        // Class start test
        testCases.put(
                // language=Java - Fields order with comments
                """
                /**
                  * Comment
                  */
                public class TestClass
                {
    
                    private final String field;
                }
                """,
                List.of("7:5 Members must be separated by 1 line(s). Current interval: 2 line(s)")
        );
        testCases.put(
                // language=Java - Fields order with comments
                """
                @Annotation
                public class TestClass
                {
    
                    private final String field;
                }
                """,
                List.of("5:5 Members must be separated by 1 line(s). Current interval: 2 line(s)")
        );
        testCases.put(
                // language=Java - Fields order with comments
                """
                @Annotation
                public class TestClass {
                    private final String field;
                }
                """,
                List.of("3:5 Members must be separated by 1 line(s). Current interval: 0 line(s)")
        );

        // Order test
        testCases.put(
                // language=Java - Fields order with comments
                """
                @Annotation
                public class TestClass {
                    public final String field;
    
                    private final String field2;
                    public final String field3;
                    private final String field4;
                }
                """,
                List.of(
                        "3:5 Members must be separated by 1 line(s). Current interval: 0 line(s)",
                        "5:5 Single-line members must be separated by 0 line(s). Current interval: 1 line(s)"
                )
        );

        testCases.put(
                // language=Java - Fields order with comments
                """
                @Annotation
                public class TestClass {
    
                    private final String field1;
    
                    private void method1() {
                    }
    
                    public void method2() {}
    
                    private void method3() {}
    
                    private final String field2;
                }
                """,
                List.of(
                        "6:5 Member groups must be separated by 2 line(s). Current interval: 1 line(s)",
                        "9:5 Member groups must be separated by 2 line(s). Current interval: 1 line(s)",
                        "9:5 public <method>, private <method> members group must be placed before private <method> group",
                        "11:5 Members must be separated by 0 or 2 line(s). Current interval: 1 line(s)",
                        "13:5 Member groups must be separated by 2 line(s). Current interval: 1 line(s)",
                        "13:5 private <field> members group must be placed before private <method> group"
                )
        );
        testCases.put(
                // language=Java - Fields order with comments
                """
                @Annotation
                public class TestClass {
    
                    private final String field1;
                    private final String field2;
    
    
                    public void method2() {}
                    private void method3() {}
    

                    private void method1() {
                    }
                }
                """,
                List.of()
        );
    }
}