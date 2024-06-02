package vn.hoidanit.laptopshop.controller.admin;

// import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.hoidanit.laptopshop.domain.Product;
// import vn.hoidanit.laptopshop.domain.Role;
// import vn.hoidanit.laptopshop.domain.User;

@Controller
public class ProductController {

    @GetMapping("/admin/product")
    public String getProduct() {
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getAddUserPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }
}