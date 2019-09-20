package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 *
 * @author wujing
 * @since 2019/9/17 17:54
 */
@RestController
@RequestMapping("/limit")
public class LimitController {

    @GetMapping("test")
    public String test() {

        return "test";
    }

}
