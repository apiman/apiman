package io.apiman.portal;

import java.io.IOException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * SPA Servlet
 *
 * @author msavy
 */
public class AngularServlet extends GenericServlet {

    private static final long serialVersionUID = 8545062359275971153L;

    /**
     * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try (var stream = getServletContext().getResourceAsStream("/index.html")) {
            long len = stream.transferTo(res.getOutputStream());
            res.setContentLength(Math.toIntExact(len));
        }
        res.setContentType("text/html; charset=UTF-8");
        res.flushBuffer();
    }

}
