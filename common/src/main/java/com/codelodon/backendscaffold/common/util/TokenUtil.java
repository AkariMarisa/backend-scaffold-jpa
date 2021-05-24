package com.codelodon.backendscaffold.common.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.security.Key;

public class TokenUtil {
    final private static TokenUtil INSTANCE = new TokenUtil();

    /**
     * 用于签发 JWT 的根密钥
     */
    final private static String ROOT_KEY = "s07p9+OCqzWyitdII5NEwVw2YdRO39W8AVin6B6OFWEfDcaEynIuRXs9RNUOATzQ/TAdlbfhr7liTdSbcDPOpw==";

    final private static Key KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ROOT_KEY));

    final private static RedisClient REDIS_CLIENT = RedisClient.create("redis://localhost:6379/0");

    /**
     * Token 生存时间 (秒)
     */
    final private static long TOKEN_TTL = 60 * 60 * 24 * 7;

    private TokenUtil() {
    }

    public static TokenUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 生成 Token
     *
     * @param subject 随带的内容
     * @return 加密后的 Token
     */
    public String generateToken(String subject) {
        return Jwts.builder().setSubject(subject).signWith(KEY).compact();
    }

    /**
     * 保存 Token
     *
     * @param id    Token 的标识，用于获取 Token
     * @param token 需要保存的 Token
     */
    public void saveToken(String id, String token) {
        StatefulRedisConnection<String, String> connection = REDIS_CLIENT.connect();
        connection.sync().set(id, token);
        connection.sync().expire(id, TOKEN_TTL);
        connection.close();
    }

    /**
     * 根据标识获取 Token
     *
     * @param id Token 的标识
     * @return 对应的 Token
     */
    public String getToken(String id) {
        StatefulRedisConnection<String, String> connection = REDIS_CLIENT.connect();
        String token = connection.sync().get(id);
        connection.close();
        return token;
    }

    /**
     * 重新刷新 Token 的过期时间
     *
     * @param id Token 的标识
     */
    public void renewToken(String id) {
        StatefulRedisConnection<String, String> connection = REDIS_CLIENT.connect();
        connection.sync().expire(id, TOKEN_TTL);
        connection.close();
    }

    /**
     * 删除 Token
     *
     * @param ids Token 的标识
     */
    public void deleteToken(String... ids) {
        StatefulRedisConnection<String, String> connection = REDIS_CLIENT.connect();
        connection.sync().del(ids);
        connection.close();
    }

    /**
     * 解析出 Token 中的内容
     *
     * @param token 需要解析的 Token
     * @return Token 中的内容
     */
    public String decodeToken(String token) {
        return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token).getBody().getSubject();
    }
}
