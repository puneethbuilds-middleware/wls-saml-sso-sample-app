package com.example.ssodiagnostics;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Initiates WebLogic SAML Single Logout; WebLogic clears the local session as part of SLO. */
public class LogoutServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String configured = getServletContext().getInitParameter("slo.redirect-uri");
    String redirectUri = configured == null || configured.trim().isEmpty()
        ? request.getScheme() + "://" + request.getServerName() + port(request) + request.getContextPath() + "/"
        : configured.trim();
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.sendRedirect("/saml2/sp/slo/init?slo_redirect_uri=" + java.net.URLEncoder.encode(redirectUri, "UTF-8"));
  }
  private static String port(HttpServletRequest request) {
    int port = request.getServerPort();
    return (request.isSecure() && port == 443) || (!request.isSecure() && port == 80) ? "" : ":" + port;
  }
}
