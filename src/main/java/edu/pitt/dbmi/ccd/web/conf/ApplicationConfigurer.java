/*
 * Copyright (C) 2016 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.web.conf;

import edu.pitt.dbmi.ccd.web.model.algorithm.AlgorithmItem;
import edu.pitt.dbmi.ccd.web.prop.TetradProperties;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ErrorPageRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * Aug 5, 2015 2:04:37 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Configuration
public class ApplicationConfigurer extends WebMvcConfigurerAdapter {

    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return registry -> {
            registry.addErrorPages(
                    new ErrorPage(HttpStatus.BAD_REQUEST, "/400"),
                    new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
                    new ErrorPage(HttpStatus.UNAUTHORIZED, "/401"),
                    new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500")
            );
        };
    }

    @Bean
    public Map<String, String> params(final TetradProperties tetradProperties) {
        return tetradProperties.getAlgoParamLabels();
    }

    @Bean
    public Map<String, AlgorithmItem> alorithmItems(final TetradProperties tetradProperties) {
        Map<String, AlgorithmItem> map = new HashMap<>();

        Map<String, String> titles = tetradProperties.getAlgoTitles();
        Map<String, String> descriptions = tetradProperties.getAlgoDescriptions();
        titles.forEach((k, v) -> {
            map.put(k, new AlgorithmItem(k, v, descriptions.get(k)));
        });

        return map;
    }

    @Bean
    public Map<String, List<AlgorithmItem>> algorithms(final TetradProperties tetradProperties) {
        Map<String, List<AlgorithmItem>> map = new LinkedHashMap<>();

        Map<String, AlgorithmItem> alorithmItems = alorithmItems(tetradProperties);

        List<AlgorithmItem> fgesAlgoItems = new LinkedList<>();
        String[] fgesAlgorithms = tetradProperties.getFgesAlgos();
        for (String name : fgesAlgorithms) {
            fgesAlgoItems.add(alorithmItems.get(name));
        }
        map.put("fges", fgesAlgoItems);

        List<AlgorithmItem> gfciAlgoItems = new LinkedList<>();
        String[] gfciAlgorithms = tetradProperties.getGfciAlgos();
        for (String name : gfciAlgorithms) {
            gfciAlgoItems.add(alorithmItems.get(name));
        }
        map.put("gfci", gfciAlgoItems);

        return map;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

}
