package com.qilin.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 获取客户端 IP。
 *
 * <p>部署到 Nginx 之后，request.getRemoteAddr() 拿到的是 Nginx 的地址，
 * 真实 IP 在 X-Forwarded-For 里（Nginx 需要配 proxy_set_header）。
 * 注意：这些请求头可以被伪造，只适合写日志/做风控参考，不能作为安全凭据。
 */
public final class IpUtils {

    private static final String UNKNOWN = "unknown";

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能是 "客户端IP, 代理1, 代理2"，取第一个
                int comma = ip.indexOf(',');
                return (comma > 0 ? ip.substring(0, comma) : ip).trim();
            }
        }
        return request.getRemoteAddr();
    }
}
