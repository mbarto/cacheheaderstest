import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CacheHeadersTestServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8607468654031262350L;

	private Pattern searchDate = Pattern.compile("^now\\((.*?)\\)$",Pattern.CASE_INSENSITIVE);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String propertyFile = "headers";
		if(req.getParameter("headers") != null && !req.getParameter("headers").equals("")) {
			propertyFile = req.getParameter("headers");
		}
		Properties headers = new Properties();
		String body = "";
		headers.load(getClass().getResourceAsStream(propertyFile + ".properties"));
		
		long expires = 60000;
		String etag = "";
		
		for(Object name : headers.keySet()) {
			String headerName = name.toString();
			String headerValue = headers.get(headerName).toString();
			Matcher m = searchDate.matcher(headerValue);
			if(m.matches()) {
				int timeOffset = 0;
				try {
					timeOffset = Integer.parseInt(m.group(1));
				} catch(NumberFormatException e) {
					
				}
				if(headerName.equalsIgnoreCase("expires")) {
					expires = timeOffset;
				}
				resp.setDateHeader(headerName, System.currentTimeMillis() + timeOffset);
				body += headerName + "=" + System.currentTimeMillis() + timeOffset + "<br/>";
			} else {
				if(headerName.equalsIgnoreCase("etag")) {
					etag = headerValue;
				}
				body += headerName + "=" + headerValue + "<br/>";
				resp.setHeader(headerName, headerValue);
			}
		}
		/*resp.setHeader("cache-control", "public");
		resp.setDateHeader("expires", System.currentTimeMillis() + 60000);
		resp.setDateHeader("last-modified", System.currentTimeMillis());
		resp.setDateHeader("date", System.currentTimeMillis());*/
		if(req.getHeader("if-none-match")!=null && !req.getHeader("if-none-match").equals("")  && req.getHeader("if-none-match").equals(etag)) {
			resp.setStatus(304);
		} else if(req.getDateHeader("if-modified-since")>0 && (System.currentTimeMillis() - req.getDateHeader("if-modified-since"))  < expires) {
			resp.setStatus(304);
		} else {
			resp.setStatus(200);
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			out.println("<html>");
			out.println("<head>");
			out.println("</head>");
			out.println("<title>CacheHeadersTestServlet</title>");
			out.println("<body>");
			out.println(body);
			out.println("</body>");
			out.println("</html>");
		}
	}
}
