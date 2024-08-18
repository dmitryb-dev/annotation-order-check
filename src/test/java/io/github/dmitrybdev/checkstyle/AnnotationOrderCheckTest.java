package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class AnnotationOrderCheckTest extends CheckstyleTest {

    @Override
    protected Class<? extends AbstractCheck> getCheckClass() {
        return AnnotationOrderCheck.class;
    }

    @Override
    protected void configure(DefaultConfiguration configuration) {
        configuration.addProperty("typeTemplate", """
            @spring.Component @Lazy @Order, @Getter @Setter
            public private final static
        """);
        configuration.addProperty("fieldTemplate", """
            @Lazy
            @Getter @Setter public private final static @Nullable
        """);
        configuration.addProperty("methodTemplate", """
            @Bean @Lazy @Order
            public private final static @Nullable
        """);
    }

    @Override
    protected void createTestCases(Map<String, Collection<String>> testCases) {
        testCases.put(
                // language=Java
                """
                @Getter
                @Setter @Scope
                @Lazy @spring.Component
                public class TestClass {
                    @Getter @Lazy
                    private @Nullable final String field;
                
                    @Lazy private
                    final String field;
                
                    @Order @Bean
                    @Nullable int bean() {}
                }
                """,
                List.of(
                        "2:1 @Setter must be placed on the same line with @Getter",
                        "3:1 @Lazy must be placed before @Setter",
                        "3:7 @spring.Component must be placed before @Lazy",
                        "5:13 @Lazy must be placed before @Getter",
                        "6:23 final must be placed before @Nullable",
                        "8:11 private must be placed on the new line after @Lazy",
                        "9:5 final must be placed on the same line with private",
                        "11:12 @Bean must be placed before @Order"
                )
        );
        testCases.put(
                // language=Java
                """
                @spring.Component @Scope @Lazy
                @Getter @Setter
                public class TestClass {
                    @Lazy
                    @Getter private final @Nullable String field;
                
                    @Bean @Order
                    @Nullable int bean() {}
                }
                """,
                List.of()
        );
    }
}