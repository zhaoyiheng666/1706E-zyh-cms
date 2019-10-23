package com.zhaoyiheng.cms.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.zhaoyiheng.cms.comon.ConstClass;
import com.zhaoyiheng.cms.entity.Article;
import com.zhaoyiheng.cms.entity.User;
import com.zhaoyiheng.cms.service.ArticleService;
import com.zhaoyiheng.cms.service.UserService;
import com.zhaoyiheng.cms.web.PageUtils;

@Controller
@RequestMapping("user")
public class UserController {
	
	@Autowired
	UserService userService;
	
	
	@Autowired
	ArticleService articleService;
	
	
	
	
    //@PostMapping// 只能接受post方法的请求
	//@RequestMapping(value = "register",method=RequestMethod.GET)
	@GetMapping("register")  // 只接受get的请求
	public String register() {
		return "user/register";
	}
	
	@RequestMapping("index")
	public String index() {
		return "user/index";
	}
	
	/**
	 * 判断用户名是否已经被占用
	 * @param username
	 * @return
	 */
	@RequestMapping("checkExist")
	@ResponseBody
	public boolean checkExist(String username) {
		return !userService.checkUserExist(username);
	}
	
	@PostMapping("register")  // 只接受POst的请求\
	public String register(HttpServletRequest request,
			@Validated User user,
			BindingResult errorResult) {
		if(errorResult.hasErrors()) {
			return "user/register";
		}
		
		int result = userService.register(user);
		if(result>0) {
			return "redirect:login";
		}else {
			request.setAttribute("errorMsg", "系统错误，请稍后重试");
			return "user/register";
		}
		
		
		
	}
	
	
	@RequestMapping(value = "login",method=RequestMethod.GET)
	public String login() {
		return "user/login";
	}
	
	@RequestMapping("logout")
	public String logout(HttpServletRequest request) {
		request.getSession().removeAttribute(ConstClass.SESSION_USER_KEY);
		return "user/login";
	}
	
	@PostMapping("login")  // 只接受POst的请求
	public String login(HttpServletRequest request,
			@Validated User user,
			BindingResult errorResult) {
		
		if(errorResult.hasErrors()) {
			return "login";
		}
		
		//登录
		User loginUser = userService.login(user);
		if(loginUser==null) {
			request.setAttribute("errorMsg", "用户名密码错误");
			return "user/login";
		}else if(loginUser.getLocked() == 0){
			//用户信息保存在session当中
			request.getSession().setAttribute(ConstClass.SESSION_USER_KEY, loginUser);
			//普通注册用户
			if(loginUser.getRole()==ConstClass.USER_ROLE_GENERAL) {
				return "redirect:home";	
			//管理员用户	
			}else if(loginUser.getRole()==ConstClass.USER_ROLE_ADMIN){
				return "redirect:../admin/index";	
			}else {
				// 其他情况
				return "user/login";
			}
		}else {
			request.setAttribute("errorMsg", "您的账号已被冻结！");
			return "user/login";
		}
	}
	
	
	/**
	 * 进入个人中心(普通注册用户)
	 * @param request
	 * @return
	 */
	@RequestMapping("home")
	public String home(HttpServletRequest request) {
		return "my/home";
	}
	
	
	/**
	 * 删除用户自己的文章
	 * @param id 文章id
	 * @return
	 */
	@RequestMapping("delArticle")
	@ResponseBody
	public boolean delArticle(Integer id) {
		return articleService.remove(id)>0;
	}
	
	/**
	 * 进入个人中心 获取我的文章
	 * @param request
	 * @return
	 */
	@RequestMapping("myarticlelist")
	public String myarticles(HttpServletRequest request,
			@RequestParam(defaultValue="1") Integer page) {
		
		User loginUser =(User) request.getSession().getAttribute(ConstClass.SESSION_USER_KEY);
		PageInfo<Article>  pageArticles = articleService.listArticleByUserId(loginUser.getId(),page);
		PageUtils.page(request, "/user/myarticlelist", 10, pageArticles.getList(), (long)pageArticles.getSize(), pageArticles.getPageNum());
		request.setAttribute("pageArticles", pageArticles);
		return "/my/list";
	}
	
	/**
	 * 管理员查询用户列表
	 * @param locked
	 * @param request
	 * @return
	 */
	@RequestMapping("list")
	public String list(Integer locked,HttpServletRequest request){
			List<User> UserList =  userService.UserList(locked);
			request.setAttribute("ulist", UserList);
		return "admin/article/UserList";
	}
	
	/**
	 * 封禁用户
	 * @param id
	 * @return
	 */
	@RequestMapping("bannedUser")
	public String bannedUser(Integer id){
		int banned = userService.bannedUser(id);
		return "admin/index";
	}
	
}
