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

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.l1.concurrentMap.ConcurrentMapCacheBuilder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-12-08
 * @since JDK8.0
 */
@WebServlet(urlPatterns = {"/v1/sms"}, name = "sms", asyncSupported = false, initParams = {
        @WebInitParam(name = "expire", value = "5*60*1000"), @WebInitParam(name = "accessKey", value = ""),
        @WebInitParam(name = "secret", value = ""), @WebInitParam(name = "signName", value = "ABC商城"),
        @WebInitParam(name = "templateCode", value = "SMS_206562265")})
public class SmsServlet extends HttpServlet {
    private static Pattern MOBILE_PATTERN = Pattern.compile("^[1](([3][0-9])|([4][5,7,9])|([5][^4,6,9])|([6][6])|([7][3,5,6,7,8])|([8][0-9])|([9][8,9]))[0-9]{8}$");
    private static Pattern SMS_CODE_PATTERN = Pattern.compile("^\\d{6,6}$");
    private static Cache<String, Integer> cache = new ConcurrentMapCacheBuilder<String, Integer>("sms").expired(15, TimeUnit.MINUTES).build();
    private static String accessKey;
    private static String secret;
    private static String signName;
    private static String templateCode;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (config != null) {
            accessKey = config.getInitParameter("accessKey");
            secret = config.getInitParameter("secret");
            signName = config.getInitParameter("signName");
            templateCode = config.getInitParameter("templateCode");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String mobile = req.getParameter("mobile");
        JsonFactory jasonFactory = new JsonFactory();
        resp.setContentType("application/json; charset=UTF-8");
        JsonGenerator generator = jasonFactory.createGenerator(resp.getOutputStream(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        generator.writeStartObject();
        if (!validate(mobile)) {
            generator.writeNumberField("code", 400);
            generator.writeStringField("msg", "错误的手机号码");
        } else {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secret);
            IAcsClient client = new DefaultAcsClient(profile);
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", "cn-hangzhou");
            request.putQueryParameter("PhoneNumbers", mobile);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", templateCode);
            int saveSmsCode = randomSixNumber();
            request.putQueryParameter("TemplateParam", "{code:" + saveSmsCode + "}");
            try {
                CommonResponse response = client.getCommonResponse(request);
                JsonParser parser = jasonFactory.createParser(response.getData());
                String code = null;
                String msg = null;
                while (!parser.isClosed()) {
                    JsonToken jsonToken = parser.nextToken();
                    if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                        String fieldName = parser.getCurrentName();
                        parser.nextToken();
                        switch (fieldName) {
                            case "Code":
                                code = parser.getValueAsString();
                                break;
                            case "Message":
                                msg = parser.getValueAsString();
                                break;
                        }
                    }
                }
                if (code.equals("OK")) {
                    cache.put(mobile, saveSmsCode);
                    generator.writeNumberField("code", 200);
                    generator.writeStringField("msg", "验证码已发送");
                } else {
                    generator.writeNumberField("code", 201);
                    generator.writeStringField("msg", msg);
                }
            } catch (ClientException e) {
                generator.writeNumberField("code", 401);
                generator.writeStringField("msg", "网络错误！验证码未能发送，请稍后再试。");
            }
        }
        generator.writeEndObject();
        generator.flush();
        generator.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mobile = null;
        int smsCode = 0;
        JsonFactory jasonFactory = new JsonFactory();
        JsonParser parser = jasonFactory.createParser(request.getInputStream());
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                switch (fieldName) {
                    case "mobile":
                        mobile = parser.getValueAsString();
                        break;
                    case "smsCode":
                        smsCode = parser.getValueAsInt();
                        break;
                }
            }
        }
        response.setContentType("application/json; charset=UTF-8");
        JsonGenerator generator = jasonFactory.createGenerator(response.getOutputStream(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        boolean checked = true;
        generator.writeStartObject();
        if (!validate(mobile)) {
            generator.writeNumberField("code", 400);
            generator.writeStringField("msg", "错误的手机号码!");
            checked = false;
        }
        if (!validate(smsCode)) {
            generator.writeNumberField("code", 400);
            generator.writeStringField("msg", "验证码格式错误!");
            checked = false;
        }
        if (checked) {
            Integer savedSmsCode = cache.get(mobile);
            if (savedSmsCode != null && savedSmsCode.intValue() == smsCode) {
                generator.writeNumberField("code", 200);
                generator.writeStringField("msg", "ok");
            } else {
                generator.writeNumberField("code", 201);
                generator.writeStringField("msg", "短信验证码不正确或已过期。");
            }
        }
        generator.writeEndObject();
        generator.flush();
        generator.close();
    }

    private boolean validate(String mobile) {
        if (mobile == null || mobile.isEmpty() || !MOBILE_PATTERN.matcher(mobile).matches())
            return false;
        return true;
    }

    private boolean validate(int smsCode) {
        if (smsCode == 0 || !SMS_CODE_PATTERN.matcher(String.valueOf(smsCode)).matches())
            return false;
        return true;
    }

    private int randomSixNumber() {
        return (int) ((Math.random() * 6 + 1) * 100000);
    }
}
