package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

class AnnotationOrderCheckTest extends CheckstyleTest {

    @Override
    protected Class<? extends AbstractCheck> getCheckClass() {
        return AnnotationOrderCheck.class;
    }

    @Override
    protected void configure(DefaultConfiguration configuration) {
        configuration.addProperty("typeTemplate", """
            @Component @Lazy @Order
            @Getter @Setter
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
    protected Map<String, Collection<String>> createTestCases() {
        return Map.ofEntries(entry(
                // language=Java
                """
                @Getter
                @Setter @Scope
                @Lazy @Component
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
                        "@Setter must be placed on the same line with @Getter",
                        "@Lazy must be placed before @Setter",
                        "@Component must be placed before @Lazy",
                        "@Lazy must be placed before @Getter",
                        "final must be placed before @Nullable",
                        "private must be placed on the new line after @Lazy",
                        "final must be placed on the same line with private",
                        "@Bean must be placed before @Order"
                )
        ), entry(
                // language=Java
                """
                @Component @Scope @Lazy
                @Getter @Setter
                public class TestClass {
                    @Lazy
                    @Getter private final @Nullable String field;
    
                    @Bean @Order
                    @Nullable int bean() {}
                }
                """,
                List.of()
        ));
    }
}