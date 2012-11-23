/**
 * Copyright (C) 2011 ArtiVisi Intermedia <info@artivisi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.artivisi.belajar.restful.ui.controller;

import com.artivisi.belajar.restful.domain.User;
import com.artivisi.belajar.restful.service.BelajarRestfulService;
import java.net.URI;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriTemplate;

@Controller
public class UserController {

    @Autowired
    private BelajarRestfulService belajarRestfulService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/user/{id}")
    @ResponseBody
    public User findById(@PathVariable String id) {
        User x = belajarRestfulService.findUserById(id);
        if (x == null) {
            throw new IllegalStateException();
        }
        fixLie(x);
        return x;
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody User x, HttpServletRequest request, HttpServletResponse response) {
        belajarRestfulService.save(x);
        String requestUrl = request.getRequestURL().toString();
        URI uri = new UriTemplate("{requestUrl}/{id}").expand(requestUrl, x.getId());
        response.setHeader("Location", uri.toASCIIString());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable String id, @RequestBody User x) {
        User a = belajarRestfulService.findUserById(id);
        if (a == null) {
            logger.warn("User dengan id [{}] tidak ditemukan", id);
            throw new IllegalStateException();
        }
        x.setId(a.getId());
        belajarRestfulService.save(x);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String id) {
        User a = belajarRestfulService.findUserById(id);
        if (a == null) {
            logger.warn("User dengan id [{}] tidak ditemukan", id);
            throw new IllegalStateException();
        }
        belajarRestfulService.delete(a);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public List<User> findAll(
            Pageable pageable,
            HttpServletResponse response) {
        List<User> hasil = belajarRestfulService.findAllUsers(pageable).getContent();

        for(User u : hasil){
            fixLie(u);
        }

        return hasil;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({IllegalStateException.class})
    public void handle() {
        logger.debug("Resource dengan URI tersebut tidak ditemukan");
    }

    private void fixLie(User x){
        x.getRole().setPermissionSet(null);
        x.getRole().setMenuSet(null);
    }
}
