/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class ForwardingServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Match a controller that creates a session before forwarding to its view.
        request.getSession();
        request.getRequestDispatcher("/counter").forward(request, response);
    }
}
