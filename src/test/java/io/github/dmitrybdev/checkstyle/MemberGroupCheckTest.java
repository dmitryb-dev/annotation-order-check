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
            CLASS
            private static FIELD
            private FIELD
            @Override public METHOD, private METHOD
            public METHOD, private METHOD
            private METHOD
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
    }
}