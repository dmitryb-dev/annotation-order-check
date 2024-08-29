package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class DeclarationOrderCheckTest extends CheckstyleTest {

    @Override
    protected Collection<AbstractCheck> getChecks() {
        var declarationOrderCheck = new DeclarationOrderCheck();
        declarationOrderCheck.setTemplate("""
            field
            public method
            public method getter
            public class
            private class
        """);
        return List.of(declarationOrderCheck);
    }

    @Override
    protected void createTestCases(Map<String, Collection<String>> testCases) {
        testCases.put(
                // language=Java
                """
                private class TestClass {
                    public boolean isField() {}
                    public void method() {}
                    private String field;
                }
                public class TestClass2 {
                    private class Inner {}
                    public class Inner2 {}
                }
                """,
                List.of(
                        "3:5 public method must be placed before public method getter",
                        "4:5 field must be placed before public method",
                        "6:1 public class must be placed before private class",
                        "8:5 public class must be placed before private class"
                )
        );
        testCases.put(
                // language=Java
                """
                public class TestClass {
                    private String field;
                    public void method() {}
                    public boolean isField() {}
                }
                private class TestClass2 {
                    public class Inner {}
                    private class Inner2 {}
                }
                """,
                List.of()
        );
    }
}