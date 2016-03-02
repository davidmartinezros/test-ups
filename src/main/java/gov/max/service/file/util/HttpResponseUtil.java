package gov.max.service.file.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Service
public class HttpResponseUtil {

    public void writeFile(HttpServletResponse response, InputStream fileStream, String fileName, String contentType, int fileSize) throws IOException {
        response.setContentType(contentType);
        response.setContentLength(fileSize);
        response.setHeader("content-disposition", "attachment; filename=" + fileName);
        IOUtils.copy(fileStream, response.getOutputStream());
        response.flushBuffer();
    }

    public void setError(Throwable t, HttpServletResponse response) throws IOException {
        try {
            // { "result": { "success": false, "error": "message" } }
            JSONObject responseJsonObject = error(t.getMessage());
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(responseJsonObject);
            out.flush();
        } catch (Throwable x) {
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, x.getMessage());
        }

    }

    public JSONObject error(String msg) throws ServletException {
        try {
            // { "result": { "success": false, "error": "msg" } }
            JSONObject result = new JSONObject();
            result.put("success", false);
            result.put("error", msg);
            return new JSONObject().put("result", result);
        } catch (JSONException e) {
            throw new ServletException(e);
        }
    }

    public JSONObject success(JSONObject params) throws ServletException {
        try {
            // { "result": { "success": true, "error": null } }
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("error", (Object) null);
            return new JSONObject().put("result", result);
        } catch (JSONException e) {
            throw new ServletException(e);
        }
    }
}
