package org.lin.fitnessuser.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author lin
 * @date 2026-03-17
 * PasswordUtil.java 负责密码的加密和验证，确保密码安全。
 */
public class PassWordUtils{
    /**
     *  BCrypt 算法的优势在于它是一种自适应哈希函数，内置了盐值（salt）机制，
     *  每次加密同一个密码都会产生不同的哈希值，可以防止彩虹表攻击。
     *  同时 BCrypt 算法具有可调节的计算复杂度，能够抵御暴力破解攻击。
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}