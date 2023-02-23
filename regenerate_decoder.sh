make tools
java -cp JPCApplication.jar:Tools.jar tools.generator.Generator
make application
java -cp JPCApplication.jar:Tools.jar tools.generator.DecoderGenerator > src/org/jpc/emulator/execution/opcodes/ExecutableTables.java
make application