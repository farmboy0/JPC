make tools
java -cp JPCApplication.jar:Tools.jar tools.Tools
make application
java -cp JPCApplication.jar:Tools.jar tools.Tools -decoder > src/org/jpc/emulator/execution/opcodes/ExecutableTables.java
make application