package com.example.score.web;

import com.example.score.model.AuthUser;
import com.example.score.service.AuthService;
import com.example.score.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 登录控制器，对应 /login。
 *
 * <p>GET：显示登录页；如果已经登录则直接跳到对应首页。</p>
 * <p>POST：调用 AuthService 校验账号密码，成功后把 AuthUser 放进 Session，
 * 再按角色跳转到教师工作台或学生成绩页。</p>
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AuthUser user = WebUtil.currentUser(request);
        if (user != null) {
            redirectByRole(request, response, user);
            return;
        }
        if ("1".equals(request.getParameter("logout"))) {
            request.setAttribute("info", "你已安全退出系统");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        AuthUser current = WebUtil.currentUser(request);
        if (current != null) {
            redirectByRole(request, response, current);
            return;
        }

        String account = WebUtil.trim(request.getParameter("account"));
        String password = request.getParameter("password");
        request.setAttribute("account", account);

        if (account.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "请输入账号和密码");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        AuthUser user = authService.login(account, password);
        if (user == null) {
            request.setAttribute("error", "账号或密码错误，请检查后重试");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        // 登录成功：创建 Session 并更换 Session ID（防止会话固定攻击）
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("currentUser", user);
        session.setMaxInactiveInterval(30 * 60); // 30 分钟无操作自动退出

        // 如果之前被过滤器记住过原地址，登录后跳回去
        String redirect = (String) session.getAttribute("loginRedirect");
        session.removeAttribute("loginRedirect");
        if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")
                && (user.isTeacher() ? redirect.startsWith("/teacher/") : redirect.startsWith("/student/"))) {
            response.sendRedirect(request.getContextPath() + redirect);
        } else {
            redirectByRole(request, response, user);
        }
    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, AuthUser user)
            throws IOException {
        String target = user.isTeacher() ? "/teacher/dashboard" : "/student/dashboard";
        response.sendRedirect(request.getContextPath() + target);
    }
}
