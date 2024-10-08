# Checkstyle Check for Annotation and Modifier Order

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dmitryb-dev/annotation-order-check.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.dmitryb-dev/annotation-order-check)

This Checkstyle check ensures that annotations and modifiers are in the correct order 
and are placed on the same or separate lines as specified in configurable templates.


## Usage

To integrate this check into your project, you need to configure Checkstyle, 
for example, using the `maven-checkstyle-plugin` in your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.4.0</version>
            <dependencies>
                <dependency>
                    <groupId>com.puppycrawl.tools</groupId>
                    <artifactId>checkstyle</artifactId>
                    <version>10.17.0</version>
                </dependency>
                <dependency>
                    <groupId>io.github.dmitryb-dev</groupId>
                    <artifactId>annotation-order-check</artifactId>
                    <version>1.0.6</version>
                </dependency>
            </dependencies>
            <configuration>
                <checkstyleRules>
                    <module name="Checker">
                        <module name="TreeWalker">
                            <module name="io.github.dmitrybdev.checkstyle.AnnotationOrderCheck">
                                <property name="typeTemplate" value="
                                    @Component @Lazy @Order
                                    @Getter @Setter
                                    public private final static
                                "/>
                                <property name="fieldTemplate" value="
                                    @Lazy
                                    @Getter() @Setter()
                                    @Getter @Setter public private final static @Nullable type
                                "/>
                                <property name="methodTemplate" value="
                                    @Bean @Lazy @Order
                                    public private final static @Nullable type
                                "/>
                            </module>
                        </module>
                    </module>
                </checkstyleRules>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Template Configuration

- **typeTemplate**: Specifies the expected order for annotations and modifiers at the class/interface level. 
- **fieldTemplate**: Specifies the expected order for field-level annotations and modifiers.
- **methodTemplate**: Specifies the expected order for constructor and method-level annotations and modifiers.


## Important Notes
- Annotations and modifiers separated by a space `" "` in the template must be on the same line in the code.
- Annotations and modifiers separated by a comma `","` or three and more spaces `"   "` in the template must be placed on separate lines in the code.
- You may define different positions for cases when an annotation has parentheses, using the `@Annotation()` and `@Annotation` syntax.
- When only `@Annotation` syntax (without parentheses) is used, it matches both annotations with and without parentheses.



# Declaration order check

This check allows you to enforce the order of fields, methods, and classes. 
The template syntax is the same as for the AnnotationOrderCheck and includes additional keywords such as `class, interface, record, @interface, method, getter, setter, field, param, type`. 
When a declaration matches multiple templates, the longest match is chosen.

Example `checkstyle.xml` configuration:

```xml
<module name="Checker">
    <module name="TreeWalker">
        <module name="io.github.dmitrybdev.checkstyle.DeclarationOrderCheck">
            <property name="template" value="
                private static field
                private field
                public method getter
                public method setter
                public constructor
                private constructor
                public abstract method
                public @Override method
                public abstract class
                public class
            "/>
        </module>
    </module>
</module>
```

# Boundary check

This check allows you to require additional new lines between members. 
For example, the following configuration:

```xml
<module name="io.github.dmitrybdev.checkstyle.BoundaryCheck">
    <property name="after" value="field"/>
    <property name="before" value="method"/>
    <property name="minNewLines" value="2"/>
</module>
```

Will require a minimum of 2 lines between the group of fields and the group of methods.
The after and before properties use the same syntax as the DeclarationOrderCheck template.

Additionally, you can define a minimum interval when the length of a member exceeds a certain threshold. 
For example:

```xml
<module name="io.github.dmitrybdev.checkstyle.BoundaryCheck">
    <property name="minLength" value="3"/>
    <property name="minNewLines" value="1"/>
</module>
```

This will trigger a check for a new line when the total space taken by the before and after declarations exceeds 3 lines.
The following is allowed:

```java
private String field1;
private String field2;
```

But the following will cause an error:

```java
private String field1;
private String field2 = """
        """; // Error: field2 takes 2 lines + field1 takes 1 line, which exceeds the allowed 3 lines
```

Comments are counted as new lines by default. 
This behavior can be changed by setting the `commentsAsNewLines` property to 0. 
If `commentsAsNewLines` is set, it specifies the maximum value for the comments counter.
For instance, if `commentsAsNewLines=1` and `minNewLines=2`, the check will require additional new line even if a comment takes more than 2 lines.