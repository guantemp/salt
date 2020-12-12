/*
 * Copyright (c) 2020. www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package salt.hoprxi.egde.servlet;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-12-09
 * @since JDK8.0
 */
@WebServlet(urlPatterns = {"/v1/wxAuth"}, name = "wxAuth", asyncSupported = false, initParams = {
        @WebInitParam(name = "appid", value = "wx16772085b996e8e0"), @WebInitParam(name = "secret", value = "")})
public class WxAuthServlet extends HttpServlet {
    private static String appid = null;
    private static String secret = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (config != null) {
            appid = config.getInitParameter("appid");
            secret = config.getInitParameter("secret");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        JsonFactory jasonFactory = new JsonFactory();
        resp.setContentType("application/json; charset=UTF-8");
        JsonGenerator generator = jasonFactory.createGenerator(resp.getOutputStream(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        if (code == null || code.isEmpty()) {
            generator.writeNumberField("code", 401);
            generator.writeStringField("msg", "网络错误！验证码未能发送，请稍后再试。");
        } else {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                URI uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.weixin.qq.com")
                        .setPathSegments("sns", "jscode2session")
                        .setParameter("grant_type", "authorization_code")
                        .setParameter("appid", appid)
                        .setParameter("secret", secret)
                        .setParameter("code", code)
                        .build();
                //System.out.println(uri.toURL().toExternalForm());
                HttpGet httpGet = new HttpGet(uri);
                try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                    System.out.println(response.getEntity());
                    HttpEntity entity = response.getEntity();
                    // do something useful with the response body
                    // and ensure it is fully consumed
                    EntityUtils.consume(entity);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        generator.flush();
        generator.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
