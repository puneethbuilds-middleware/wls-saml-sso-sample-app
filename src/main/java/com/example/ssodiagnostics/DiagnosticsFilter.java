package com.example.ssodiagnostics;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Collects what an application can observe after WebLogic completes authentication. */
@WebFilter("/*")
public class DiagnosticsFilter implements Filter {
  private static final Set<String> SECRET_HEADERS = new HashSet<>(Arrays.asList("authorization", "cookie", "set-cookie", "proxy-authorization", "x-sso-token", "x-saml-assertion", "x-jwt-assertion"));
  @Override public void init(FilterConfig config) throws ServletException { }
  @Override public void destroy() { }
  @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) request;
    Map<String,String> facts = new LinkedHashMap<>();
    facts.put("Observed at", Instant.now().toString());
    facts.put("Request URL", r.getRequestURL().toString());
    facts.put("Request method", r.getMethod());
    facts.put("Authenticated principal", principal(r));
    facts.put("Remote user", value(r.getRemoteUser()));
    facts.put("Authentication type", value(r.getAuthType()));
    facts.put("Session", r.getSession(false) == null ? "No session" : "Present; SHA-256 fingerprint=" + hash(r.getSession(false).getId()));
    facts.put("Client address", r.getRemoteAddr());
    facts.put("Forwarded-for", value(r.getHeader("X-Forwarded-For")));
    facts.put("WebLogic authenticated", r.getUserPrincipal() == null ? "No (or authentication did not reach this application)" : "Yes");
    r.setAttribute("ssoFacts", facts);
    r.setAttribute("ssoHeaders", safeHeaders(r));
    r.setAttribute("ssoToken", inspectToken(r));
    r.setAttribute("ssoRoles", roles(r));
    chain.doFilter(request, response);
  }
  private static String principal(HttpServletRequest r) { Principal p = r.getUserPrincipal(); return p == null ? "Not available" : p.getName() + " (" + p.getClass().getName() + ")"; }
  private static String value(String s) { return s == null || s.trim().isEmpty() ? "Not supplied" : s; }
  private static Map<String,String> safeHeaders(HttpServletRequest r) {
    Map<String,String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Enumeration<String> names = r.getHeaderNames();
    while (names != null && names.hasMoreElements()) { String n=names.nextElement(); String v=r.getHeader(n); result.put(n, SECRET_HEADERS.contains(n.toLowerCase()) ? "[redacted; SHA-256="+hash(v)+"]" : limited(v)); }
    return result;
  }
  private static Map<String,String> roles(HttpServletRequest r) {
    Map<String,String> result = new LinkedHashMap<>();
    String configured = r.getServletContext().getInitParameter("diagnostic.roles");
    if (configured == null || configured.trim().isEmpty()) { result.put("Role checks", "Set context-param diagnostic.roles to a comma-separated list to test roles."); return result; }
    for (String role : configured.split(",")) { role=role.trim(); if (!role.isEmpty()) result.put(role, Boolean.toString(r.isUserInRole(role))); }
    return result;
  }
  private static Map<String,String> inspectToken(HttpServletRequest r) {
    Map<String,String> result = new LinkedHashMap<>(); String token=null; String source=null;
    for (String h : new String[]{"X-SSO-Token", "X-SAML-Assertion", "X-JWT-Assertion", "Authorization"}) { if (r.getHeader(h)!=null) { token=r.getHeader(h); source="request header "+h; break; } }
    if (token == null && r.getParameter("SAMLResponse") != null) { token=r.getParameter("SAMLResponse"); source="form/query parameter SAMLResponse"; }
    if (token == null) { result.put("Status", "No assertion or token was exposed to the application."); result.put("Why this is normal", "WebLogic normally consumes the SAML response during container authentication and exposes the resulting principal, not the raw assertion."); return result; }
    if (token.startsWith("Bearer ")) token=token.substring(7);
    result.put("Source", source); result.put("Length", Integer.toString(token.length())); result.put("SHA-256 fingerprint", hash(token));
    String[] parts=token.split("\\.");
    if (parts.length == 3) { result.put("Format", "JWT (three dot-separated segments)"); result.put("JWT header", decodeJson(parts[0])); result.put("JWT claims", decodeJson(parts[1])); }
    else { result.put("Format", "SAML/XML or an opaque token"); result.put("Preview", "Raw assertions are intentionally not rendered. Use the fingerprint above to correlate server logs."); }
    return result;
  }
  private static String decodeJson(String s) { try { return limited(new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8)); } catch (Exception e) { return "Could not decode segment: "+e.getClass().getSimpleName(); } }
  private static String hash(String s) { try { byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)); StringBuilder x=new StringBuilder(); for(byte n:b)x.append(String.format("%02x",n)); return x.toString(); } catch(Exception e) { return "unavailable"; } }
  private static String limited(String s) { if(s==null)return "Not supplied"; return s.length()>2048 ? s.substring(0,2048)+" … [truncated]" : s; }
}
