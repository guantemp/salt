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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-12-08
 * @since JDK8.0
 */
@WebServlet(urlPatterns = {"/v1/sms"}, name = "sms", asyncSupported = false, initParams = {
        @WebInitParam(name = "cookie_expired", value = "300"), @WebInitParam(name = "accessKey", value = "LTAI4GCjirCugpiiYdM5bE3P"),
        @WebInitParam(name = "secret", value = "EslAOAuoJdbNnh14rROAiOQa8M5OoK")})
public class SmsServlet extends HttpServlet {
    private static Pattern MOBILE_PATTERN = Pattern.compile("^[1](([3][0-9])|([4][5,7,9])|([5][^4,6,9])|([6][6])|([7][3,5,6,7,8])|([8][0-9])|([9][8,9]))[0-9]{8}$");
    private static Pattern SMS_CODE_PATTERN = Pattern.compile("^\\d{6,6}$");
    private static String accessKey;
    private static String secret;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (config != null) {
            accessKey = config.getInitParameter("accessKey");
            secret = config.getInitParameter("secret");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", "13679692301");
        request.putQueryParameter("SignName", "ABC商城");
        request.putQueryParameter("TemplateCode", "SMS_206562265");
        request.putQueryParameter("TemplateParam", "{code:652546}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonFactory jasonFactory = new JsonFactory();
        JsonParser parser = jasonFactory.createParser(request.getInputStream());
        String mobile = null;
        String smsCode = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            switch (fieldname) {
                case "mobile":
                    parser.nextValue();
                    mobile = parser.getValueAsString();
                    break;
                case "smsCode":
                    parser.nextValue();
                    smsCode = parser.getValueAsString();
            }
        }
        response.setContentType("application/json; charset=UTF-8");
        JsonGenerator generator = jasonFactory.createGenerator(response.getOutputStream(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        if (validate(mobile, smsCode)) {

        } else {
            generator.writeStringField("code", "400");
            generator.writeStringField("msg", "Wrong request format");
        }
        generator.flush();
        generator.close();
    }

    private boolean validate(String mobile, String smsCode) {
        mobile = mobile.trim();
        smsCode = smsCode.trim();
        if (mobile == null || mobile.isEmpty() || !MOBILE_PATTERN.matcher(mobile).matches() ||
                smsCode == null || smsCode.isEmpty() || !SMS_CODE_PATTERN.matcher(smsCode).matches())
            return false;
        return true;
    }
}
