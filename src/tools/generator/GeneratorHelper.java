package tools.generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GeneratorHelper {
    private static final String LICENSE_HEADER_FILE = "LicenseHeader";

    private GeneratorHelper() {
    }

    public static String readLicenseHeader() throws IOException {
        final StringBuilder header = new StringBuilder();
        final BufferedReader r = new BufferedReader(new FileReader(LICENSE_HEADER_FILE));

        String line;
        try {
            while ((line = r.readLine()) != null) {
                header.append(line);
                header.append("\n");
            }
        } finally {
            r.close();
        }
        header.append("\n");

        return header.toString();
    }
}
