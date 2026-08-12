<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SAML SSO Test Application</title>

  <style>
    <%@ include file="/WEB-INF/style.css" %>

    .simple-page {
      max-width: 880px;
      margin: 36px auto;
      padding: 0 22px 48px;
    }

    .simple-page header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 18px;
      padding: 0 0 20px;
      border-bottom: 1px solid #dbe3ef;
    }

    .simple-page h1 {
      font-size: 30px;
    }

    .simple-page h2 {
      margin: 0 0 10px;
      font-size: 21px;
    }

    .simple-page p {
      max-width: 760px;
    }

    .simple-page section {
      margin-top: 24px;
      padding-top: 22px;
      border-top: 1px solid #dbe3ef;
    }

    .simple-page .intro {
      margin: 20px 0 0;
      color: #475467;
      font-size: 17px;
    }

    .simple-page .plain-note {
      padding: 16px 18px;
      border-left: 4px solid #155eef;
      background: #f5f8ff;
      color: #344054;
    }

    .simple-page ol {
      padding-left: 23px;
    }

    .simple-page li {
      margin: 10px 0;
    }

    .simple-page .small {
      color: #667085;
      font-size: 13px;
    }

    .simple-page .top-test {
      flex: 0 0 auto;
      white-space: nowrap;
    }

    @media (max-width: 720px) {
      .simple-page {
        margin: 24px auto;
        padding: 0 16px 34px;
      }

      .simple-page h1 {
        font-size: 26px;
      }

      .simple-page header {
        align-items: flex-start;
        flex-direction: column;
      }

      .simple-page .top-test {
        width: 100%;
        justify-content: center;
      }
    }
  </style>
</head>

<body>
  <main class="simple-page">
    <header>
      <div>
        <p class="eyebrow">WebLogic SAML Sample Application</p>
        <h1>SAML SSO Sample Application</h1>
      </div>

      <a class="button top-test" href="restricted/diagnostics.jsp">Test SSO</a>
    </header>

    <p class="intro">
      Click <strong>Test SSO</strong> to validate the WebLogic SAML configuration
      with this sample application.
    </p>

    <section>
      <h2>Why test with this sample App?</h2>

      <p>
        SAML Service Provider configuration in WebLogic is independent of the
        application deployed on that server. Any deployed application can
        participate in SSO when its protected page URI is added to the SAML
        partner Redirect URIs.
      </p>

      <div class="plain-note">
          If this sample signs in successfully, it confirms that the WebLogic SAML configuration is validated and working correctly for this server. You can then focus on your custom application's protected URI, role mapping, deployment configuration, or context path.

      </div>
    </section>

    <section>
      <h2>Configure this sample Application for SSO</h2>

      <ol>
        <li>Deploy this WAR on the WebLogic server where you want to test SAML.</li>

        <li>
          In the WebLogic Console, go to
          <strong>
            Security Realms → your realm → Providers → SAML2 Identity Asserter
            → Management → your partner
          </strong>.
        </li>

        <li>
          In <strong>Redirect URIs</strong>, add this protected page URI:
          <br>
          <code>/saml-sso-diagnostics/restricted/diagnostics.jsp</code>
        </li>

        <li>Save and activate the change.</li>
      </ol>

      
    </section>

    <section>
      <h2>Configure Sign out (SAML SLO)</h2>

      <p>
        The Sign out button completely signs the user out only when SAML Single
        Logout is configured. (Single Logout is supported from WebLogic version 12.2.1.4 and above)
      </p>

      <ol>
        <li>
          Apply the latest WebLogic PSU/SPB.
        </li>

        <li>
          In the WebLogic Console, go to
          <strong>
            Environment → Servers → server where saml is configured → Configuration →
            Federation Services → SAML 2.0 Service Provider
          </strong>,
          then enable Single Logout. 
        </li>
In the WebLogic Remote Console, go to
          <strong>
            Edit Tree → Servers → server where saml is configured → Security →
            Saml 2.0 Service Provider → 
          </strong>
          then turn ON Single Logout Enabled. 

        <li>
          Add the exact externally accessible application URL to
          <strong>Allowed Redirect URIs</strong>. For example:
          <br>
          <code>http://localhost:7001/saml-sso-diagnostics/</code>
        </li>

        <li>
          Configure the IdP partner's Single Logout endpoint and confirm that
          the IdP supports SAML SLO.
        </li>
      </ol>

      <p class="small">
        If the application is deployed on port 8001, use:
        <br>
        <code>Allowed Redirect URIs: http://localhost:8001/saml-sso-diagnostics/</code>
        <br><br>

        If users access the application through a load balancer or web server,
        use the external URL instead, for example:
        <br>
        <code>Allowed Redirect URIs: https://sso.example.com/saml-sso-diagnostics/</code>
      </p>
    </section>
  </main>
</body>
</html>
