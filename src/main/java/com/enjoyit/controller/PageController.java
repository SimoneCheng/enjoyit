package com.enjoyit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/dashboard/publish")
    public String publishPage() {
        return "publish";
    }

    @GetMapping("/dashboard/menu")
    public String menuPage() {
        return "menu";
    }

    @GetMapping("/dashboard/orders")
    public String ordersPage() {
        return "orders";
    }
}
