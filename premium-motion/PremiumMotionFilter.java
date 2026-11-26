package com.rishi.bankweb;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Adds the optional motion stylesheet and script to HTML responses without
 * changing the existing Thymeleaf templates or banking controller logic.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PremiumMotionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapped);
            String contentType = wrapped.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("text/html")
                    && wrapped.getStatus() == HttpServletResponse.SC_OK) {
                byte[] original = wrapped.getContentAsByteArray();
                Charset charset = StandardCharsets.UTF_8;
                if (wrapped.getCharacterEncoding() != null) {
                    try { charset = Charset.forName(wrapped.getCharacterEncoding()); }
                    catch (Exception ignored) { /* Keep UTF-8 fallback. */ }
                }
                String html = new String(original, charset);
                String css = "<link rel=\"stylesheet\" href=\"/css/premium-motion.css\">";
                String js = "<script src=\"/js/premium-motion.js\" defer></script>";
                if (!html.contains("/css/premium-motion.css") && html.toLowerCase().contains("</head>")) {
                    html = html.replaceFirst("(?i)</head>", java.util.regex.Matcher.quoteReplacement(css + "</head>"));
                }
                if (!html.contains("/js/premium-motion.js") && html.toLowerCase().contains("</body>")) {
                    html = html.replaceFirst("(?i)</body>", java.util.regex.Matcher.quoteReplacement(js + "</body>"));
                }
                wrapped.resetBuffer();
                wrapped.getOutputStream().write(html.getBytes(charset));
            }
        } finally {
            wrapped.copyBodyToResponse();
        }
    }
}
