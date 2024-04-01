package com.ian.web.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@Configuration
//@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String[] PUBLIC = new String[] 
			{"/login","/logout","/error","/reports/**","/api/**"};
	private static final String[] ASSETS = new String[]
			{"/assets/**","/global_assets/**",
					"/admin_js/**","/parent_css/**","/parent_js/**",
					"/js/*","/js/**","/css/*","/css/**","/images/*","/images/**"};
	
	private UserDetailsService userDetailsService;
	
	public SecurityConfig(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		// configure AuthenticationManager so that it knows from where to load
//		// user for matching credentials
//		// Use BCryptPasswordEncoder
//		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String landingURL = "/dashboard";
		http
			.csrf().disable()
			.authorizeRequests()
				.antMatchers(PUBLIC).permitAll()
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl(landingURL, true)
				//.successForwardUrl("/success");
				.failureUrl("/login?error")
				.and()
			.logout()
				//.and()
			;
	}

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers(ASSETS);
	}

//	@Bean
//	public PasswordEncoder passwordEncoder() throws NoSuchAlgorithmException {
//		int strength = 11;
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(strength); 
//		return encoder;
//	}
	
	@SuppressWarnings("deprecation")
	@Bean
	public static NoOpPasswordEncoder passwordEncoder() {
	  return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
	}
	
}
