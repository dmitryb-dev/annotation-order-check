package io.github.dmitrybdev.checkstyle;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public abstract class CheckstyleTest {
    protected abstract Collection<AbstractCheck> getChecks();
    protected abstract void createTestCases(Map<String, Collection<String>> testCases);


    @ParameterizedTest
    @MethodSource("getTestCases")
    public final void check(String source, Collection<String> expectedViolations) throws Exception {
        TreeWalker treeWalker = new TreeWalker();
        treeWalker.setModuleFactory(new PackageObjectFactory(
                PackageNamesLoader.getPackageNames(this.getClass().getClassLoader()),
                this.getClass().getClassLoader()
        ));

        Collection<AbstractCheck> checks = getChecks();
        for (AbstractCheck check : checks) {
            check.init();
            check.configure(new DefaultConfiguration("Test"));
            MethodUtils.invokeMethod(treeWalker, true, "registerCheck", check);
        }

        treeWalker.process(new File("Test.java"), new FileText(null, source.lines().toList()));

        Collection<String> actualViolations = checks.stream().flatMap(c -> c.getViolations().stream())
                .map(v -> "%d:%d %s".formatted(v.getLineNo(), v.getColumnNo(), v.getViolation()))
                .toList();

        assertThat(actualViolations).containsExactlyElementsOf(expectedViolations);
    }

    private Stream<Arguments> getTestCases() {
        Map<String, Collection<String>> testCases = new LinkedHashMap<>();
        createTestCases(testCases);
        return testCases.entrySet().stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }
}
