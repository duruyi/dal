package com.ctrip.platform.dal.daogen.utils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jasig.cas.client.util.AssertionHolder;

import com.ctrip.platform.dal.daogen.entity.LoginUser;

public class UserFilter implements Filter {

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		try {
			String userNo = AssertionHolder.getAssertion().getPrincipal()
					.getAttributes().get("employee").toString();
			LoginUser user = null;
			user = SpringBeanGetter.getDaoOfLoginUser().getUserByNo(userNo);
			if(user==null){
				user = new LoginUser();
				user.setUserNo(userNo);
				user.setUserName(AssertionHolder.getAssertion().getPrincipal()
						.getAttributes().get("sn").toString());
				user.setUserEmail(AssertionHolder.getAssertion().getPrincipal()
						.getAttributes().get("mail").toString());
				SpringBeanGetter.getDaoOfLoginUser().insertUser(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			arg2.doFilter(arg0, arg1);
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

}
