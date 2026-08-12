# SAML SSO Diagnostics for WebLogic

## Build and deploy

```bash
mvn clean package
# Deploy target/saml-sso-diagnostics.war from the WebLogic Administration Console.
```

Open `/saml-sso-diagnostics/`, then select **Start SSO test**. Configure your WebLogic SAML integration so `/restricted/*` is protected. The generated `WEB-INF/weblogic.xml` assigns the `sso-user` application role to WebLogic's built-in `users` runtime group, which contains all authenticated users. The protected page prominently displays the authenticated user name.

## What it reports

After authentication, the application reports the WebLogic principal, remote user, authentication type, session, client/request context, role checks, and safely redacted request headers.

## Important limitation

With WebLogic as the SAML service provider, WebLogic consumes and validates the SAML response before dispatching the protected request. Therefore, raw SAML assertion XML is normally **not available to a WAR**. That is correct and desirable. The principal and roles shown here are the portable proof of the successful handoff. For assertion-level diagnostics use WebLogic federation/security debug logs and the identity provider's audit log. The generated error page gives a safe troubleshooting checklist, but container-level failures can still occur before the application error page runs.

## Compatibility

The project uses Servlet 3.1 / `javax.servlet`, appropriate for conventional WebLogic 12.2.1.x and 14.1.1.x deployments. It has no dependency on a particular identity provider.

## Single Logout

The **Sign out** button initiates WebLogic SAML Single Logout at `/saml2/sp/slo/init`; it does not merely clear the local application session. To use it, configure WebLogic as a SAML Service Provider with SLO enabled, import/configure the IdP's SLO endpoint, and allow the post-logout URL in **Allowed redirect URIs**. For the default context root, allow the full external URL ending in `/saml-sso-diagnostics/`.

Set the `slo.redirect-uri` context parameter in `WEB-INF/web.xml` to that full external URL when the application is behind a reverse proxy or load balancer. Ensure the WebLogic installation is patched with the latest PSU/SPB. SLO works with WebLogic version 12214 and above.
