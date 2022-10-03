package com.sparta.cookbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
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
public class SwaggerConfig{

    //스웨거 페이지에 소개될 설명들
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Frigo")
                .description("우리집 식재료 동반자")
                .build();
    }

    @Bean
    public Docket commonApi() {
        //Authentication header 처리를 위해 사용
        ParameterBuilder aParameterBuilder = new ParameterBuilder();
        aParameterBuilder.name("Authorization") //헤더 이름
                .description("Access_Token") //설명
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();

        ParameterBuilder aParameterBuilder2 = new ParameterBuilder();
        aParameterBuilder2.name("Refresh_Token") //헤더 이름
                .description("Refresh_Token") //설명
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();

        List<Parameter> aParameters = new ArrayList<>();
        aParameters.add(aParameterBuilder.build());
        aParameters.add(aParameterBuilder2.build());


        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("example")//빈설정을 여러개 해줄경우 구분하기 위한 구분자.
                .globalOperationParameters(aParameters)
                .apiInfo(this.apiInfo())//스웨거 설명
                .select()//apis, paths를 사용하주기 위한 builder
                .apis(RequestHandlerSelectors.any())//탐색할 클래스 필터링
                .paths(PathSelectors.any())//스웨거에서 보여줄 api 필터링
                .build();
    }
}
