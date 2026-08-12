# SAML SSO Diagnostics for WebLogic

Use this WAR to verify a WebLogic Server SAML 2.0 Service Provider integration. It exposes the authentication and request details that are safely available to an application after WebLogic completes SSO.

## Prerequisites

- Oracle WebLogic Server 12.2.1.4 or 14.1.1.x.
- JDK 8 and Maven 3.6 or later to build the WAR.
- Access to deploy the application and configure SAML 2.0 federation services in the target WebLogic domain.
- A configured identity provider and a protected application path (`/restricted/*` by default).

## Build and deploy

```bash
mvn clean package
```

Deploy `target/saml-sso-diagnostics.war` from the WebLogic Administration Console. The default context root is `/saml-sso-diagnostics/`.

Configure your WebLogic SAML integration so `/restricted/*` is protected. The generated `WEB-INF/weblogic.xml` assigns the `sso-user` application role to WebLogic's built-in `users` runtime group, which contains all authenticated users.

## Verify SSO

1. Open `http://<wls-host>:<wls-port>/saml-sso-diagnostics/`.
2. Select **Start SSO test** and complete authentication with the configured identity provider.
3. Confirm that the protected result page shows **SSO login succeeded** and the expected authenticated principal.
4. Review the displayed authentication facts, role checks, and request context. Sensitive request headers are redacted and tokens are represented only by a SHA-256 fingerprint.

If access is denied before the application runs, review the WebLogic server log and the identity provider audit log using the timestamp and request details shown on the error page.

## What it reports

After authentication, the application reports the WebLogic principal, remote user, authentication type, session, client/request context, role checks, and safely redacted request headers.

## Important limitation

With WebLogic as the SAML service provider, WebLogic consumes and validates the SAML response before dispatching the protected request. Therefore, raw SAML assertion XML is normally **not available to a WAR**. That is correct and desirable. The principal and roles shown here are the portable proof of the successful handoff. For assertion-level diagnostics use WebLogic federation/security debug logs and the identity provider's audit log. The generated error page gives a safe troubleshooting checklist, but container-level failures can still occur before the application error page runs.

## Compatibility

The project uses Servlet 3.1 / `javax.servlet`, appropriate for conventional WebLogic 12.2.1.x and 14.1.1.x deployments. It has no dependency on a particular identity provider.

## Single Logout

The **Sign out** button initiates WebLogic SAML Single Logout at `/saml2/sp/slo/init`; it does not merely clear the local application session. To use it, configure WebLogic as a SAML Service Provider with SLO enabled, import/configure the IdP's SLO endpoint, and allow the post-logout URL in **Allowed redirect URIs**. For the default context root, allow the full external URL ending in `/saml-sso-diagnostics/`.

Set the `slo.redirect-uri` context parameter in `WEB-INF/web.xml` to that full external URL when the application is behind a reverse proxy or load balancer. Ensure the WebLogic installation is patched with the latest PSU/SPB. SLO works with WebLogic version 12214 and above.

## Notes

- This application does not expose raw SAML assertions. WebLogic validates and consumes them before dispatching the protected request.
- In a clustered production environment, follow Oracle's SAML guidance for the security store and replicated cache.
- Enable WebLogic SAML debugging only while collecting diagnostic evidence, then disable it.

## Maintainer

Puneeth Prakash [@puneethbuilds-middleware](https://github.com/puneethbuilds-middleware)

## License

See [LICENSE](LICENSE).

## Reference

- Oracle WebLogic Server: [Configuring SAML 2.0 Services](https://docs.oracle.com/en/middleware/standalone/weblogic-server/14.1.1.0/secmg/saml20.html)
