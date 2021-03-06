package com.scratbai.yueci.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.scratbai.yueci.pojo.ValidateResult;
import com.scratbai.yueci.service.UserService;

@Controller
public class Login {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Login.class);
	private static final int PERSISTENT_LOGIN_EFFECTIVE_DAYS = 30;
	private static final int STANDARD_LOGIN_EFFECTIVE_SECONDS = 1800;
	private static final int PERSISTENT_LOGIN_EFFECTIVE_SECONDS = PERSISTENT_LOGIN_EFFECTIVE_DAYS * 24 * 60 * 60;

	@Autowired
	private UserService userService;

	@RequestMapping("/login/{errorInfo}")
	public String requestLogin(@PathVariable String errorInfo, Model model)
			throws UnsupportedEncodingException {
		byte[] bytes = errorInfo.getBytes("ISO-8859-1");
		errorInfo = new String(bytes, "UTF-8");
		model.addAttribute("errorInfo", errorInfo);
		return "login";
	}

	@RequestMapping(value = "/login")
	public String requestLogin(HttpSession session, Model model) {
		Object user = session.getAttribute("user");
		Object uid = session.getAttribute("uid");
		if (user != null && uid != null) {
			return "redirect:home";
		}
		model.addAttribute("errorInfo");
		return "login";
	}

	@RequestMapping("/validate")
	public String validate(@RequestParam(required = true) String uid,
			@RequestParam(required = true) String password,
			@RequestParam(required = false) String[] rememberMe,
			HttpServletResponse response, HttpSession session, Model model) {
		ValidateResult validateResult = userService.validate(uid, password);
		if (validateResult == ValidateResult.SUCCESS) {
			if (rememberMe != null && rememberMe.length != 0) {
				rememberMe(response, uid);
			} else {
				forgiveMe(response, uid);
			}
			session.setAttribute("user", userService.getUser(uid));
			session.setAttribute("isLogin", true);
			session.setAttribute("uid", uid);
			return "redirect:home";
		}

		String result = "";
		try {
			result = "redirect:login/"
					+ URLEncoder.encode(validateResult.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void forgiveMe(HttpServletResponse response, String uid) {
		userService.forgiveMe(response, uid);
	}

	private void rememberMe(HttpServletResponse response, String uid) {
		userService.rememberMe(response, uid,
				PERSISTENT_LOGIN_EFFECTIVE_SECONDS);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
