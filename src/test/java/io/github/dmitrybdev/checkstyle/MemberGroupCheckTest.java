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
            <constructor>, private <constructor>
            private <constructor>
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
                        "3:5 Single-line members must be separated by 0 lines. Current interval: 1 line",
                        "5:5 Single-line members must be separated by 0 lines. Current interval: 1 line",
                        "10:5 Single-line members must be separated by 0 lines. Current interval: 1 line",
                        "11:5 Members must be separated by 1 line. Current interval: 0 lines"
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
                List.of("7:5 Members must be separated by 1 line. Current interval: 2 lines")
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
                List.of("5:5 Members must be separated by 1 line. Current interval: 2 lines")
        );
        testCases.put(
                // language=Java - Fields order with comments
                """
                @Annotation
                public class TestClass {
                    private final String field;
                }
                """,
                List.of("3:5 Members must be separated by 1 line. Current interval: 0 lines")
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
                        "3:5 Members must be separated by 1 line. Current interval: 0 lines",
                        "5:5 Single-line members must be separated by 0 lines. Current interval: 1 line"
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
                        "6:5 Member groups must be separated by 2 lines. Current interval: 1 line",
                        "9:5 Member groups must be separated by 2 lines. Current interval: 1 line",
                        "9:5 public <method>, private <method> members group must be placed before private <method> group",
                        "11:5 Single-line members must be separated by 0 lines. Member groups must be separated by 2 lines. Current interval: 1 line",
                        "13:5 Member groups must be separated by 2 lines. Current interval: 1 line",
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

        // Constructors test
        testCases.put(
                // language=Java - Fields order with comments
                """
                public class TestClass {
                
                    public TestClass() {}
                
                    private TestClass() {}
                    private TestClass() {
                    }
                
                    public TestClass() {
                
                    }
                    private TestClass() {}
                }
                """,
                List.of(
                        "3:5 Single-line members must be separated by 0 lines. Current interval: 1 line",
                        "5:5 Single-line members must be separated by 0 lines. Member groups must be separated by 2 lines. Current interval: 1 line",
                        "6:5 Members must be separated by 1 line. Current interval: 0 lines",
                        "9:5 <constructor>, private <constructor> members group must be placed before private <constructor> group",
                        "9:5 Member groups must be separated by 2 lines. Current interval: 1 line",
                        "12:5 Members must be separated by 1 line. Member groups must be separated by 2 lines. Current interval: 0 lines"
                )
        );
        testCases.put(
                // language=Java - Fields order with comments
                """
                public class TestClass {
                    public TestClass() {}
                    private TestClass() {}
                
                    private TestClass() {
                    }
                
                
                    public TestClass() {
                
                    }
                
                
                    private TestClass() {}
                }
                """,
                List.of()
        );
    }
}