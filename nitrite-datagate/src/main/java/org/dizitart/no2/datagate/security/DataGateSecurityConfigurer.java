/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.datagate.security;

import org.dizitart.no2.datagate.services.UserAccountService;
import org.dizitart.no2.sync.types.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import static org.dizitart.no2.datagate.Constants.*;

/**
 * Data Gate security configuration.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Configuration
@EnableWebSecurity
public class DataGateSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserAccountService userAccountService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/v1/collection/**").hasAuthority(AUTH_USER)
                .antMatchers("/api/v1/user/**").hasAuthority(AUTH_CLIENT)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic();

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/admin/**").hasAuthority(AUTH_ADMIN)
                .and()
                .formLogin()
                .loginPage("/login")
                .failureUrl("/loginError")
                .defaultSuccessUrl("/admin/")
                .permitAll()
                .and()
                .logout()
                .permitAll();

        http.rememberMe()
                .rememberMeServices(rememberMeServices())
                .key("password");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return username -> {
            UserAccount account = userAccountService.findByUsername(username);
            if(account != null) {
                RequestContext.getInstance().set(account);

                return new User(account.getUserName(), account.getPassword(),
                        account.getEnabled(), account.getAccountNonExpired(),
                        true, account.getAccountNonLocked(),
                        AuthorityUtils.createAuthorityList(account.getAuthorities()));
            } else {
                throw new UsernameNotFoundException("could not find the user '"
                        + username + "'");
            }
        };
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices rememberMeServices
                = new TokenBasedRememberMeServices("password", userDetailsService());
        rememberMeServices.setCookieName("cookieName");
        rememberMeServices.setParameter("rememberMe");
        rememberMeServices.setAlwaysRemember(true);
        rememberMeServices.setTokenValiditySeconds(Integer.MAX_VALUE);
        return rememberMeServices;
    }
}
