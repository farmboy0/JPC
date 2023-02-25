make tools
java -cp JPCApplication.jar:Tools.jar tools.generator.Generator
make application
java -cp JPCApplication.jar:Tools.jar tools.generator.DecoderGenerator
make application