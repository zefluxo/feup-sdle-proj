package sdle.cloud.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import sdle.crdt.utils.Pair;
import sdle.crdt.utils.PairKeyDeserializer;

import java.util.List;

public class CustomObjectMapper {

    // Replaces the CDI producer for ObjectMapper built into Quarkus
    @Singleton
    @Produces
    ObjectMapper objectMapper(@All List<ObjectMapperCustomizer> customizers) {

        SimpleModule nioModule = new SimpleModule();
        nioModule.addKeyDeserializer(Pair.class, new PairKeyDeserializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(nioModule);

        // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
        for (ObjectMapperCustomizer customizer : customizers) {
            customizer.customize(mapper);
        }

        return mapper;
    }
}