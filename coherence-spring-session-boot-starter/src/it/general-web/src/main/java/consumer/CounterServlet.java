/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import java.io.IOException;
import jakarta.servlet.http.*;
public class CounterServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var session = request.getSession(); Integer count = (Integer) session.getAttribute("count");
        count = count == null ? 1 : count + 1; session.setAttribute("count", count);
        response.getWriter().print(session.getId() + ":" + count);
    }
}
