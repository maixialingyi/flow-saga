package com.flow.saga.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    //不同环境配置是否启动swagger
    @Value("${swagger.enable}")
    private Boolean swaggerEnable;
    
    public static final String VERSION = "1.0.0";

    //构建 api文档的详细信息函数,注意这里的注解引用的是哪个
    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 页面标题
                .title("springcloud swagger api")
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .termsOfServiceUrl("")
                .version(VERSION)
                .contact(new springfox.documentation.service.Contact("jiangshaoyue", "", "18518505760@163.com"))
                .build();
    }

    //swagger2的配置文件，这里可以配置swagger2的一些基本的内容，比如扫描的包等等
    @Bean
    public Docket customDocket() {
        List<Parameter> pars = new ArrayList<Parameter>();
        ParameterBuilder parameterBuilder0 = new ParameterBuilder();
        parameterBuilder0.name("Authorization").description("token").modelRef(new ModelRef("string"))
                .parameterType("header").required(false).build();

        pars.add(parameterBuilder0.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .build()
                .apiInfo(apiInfo())
                .enable(swaggerEnable)
                .globalOperationParameters(pars);
    }
}
