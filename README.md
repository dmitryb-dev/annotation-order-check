# Custom Checkstyle Check for Annotation and Modifier Order

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
                    <version>1.0.0</version>
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
                                    @Getter @Setter public private final static @Nullable
                                "/>
                                <property name="methodTemplate" value="
                                    @Bean @Lazy @Order
                                    public private final static @Nullable
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
- Annotations and modifiers placed on the same line in the template must also be on the same line in the code.
- Annotations and modifiers separated by a newline `\n` in the template must be placed on separate lines in the code.