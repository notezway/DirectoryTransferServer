package ru.ntzw.com.dt.server.model.random;

import java.util.Random;

public class RandomStringService {

    private static final char[] symbols = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final Random random = new Random();

    public String generate(int length) {
        char[] chars = new char[length];
        for(int i = 0; i < length; i++) {
            chars[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(chars);
    }
}
