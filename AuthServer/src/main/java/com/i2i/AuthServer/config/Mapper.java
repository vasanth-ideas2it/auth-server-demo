package com.i2i.AuthServer.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Mapper {

    @Bean
    public ModelMapper modelMapper()
    {
        ModelMapper mapper=new ModelMapper();
         mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE);
         return mapper;
    }
}
