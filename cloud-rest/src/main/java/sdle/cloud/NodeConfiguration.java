package sdle.cloud;


import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
@ConfigMapping(prefix = "sdle.node")
public interface NodeConfiguration {
    @WithName("bootstrap")
    @WithDefault("10.5.0.11,10.5.0.12,10.5.0.13")
    List<String> getBootstrapList();

    @WithName("id")
    @WithDefault("node")
    String getNodeId();

    @WithName("port")
    @WithDefault("7788")
    Integer getNodePort();

    @WithName("dataDir")
    @WithDefault("data")
    String getDataDir();

}
