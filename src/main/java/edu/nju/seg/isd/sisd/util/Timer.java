package edu.nju.seg.isd.sisd.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

@NotNull
public class Timer {

    private final Instant start;

    private Instant last;

    public Timer() {
        this.start = Instant.now();
        this.last = start;
    }

    /**
     * count the time duration from the last count
     *
     * @param prompt the prompt message
     */
    public void lap(@NotNull String prompt) {
        Instant now = Instant.now();
        Duration d = Duration.between(last, now);
        String output = String.format("%s [time duration: %d seconds]", prompt, d.getSeconds());
        System.out.println(output);
        this.last = now;
    }

    /**
     * count the time duration from the start
     *
     * @param prompt the prompt message
     */
    public void untilNow(@NotNull String prompt) {
        Instant now = Instant.now();
        Duration d = Duration.between(start, now);
        String output = String.format("%s [total time: %d seconds]", prompt, d.getSeconds());
        System.out.println(output);
    }

}
