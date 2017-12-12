# vba-interpreter
An interpreter for Microsoft Office VBA Macros written in Java. The purpose of
this interpreter is to detect malware in macros.

The project is currently in alpha stage, there is a lot of functionality not
yet implemented and plenty of bugs. Nevertheless, we welcome if you fork this
project, report issues on Github, and send us pull requests for code
improvements.

## Compiling
This project uses a maven build system. Install Maven 2 on your distribution to
compile.
To create the binary jar file, run:
```
mvn package
```

## Running
Run with:
```
java -jar target/vbainterpreter-*-with-dependencies.jar <document>
```

There is also a way to dump the VBA code seen by the interpreter:
```
java -classpath vbainterpreter-*-with-dependencies.jar thirdparty.org.apache.poi.poifs.macros.VBAMacroExtractor <document>
```

## License
This code is licensed under the MIT license. See the LICENSE file for the
license's text.  This project is based on [vba-interpreter by
Ishua](https://github.com/Inshua/vba-interpreter).


