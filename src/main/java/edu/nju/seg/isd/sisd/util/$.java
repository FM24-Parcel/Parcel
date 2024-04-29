package edu.nju.seg.isd.sisd.util;

import edu.nju.seg.isd.sisd.exception.Undefined;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class $ {

    /** a todo placeholder for satisfying the type checker
     *
     * @return null
     * @param <T> any type
     */
    public static <T> T TODO() {
        throw new Undefined();
    }

    /** a todo placeholder for satisfying the type checker
     *
     * @param comment the todo comment
     * @return null
     * @param <T> any type
     */
    public static <T> T TODO(@NotNull String comment) {
        throw new Undefined(comment);
    }

    @NotNull
    public static Optional<String> readContent(@NotNull File f) {
        try {
            return Optional.of(Files.readString(f.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
