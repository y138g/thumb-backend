package com.itgr.thumbbackend;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itgr.thumbbackend.model.empty.User;
import com.itgr.thumbbackend.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class ThumbBackendApplicationTests {

    @Resource
    private UserService userService;

    @Resource
    private MockMvc mockMvc;

    @Test
    void testLoginAndExportSessionToCsv() throws Exception {
        List<User> list = userService.list();
        ObjectMapper objectMapper = new ObjectMapper();

        try (PrintWriter writer = new PrintWriter(new FileWriter("satoken_output.csv", true))) {
            // 如果文件是第一次写入，可以加一个逻辑写表头
            writer.println("userId,tokenValue,timestamp");

            for (User user : list) {
                long testUserId = user.getId();

                // 创建登录请求体
                String requestBody = "{\"id\":" + testUserId + "}";

                MvcResult result = mockMvc.perform(post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn();

                // 验证响应状态码
                int status = result.getResponse().getStatus();
                assertThat(status).isEqualTo(200);

                // 解析JSON响应体，从响应中获取用户信息
                String responseBody = result.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                assertThat(jsonNode.get("code").asInt()).isEqualTo(0); // 确认响应成功

                // 检查Set-Cookie头中的satoken
                String tokenValue = null;
                List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
                if (!setCookieHeaders.isEmpty()) {
                    tokenValue = setCookieHeaders.stream()
                            .filter(cookie -> cookie.startsWith("satoken="))
                            .map(cookie -> cookie.split(";")[0].replace("satoken=", ""))
                            .findFirst()
                            .orElse(null);
                }

                // 如果cookie中没找到，尝试获取header中的token
                if (tokenValue == null) {
                    tokenValue = result.getResponse().getHeader("satoken");
                }

                // 如果仍为null，记录为"token-not-found"
                if (tokenValue == null) {
                    tokenValue = "token-not-found";
                }

                writer.printf("%d,%s,%s%n", testUserId, tokenValue, LocalDateTime.now());

                System.out.println("✅ 写入 CSV：" + testUserId + " -> " + tokenValue);
            }
        }
    }

    /**
     * 新增20w用户
     */
    @Test
    void addUser() {
        for (int i = 0; i < 200000; i++) {
            User user = new User();
            user.setUsername(RandomUtil.randomString(6));
            userService.save(user);
        }
    }


    @Test
    void contextLoads() {
    }

}
