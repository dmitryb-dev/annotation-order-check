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
                    <version>1.0.5</version>
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