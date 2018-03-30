# jsonpath-maven-plugin: A maven plugin for evaluating JsonPath expressions

## Overview

jsonpath-maven-plugin is an Apache 2.0 licensed maven plugin in Java 7.

## Goal Examples

### set-properties

This example uses the `set-properties` goal to set some maven properties from the information contained within a NPM `package.json` file.

```
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>ca.szc.maven</groupId>
                <artifactId>jsonpath-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <id>read-devdependencies</id>
                        <phase>initialize</phase>
                        <goals>
                            <goal>set-properties</goal>
                        </goals>
                        <configuration>
                            <file>package.json</file>
                            <properties>
                                <version.js.phantomjs-prebuilt>$.devDependencies.phantomjs-prebuilt</version.js.phantomjs-prebuilt>
                                <version.js.nsp>$.devDependencies.nsp</version.js.nsp>
                            </properties>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### modify

This example uses the `modify` goal to change the version of a NPM `package.json` file.

```
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>ca.szc.maven</groupId>
                <artifactId>jsonpath-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <id>update-version</id>
                        <phase>process-resources</phase>
                        <goals>
                            <goal>modify</goal>
                        </goals>
                        <configuration>
                            <file>${project.build.directory}/package.json</file>
                            <modifications>
                                <modification>
                                    <expression>$.version</expression>
                                    <value>${project.version}</value>
                                </modification>
                            </modifications>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
