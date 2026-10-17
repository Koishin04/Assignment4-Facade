package restaurant.storage;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonFiles {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setVisibility(
                PropertyAccessor.ALL,
                JsonAutoDetect.Visibility.NONE
        );

        mapper.setVisibility(
                PropertyAccessor.FIELD,
                JsonAutoDetect.Visibility.ANY
        );
    }

    public static <T> List<T> read(Path file, Class<T> type) {
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }

        try {
            List<T> values = mapper.readValue(
                    file.toFile(),
                    mapper.getTypeFactory()
                            .constructCollectionType(ArrayList.class, type)
            );

            if (values == null) {
                throw new IOException("Expected a JSON array: " + file);
            }

            return values;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    public static void write(Path file, Object value) {
        Path target = file.toAbsolutePath();
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");

        try {
            Files.createDirectories(target.getParent());

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temp.toFile(), value);

            Files.move(
                    temp,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot save " + file, e);
        }
    }
}