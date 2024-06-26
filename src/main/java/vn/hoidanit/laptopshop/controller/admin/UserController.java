package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
// import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Role;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.RoleService;
import vn.hoidanit.laptopshop.service.UploadService;
import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
            UploadService uploadService,
            PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @RequestMapping("/admin/user")
    public String getUserPage(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        Page<User> pagelist = this.userService.getAllUsers(pageable);
        List<User> users = pagelist.getContent();
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagelist.getTotalPages());
        return "admin/user/show";
    }

    @RequestMapping("/admin/user/{iduser}")
    public String getUserDetailPage(Model model, @PathVariable long iduser) {
        User user = this.userService.getUserById(iduser);
        model.addAttribute("infoUser", user);
        return "admin/user/detail";
    }

    @GetMapping("/admin/user/create")
    public String getAddUserPage(Model model) {
        List<Role> roles = this.roleService.getAllRole();
        model.addAttribute("listRole", roles);
        model.addAttribute("newUser", new User()); // new User() là contractor được tạo tự động bên domain
        return "admin/user/create";
    }

    @PostMapping(value = "/admin/user/create")
    public String createUserPage(Model model,
            @ModelAttribute("newUser") @Valid User hoidanit,
            BindingResult newUserBindingResult,
            @RequestParam("avatarFile") MultipartFile file) {
        // newUser <=> modelAttribute="newUser" trong form
        // avatarFile <=> name="avatarFile" trong thẻ input type=file của form

        List<FieldError> errors = newUserBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }

        // validate
        if (newUserBindingResult.hasErrors()) {
            List<Role> roles = this.roleService.getAllRole();
            model.addAttribute("listRole", roles);
            return "admin/user/create";
        }
        //
        String avatar = this.uploadService.handleSaveUpLoadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(hoidanit.getPassword());

        // save
        hoidanit.setAvatar(avatar);
        hoidanit.setPassword(hashPassword);
        hoidanit.setRole(this.userService.getRoleByName(hoidanit.getRole().getName()));
        this.userService.handleSaveUser(hoidanit);
        return "redirect:/admin/user";
    }

    @RequestMapping("/admin/user/update/{iduser}")
    public String getUpdateUserPage(Model model, @PathVariable long iduser) {
        User currentUser = this.userService.getUserById(iduser);
        model.addAttribute("updateUser", currentUser);
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(Model model,
            @ModelAttribute("updateUser") @Valid User hoidanit,
            BindingResult bindingResult,
            @RequestParam("avatarFile") MultipartFile file) {
        // updateUser <=> modelAttribute="updateUser" trong form update
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        User currentUser = this.userService.getUserById(hoidanit.getId());

        if (bindingResult.hasErrors()) {
            return "admin/user/update";
        }

        if (currentUser != null) {
            if (!file.isEmpty()) {
                String newAvatar = this.uploadService.handleSaveUpLoadFile(file, "avatar");
                currentUser.setAvatar(newAvatar);
            }
            currentUser.setPhone(hoidanit.getPhone());
            currentUser.setFullName(hoidanit.getFullName());
            currentUser.setAddress(hoidanit.getAddress());
            currentUser.setRole(this.userService.getRoleByName(hoidanit.getRole().getName()));
            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/admin/user/delete/{iduser}")
    public String getDeleteUserPage(Model model, @PathVariable long iduser) {
        User deleteUser = this.userService.getUserById(iduser);
        model.addAttribute("deleteUser", deleteUser);
        return "admin/user/delete";
    }

    @PostMapping("/admin/user/delete")
    public String postDeleteUser(Model model, @ModelAttribute("newUser") User hoidanit) {
        this.userService.deleteUserByID(hoidanit.getId());
        return "redirect:/admin/user";
    }
}
